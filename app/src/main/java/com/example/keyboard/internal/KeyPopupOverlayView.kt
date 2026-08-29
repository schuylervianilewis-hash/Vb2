package com.example.keyboard.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import com.example.foundation.utils.ResourceUtils

/**
 * High-performance hardware canvas overlay for Key Preview and MoreKeys 2-Row Popups.
 * Anchored to the IME root FrameLayout so it seamlessly floats OVER the top toolbar / suggestion strip.
 */
class KeyPopupOverlayView(context: Context) : View(context) {

    // Popup Data Classes
    data class PreviewState(
        val label: String,
        val bounds: RectF
    )

    data class MoreKeysState(
        val parentKey: Key,
        val items: List<String>,
        val itemBounds: List<RectF>,
        val cardBounds: RectF,
        var selectedIndex: Int
    )

    private var previewState: PreviewState? = null
    private var moreKeysState: MoreKeysState? = null

    // Preview Bubble Paints (HeliBoard Soft Pill Look)
    private val previewBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CFD8DC") // Soft grey-blue pill
        style = Paint.Style.FILL
    }

    private val previewShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90A4AE")
        style = Paint.Style.STROKE
        strokeWidth = ResourceUtils.dpToPx(context, 1f)
    }

    private val previewTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#263238")
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 26f)
    }

    // MoreKeys Multi-Row Popup Card Paints (Exact HeliBoard Theme)
    private val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#ECEFF1") // HeliBoard soft off-white card
        style = Paint.Style.FILL
        setShadowLayer(
            ResourceUtils.dpToPx(context, 6f),
            0f,
            ResourceUtils.dpToPx(context, 2f),
            Color.parseColor("#40000000")
        )
    }

    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CFD8DC")
        style = Paint.Style.STROKE
        strokeWidth = ResourceUtils.dpToPx(context, 1f)
    }

    private val itemSelectedBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0BEC5") // Soft Slate-Grey highlight
        style = Paint.Style.FILL
    }

    private val itemTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#000000") // Deep pure black glyphs
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 20f)
    }

    private val itemSelectedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#000000") // Deep pure black selected glyphs
        textAlign = Paint.Align.CENTER
        textSize = ResourceUtils.spToPx(context, 22f)
    }

    private val cardCornerRadius = ResourceUtils.dpToPx(context, 10f)
    private val cellCornerRadius = ResourceUtils.dpToPx(context, 6f)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        visibility = GONE
    }

    fun showKeyPreview(key: Key, offsetYInRoot: Float) {
        val bubbleWidth = key.bounds.width() * 1.15f
        val bubbleHeight = key.bounds.height() * 1.35f
        val centerX = key.bounds.centerX()
        val bottomY = offsetYInRoot + key.bounds.top - ResourceUtils.dpToPx(context, 4f)
        val topY = bottomY - bubbleHeight

        val bounds = RectF(
            centerX - bubbleWidth / 2f,
            topY,
            centerX + bubbleWidth / 2f,
            bottomY
        )

        previewState = PreviewState(key.label, bounds)
        visibility = VISIBLE
        invalidate()
    }

    fun dismissKeyPreview() {
        if (previewState != null) {
            previewState = null
            if (moreKeysState == null) visibility = GONE
            invalidate()
        }
    }

    fun showMoreKeys(key: Key, items: List<String>, offsetYInRoot: Float): Boolean {
        if (items.isEmpty()) return false

        // Compute 1 or 2 rows based on item count
        val isTwoRows = items.size > 7
        val columns = if (isTwoRows) (items.size + 1) / 2 else items.size
        val cellWidth = maxOf(ResourceUtils.dpToPx(context, 38f), (key.bounds.width() * 0.9f))
        val cellHeight = ResourceUtils.dpToPx(context, 44f)
        val cardPadding = ResourceUtils.dpToPx(context, 6f)

        val totalCardWidth = columns * cellWidth + (cardPadding * 2)
        val totalCardHeight = (if (isTwoRows) 2 else 1) * cellHeight + (cardPadding * 2)

        val minMargin = ResourceUtils.dpToPx(context, 6f)
        val maxRight = (parent as? View)?.width?.toFloat() ?: (width.toFloat().takeIf { it > 0 } ?: 1080f)

        // Center card on key, but clamp within screen margins
        val startX = (key.bounds.centerX() - totalCardWidth / 2f).coerceIn(minMargin, maxRight - totalCardWidth - minMargin)
        // Position card well above key, freely overlapping the top toolbar / suggestion strip
        val cardTop = (offsetYInRoot + key.bounds.top - totalCardHeight - ResourceUtils.dpToPx(context, 10f)).coerceAtLeast(minMargin)
        val cardBounds = RectF(startX, cardTop, startX + totalCardWidth, cardTop + totalCardHeight)

        // Compute individual cell bounds
        val itemBounds = mutableListOf<RectF>()
        for (i in items.indices) {
            val row = if (isTwoRows) i / columns else 0
            val col = if (isTwoRows) i % columns else i
            val cellLeft = startX + cardPadding + col * cellWidth
            val cellTop = cardTop + cardPadding + row * cellHeight
            itemBounds.add(RectF(cellLeft, cellTop, cellLeft + cellWidth, cellTop + cellHeight))
        }

        moreKeysState = MoreKeysState(
            parentKey = key,
            items = items,
            itemBounds = itemBounds,
            cardBounds = cardBounds,
            selectedIndex = 0
        )
        // Key preview is replaced by morekeys card
        previewState = null
        visibility = VISIBLE
        invalidate()
        return true
    }

    fun handleMoreKeysMove(x: Float, y: Float) {
        val state = moreKeysState ?: return
        for (i in state.itemBounds.indices) {
            val rect = state.itemBounds[i]
            if (x >= rect.left && x <= rect.right && y >= rect.top - 20 && y <= rect.bottom + 20) {
                if (state.selectedIndex != i) {
                    state.selectedIndex = i
                    invalidate()
                }
                return
            }
        }
    }

    fun handleMoreKeysUp(x: Float, y: Float): String? {
        val state = moreKeysState ?: return null
        var selected: String? = null
        if (state.selectedIndex in state.items.indices) {
            selected = state.items[state.selectedIndex]
        }
        moreKeysState = null
        previewState = null
        visibility = GONE
        invalidate()
        return selected
    }

    fun dismissMoreKeys() {
        if (moreKeysState != null) {
            moreKeysState = null
            if (previewState == null) visibility = GONE
            invalidate()
        }
    }

    fun isShowingMoreKeys(): Boolean = moreKeysState != null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Draw Key Preview Bubble if active
        previewState?.let { preview ->
            val r = preview.bounds
            val corner = ResourceUtils.dpToPx(context, 12f)
            canvas.drawRoundRect(r, corner, corner, previewBgPaint)
            canvas.drawRoundRect(r, corner, corner, previewShadowPaint)

            val textY = r.centerY() - ((previewTextPaint.descent() + previewTextPaint.ascent()) / 2)
            canvas.drawText(preview.label, r.centerX(), textY, previewTextPaint)
        }

        // 2. Draw MoreKeys Floating Multi-Row Card if active
        moreKeysState?.let { card ->
            val cr = card.cardBounds
            canvas.drawRoundRect(cr, cardCornerRadius, cardCornerRadius, cardBgPaint)
            canvas.drawRoundRect(cr, cardCornerRadius, cardCornerRadius, cardBorderPaint)

            for (i in card.items.indices) {
                val itemRect = card.itemBounds[i]
                val isSelected = i == card.selectedIndex

                if (isSelected) {
                    canvas.drawRoundRect(itemRect, cellCornerRadius, cellCornerRadius, itemSelectedBgPaint)
                }

                val paint = if (isSelected) itemSelectedTextPaint else itemTextPaint
                val textY = itemRect.centerY() - ((paint.descent() + paint.ascent()) / 2)
                canvas.drawText(card.items[i], itemRect.centerX(), textY, paint)
            }
        }
    }
}
