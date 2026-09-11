package com.example.ime.modal

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ime.keyboard.KeyboardTheme

/**
 * Unified 4-Button Bottom Bar Component exclusively for Clipboard and Emoji modals.
 * Layout: [ ABC ] [ Space ] [ ⌫ Backspace ] [ ↵ Enter ]
 * Built with authentic HeliBoard tactile keycaps (10dp corner radius, 1dp dark bottom bevel,
 * and stadium pill functional keys).
 */
class ModalBottomBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onAbcClick: (() -> Unit)? = null
    var onSpaceClick: (() -> Unit)? = null
    var onDeleteClick: (() -> Unit)? = null
    var onEnterClick: (() -> Unit)? = null

    private var theme = KeyboardTheme.loadFromPrefs(context)

    // Button bounding rectangles
    private val abcRect = RectF()
    private val spaceRect = RectF()
    private val deleteRect = RectF()
    private val enterRect = RectF()

    // Paints
    private val backgroundPaint = Paint()
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val actionKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val enterKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyBevelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val actionKeyBevelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val pressedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val spaceTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    private val spaceStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private var deleteIcon: Drawable? = null
    private var enterIcon: Drawable? = null

    private var pressedIndex = -1 // 0: ABC, 1: Space, 2: Delete, 3: Enter
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isRepeatingDelete = false

    private val repeatDeleteRunnable = object : Runnable {
        override fun run() {
            if (pressedIndex == 2) {
                isRepeatingDelete = true
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onDeleteClick?.invoke()
                mainHandler.postDelayed(this, 50)
            }
        }
    }

    init {
        updatePaints()
        loadIcons()
    }

    fun reloadTheme() {
        theme = KeyboardTheme.loadFromPrefs(context)
        updatePaints()
        loadIcons()
        requestLayout()
        invalidate()
    }

    private fun updatePaints() {
        val density = resources.displayMetrics.density
        backgroundPaint.color = theme.backgroundColor
        keyBgPaint.color = 0xFFFFFFFF.toInt()
        actionKeyPaint.color = 0xFFE2E8F0.toInt()
        enterKeyPaint.color = theme.enterKeyColor
        keyBevelPaint.color = theme.keyBottomBevelColor
        actionKeyBevelPaint.color = theme.actionKeyBevelColor
        pressedPaint.color = 0xFFCBD5E1.toInt()

        spaceStrokePaint.color = 0xFFCBD5E1.toInt()
        spaceStrokePaint.strokeWidth = 1f * density

        textPaint.color = Color.BLACK
        textPaint.textSize = 15f * density

        spaceTextPaint.color = 0xFF64748B.toInt()
        spaceTextPaint.textSize = 13f * density
    }

    private fun loadIcons() {
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.sym_keyboard_delete_rounded)?.mutate()
        deleteIcon?.setTint(Color.BLACK)

        enterIcon = ContextCompat.getDrawable(context, R.drawable.sym_keyboard_return_rounded)?.mutate()
        enterIcon?.setTint(theme.enterTextColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val desiredHeight = (theme.keyHeightDp * density + theme.verticalGapDp * density).toInt()
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeButtonRects(w.toFloat(), h.toFloat())
    }

    private fun computeButtonRects(w: Float, h: Float) {
        val density = resources.displayMetrics.density
        val horizGap = theme.horizontalGapDp * density
        val vertGap = theme.verticalGapDp * density / 2f
        val sidePadding = horizGap

        val availableW = w - sidePadding * 2f
        val keyHeight = h - vertGap * 2f
        val topY = vertGap
        val botY = topY + keyHeight

        val actionW = (availableW * 0.16f).coerceIn(48f * density, 80f * density)
        val spaceW = availableW - (actionW * 3f) - (horizGap * 3f)

        // 1. [ ABC ]
        var curX = sidePadding
        abcRect.set(curX, topY, curX + actionW, botY)
        curX += actionW + horizGap

        // 2. [ Space ]
        spaceRect.set(curX, topY, curX + spaceW, botY)
        curX += spaceW + horizGap

        // 3. [ ⌫ Backspace ]
        deleteRect.set(curX, topY, curX + actionW, botY)
        curX += actionW + horizGap

        // 4. [ ↵ Enter ]
        enterRect.set(curX, topY, curX + actionW, botY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        val density = resources.displayMetrics.density

        // 1. Render [ ABC ] stadium pill
        drawPillKey(canvas, abcRect, actionKeyPaint, null, pressedIndex == 0)
        val abcBaseline = abcRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText("ABC", abcRect.centerX(), abcBaseline, textPaint)

        // 2. Render [ Space ] stadium pill
        drawPillKey(canvas, spaceRect, keyBgPaint, spaceStrokePaint, pressedIndex == 1)
        val spaceBaseline = spaceRect.centerY() - (spaceTextPaint.descent() + spaceTextPaint.ascent()) / 2f
        canvas.drawText("VianBoard", spaceRect.centerX(), spaceBaseline, spaceTextPaint)

        // 3. Render [ ⌫ Backspace ] stadium pill
        drawPillKey(canvas, deleteRect, actionKeyPaint, null, pressedIndex == 2)
        deleteIcon?.let { icon ->
            val iconSize = (22f * density).toInt()
            val left = (deleteRect.centerX() - iconSize / 2f).toInt()
            val top = (deleteRect.centerY() - iconSize / 2f).toInt()
            icon.setBounds(left, top, left + iconSize, top + iconSize)
            icon.draw(canvas)
        }

        // 4. Render [ ↵ Enter ] stadium pill
        drawPillKey(canvas, enterRect, enterKeyPaint, null, pressedIndex == 3)
        enterIcon?.let { icon ->
            val iconSize = (22f * density).toInt()
            val left = (enterRect.centerX() - iconSize / 2f).toInt()
            val top = (enterRect.centerY() - iconSize / 2f).toInt()
            icon.setBounds(left, top, left + iconSize, top + iconSize)
            icon.draw(canvas)
        }
    }

    private fun drawPillKey(
        canvas: Canvas,
        rect: RectF,
        surfacePaint: Paint,
        strokePaint: Paint?,
        isPressed: Boolean
    ) {
        val radius = rect.height() / 2f
        if (isPressed) {
            canvas.drawRoundRect(rect, radius, radius, pressedPaint)
        } else {
            canvas.drawRoundRect(rect, radius, radius, surfacePaint)
            strokePaint?.let {
                canvas.drawRoundRect(rect, radius, radius, it)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedIndex = findButtonIndex(x, y)
                if (pressedIndex != -1) {
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    if (pressedIndex == 2) {
                        isRepeatingDelete = false
                        mainHandler.postDelayed(repeatDeleteRunnable, 400)
                    }
                    invalidate()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val newIndex = findButtonIndex(x, y)
                if (newIndex != pressedIndex) {
                    mainHandler.removeCallbacks(repeatDeleteRunnable)
                    pressedIndex = newIndex
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                mainHandler.removeCallbacks(repeatDeleteRunnable)
                val targetIndex = pressedIndex
                pressedIndex = -1
                invalidate()

                if (targetIndex != -1) {
                    when (targetIndex) {
                        0 -> onAbcClick?.invoke()
                        1 -> onSpaceClick?.invoke()
                        2 -> {
                            if (!isRepeatingDelete) {
                                onDeleteClick?.invoke()
                            }
                            isRepeatingDelete = false
                        }
                        3 -> onEnterClick?.invoke()
                    }
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                mainHandler.removeCallbacks(repeatDeleteRunnable)
                pressedIndex = -1
                isRepeatingDelete = false
                invalidate()
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findButtonIndex(x: Float, y: Float): Int {
        return when {
            abcRect.contains(x, y) -> 0
            spaceRect.contains(x, y) -> 1
            deleteRect.contains(x, y) -> 2
            enterRect.contains(x, y) -> 3
            else -> -1
        }
    }
}
