package com.example.ime

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ime.clipboard.ClipboardStorage
import com.example.ime.clipboard.VianClipboardModalView
import com.example.ime.emoji.VianEmojiModalView
import com.example.ime.quicknotes.QuickNotesStorage
import com.example.ime.quicknotes.VianQuickNotesModalView
import com.example.ime.keyboard.KeyData
import com.example.ime.keyboard.KeyType
import com.example.ime.keyboard.VianKeyboardView
import com.example.ime.settings.SettingsActivity
import com.example.ime.toolbar.ToolbarPreferences
import com.example.ime.toolbar.ToolbarTool
import com.example.logger.LogKeeper

class VianBoardService : InputMethodService() {

    private var inputViewContainer: FrameLayout? = null
    private var keyboardView: VianKeyboardView? = null
    private var activeModalView: View? = null
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

    private lateinit var clipboardStorage: ClipboardStorage
    private var clipboardManager: ClipboardManager? = null
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        capturePrimaryClip()
    }

    override fun onCreate() {
        super.onCreate()
        LogKeeper.logComponentStart("VianBoardService")
        clipboardStorage = ClipboardStorage(this)
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    }

    private fun capturePrimaryClip() {
        try {
            val clip = clipboardManager?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0)?.coerceToText(this)?.toString()?.trim()
                if (!text.isNullOrEmpty()) {
                    clipboardStorage.addClip(text)
                    LogKeeper.logEvent("IME", "Auto-captured clip into storage without modal")
                }
            }
        } catch (e: Exception) {
            LogKeeper.logError("IME", "CLIPBOARD_CAPTURE_FAIL", e.message ?: "Unknown error")
        }
    }

    override fun onConfigureWindow(win: android.view.Window, isFullscreen: Boolean, isCandidatesOnly: Boolean) {
        super.onConfigureWindow(win, isFullscreen, isCandidatesOnly)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            win.setDecorFitsSystemWindows(false)
        }
        win.navigationBarColor = Color.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            win.isNavigationBarContrastEnforced = false
        }
    }

    override fun onComputeInsets(outInsets: InputMethodService.Insets) {
        super.onComputeInsets(outInsets)
        outInsets.contentTopInsets = 0
        outInsets.visibleTopInsets = 0
        outInsets.touchableInsets = InputMethodService.Insets.TOUCHABLE_INSETS_FRAME
    }

    override fun onCreateInputView(): View {
        LogKeeper.logEvent("IME", "onCreateInputView initializing 2D Canvas engine with Option C popups and on-demand modals")
        val container = FrameLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.BLACK)
        }

        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(0, 0, 0, navInsets.bottom)
            WindowInsetsCompat.CONSUMED
        }

        val view = VianKeyboardView(this).apply {
            onKeyAction = { key -> handleKeyAction(key) }
            onTextCommit = { text -> currentInputConnection?.commitText(text, 1) }
            onActionSelection = { handleSelectionAction() }
            onActionClipboard = { showClipboardModal() }
            onActionExpand = { toggleToolbarExpand() }

            onToolbarToolClick = { tool -> handleToolbarToolClick(tool) }
            onToolbarToolLongClick = { tool -> handleToolbarToolLongClick(tool) }
            onAnchorLongClick = { handleAnchorLongClick() }
            onCommaPopupSelected = { item -> handleCommaPopupAction(item) }
        }
        container.addView(view)
        keyboardView = view
        inputViewContainer = container
        return container
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        LogKeeper.logEvent("IME", "onStartInputView (restarting=$restarting)")
        keyboardView?.reloadTheme()

        // Auto-register clipboard listener and capture any current clip to storage
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
            clipboardManager?.addPrimaryClipChangedListener(clipboardListener)
            capturePrimaryClip()
        } catch (e: Exception) {
            LogKeeper.logError("IME", "CLIPBOARD_LISTENER_FAIL", e.message ?: "Unknown error")
        }

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
                // Select surrounding word on tap
                selectSurroundingWord()
            }
            ToolbarTool.SELECT_ALL -> {
                ic?.performContextMenuAction(android.R.id.selectAll)
            }
            ToolbarTool.COPY -> {
                ic?.performContextMenuAction(android.R.id.copy)
                Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
                handler.postDelayed({ capturePrimaryClip() }, 100)
            }
            ToolbarTool.PASTE -> {
                ic?.performContextMenuAction(android.R.id.paste)
                handler.postDelayed({ capturePrimaryClip() }, 100)
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
                showQuickNotesModal()
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
                showClipboardModal()
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
                showEmojiModal()
            }
            ToolbarTool.NUMBER_ROW -> {
                Toast.makeText(this, "Number row toggle", Toast.LENGTH_SHORT).show()
            }
            ToolbarTool.CLEAR_CLIPBOARD -> {
                ClipboardStorage(this).clearUnpinned()
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
                // Long press copy -> Quick Notes modal
                showQuickNotesModal()
            }
            ToolbarTool.PASTE -> {
                // Long press paste -> clipboard history modal
                showClipboardModal()
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
                sendDelete()
            }
            KeyType.ENTER -> {
                sendEnter()
            }
            else -> {
                if (key.label.isNotEmpty()) {
                    ic.commitText(key.label, 1)
                }
            }
        }
    }

    private fun sendDelete() {
        val ic = currentInputConnection ?: return
        val selectedText = ic.getSelectedText(0)
        if (selectedText.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
        } else {
            ic.commitText("", 1)
        }
    }

    private fun sendEnter() {
        val ic = currentInputConnection ?: return
        val imeOptions = currentInputEditorInfo?.imeOptions ?: 0
        val actionId = imeOptions and EditorInfo.IME_MASK_ACTION
        if (actionId != EditorInfo.IME_ACTION_NONE && actionId != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(actionId)
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER, 0)
        }
    }

    private fun handleSelectionAction() {
        selectSurroundingWord()
    }

    private fun selectSurroundingWord() {
        val ic = currentInputConnection ?: return

        // 1. If text is already selected, expand to select all
        val currentSelection = ic.getSelectedText(0)
        if (!currentSelection.isNullOrEmpty()) {
            ic.performContextMenuAction(android.R.id.selectAll)
            return
        }

        // 2. Query text before and after cursor (up to 120 chars each)
        val before = ic.getTextBeforeCursor(120, 0)?.toString() ?: ""
        val after = ic.getTextAfterCursor(120, 0)?.toString() ?: ""

        if (before.isEmpty() && after.isEmpty()) {
            ic.performContextMenuAction(android.R.id.selectAll)
            return
        }

        val isWordChar = { c: Char -> c.isLetterOrDigit() || c == '_' }

        var leftCount = 0
        for (i in before.length - 1 downTo 0) {
            if (isWordChar(before[i])) {
                leftCount++
            } else {
                break
            }
        }

        var rightCount = 0
        for (i in 0 until after.length) {
            if (isWordChar(after[i])) {
                rightCount++
            } else {
                break
            }
        }

        if (leftCount > 0 || rightCount > 0) {
            val req = android.view.inputmethod.ExtractedTextRequest()
            val extracted = ic.getExtractedText(req, 0)
            if (extracted != null && extracted.selectionStart >= 0) {
                val cursor = extracted.selectionStart
                val selStart = (cursor - leftCount).coerceAtLeast(0)
                val selEnd = (cursor + rightCount).coerceAtMost(extracted.text?.length ?: (cursor + rightCount))
                ic.setSelection(selStart, selEnd)
                return
            }
        }
        ic.performContextMenuAction(android.R.id.selectAll)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isCtrlPressed) {
            if (keyCode == KeyEvent.KEYCODE_W || keyCode == KeyEvent.KEYCODE_V) {
                if (event.repeatCount == 0) {
                    event.startTracking()
                }
                if (event.repeatCount > 0) {
                    // Hardware key held down (Desktop long-press equivalent)
                    if (keyCode == KeyEvent.KEYCODE_W) {
                        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                        Toast.makeText(this, "Select All", Toast.LENGTH_SHORT).show()
                        return true
                    } else if (keyCode == KeyEvent.KEYCODE_V) {
                        showClipboardModal()
                        return true
                    }
                }
            }
            when (keyCode) {
                KeyEvent.KEYCODE_W -> {
                    if (event.isShiftPressed) {
                        currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                        Toast.makeText(this, "Select All", Toast.LENGTH_SHORT).show()
                        return true
                    }
                }
                KeyEvent.KEYCODE_A -> {
                    currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                    return true
                }
                KeyEvent.KEYCODE_V -> {
                    if (event.isShiftPressed) {
                        showClipboardModal()
                        return true
                    }
                    handler.postDelayed({ capturePrimaryClip() }, 100)
                }
                KeyEvent.KEYCODE_C, KeyEvent.KEYCODE_X -> {
                    handler.postDelayed({ capturePrimaryClip() }, 100)
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isCtrlPressed) {
            when (keyCode) {
                KeyEvent.KEYCODE_W -> {
                    currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
                    Toast.makeText(this, "Select All", Toast.LENGTH_SHORT).show()
                    return true
                }
                KeyEvent.KEYCODE_V -> {
                    showClipboardModal()
                    return true
                }
            }
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.isCtrlPressed && !event.isCanceled) {
            if (keyCode == KeyEvent.KEYCODE_W && event.repeatCount == 0 && !event.isShiftPressed) {
                selectSurroundingWord()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun showClipboardModal() {
        val container = inputViewContainer ?: return
        dismissActiveModal()

        val clipboardView = VianClipboardModalView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                keyboardView?.height?.coerceAtLeast(300) ?: FrameLayout.LayoutParams.WRAP_CONTENT
            )
            onDismissToAlpha = { dismissActiveModal() }
            onCommitText = { text -> currentInputConnection?.commitText(text, 1) }
            onDelete = { sendDelete() }
            onEnter = { sendEnter() }
            onSelectAll = { currentInputConnection?.performContextMenuAction(android.R.id.selectAll) }
            onCut = { currentInputConnection?.performContextMenuAction(android.R.id.cut) }
            onCopy = { currentInputConnection?.performContextMenuAction(android.R.id.copy) }
            onPaste = { currentInputConnection?.performContextMenuAction(android.R.id.paste) }
            onNavigate = { dir ->
                when (dir) {
                    1 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT, 0)
                    2 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT, 0)
                    3 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP, 0)
                    4 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN, 0)
                }
            }
        }

        keyboardView?.visibility = View.INVISIBLE
        container.addView(clipboardView)
        activeModalView = clipboardView
        LogKeeper.logEvent("IME", "Clipboard modal opened on-demand")
    }

    private fun showQuickNotesModal() {
        val container = inputViewContainer ?: return
        dismissActiveModal()

        val quickNotesView = VianQuickNotesModalView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                keyboardView?.height?.coerceAtLeast(300) ?: FrameLayout.LayoutParams.WRAP_CONTENT
            )
            onDismissToAlpha = { dismissActiveModal() }
            onCommitText = { text -> currentInputConnection?.commitText(text, 1) }
            onDelete = { sendDelete() }
            onEnter = { sendEnter() }
            onSelectAll = { currentInputConnection?.performContextMenuAction(android.R.id.selectAll) }
            onPasteToNewNote = {
                val clipMgr = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                val clipText = clipMgr?.primaryClip?.getItemAt(0)?.coerceToText(this@VianBoardService)?.toString()?.trim() ?: ""
                if (clipText.isNotEmpty()) {
                    val intent = Intent(this@VianBoardService, com.example.ime.quicknotes.QuickNoteEditActivity::class.java).apply {
                        putExtra(com.example.ime.quicknotes.QuickNoteEditActivity.EXTRA_OLD_TEXT, clipText)
                        putExtra(com.example.ime.quicknotes.QuickNoteEditActivity.EXTRA_IS_NEW, true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this@VianBoardService, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                }
            }
            onNavigate = { dir ->
                when (dir) {
                    1 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT, 0)
                    2 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT, 0)
                    3 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP, 0)
                    4 -> sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN, 0)
                }
            }
        }

        keyboardView?.visibility = View.INVISIBLE
        container.addView(quickNotesView)
        activeModalView = quickNotesView
        LogKeeper.logEvent("IME", "Quick Notes modal opened on-demand")
    }

    private fun showEmojiModal() {
        val container = inputViewContainer ?: return
        dismissActiveModal()

        val emojiView = VianEmojiModalView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                keyboardView?.height?.coerceAtLeast(300) ?: FrameLayout.LayoutParams.WRAP_CONTENT
            )
            onDismissToAlpha = { dismissActiveModal() }
            onEmojiCommit = { emoji -> currentInputConnection?.commitText(emoji, 1) }
            onDelete = { sendDelete() }
            onEnter = { sendEnter() }
        }

        keyboardView?.visibility = View.INVISIBLE
        container.addView(emojiView)
        activeModalView = emojiView
        LogKeeper.logEvent("IME", "Emoji modal opened on-demand")
    }

    private fun dismissActiveModal() {
        activeModalView?.let { modal ->
            inputViewContainer?.removeView(modal)
            activeModalView = null
        }
        keyboardView?.visibility = View.VISIBLE
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
                showEmojiModal()
            }
            "Clipboard" -> {
                showClipboardModal()
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
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
        } catch (e: Exception) {
            // Ignore
        }
        dismissActiveModal()
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        LogKeeper.logComponentStop("VianBoardService")
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
        } catch (e: Exception) {
            // Ignore
        }
        dismissActiveModal()
        inputViewContainer = null
        keyboardView = null
        super.onDestroy()
    }
}
