package com.example.ime

import android.content.Intent
import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.diagnostics.LogLevel
import com.example.diagnostics.LogKeeper
import com.example.diagnostics.LogTag
import com.example.engine.core.SuggestedWordInfo
import com.example.engine.core.WordComposer
import com.example.foundation.common.Constants
import com.example.foundation.utils.DeviceUtils
import com.example.foundation.utils.ResourceUtils
import com.example.keyboard.internal.Key
import com.example.keyboard.internal.KeyPopupOverlayView
import com.example.keyboard.internal.KeyboardLayoutBuilder
import com.example.keyboard.internal.MainKeyboardView
import com.example.keyboard.internal.PointerTracker
import com.example.vault.ui.VaultOverlayView

/**
 * Main InputMethodService for Vian Board.
 * Connects IME lifecycle with Canvas Keyboard View, Suggestion Bar, Dictionary Engine, and Modals.
 */
class VianBoardService : InputMethodService(),
    PointerTracker.KeyboardActionListener,
    SuggestionStripView.SuggestionStripListener,
    ModalOverlayManager.ModalActionListener,
    VaultOverlayView.VaultOverlayActionListener {

    private lateinit var rootContainer: FrameLayout
    private lateinit var mainKeyboardView: MainKeyboardView
    private lateinit var suggestionStripView: SuggestionStripView
    private var modalOverlayManager: ModalOverlayManager? = null
    private var vaultOverlayView: VaultOverlayView? = null

    private val richInputConnection = RichInputConnection()
    private val wordComposer = WordComposer()
    private val subtypeSwitcher = SubtypeSwitcher()
    private lateinit var feedbackManager: AudioAndHapticFeedbackManager
    private lateinit var generalSettingsManager: com.example.settings.GeneralSettingsManager
    private var generalSettings = com.example.settings.GeneralSettings()

    private var currentShiftState = Constants.SHIFT_OFF
    private var swipeDeleteInitialTextBefore: String = ""
    private var swipeDeleteWordsSelected: Int = 0
    private var swipeDeleteCharsSelected: Int = 0

    override fun onCreate() {
        super.onCreate()
        LogKeeper.log(LogTag.IME, LogLevel.INFO, "VianBoardService onCreate initialized")
        feedbackManager = AudioAndHapticFeedbackManager(this)
        generalSettingsManager = com.example.settings.GeneralSettingsManager(this)
        generalSettings = generalSettingsManager.load()
    }

    override fun onCreateInputView(): View {
        val totalHeight = DeviceUtils.getBaseKeyboardHeight(this)
        val stripHeight = ResourceUtils.dpToPx(this, 40f).toInt()
        val keyboardHeight = totalHeight - stripHeight

        // Solid canvas backdrop
        rootContainer = FrameLayout(this).apply {
            setBackgroundColor(Color.parseColor("#E8ECEF"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val linearContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#E8ECEF"))
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                totalHeight
            )
        }

        // Apply window insets dynamically to float cleanly above Android 3-button / gesture navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(rootContainer) { view, insets ->
            val navInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(0, 0, 0, navInsets.bottom)
            insets
        }

        // 1. Suggestion Strip View (Top 40dp)
        suggestionStripView = SuggestionStripView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                stripHeight
            )
            listener = this@VianBoardService
        }
        linearContainer.addView(suggestionStripView)

        // 2. Key Popup Overlay View (Floating Key Preview & MoreKeys 2-Row Popups)
        val popupOverlay = KeyPopupOverlayView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                totalHeight
            )
        }

        // 3. Main Hardware Canvas Keyboard View
        mainKeyboardView = MainKeyboardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                keyboardHeight
            )
            actionListener = this@VianBoardService
            this.popupOverlay = popupOverlay
            this.keyboardOffsetYInRoot = stripHeight.toFloat()
            setKeyStyling(
                cornerRadiusDp = generalSettings.keyCornerRadiusDp,
                horizontalGapDp = generalSettings.keyHorizontalGapDp,
                verticalGapDp = generalSettings.keyVerticalGapDp,
                borderWidthDp = generalSettings.keyBorderWidthDp,
                outlineEnabled = generalSettings.keyOutlineEnabled
            )
        }
        linearContainer.addView(mainKeyboardView)

        rootContainer.addView(linearContainer)
        rootContainer.addView(popupOverlay)

        // 4. Modal Overlay Manager is created strictly on-demand to save memory
        return rootContainer
    }

    private fun getOrCreateModalOverlay(): ModalOverlayManager {
        var manager = modalOverlayManager
        if (manager == null) {
            manager = ModalOverlayManager(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                listener = this@VianBoardService
            }
            modalOverlayManager = manager
            rootContainer.addView(manager)
        }
        return manager
    }

    private fun dismissModalIfShowing() {
        modalOverlayManager?.dismiss()
    }

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        if (::rootContainer.isInitialized) {
            val loc = IntArray(2)
            rootContainer.getLocationInWindow(loc)
            outInsets.contentTopInsets = loc[1]
            outInsets.visibleTopInsets = loc[1]
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_FRAME
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (::generalSettingsManager.isInitialized) {
            generalSettings = generalSettingsManager.load()
            if (::mainKeyboardView.isInitialized) {
                mainKeyboardView.setKeyStyling(
                    cornerRadiusDp = generalSettings.keyCornerRadiusDp,
                    horizontalGapDp = generalSettings.keyHorizontalGapDp,
                    verticalGapDp = generalSettings.keyVerticalGapDp,
                    borderWidthDp = generalSettings.keyBorderWidthDp,
                    outlineEnabled = generalSettings.keyOutlineEnabled
                )
            }
        }
        richInputConnection.setInputConnection(currentInputConnection)
        wordComposer.reset()
        currentShiftState = Constants.SHIFT_OFF

        val inputClass = info?.inputType?.and(android.text.InputType.TYPE_MASK_CLASS)
        val inputVariation = info?.inputType?.and(android.text.InputType.TYPE_MASK_VARIATION)
        val imeAction = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)

        // Adapt layout to target input field
        when {
            inputClass == android.text.InputType.TYPE_CLASS_PHONE -> {
                mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.NUMBER_PAD)
            }
            inputClass == android.text.InputType.TYPE_CLASS_NUMBER ||
            inputClass == android.text.InputType.TYPE_CLASS_DATETIME -> {
                mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.NUMBER_PAD)
            }
            else -> {
                mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
            }
        }

        suggestionStripView.clear()
        dismissModalIfShowing()
        LogKeeper.log(LogTag.IME, LogLevel.DEBUG, "onStartInputView: restarting=$restarting, inputType=${info?.inputType}")
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        richInputConnection.setInputConnection(null)
        wordComposer.reset()
        suggestionStripView.clear()
        vaultOverlayView?.repository?.resetAutoLockTimer()
        LogKeeper.log(LogTag.IME, LogLevel.DEBUG, "onFinishInputView: finishingInput=$finishingInput")
    }

    // PointerTracker.KeyboardActionListener Callbacks
    override fun onKeyPress(key: Key) {
        val isSpecial = key.code < 0 || key.code == Constants.CODE_ENTER || key.code == Constants.CODE_DELETE
        if (::feedbackManager.isInitialized) {
            feedbackManager.performKeyPressFeedback(mainKeyboardView, generalSettings, isSpecial)
        }
    }

    override fun onKeyRelease(key: Key) {
        when (key.code) {
            Constants.CODE_SHIFT -> handleShiftTap()
            Constants.CODE_SWITCH_ALPHA_SYMBOL -> handleSymbolSwitch(key.label)
            Constants.CODE_NUMPAD -> handleNumpadSwitch()
            Constants.CODE_DELETE -> handleDelete()
            Constants.CODE_SPACE -> handleSpace()
            Constants.CODE_ENTER -> handleEnter()
            Constants.CODE_CLIPBOARD -> getOrCreateModalOverlay().showModal(ModalOverlayManager.ModalType.CLIPBOARD)
            Constants.CODE_PROMPT_LIST -> getOrCreateModalOverlay().showModal(ModalOverlayManager.ModalType.PROMPT_LIST)
            Constants.CODE_DESKTOP_NAV -> getOrCreateModalOverlay().showModal(ModalOverlayManager.ModalType.DESKTOP_NAV)
            Constants.CODE_EMOJI -> getOrCreateModalOverlay().showModal(ModalOverlayManager.ModalType.EMOJI)
            Constants.CODE_VAULT -> handleVaultTrigger()
            Constants.CODE_VOICE -> handleVoiceInputTrigger()
            else -> handleCharacterKey(key)
        }
    }

    override fun onKeyLongPress(key: Key) {
        when (key.code) {
            Constants.CODE_SHIFT -> {
                // Long press shift directly triggers Caps Lock mode
                currentShiftState = Constants.SHIFT_LOCKED
                mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_CAPSLOCK)
            }
            Constants.CODE_DELETE -> {
                richInputConnection.deleteLastWord()
                wordComposer.reset()
                updateSuggestions()
            }
            Constants.CODE_NUMPAD -> {
                Toast.makeText(this, "Vian Vault: Encrypted Offline Storage (Phase 4)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMoreKeySelected(candidate: String) {
        when (candidate) {
            "⚙", "⚙️" -> {
                // Settings action
                val intent = Intent(this, com.example.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("destination", "settings")
                }
                startActivity(intent)
                return
            }
            "📋" -> {
                getOrCreateModalOverlay().showModal(ModalOverlayManager.ModalType.CLIPBOARD)
                return
            }
            "☺", "😀" -> {
                getOrCreateModalOverlay().showModal(ModalOverlayManager.ModalType.EMOJI)
                return
            }
            "🌐" -> {
                val nextSubtype = subtypeSwitcher.switchToNextSubtype()
                Toast.makeText(this, nextSubtype.displayName, Toast.LENGTH_SHORT).show()
                return
            }
            "⤢", "✋" -> {
                Toast.makeText(this, "One-Handed / Resize Mode (Phase 4)", Toast.LENGTH_SHORT).show()
                return
            }
            "🪵" -> {
                val intent = Intent(this, com.example.MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("destination", "log_keeper")
                }
                startActivity(intent)
                return
            }
            "🔐" -> {
                Toast.makeText(this, "Personal Vault: Coming in Phase 4", Toast.LENGTH_SHORT).show()
                return
            }
            "🛡" -> {
                Toast.makeText(this, "Security Vault: Encrypted Storage (Phase 4)", Toast.LENGTH_SHORT).show()
                return
            }
            "📝" -> {
                getOrCreateModalOverlay().showModal(ModalOverlayManager.ModalType.PROMPT_LIST)
                return
            }
            "🎙", "🎙️" -> {
                Toast.makeText(this, "Voice Input: Coming in Phase 4", Toast.LENGTH_SHORT).show()
                return
            }
        }

        richInputConnection.commitText(candidate, 1)
        if (candidate.length == 1 && candidate[0].isLetter()) {
            wordComposer.add(candidate[0].code)
        } else {
            wordComposer.reset()
        }

        // Reset shift if single shift was active
        if (currentShiftState == Constants.SHIFT_ON) {
            currentShiftState = Constants.SHIFT_OFF
            mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
        }

        updateSuggestions()
    }

    override fun onSpacebarSlide(deltaX: Float) {
        val steps = (deltaX / 14f).toInt()
        if (steps != 0) {
            richInputConnection.moveCursor(steps)
            try {
                mainKeyboardView.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            } catch (_: Exception) {}
        }
    }

    override fun onBackspaceSwipe(deltaX: Float) {
        // DeltaX is negative when dragging leftward from Backspace key
        val dragDistPx = -deltaX
        val stepSizePx = ResourceUtils.dpToPx(this, 28f)
        val targetWordCount = (dragDistPx / stepSizePx).toInt().coerceAtLeast(0)

        if (swipeDeleteInitialTextBefore.isEmpty()) {
            swipeDeleteInitialTextBefore = richInputConnection.getTextBeforeCursor(1000)
        }

        val text = swipeDeleteInitialTextBefore
        if (text.isEmpty()) return

        if (targetWordCount == 0) {
            // Dragged back towards origin - clear selection
            if (swipeDeleteWordsSelected > 0) {
                val currentCursor = text.length
                richInputConnection.setSelection(currentCursor, currentCursor)
                swipeDeleteWordsSelected = 0
                swipeDeleteCharsSelected = 0
            }
            return
        }

        // Calculate character span for targetWordCount words backward
        var wordCount = 0
        var charIndex = text.length - 1

        while (charIndex >= 0 && wordCount < targetWordCount) {
            // Skip whitespaces
            while (charIndex >= 0 && text[charIndex].isWhitespace()) {
                charIndex--
            }
            if (charIndex < 0) break

            // Skip word characters
            while (charIndex >= 0 && !text[charIndex].isWhitespace()) {
                charIndex--
            }
            wordCount++
        }

        val charsToSelect = text.length - 1 - charIndex
        if (charsToSelect != swipeDeleteCharsSelected) {
            swipeDeleteWordsSelected = wordCount
            swipeDeleteCharsSelected = charsToSelect
            val selectionStart = (text.length - charsToSelect).coerceAtLeast(0)
            val selectionEnd = text.length
            richInputConnection.setSelection(selectionStart, selectionEnd)

            try {
                mainKeyboardView.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            } catch (_: Exception) {}
        }
    }

    override fun onBackspaceSwipeRelease() {
        if (swipeDeleteCharsSelected > 0) {
            richInputConnection.deleteSelectedText()
            try {
                mainKeyboardView.performHapticFeedback(
                    HapticFeedbackConstants.CONFIRM,
                    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
                )
            } catch (_: Exception) {}
        }
        swipeDeleteInitialTextBefore = ""
        swipeDeleteWordsSelected = 0
        swipeDeleteCharsSelected = 0
        wordComposer.reset()
        updateSuggestions()
    }

    private fun handleCharacterKey(key: Key) {
        val code = key.code
        richInputConnection.commitText(key.label, 1)
        wordComposer.add(code)

        // Reset shift if single shift was active
        if (currentShiftState == Constants.SHIFT_ON) {
            currentShiftState = Constants.SHIFT_OFF
            mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
        }

        updateSuggestions()
    }

    private fun handleDelete() {
        if (!wordComposer.isEmpty) {
            wordComposer.deleteLast()
        }
        richInputConnection.deleteBackward(1)
        updateSuggestions()
    }

    private fun handleSpace() {
        richInputConnection.commitText(" ", 1)
        wordComposer.reset()
        suggestionStripView.clear()
    }

    private fun handleEnter() {
        wordComposer.reset()
        suggestionStripView.clear()
        richInputConnection.sendEnterKeyEvent()
    }

    private fun handleShiftTap() {
        currentShiftState = when (currentShiftState) {
            Constants.SHIFT_OFF -> {
                mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER)
                Constants.SHIFT_ON
            }
            Constants.SHIFT_ON -> {
                mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
                Constants.SHIFT_OFF
            }
            Constants.SHIFT_LOCKED -> {
                // If Caps Lock was locked, tapping shift unlocks and returns to lowercase
                mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
                Constants.SHIFT_OFF
            }
            else -> {
                mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
                Constants.SHIFT_OFF
            }
        }
    }

    private fun handleSymbolSwitch(label: String) {
        currentShiftState = Constants.SHIFT_OFF
        when (label) {
            "?123" -> mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.SYMBOLS_1)
            "=\\<" -> mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.SYMBOLS_2)
            "ABC" -> mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER)
        }
    }

    private fun handleNumpadSwitch() {
        currentShiftState = Constants.SHIFT_OFF
        mainKeyboardView.setLayoutMode(KeyboardLayoutBuilder.LayoutMode.NUMBER_PAD)
    }

    private fun updateSuggestions() {
        if (wordComposer.isEmpty) {
            suggestionStripView.clear()
            return
        }
    }

    // SuggestionStripView.SuggestionStripListener
    override fun onSuggestionClicked(wordInfo: SuggestedWordInfo) {
        richInputConnection.deleteBackward(wordComposer.size)
        richInputConnection.commitText(wordInfo.word + " ", 1)
        wordComposer.reset()
        suggestionStripView.clear()
    }

    override fun onFastActionClicked(actionCode: Int) {
        when (actionCode) {
            Constants.CODE_SELECT_ALL -> richInputConnection.selectAll()
            Constants.CODE_COPY -> richInputConnection.copy()
            Constants.CODE_PASTE -> richInputConnection.paste()
        }
    }

    // ModalOverlayManager.ModalActionListener
    override fun onDismissModal() {
        // Modal dismissed back to main keyboard
    }

    override fun onBottomBarAction(code: Int) {
        when (code) {
            Constants.CODE_SPACE -> richInputConnection.commitText(" ", 1)
            Constants.CODE_DELETE -> richInputConnection.deleteBackward(1)
            Constants.CODE_ENTER -> richInputConnection.sendEnterKeyEvent()
        }
    }

    override fun onPasteItem(content: String) {
        richInputConnection.commitText(content, 1)
        dismissModalIfShowing()
    }

    private fun handleVoiceInputTrigger() {
        Toast.makeText(this, "Voice Input: Offline Engine (Phase 4)", Toast.LENGTH_SHORT).show()
    }

    private fun handleVaultTrigger() {
        var vaultOverlay = vaultOverlayView
        if (vaultOverlay == null) {
            vaultOverlay = VaultOverlayView(this).apply {
                actionListener = this@VianBoardService
            }
            vaultOverlayView = vaultOverlay
        } else {
            vaultOverlay.updateUiState()
        }
        getOrCreateModalOverlay().showModal(ModalOverlayManager.ModalType.VAULT, vaultOverlay)
    }

    // VaultOverlayView.VaultOverlayActionListener
    override fun onInjectText(text: String) {
        richInputConnection.commitText(text, 1)
        dismissModalIfShowing()
    }

    override fun onDismissRequested() {
        dismissModalIfShowing()
    }

    override fun onSpaceClicked() {
        richInputConnection.commitText(" ", 1)
    }

    override fun onDeleteClicked() {
        richInputConnection.deleteBackward(1)
    }

    override fun onEnterClicked() {
        richInputConnection.sendEnterKeyEvent()
    }

    override fun onDestroy() {
        super.onDestroy()
        vaultOverlayView?.repository?.lockVault()
        modalOverlayManager = null
        vaultOverlayView = null
        LogKeeper.log(LogTag.IME, LogLevel.INFO, "VianBoardService onDestroy")
    }
}
