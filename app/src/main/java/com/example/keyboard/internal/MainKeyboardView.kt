package com.example.keyboard.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
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
 * Hardware-accelerated Canvas-based keyboard view matching reference screenshot styling.
 * Renders pure white pill keycaps, slate labels, top-right sub-labels, and vector icons.
 */
class MainKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), PointerTracker.KeyboardActionListener {

    private val layoutBuilder = KeyboardLayoutBuilder()
    private var keys: List<Key> = emptyList()
    private var layoutMode = KeyboardLayoutBuilder.LayoutMode.ALPHA_LOWER
    private var currencySymbol = "₹"
    private var showNumberRow = true

    var actionListener: PointerTracker.KeyboardActionListener? = null
    private val pointerTracker = PointerTracker(this)

    // Vector Drawables
    private val shiftDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_ime_shift)
    private val backspaceDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_ime_backspace)
    private val enterDrawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_ime_enter)

    // Keycap Paints (Aesthetic matching 12-screenshot suite)
    private val keyBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFFFFF") // Crisp White
    }

    private val keyBgPressedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E0F2FE") // Soft Sky Blue tint on press
    }

    private val keyFunctionalBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#E2E8F0") // Soft Slate-Blue for functional keys
    }

    private val keyShadowBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = ResourceUtils.dpToPx(context, 1f)
        color = Color.parseColor("#CBD5E1") // Subtle border outline
    }

    // Label Paints
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A") // Deep Slate-900
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 20f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val numberPadDigitLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 26f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#64748B") // Slate-500 for corner sub-labels
        textAlign = Paint.Align.RIGHT
        textSize = ResourceUtils.spToPx(context, 11f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val shiftActiveLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0284C7") // Sky-Blue for active shift
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 20f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val shiftLockedBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0284C7")
        strokeWidth = ResourceUtils.dpToPx(context, 2.5f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    // More Keys 2-Row Popup Paints
    private val popupBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        style = Paint.Style.FILL
    }

    private val popupBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#38BDF8") // Sky-Blue border
        strokeWidth = ResourceUtils.dpToPx(context, 1.5f)
        style = Paint.Style.STROKE
    }

    private val popupItemSelectedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0EA5E9") // Active Sky-Blue fill
        style = Paint.Style.FILL
    }

    private val popupItemTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 18f)
    }

    private val popupItemSelectedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFFFFF")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 18f)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private data class MoreKeyPopup(
        val parentKey: Key,
        val items: List<String>,
        val itemBounds: List<RectF>,
        val popupBounds: RectF,
        var selectedIndex: Int
    )

    private var activeMoreKeyPopup: MoreKeyPopup? = null

    private val keyCornerRadius = ResourceUtils.dpToPx(context, 14f) // Pill rounded keycaps
    private val keyMarginHorizontal = ResourceUtils.dpToPx(context, 3.5f)
    private val keyMarginVertical = ResourceUtils.dpToPx(context, 4.5f)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        setBackgroundColor(Color.parseColor("#F1F5F9")) // Soft Slate-100 backdrop
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
        val isUpper = layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER || layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_CAPSLOCK
        val candidates = com.example.ime.data.MoreKeysSpecs.LONG_PRESS_POPUP_GRID[key.label.uppercase()] ?: emptyList()
        if (candidates.isEmpty()) return false

        val itemWidth = maxOf(key.bounds.width(), ResourceUtils.dpToPx(context, 38f))
        val itemHeight = key.bounds.height()
        val totalWidth = itemWidth * candidates.size
        val minMargin = ResourceUtils.dpToPx(context, 4f)
        val startX = (key.bounds.centerX() - totalWidth / 2f).coerceIn(minMargin, maxOf(minMargin, width - totalWidth - minMargin))
        val topY = (key.bounds.top - itemHeight - ResourceUtils.dpToPx(context, 8f)).coerceAtLeast(minMargin)

        val itemBounds = candidates.indices.map { i ->
            RectF(startX + i * itemWidth, topY, startX + (i + 1) * itemWidth, topY + itemHeight)
        }
        val popupBounds = RectF(startX, topY, startX + totalWidth, topY + itemHeight)

        activeMoreKeyPopup = MoreKeyPopup(
            parentKey = key,
            items = candidates,
            itemBounds = itemBounds,
            popupBounds = popupBounds,
            selectedIndex = 0
        )
        invalidate()
        return true
    }

    fun handleMoreKeysMove(x: Float, y: Float) {
        val popup = activeMoreKeyPopup ?: return
        for (i in popup.itemBounds.indices) {
            val rect = popup.itemBounds[i]
            if (x >= rect.left && x <= rect.right) {
                if (popup.selectedIndex != i) {
                    popup.selectedIndex = i
                    invalidate()
                }
                return
            }
        }
    }

    fun handleMoreKeysUp(x: Float, y: Float): String? {
        val popup = activeMoreKeyPopup ?: return null
        var selected: String? = null
        if (popup.selectedIndex in popup.items.indices) {
            selected = popup.items[popup.selectedIndex]
        }
        activeMoreKeyPopup = null
        invalidate()
        return selected
    }

    fun dismissMoreKeys() {
        if (activeMoreKeyPopup != null) {
            activeMoreKeyPopup = null
            invalidate()
        }
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
            val paint = when {
                key.isPressed -> keyBgPressedPaint
                key.isFunctional -> keyFunctionalBgPaint
                else -> keyBgPaint
            }

            // Draw Key Pill Background
            canvas.drawRoundRect(key.bounds, keyCornerRadius, keyCornerRadius, paint)
            // Draw Subtle Border Shadow
            canvas.drawRoundRect(key.bounds, keyCornerRadius, keyCornerRadius, keyShadowBorderPaint)

            // Determine text / icon rendering
            val isShiftKey = key.code == Constants.CODE_SHIFT
            val isDeleteKey = key.code == Constants.CODE_DELETE
            val isEnterKey = key.code == Constants.CODE_ENTER
            val isShiftActive = isShiftKey && (layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_UPPER || layoutMode == KeyboardLayoutBuilder.LayoutMode.ALPHA_CAPSLOCK)
            val isNumpadDigit = layoutMode == KeyboardLayoutBuilder.LayoutMode.NUMBER_PAD && key.code in '0'.code..'9'.code

            if (isShiftKey && shiftDrawable != null) {
                drawVectorInKey(canvas, shiftDrawable, key.bounds, isShiftActive)
            } else if (isDeleteKey && backspaceDrawable != null) {
                drawVectorInKey(canvas, backspaceDrawable, key.bounds, false)
            } else if (isEnterKey && enterDrawable != null) {
                drawVectorInKey(canvas, enterDrawable, key.bounds, false)
            } else {
                val currentLabelPaint = when {
                    isShiftActive -> shiftActiveLabelPaint
                    isNumpadDigit -> numberPadDigitLabelPaint
                    else -> labelPaint
                }

                // Draw Center Primary Label
                val centerY = key.bounds.centerY() - (currentLabelPaint.descent() + currentLabelPaint.ascent()) / 2
                canvas.drawText(key.label, key.bounds.centerX(), centerY, currentLabelPaint)
            }

            // Draw Top-Right Sub-label Hint (if present)
            key.hintLabel?.let { hint ->
                if (hint.isNotEmpty()) {
                    val hintX = key.bounds.right - ResourceUtils.dpToPx(context, 4.5f)
                    val hintY = key.bounds.top + ResourceUtils.dpToPx(context, 12f)
                    canvas.drawText(hint, hintX, hintY, hintPaint)
                }
            }
        }

        // Draw More Keys Popup on top if active
        activeMoreKeyPopup?.let { popup ->
            canvas.drawRoundRect(popup.popupBounds, keyCornerRadius, keyCornerRadius, popupBgPaint)
            canvas.drawRoundRect(popup.popupBounds, keyCornerRadius, keyCornerRadius, popupBorderPaint)

            for (i in popup.items.indices) {
                val rect = popup.itemBounds[i]
                val isSelected = i == popup.selectedIndex

                if (isSelected) {
                    canvas.drawRoundRect(rect, keyCornerRadius * 0.8f, keyCornerRadius * 0.8f, popupItemSelectedPaint)
                }

                val textPaint = if (isSelected) popupItemSelectedTextPaint else popupItemTextPaint
                val centerY = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
                canvas.drawText(popup.items[i], rect.centerX(), centerY, textPaint)
            }
        }
    }

    private fun drawVectorInKey(canvas: Canvas, drawable: Drawable, bounds: RectF, isTintActive: Boolean) {
        val iconSize = ResourceUtils.dpToPx(context, 22f).toInt()
        val left = (bounds.centerX() - iconSize / 2f).toInt()
        val top = (bounds.centerY() - iconSize / 2f).toInt()
        drawable.bounds = Rect(left, top, left + iconSize, top + iconSize)
        val tintColor = if (isTintActive) Color.parseColor("#0284C7") else Color.parseColor("#0F172A")
        drawable.setTint(tintColor)
        drawable.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (activeMoreKeyPopup != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    handleMoreKeysMove(event.x, event.y)
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    val selected = handleMoreKeysUp(event.x, event.y)
                    if (selected != null) {
                        actionListener?.onMoreKeySelected(selected)
                    }
                    pointerTracker.isShowingMoreKeys = false
                    return true
                }
                MotionEvent.ACTION_CANCEL -> {
                    dismissMoreKeys()
                    pointerTracker.isShowingMoreKeys = false
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
                pointerTracker.isShowingMoreKeys = true
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

