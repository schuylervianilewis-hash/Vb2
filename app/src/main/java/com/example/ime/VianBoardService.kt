package com.example.ime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.example.ime.keyboard.KeyData
import com.example.ime.keyboard.KeyType
import com.example.ime.keyboard.VianKeyboardView
import com.example.ime.settings.SettingsActivity
import com.example.ime.toolbar.ToolbarPreferences
import com.example.ime.toolbar.ToolbarTool
import com.example.logger.LogKeeper

class VianBoardService : InputMethodService() {

    private var keyboardView: VianKeyboardView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isTempIncognitoActive = false
    private val incognitoExpireRunnable = Runnable {
        isTempIncognitoActive = false
        keyboardView?.let { kv ->
            kv.layout.isIncognitoActive = false
            kv.invalidate()
        }
        Toast.makeText(this, "Incognito mode ended", Toast.LENGTH_SHORT).show()
    }

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
            onActionExpand = { toggleToolbarExpand() }

            onToolbarToolClick = { tool -> handleToolbarToolClick(tool) }
            onToolbarToolLongClick = { tool -> handleToolbarToolLongClick(tool) }
            onAnchorLongClick = { handleAnchorLongClick() }
            onCommaPopupSelected = { item -> handleCommaPopupAction(item) }
        }
        keyboardView = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        LogKeeper.logEvent("IME", "onStartInputView (restarting=$restarting)")
        keyboardView?.reloadTheme()

        // Check if input requests numbers (EditorInfo.TYPE_CLASS_NUMBER or TYPE_CLASS_PHONE)
        val inputType = info?.inputType ?: 0
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        if (inputClass == InputType.TYPE_CLASS_NUMBER || inputClass == InputType.TYPE_CLASS_PHONE) {
            keyboardView?.setMode(com.example.ime.keyboard.KeyboardMode.NUMPAD)
        } else {
            keyboardView?.setMode(com.example.ime.keyboard.KeyboardMode.CHARACTERS)
        }
    }

    private fun toggleToolbarExpand() {
        keyboardView?.let { kv ->
            kv.layout.isToolbarExpanded = !kv.layout.isToolbarExpanded
            val density = resources.displayMetrics.density
            kv.layout.buildLayout(kv.width.toFloat(), kv.height.toFloat(), kv.theme, density, kv.bottomNavInsetPx)
            kv.invalidate()
        }
    }

    private fun handleAnchorLongClick() {
        // Long pressing the chevron or anchor toggles temporary 3-minute incognito mode
        activateTempIncognito(180_000L)
    }

    private fun activateTempIncognito(durationMs: Long) {
        isTempIncognitoActive = true
        handler.removeCallbacks(incognitoExpireRunnable)
        handler.postDelayed(incognitoExpireRunnable, durationMs)

        keyboardView?.let { kv ->
            kv.layout.isIncognitoActive = true
            val density = resources.displayMetrics.density
            kv.layout.buildLayout(kv.width.toFloat(), kv.height.toFloat(), kv.theme, density, kv.bottomNavInsetPx)
            kv.invalidate()
        }
        Toast.makeText(this, "Incognito mode active (3 min)", Toast.LENGTH_SHORT).show()
    }

    private fun handleToolbarToolClick(tool: ToolbarTool) {
        val ic = currentInputConnection
        when (tool) {
            ToolbarTool.UNDO -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_Z, KeyEvent.META_CTRL_ON)
            }
            ToolbarTool.REDO -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_Y, KeyEvent.META_CTRL_ON)
            }
            ToolbarTool.SELECT_WORD -> {
                // Select surrounding word
                ic?.performContextMenuAction(android.R.id.selectAll)
            }
            ToolbarTool.SELECT_ALL -> {
                ic?.performContextMenuAction(android.R.id.selectAll)
            }
            ToolbarTool.COPY -> {
                ic?.performContextMenuAction(android.R.id.copy)
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.PASTE -> {
                ic?.performContextMenuAction(android.R.id.paste)
            }
            ToolbarTool.UP -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP)
            }
            ToolbarTool.DOWN -> {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN)
            }
            ToolbarTool.INCOGNITO -> {
                // Toggle incognito mode
                if (isTempIncognitoActive) {
                    handler.removeCallbacks(incognitoExpireRunnable)
                    isTempIncognitoActive = false
                    keyboardView?.let { kv ->
                        kv.layout.isIncognitoActive = false
                        val density = resources.displayMetrics.density
                        kv.layout.buildLayout(kv.width.toFloat(), kv.height.toFloat(), kv.theme, density)
                        kv.invalidate()
                    }
                    Toast.makeText(this, "Incognito mode disabled", Toast.LENGTH_SHORT).show()
                } else {
                    activateTempIncognito(180_000L)
                }
            }
            ToolbarTool.VOICE -> {
                Toast.makeText(this, "Voice input requested", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.PROMPT_LIST -> {
                Toast.makeText(this, "Prompt list", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.SECURITY_VAULT -> {
                Toast.makeText(this, "Security vault", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.DESKTOP_SHORTCUTS -> {
                Toast.makeText(this, "Desktop shortcuts modal", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.SETTINGS -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
            ToolbarTool.CLIPBOARD -> {
                handleClipboardAction()
            }
            ToolbarTool.TEXT_EDIT -> {
                // Text editing pad / select all & copy
                ic?.performContextMenuAction(android.R.id.selectAll)
                Toast.makeText(this, "Text editing: All text selected", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.THEME -> {
                val intent = Intent(this, com.example.ime.settings.AppearanceSettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
            ToolbarTool.EMOJI -> {
                ic?.commitText("😊", 1)
            }
            ToolbarTool.NUMBER_ROW -> {
                Toast.makeText(this, "Number row toggle", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.CLEAR_CLIPBOARD -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                clipboard?.clearPrimaryClip()
                Toast.makeText(this, "Clipboard cleared", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.ONE_HANDED -> {
                Toast.makeText(this, "One-handed mode: Coming soon", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.FLOATING -> {
                Toast.makeText(this, "Floating keyboard: Coming soon", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.LOG_KEEPER -> {
                val logs = LogKeeper.getLogs()
                Toast.makeText(this, "Log Keeper: ${logs.size} entries", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.PERSONAL_VAULT -> {
                Toast.makeText(this, "Personal vault: Coming soon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleToolbarToolLongClick(tool: ToolbarTool) {
        val ic = currentInputConnection
        when (tool) {
            ToolbarTool.INCOGNITO -> {
                activateTempIncognito(180_000L)
            }
            ToolbarTool.SELECT_WORD -> {
                // Long press select word -> select all
                ic?.performContextMenuAction(android.R.id.selectAll)
                Toast.makeText(this, "Select All", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.COPY -> {
                // Long press copy -> prompt list modal
                Toast.makeText(this, "Prompt list modal", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.PASTE -> {
                // Long press paste -> clipboard history
                handleClipboardAction()
            }
            else -> {
                // For other tools, repeat standard action
                handleToolbarToolClick(tool)
            }
        }
    }

    private fun sendDownUpKeyEvents(keyCode: Int, metaState: Int) {
        val ic = currentInputConnection ?: return
        val eventTime = android.os.SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
        ic.sendKeyEvent(KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP, keyCode, 0, metaState))
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

    private fun handleCommaPopupAction(item: String) {
        when (item) {
            "Settings" -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
            }
            "Emoji" -> {
                // Switch to emoji / symbols view or commit smiley emoji
                currentInputConnection?.commitText("😊", 1)
            }
            "Clipboard" -> {
                handleClipboardAction()
            }
            "Log Keeper" -> {
                val logs = LogKeeper.getLogs()
                Toast.makeText(this, "Log Keeper: ${logs.size} entries", Toast.LENGTH_SHORT).show()
            }
            "Shortcuts" -> {
                Toast.makeText(this, "Desktop shortcuts modal", Toast.LENGTH_SHORT).show()
            }
            "Voice" -> {
                Toast.makeText(this, "Voice input: Coming soon", Toast.LENGTH_SHORT).show()
            }
            "One Hand" -> {
                Toast.makeText(this, "One hand mode: Coming soon", Toast.LENGTH_SHORT).show()
            }
            "Floating" -> {
                Toast.makeText(this, "Floating keyboard: Coming soon", Toast.LENGTH_SHORT).show()
            }
            "Personal Vault" -> {
                Toast.makeText(this, "Personal vault: Coming soon", Toast.LENGTH_SHORT).show()
            }
            "Security Vault" -> {
                Toast.makeText(this, "Security vault: Coming soon", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "$item: Coming soon", Toast.LENGTH_SHORT).show()
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
