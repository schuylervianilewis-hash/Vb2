package com.example.ime.keyboard

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

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
    var onKeyLongPress: ((KeyData) -> Unit)? = null

    // Canvas Paints (Pre-allocated, zero allocation in onDraw)
    private val backgroundPaint = Paint()
    private val keyBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val actionKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
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
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.DEFAULT
    }
    private val popupBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 4f, 0x66000000)
    }
    private val popupTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private var activePressedKey: KeyData? = null
    private var lastShiftPressTime = 0L

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
            } else {
                onKeyLongPress?.invoke(key)
            }
        }
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        updatePaints()
    }

    private fun updatePaints() {
        val density = resources.displayMetrics.density

        backgroundPaint.color = theme.backgroundColor
        keyBackgroundPaint.color = theme.keyBackgroundColor
        actionKeyPaint.color = theme.actionKeyColor
        pressedKeyPaint.color = theme.pressedKeyColor

        borderPaint.color = theme.borderColor
        borderPaint.strokeWidth = theme.borderWidthDp * density

        textPaint.color = theme.textColor
        textPaint.textSize = 19f * density

        actionTextPaint.color = theme.textColor
        actionTextPaint.textSize = 14f * density

        hintPaint.color = theme.hintColor
        hintPaint.textSize = 10f * density

        popupBackgroundPaint.color = theme.popupBackgroundColor
        popupTextPaint.color = theme.popupTextColor
        popupTextPaint.textSize = 28f * density
    }

    fun reloadTheme() {
        theme = KeyboardTheme.loadFromPrefs(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density
        // Total calculated keyboard height: 4 rows + gaps + paddings
        val calculatedHeight = (theme.keyHeightDp * 4 * density) + (theme.verticalGapDp * 3 * density) + (16f * density)
        val height = calculatedHeight.toInt().coerceAtLeast((220 * density).toInt())
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val density = resources.displayMetrics.density
        layout.buildLayout(w.toFloat(), h.toFloat(), theme, density)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        val cornerRadius = theme.keyCornerRadiusDp * density
        val hasBorder = theme.borderWidthDp > 0

        // 1. Draw Keyboard Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // 2. Draw Keys
        for (key in layout.keys) {
            val isActionKey = key.type != KeyType.CHARACTER && key.type != KeyType.SPACE

            val currentBgPaint = when {
                key.isPressed -> pressedKeyPaint
                isActionKey -> actionKeyPaint
                else -> keyBackgroundPaint
            }

            // Key background rect
            canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, currentBgPaint)

            // Key border
            if (hasBorder) {
                canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, borderPaint)
            }

            // Key label
            val textY = key.bounds.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
            val paintToUse = if (key.type == KeyType.CHARACTER || key.type == KeyType.SPACE) textPaint else actionTextPaint
            canvas.drawText(key.label, key.bounds.centerX(), textY, paintToUse)

            // Hint label (top right corner)
            if (theme.showHints && key.hintLabel != null) {
                val hintX = key.bounds.right - (4f * density)
                val hintY = key.bounds.top + (11f * density)
                canvas.drawText(key.hintLabel, hintX, hintY, hintPaint)
            }
        }

        // 3. Draw Inline Key Popup Bubble (if active and enabled)
        if (theme.showPopups && activePressedKey != null && activePressedKey?.type == KeyType.CHARACTER) {
            val key = activePressedKey!!
            val popupWidth = key.bounds.width() * 1.3f
            val popupHeight = key.bounds.height() * 1.3f
            val popupLeft = key.bounds.centerX() - (popupWidth / 2)
            val popupTop = key.bounds.top - popupHeight - (6f * density)
            val popupRect = RectF(popupLeft, popupTop, popupLeft + popupWidth, key.bounds.top)

            val bubbleRadius = (theme.keyCornerRadiusDp + 4f) * density
            canvas.drawRoundRect(popupRect, bubbleRadius, bubbleRadius, popupBackgroundPaint)
            if (hasBorder) {
                canvas.drawRoundRect(popupRect, bubbleRadius, bubbleRadius, borderPaint)
            }

            val pTextY = popupRect.centerY() - ((popupTextPaint.descent() + popupTextPaint.ascent()) / 2)
            canvas.drawText(key.label, popupRect.centerX(), pTextY, popupTextPaint)
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
                    mainHandler.postDelayed(longPressRunnable, 400)
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val key = layout.findKeyAt(x, y)
                if (key != activePressedKey) {
                    activePressedKey?.isPressed = false
                    mainHandler.removeCallbacks(longPressRunnable)
                    mainHandler.removeCallbacks(repeatBackspaceRunnable)
                    activePressedKey = key
                    key?.isPressed = true
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                mainHandler.removeCallbacks(longPressRunnable)
                mainHandler.removeCallbacks(repeatBackspaceRunnable)

                val key = activePressedKey
                if (key != null && !isRepeatingBackspace) {
                    handleKeySelection(key)
                }

                activePressedKey?.isPressed = false
                activePressedKey = null
                isRepeatingBackspace = false
                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                mainHandler.removeCallbacks(longPressRunnable)
                mainHandler.removeCallbacks(repeatBackspaceRunnable)
                activePressedKey?.isPressed = false
                activePressedKey = null
                isRepeatingBackspace = false
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
                layout.buildLayout(width.toFloat(), height.toFloat(), theme, density)
                invalidate()
            }

            KeyType.SYMBOLS_TOGGLE -> {
                layout.mode = if (layout.mode == KeyboardMode.CHARACTERS) KeyboardMode.SYMBOLS_1 else KeyboardMode.CHARACTERS
                val density = resources.displayMetrics.density
                layout.buildLayout(width.toFloat(), height.toFloat(), theme, density)
                invalidate()
            }

            KeyType.SYMBOLS_MORE_TOGGLE -> {
                layout.mode = if (layout.mode == KeyboardMode.SYMBOLS_1) KeyboardMode.SYMBOLS_2 else KeyboardMode.SYMBOLS_1
                val density = resources.displayMetrics.density
                layout.buildLayout(width.toFloat(), height.toFloat(), theme, density)
                invalidate()
            }

            else -> {
                onKeyAction?.invoke(key)
                // Auto-reset Shift if single capitalized letter typed
                if (layout.shiftState == ShiftState.ON && key.type == KeyType.CHARACTER) {
                    layout.shiftState = ShiftState.OFF
                    val density = resources.displayMetrics.density
                    layout.buildLayout(width.toFloat(), height.toFloat(), theme, density)
                    invalidate()
                }
            }
        }
    }
}
