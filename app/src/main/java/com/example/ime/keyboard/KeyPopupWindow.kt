package com.example.ime.keyboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow

class KeyPopupWindow(private val context: Context) {

    private val popupWindow = PopupWindow(context).apply {
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isTouchable = false
        isFocusable = false
        isOutsideTouchable = false
        elevation = 16f
    }

    private val popupView = PopupCanvasView(context)

    init {
        popupWindow.contentView = popupView
    }

    fun showSingleKey(anchor: View, key: KeyData, theme: KeyboardTheme) {
        popupView.mode = PopupMode.SINGLE
        popupView.theme = theme
        popupView.targetKey = key
        popupView.items = listOf(key.label)
        popupView.selectedIndex = 0

        val density = context.resources.displayMetrics.density
        val popupWidth = (key.bounds.width() * 1.35f).toInt().coerceAtLeast((48 * density).toInt())
        val popupHeight = (key.bounds.height() * 1.35f).toInt().coerceAtLeast((56 * density).toInt())

        popupWindow.width = popupWidth
        popupWindow.height = popupHeight

        val location = IntArray(2)
        anchor.getLocationInWindow(location)

        val posX = (location[0] + key.bounds.centerX() - (popupWidth / 2)).toInt()
        val posY = (location[1] + key.bounds.top - popupHeight - (4 * density)).toInt()

        if (popupWindow.isShowing) {
            popupWindow.update(posX, posY, popupWidth, popupHeight)
            popupView.invalidate()
        } else {
            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, posX, posY)
        }
    }

    fun showMoreKeys(anchor: View, key: KeyData, items: List<String>, theme: KeyboardTheme) {
        if (items.isEmpty()) return
        popupView.mode = PopupMode.MULTI
        popupView.theme = theme
        popupView.targetKey = key
        popupView.items = items
        popupView.selectedIndex = 0

        val density = context.resources.displayMetrics.density
        val itemWidth = (38f * density)
        val popupWidth = (items.size * itemWidth + (12f * density)).toInt()
        val popupHeight = (54f * density).toInt()

        popupWindow.width = popupWidth
        popupWindow.height = popupHeight

        val location = IntArray(2)
        anchor.getLocationInWindow(location)

        // Center strip over the key, clamp to screen edges
        val screenWidth = context.resources.displayMetrics.widthPixels
        val desiredX = location[0] + key.bounds.centerX() - (popupWidth / 2)
        val clampedX = desiredX.coerceIn(8f * density, screenWidth - popupWidth - (8f * density)).toInt()
        val posY = (location[1] + key.bounds.top - popupHeight - (6 * density)).toInt()

        popupView.popupWindowScreenX = clampedX.toFloat()
        popupView.itemWidth = itemWidth

        if (popupWindow.isShowing) {
            popupWindow.update(clampedX, posY, popupWidth, popupHeight)
            popupView.invalidate()
        } else {
            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, clampedX, posY)
        }
    }

    fun updateSelection(touchXOnScreen: Float) {
        if (popupView.mode == PopupMode.MULTI && popupView.items.isNotEmpty()) {
            val relativeX = touchXOnScreen - popupView.popupWindowScreenX - (6f * context.resources.displayMetrics.density)
            val index = (relativeX / popupView.itemWidth).toInt().coerceIn(0, popupView.items.size - 1)
            if (index != popupView.selectedIndex) {
                popupView.selectedIndex = index
                popupView.invalidate()
            }
        }
    }

    fun getSelectedItem(): String? {
        return popupView.items.getOrNull(popupView.selectedIndex)
    }

    fun isShowing(): Boolean = popupWindow.isShowing

    fun dismiss() {
        if (popupWindow.isShowing) {
            popupWindow.dismiss()
        }
    }

    private enum class PopupMode { SINGLE, MULTI }

    private class PopupCanvasView(context: Context) : View(context) {
        var mode = PopupMode.SINGLE
        var theme = KeyboardTheme()
        var targetKey: KeyData? = null
        var items: List<String> = emptyList()
        var selectedIndex: Int = 0
        var popupWindowScreenX: Float = 0f
        var itemWidth: Float = 0f

        private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            setShadowLayer(10f, 0f, 4f, 0x40000000)
        }
        private val selectedBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = 0xFF0284C7.toInt() // Accent Sky 600
        }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        private val selectedTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            color = 0xFFFFFFFF.toInt()
        }

        init {
            setLayerType(LAYER_TYPE_SOFTWARE, null) // For shadow rendering
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val density = resources.displayMetrics.density
            backgroundPaint.color = theme.popupBackgroundColor
            textPaint.color = theme.popupTextColor
            textPaint.textSize = if (mode == PopupMode.SINGLE) 24f * density else 19f * density
            selectedTextPaint.textSize = 20f * density

            val rect = RectF(4f * density, 4f * density, width - (4f * density), height - (4f * density))
            val cornerRadius = 12f * density

            // 1. Draw rounded bubble
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)

            if (mode == PopupMode.SINGLE) {
                val label = items.firstOrNull() ?: ""
                val textY = rect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
                canvas.drawText(label, rect.centerX(), textY, textPaint)
            } else {
                // Multi-key horizontal strip
                val startX = rect.left + (2f * density)
                val itemH = rect.height()

                for (i in items.indices) {
                    val itemLeft = startX + (i * itemWidth)
                    val itemRight = itemLeft + itemWidth
                    val itemRect = RectF(itemLeft, rect.top, itemRight, rect.bottom)

                    if (i == selectedIndex) {
                        val selRadius = 8f * density
                        val selInsetRect = RectF(itemLeft + (2f * density), rect.top + (2f * density), itemRight - (2f * density), rect.bottom - (2f * density))
                        canvas.drawRoundRect(selInsetRect, selRadius, selRadius, selectedBgPaint)

                        val textY = selInsetRect.centerY() - ((selectedTextPaint.descent() + selectedTextPaint.ascent()) / 2)
                        canvas.drawText(items[i], selInsetRect.centerX(), textY, selectedTextPaint)
                    } else {
                        val textY = itemRect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
                        canvas.drawText(items[i], itemRect.centerX(), textY, textPaint)
                    }
                }
            }
        }
    }
}
