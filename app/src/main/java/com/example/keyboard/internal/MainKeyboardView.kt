package com.example.keyboard.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.R
import com.example.foundation.common.Constants
import com.example.foundation.utils.ResourceUtils

/**
 * Hardware-accelerated Canvas-based keyboard view matching HeliBoard reference screenshot styling.
 * Renders crisp white soft rounded rectangle keycaps, slate labels, top-right sub-labels, and vector icons.
 */
class MainKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), PointerTracker.KeyboardActionListener {

    private val layoutBuilder = KeyboardLayoutBuilder()
    private var keys: List<Key> = emptyList()
    val currentKeys: List<Key> get() = keys
    private var layoutMode = KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER
    private var currencySymbol = "₹"
    private var showNumberRow = true

    var actionListener: PointerTracker.KeyboardActionListener? = null
    var popupOverlay: KeyPopupOverlayView? = null
    var keyboardOffsetYInRoot: Float = 0f

    private val pointerTracker = PointerTracker(this)

    // Vector Drawables
    private val shiftDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_ime_shift)
    private val backspaceDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_ime_backspace)
    private val enterDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_ime_enter)

    // Keycap Paints (Aesthetic matching 12-screenshot HeliBoard suite)
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFFFFF") // Crisp White Keycaps
    }

    private val keyBgPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#DDE2E6") // Soft neutral grey press tint
    }

    private val keyFunctionalBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#DDE2E6") // Soft Slate/Grey for functional keys
    }

    private val keyActionEnterBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#78909C") // HeliBoard soft slate-blue action key
    }

    private val keyShadowBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = ResourceUtils.dpToPx(context, 0.75f)
        color = Color.parseColor("#D0D5DD") // Subtle keycap outline
    }

    // Label Paints
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#202124") // Deep neutral dark charcoal
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 20f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val numberPadDigitLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#202124")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 28f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575") // HeliBoard neutral grey sub-labels
        textAlign = Paint.Align.RIGHT
        textSize = ResourceUtils.spToPx(context, 10f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val hintCenterBottomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9E9E9E")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 10f)
    }

    private val shiftActiveLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1976D2")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 20f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val shiftLockedBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1976D2")
        strokeWidth = ResourceUtils.dpToPx(context, 2.5f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // Soft rounded rectangles matching HeliBoard screenshots (Customizable)
    private var keyCornerRadius = ResourceUtils.dpToPx(context, 6f)
    private var keyMarginHorizontal = ResourceUtils.dpToPx(context, 2.5f)
    private var keyMarginVertical = ResourceUtils.dpToPx(context, 3.5f)
    private var keyOutlineEnabled = true

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setBackgroundColor(Color.parseColor("#E8ECEF")) // Clean off-white HeliBoard backdrop
    }

    fun setKeyStyling(
        cornerRadiusDp: Float,
        horizontalGapDp: Float,
        verticalGapDp: Float,
        borderWidthDp: Float,
        outlineEnabled: Boolean
    ) {
        this.keyCornerRadius = ResourceUtils.dpToPx(context, cornerRadiusDp)
        this.keyMarginHorizontal = ResourceUtils.dpToPx(context, horizontalGapDp)
        this.keyMarginVertical = ResourceUtils.dpToPx(context, verticalGapDp)
        this.keyShadowBorderPaint.strokeWidth = ResourceUtils.dpToPx(context, borderWidthDp)
        this.keyOutlineEnabled = outlineEnabled
        dismissMoreKeys()
        rebuildKeys()
        invalidate()
    }

    fun setLayoutMode(mode: KeyboardLayoutBuilder.LayoutMode) {
        this.layoutMode = mode
        dismissMoreKeys()
        rebuildKeys()
        invalidate()
    }

    fun setCurrencySymbol(currency: String) {
        this.currencySymbol = currency
        dismissMoreKeys()
        rebuildKeys()
        invalidate()
    }

    fun setShowNumberRow(show: Boolean) {
        this.showNumberRow = show
        dismissMoreKeys()
        rebuildKeys()
        invalidate()
    }

    fun showMoreKeysPopup(key: Key): Boolean {
        val candidates = com.example.ime.data.MoreKeysSpecs.LONG_PRESS_POPUP_GRID[key.label.uppercase()] ?: emptyList()
        if (candidates.isEmpty()) return false

        pointerTracker.isShowingMoreKeys = true
        return popupOverlay?.showMoreKeys(key, candidates, keyboardOffsetYInRoot) ?: false
    }

    fun handleMoreKeysMove(x: Float, y: Float) {
        popupOverlay?.handleMoreKeysMove(x, y + keyboardOffsetYInRoot)
    }

    fun handleMoreKeysUp(x: Float, y: Float): String? {
        pointerTracker.isShowingMoreKeys = false
        return popupOverlay?.handleMoreKeysUp(x, y + keyboardOffsetYInRoot)
    }

    fun dismissMoreKeys() {
        pointerTracker.isShowingMoreKeys = false
        popupOverlay?.dismissMoreKeys()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredHeight = ResourceUtils.dpToPx(context, 230f).toInt()
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rebuildKeys()
    }

    private fun rebuildKeys() {
        if (width > 0 && height > 0) {
            keys = layoutBuilder.buildKeyboard(
                width = width.toFloat(),
                height = height.toFloat(),
                mode = layoutMode,
                showNumberRow = showNumberRow,
                currencySymbol = currencySymbol,
                keyMarginHorizontal = keyMarginHorizontal,
                keyMarginVertical = keyMarginVertical
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (key in keys) {
            val isActionEnter = key.code == Constants.CODE_ENTER
            val paint = when {
                key.isPressed -> keyBgPressedPaint
                isActionEnter -> keyActionEnterBgPaint
                key.isFunctional -> keyFunctionalBgPaint
                else -> keyBgPaint
            }

            // Draw Key Soft Rounded Rectangle Background
            canvas.drawRoundRect(key.bounds, keyCornerRadius, keyCornerRadius, paint)
            if (keyOutlineEnabled && !isActionEnter) {
                canvas.drawRoundRect(key.bounds, keyCornerRadius, keyCornerRadius, keyShadowBorderPaint)
            }

            // Draw Key Icons / Labels
            when (key.code) {
                Constants.CODE_SHIFT -> {
                    val isUpper = layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER
                    val isCaps = layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_CAPSLOCK
                    if (isCaps) {
                        drawCenteredIcon(canvas, key.bounds, shiftDrawable, Color.parseColor("#1976D2"))
                        val barY = key.bounds.bottom - ResourceUtils.dpToPx(context, 7f)
                        val halfW = ResourceUtils.dpToPx(context, 8f)
                        canvas.drawLine(key.bounds.centerX() - halfW, barY, key.bounds.centerX() + halfW, barY, shiftLockedBarPaint)
                    } else if (isUpper) {
                        drawCenteredIcon(canvas, key.bounds, shiftDrawable, Color.parseColor("#1976D2"))
                    } else {
                        drawCenteredIcon(canvas, key.bounds, shiftDrawable, Color.parseColor("#37474F"))
                    }
                }
                Constants.CODE_DELETE -> {
                    drawCenteredIcon(canvas, key.bounds, backspaceDrawable, Color.parseColor("#37474F"))
                }
                Constants.CODE_ENTER -> {
                    drawCenteredIcon(canvas, key.bounds, enterDrawable, Color.parseColor("#FFFFFF"))
                    // Draw triple dots hint at bottom-right if applicable
                    val dotsX = key.bounds.right - ResourceUtils.dpToPx(context, 7f)
                    val dotsY = key.bounds.bottom - ResourceUtils.dpToPx(context, 5f)
                    canvas.drawText("…", dotsX, dotsY, hintPaint)
                }
                else -> {
                    val isNumpadDigit = layoutMode == KeyboardLayoutBuilder.LayoutMode.NUMBER_PAD &&
                            key.label.length == 1 && key.label[0].isDigit()

                    // Main Text
                    val textPaint = if (isNumpadDigit) numberPadDigitLabelPaint else labelPaint
                    val centerX = key.bounds.centerX()
                    val textY = key.bounds.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
                    canvas.drawText(key.label, centerX, textY, textPaint)

                    // Secondary Sub-label Hint (Top-Right or Bottom-Center)
                    key.hintLabel?.let { hint ->
                        if (hint == "…") {
                            val dotX = key.bounds.centerX()
                            val dotY = key.bounds.bottom - ResourceUtils.dpToPx(context, 3f)
                            canvas.drawText(hint, dotX, dotY, hintCenterBottomPaint)
                        } else {
                            val hintX = key.bounds.right - ResourceUtils.dpToPx(context, 4f)
                            val hintY = key.bounds.top + ResourceUtils.dpToPx(context, 11f)
                            canvas.drawText(hint, hintX, hintY, hintPaint)
                        }
                    }
                }
            }
        }
    }

    private fun drawCenteredIcon(canvas: Canvas, bounds: RectF, drawable: Drawable?, tintColor: Int) {
        if (drawable == null) return
        val iconSize = ResourceUtils.dpToPx(context, 22f).toInt()
        val left = (bounds.centerX() - iconSize / 2f).toInt()
        val top = (bounds.centerY() - iconSize / 2f).toInt()
        drawable.setBounds(left, top, left + iconSize, top + iconSize)
        drawable.setTint(tintColor)
        drawable.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (pointerTracker.isShowingMoreKeys) {
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    handleMoreKeysMove(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    val candidate = handleMoreKeysUp(event.x, event.y)
                    if (candidate != null) {
                        actionListener?.onMoreKeySelected(candidate)
                    }
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    dismissMoreKeys()
                    return true
                }
            }
        }
        val handled = pointerTracker.processTouchEvent(event, keys)
        invalidate()
        return handled || super.onTouchEvent(event)
    }

    // PointerTracker Callbacks
    override fun onKeyPress(key: Key) {
        actionListener?.onKeyPress(key)
    }

    override fun onKeyRelease(key: Key) {
        actionListener?.onKeyRelease(key)
    }

    override fun onKeyLongPress(key: Key) {
        if (key.code == Constants.CODE_SHIFT ||
            key.code == Constants.CODE_DELETE ||
            key.code == Constants.CODE_NUMPAD) {
            actionListener?.onKeyLongPress(key)
        } else {
            if (showMoreKeysPopup(key)) {
                // Handled via KeyPopupOverlayView
            } else {
                actionListener?.onKeyLongPress(key)
            }
        }
    }

    override fun onSpacebarSlide(deltaX: Float) {
        actionListener?.onSpacebarSlide(deltaX)
    }

    override fun onBackspaceSwipe(deltaX: Float) {
        actionListener?.onBackspaceSwipe(deltaX)
    }

    override fun onBackspaceSwipeRelease() {
        actionListener?.onBackspaceSwipeRelease()
    }

    override fun onMoreKeySelected(candidate: String) {
        actionListener?.onMoreKeySelected(candidate)
    }
}
