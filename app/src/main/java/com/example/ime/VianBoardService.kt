package com.example.ime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import com.example.ime.keyboard.KeyData
import com.example.ime.keyboard.KeyType
import com.example.ime.keyboard.VianKeyboardView
import com.example.logger.LogKeeper

class VianBoardService : InputMethodService() {

    private var keyboardView: VianKeyboardView? = null

    override fun onCreate() {
        super.onCreate()
        LogKeeper.logComponentStart("VianBoardService")
    }

    override fun onConfigureWindow(win: android.view.Window, isFullscreen: Boolean, isCandidatesOnly: Boolean) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            win.setDecorFitsSystemWindows(true)
        }
    }

    override fun onCreateInputView(): View {
        LogKeeper.logEvent("IME", "onCreateInputView initializing 2D Canvas engine with Option C popups")
        val view = VianKeyboardView(this).apply {
            onKeyAction = { key -> handleKeyAction(key) }
            onTextCommit = { text -> currentInputConnection?.commitText(text, 1) }
            onActionSelection = { handleSelectionAction() }
            onActionClipboard = { handleClipboardAction() }
            onActionExpand = { /* Toolbar expand/collapse toggle */ }
        }
        keyboardView = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        LogKeeper.logEvent("IME", "onStartInputView (restarting=$restarting)")
        keyboardView?.reloadTheme()
    }

    private fun handleKeyAction(key: KeyData) {
        val ic = currentInputConnection ?: return
        when (key.type) {
            KeyType.CHARACTER -> {
                ic.commitText(key.label, 1)
            }
            KeyType.SPACE -> {
                ic.commitText(" ", 1)
            }
            KeyType.COMMA -> {
                ic.commitText(",", 1)
            }
            KeyType.PERIOD -> {
                ic.commitText(".", 1)
            }
            KeyType.DELETE -> {
                val selectedText = ic.getSelectedText(0)
                if (selectedText.isNullOrEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.commitText("", 1)
                }
            }
            KeyType.ENTER -> {
                val imeOptions = currentInputEditorInfo?.imeOptions ?: 0
                val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
                if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
                    ic.performEditorAction(actionId)
                } else {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                }
            }
            else -> {
                if (key.label.isNotEmpty()) {
                    ic.commitText(key.label, 1)
                }
            }
        }
    }

    private fun handleSelectionAction() {
        val ic = currentInputConnection ?: return
        // Select word or line around cursor
        ic.performContextMenuAction(android.R.id.selectAll)
    }

    private fun handleClipboardAction() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(this).toString()
            if (text.isNotEmpty()) {
                currentInputConnection?.commitText(text, 1)
            }
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        LogKeeper.logEvent("IME", "onFinishInputView")
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        LogKeeper.logComponentStop("VianBoardService")
        keyboardView = null
        super.onDestroy()
    }
}
