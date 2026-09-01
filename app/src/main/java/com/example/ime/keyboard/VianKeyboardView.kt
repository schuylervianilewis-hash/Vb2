package com.example.ime.keyboard

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets

class VianKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var theme: KeyboardTheme = KeyboardTheme.loadFromPrefs(context)
        set(value) {
            field = value
            updatePaints()
            requestLayout()
            invalidate()
        }

    val layout = KeyboardLayout()
    var onKeyAction: ((KeyData) -> Unit)? = null
    var onTextCommit: ((String) -> Unit)? = null
    var onActionExpand: (() -> Unit)? = null
    var onActionSelection: (() -> Unit)? = null
    var onActionClipboard: (() -> Unit)? = null

    private val popupWindow = KeyPopupWindow(context)

    // Canvas Paints (Pre-allocated)
    private val backgroundPaint = Paint()
    private val toolbarBackgroundPaint = Paint()
    private val keyBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val actionKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val enterKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pressedKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val actionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val enterTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.DEFAULT
    }
    private val toolbarTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private var activePressedKey: KeyData? = null
    private var lastShiftPressTime = 0L
    private var isMultiPopupActive = false
    private var bottomNavInsetPx = 0f

    // Long-press and repeat handler
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isRepeatingBackspace = false

    private val repeatBackspaceRunnable = object : Runnable {
        override fun run() {
            if (activePressedKey?.type == KeyType.DELETE) {
                isRepeatingBackspace = true
                activePressedKey?.let { onKeyAction?.invoke(it) }
                mainHandler.postDelayed(this, 50)
            }
        }
    }

    private val longPressRunnable = Runnable {
        activePressedKey?.let { key ->
            if (key.type == KeyType.DELETE) {
                mainHandler.post(repeatBackspaceRunnable)
            } else if (key.moreKeys.isNotEmpty()) {
                isMultiPopupActive = true
                popupWindow.showMoreKeys(this@VianKeyboardView, key, key.moreKeys, theme)
            }
        }
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        updatePaints()

        setOnApplyWindowInsetsListener { _, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val navInsets = insets.getInsets(WindowInsets.Type.navigationBars())
                bottomNavInsetPx = navInsets.bottom.toFloat()
            } else {
                @Suppress("DEPRECATION")
                bottomNavInsetPx = insets.systemWindowInsetBottom.toFloat()
            }
            requestLayout()
            insets
        }
    }

    private fun updatePaints() {
        val density = resources.displayMetrics.density

        backgroundPaint.color = theme.backgroundColor
        toolbarBackgroundPaint.color = theme.backgroundColor
        keyBackgroundPaint.color = theme.keyBackgroundColor
        actionKeyPaint.color = theme.actionKeyColor
        enterKeyPaint.color = theme.enterKeyColor
        pressedKeyPaint.color = theme.pressedKeyColor

        borderPaint.color = theme.borderColor
        borderPaint.strokeWidth = theme.borderWidthDp * density

        textPaint.color = theme.textColor
        textPaint.textSize = 21f * density

        actionTextPaint.color = theme.textColor
        actionTextPaint.textSize = 15f * density

        enterTextPaint.color = theme.enterTextColor
        enterTextPaint.textSize = 18f * density

        hintPaint.color = theme.hintColor
        hintPaint.textSize = 10.5f * density

        toolbarTextPaint.color = theme.textColor
        toolbarTextPaint.textSize = 14.5f * density
    }

    fun reloadTheme() {
        theme = KeyboardTheme.loadFromPrefs(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density
        // 5 rows + toolbar + vertical gaps + padding + system navigation bar inset
        val rowsTotalHeight = (theme.keyHeightDp * 5 * density)
        val toolbarHeight = (theme.toolbarHeightDp * density)
        val gapsHeight = (theme.verticalGapDp * 5 * density)
        val paddingHeight = (12f * density)
        val totalCalculatedHeight = (rowsTotalHeight + toolbarHeight + gapsHeight + paddingHeight + bottomNavInsetPx).toInt()
        val height = totalCalculatedHeight.coerceAtLeast((260 * density).toInt())
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val density = resources.displayMetrics.density
        layout.buildLayout(w.toFloat(), h.toFloat(), theme, density, bottomNavInsetPx)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cornerRadius = theme.keyCornerRadiusDp * density
        val hasBorder = theme.borderWidthDp > 0

        // 1. Draw Keyboard Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // 2. Draw Top Toolbar Items
        for (key in layout.toolbarKeys) {
            val bgPaint = if (key.isPressed) pressedKeyPaint else toolbarBackgroundPaint
            canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, bgPaint)

            val textY = key.bounds.centerY() - ((toolbarTextPaint.descent() + toolbarTextPaint.ascent()) / 2)
            canvas.drawText(key.label, key.bounds.centerX(), textY, toolbarTextPaint)
        }

        // 3. Draw Main Keys
        for (key in layout.keys) {
            val isActionKey = key.type != KeyType.CHARACTER && key.type != KeyType.SPACE && key.type != KeyType.COMMA && key.type != KeyType.PERIOD
            val isEnter = key.type == KeyType.ENTER

            val currentBgPaint = when {
                key.isPressed -> pressedKeyPaint
                isEnter -> enterKeyPaint
                isActionKey -> actionKeyPaint
                else -> keyBackgroundPaint
            }

            // Key background rect
            canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, currentBgPaint)

            // Key border if set
            if (hasBorder) {
                canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, borderPaint)
            }

            // Key label
            val textY = key.bounds.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
            val paintToUse = when {
                isEnter -> enterTextPaint
                key.type == KeyType.CHARACTER || key.type == KeyType.SPACE || key.type == KeyType.COMMA || key.type == KeyType.PERIOD -> textPaint
                else -> actionTextPaint
            }
            canvas.drawText(key.label, key.bounds.centerX(), textY, paintToUse)

            // Hint label (top right corner)
            if (theme.showHints && key.hintLabel != null) {
                val hintX = key.bounds.right - (4f * density)
                val hintY = key.bounds.top + (11f * density)
                canvas.drawText(key.hintLabel, hintX, hintY, hintPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val key = layout.findKeyAt(x, y)
                if (key != null) {
                    activePressedKey = key
                    key.isPressed = true
                    isRepeatingBackspace = false
                    isMultiPopupActive = false

                    // Show single popup bubble via PopupWindow (Option C)
                    if (theme.showPopups && (key.type == KeyType.CHARACTER || key.type == KeyType.COMMA || key.type == KeyType.PERIOD)) {
                        popupWindow.showSingleKey(this, key, theme)
                    }

                    mainHandler.postDelayed(longPressRunnable, 350)
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (isMultiPopupActive) {
                    popupWindow.updateSelection(event.rawX)
                } else {
                    val key = layout.findKeyAt(x, y)
                    if (key != activePressedKey) {
                        activePressedKey?.isPressed = false
                        mainHandler.removeCallbacks(longPressRunnable)
                        mainHandler.removeCallbacks(repeatBackspaceRunnable)
                        popupWindow.dismiss()

                        activePressedKey = key
                        key?.isPressed = true
                        if (key != null && theme.showPopups && (key.type == KeyType.CHARACTER || key.type == KeyType.COMMA || key.type == KeyType.PERIOD)) {
                            popupWindow.showSingleKey(this, key, theme)
                            mainHandler.postDelayed(longPressRunnable, 350)
                        }
                        invalidate()
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                mainHandler.removeCallbacks(longPressRunnable)
                mainHandler.removeCallbacks(repeatBackspaceRunnable)

                if (isMultiPopupActive) {
                    val selected = popupWindow.getSelectedItem()
                    if (!selected.isNullOrEmpty()) {
                        onTextCommit?.invoke(selected)
                    }
                } else {
                    val key = activePressedKey
                    if (key != null && !isRepeatingBackspace) {
                        handleKeySelection(key)
                    }
                }

                popupWindow.dismiss()
                activePressedKey?.isPressed = false
                activePressedKey = null
                isRepeatingBackspace = false
                isMultiPopupActive = false
                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                mainHandler.removeCallbacks(longPressRunnable)
                mainHandler.removeCallbacks(repeatBackspaceRunnable)
                popupWindow.dismiss()
                activePressedKey?.isPressed = false
                activePressedKey = null
                isRepeatingBackspace = false
                isMultiPopupActive = false
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleKeySelection(key: KeyData) {
        when (key.type) {
            KeyType.SHIFT -> {
                val now = System.currentTimeMillis()
                layout.shiftState = when (layout.shiftState) {
                    ShiftState.OFF -> ShiftState.ON
                    ShiftState.ON -> {
                        if (now - lastShiftPressTime < 350) ShiftState.CAPS_LOCK else ShiftState.OFF
                    }
                    ShiftState.CAPS_LOCK -> ShiftState.OFF
                }
                lastShiftPressTime = now
                val density = resources.displayMetrics.density
                layout.buildLayout(width.toFloat(), height.toFloat(), theme, density, bottomNavInsetPx)
                invalidate()
            }

            KeyType.SYMBOLS_TOGGLE -> {
                layout.mode = if (layout.mode == KeyboardMode.CHARACTERS) KeyboardMode.SYMBOLS_1 else KeyboardMode.CHARACTERS
                val density = resources.displayMetrics.density
                layout.buildLayout(width.toFloat(), height.toFloat(), theme, density, bottomNavInsetPx)
                invalidate()
            }

            KeyType.SYMBOLS_MORE_TOGGLE -> {
                layout.mode = if (layout.mode == KeyboardMode.SYMBOLS_1) KeyboardMode.SYMBOLS_2 else KeyboardMode.SYMBOLS_1
                val density = resources.displayMetrics.density
                layout.buildLayout(width.toFloat(), height.toFloat(), theme, density, bottomNavInsetPx)
                invalidate()
            }

            KeyType.ACTION_EXPAND -> {
                onActionExpand?.invoke()
            }

            KeyType.ACTION_SELECTION -> {
                onActionSelection?.invoke()
            }

            KeyType.ACTION_CLIPBOARD -> {
                onActionClipboard?.invoke()
            }

            KeyType.SUGGESTION -> {
                onTextCommit?.invoke(key.label + " ")
            }

            else -> {
                onKeyAction?.invoke(key)
                // Auto-reset Shift if single capitalized letter typed
                if (layout.shiftState == ShiftState.ON && key.type == KeyType.CHARACTER) {
                    layout.shiftState = ShiftState.OFF
                    val density = resources.displayMetrics.density
                    layout.buildLayout(width.toFloat(), height.toFloat(), theme, density, bottomNavInsetPx)
                    invalidate()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        popupWindow.dismiss()
        super.onDetachedFromWindow()
    }
}
