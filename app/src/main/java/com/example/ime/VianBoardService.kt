package com.example.ime

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
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

    override fun onCreateInputView(): View {
        LogKeeper.logEvent("IME", "onCreateInputView initializing 2D Canvas engine")
        val view = VianKeyboardView(this).apply {
            onKeyAction = { key -> handleKeyAction(key) }
            onKeyLongPress = { key -> handleKeyLongPress(key) }
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

    private fun handleKeyLongPress(key: KeyData) {
        val ic = currentInputConnection ?: return
        if (key.hintLabel != null) {
            ic.commitText(key.hintLabel, 1)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        LogKeeper.logEvent("IME", "onFinishInputView (finishingInput=$finishingInput)")
    }

    override fun onDestroy() {
        LogKeeper.logComponentStop("VianBoardService")
        keyboardView = null
        super.onDestroy()
    }
}
