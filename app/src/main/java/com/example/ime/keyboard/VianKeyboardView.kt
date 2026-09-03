package com.example.ime.keyboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import androidx.core.content.ContextCompat
import com.example.R
import com.example.ime.toolbar.ToolbarPreferences
import com.example.ime.toolbar.ToolbarTool

class VianKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var theme: KeyboardTheme = KeyboardTheme.loadFromPrefs(context)
        set(value) {
            field = value
            updatePaints()
            val density = resources.displayMetrics.density
            layout.buildLayout(width.toFloat(), height.toFloat(), theme, density, bottomNavInsetPx)
            requestLayout()
            invalidate()
        }

    val layout = KeyboardLayout()
    var onKeyAction: ((KeyData) -> Unit)? = null
    var onTextCommit: ((String) -> Unit)? = null
    var onActionExpand: (() -> Unit)? = null
    var onActionSelection: (() -> Unit)? = null
    var onActionClipboard: (() -> Unit)? = null

    // Toolbar tool callbacks
    var onToolbarToolClick: ((ToolbarTool) -> Unit)? = null
    var onToolbarToolLongClick: ((ToolbarTool) -> Unit)? = null
    var onAnchorLongClick: (() -> Unit)? = null
    var onCommaPopupSelected: ((String) -> Unit)? = null

    private val toolbarPrefs = ToolbarPreferences(context)
    private val iconCache = mutableMapOf<Int, Drawable>()
    private var isLongPressTriggered = false

    // Horizontal scroll tracking for expanded toolbar
    private var isToolbarScrolling = false
    private var toolbarTouchStartX = 0f
    private var toolbarInitialScrollOffset = 0f
    private val touchSlop = android.view.ViewConfiguration.get(context).scaledTouchSlop.toFloat()

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
        typeface = Typeface.DEFAULT
    }
    private val actionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val enterTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.DEFAULT
    }
    private val toolbarTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    // Vector Icon Paints
    private val iconStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val iconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val enterIconStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val enterIconFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val pathHelper = Path()

    private var activePressedKey: KeyData? = null
    private var lastShiftPressTime = 0L
    private var isMultiPopupActive = false
    private var isCommaGridPopupActive = false
    private var bottomNavInsetPx = 0f

    // Comma popup item definitions (10 items across 2 rows)
    private val commaPopupItems = listOf(
        "Settings", "Emoji", "Clipboard", "Log Keeper", "Shortcuts",
        "Voice", "One Hand", "Floating", "Personal Vault", "Security Vault"
    )
    private val commaPopupIcons = listOf(
        R.drawable.ic_settings,
        R.drawable.ic_emoji_smileys,
        R.drawable.ic_clipboard,
        R.drawable.ic_log_keeper,
        R.drawable.ic_desktop_shortcuts,
        R.drawable.ic_mic,
        R.drawable.ic_one_hand,
        R.drawable.ic_floating_keyboard,
        R.drawable.ic_personal_vault,
        R.drawable.ic_security_vault
    )

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
            } else if (key.type == KeyType.ACTION_EXPAND) {
                isLongPressTriggered = true
                onAnchorLongClick?.invoke()
            } else if (key.type == KeyType.TOOLBAR_TOOL) {
                key.tool?.let { tool ->
                    isLongPressTriggered = true
                    onToolbarToolLongClick?.invoke(tool)
                }
            } else if (key.type == KeyType.COMMA) {
                isLongPressTriggered = true
                isCommaGridPopupActive = true
                popupWindow.showGridKeys(
                    anchor = this@VianKeyboardView,
                    key = key,
                    items = commaPopupItems,
                    iconResIds = commaPopupIcons,
                    cols = 5,
                    rows = 2,
                    theme = theme
                )
            } else if (key.moreKeys.isNotEmpty()) {
                isMultiPopupActive = true
                popupWindow.showMoreKeys(this@VianKeyboardView, key, key.moreKeys, theme)
            }
        }
    }

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        layout.pinnedTools = toolbarPrefs.getPinnedTools()
        layout.expandedTools = toolbarPrefs.getExpandedTools()
        layout.hidePinnedWhenExpanded = toolbarPrefs.hidePinnedWhenExpanded
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
        actionTextPaint.textSize = 14f * density

        enterTextPaint.color = theme.enterTextColor
        enterTextPaint.textSize = 18f * density

        hintPaint.color = theme.hintColor
        hintPaint.textSize = 10.5f * density

        toolbarTextPaint.color = theme.textColor
        toolbarTextPaint.textSize = 14.5f * density

        iconStrokePaint.color = theme.textColor
        iconStrokePaint.strokeWidth = 2.2f * density

        iconFillPaint.color = theme.textColor

        enterIconStrokePaint.color = theme.enterTextColor
        enterIconStrokePaint.strokeWidth = 2.4f * density

        enterIconFillPaint.color = theme.enterTextColor
    }

    fun reloadTheme() {
        theme = KeyboardTheme.loadFromPrefs(context)
        reloadToolbarConfiguration()
    }

    fun reloadToolbarConfiguration() {
        layout.pinnedTools = toolbarPrefs.getPinnedTools()
        layout.expandedTools = toolbarPrefs.getExpandedTools()
        layout.hidePinnedWhenExpanded = toolbarPrefs.hidePinnedWhenExpanded
        val density = resources.displayMetrics.density
        layout.buildLayout(width.toFloat(), height.toFloat(), theme, density, bottomNavInsetPx)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val density = resources.displayMetrics.density
        val rowCount = if (layout.mode == KeyboardMode.NUMPAD) 4 else 5
        val rowsTotalHeight = (theme.keyHeightDp * rowCount * density)
        val toolbarHeight = (theme.toolbarHeightDp * density)
        val gapsHeight = (theme.verticalGapDp * rowCount * density)
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
        if (layout.isToolbarExpanded && layout.toolbarScrollBounds.width() > 0) {
            // A. Draw Anchor Key first (fixed, non-scrolling)
            val anchorKey = layout.toolbarKeys.firstOrNull { it.type == KeyType.ACTION_EXPAND }
            if (anchorKey != null) {
                val bgPaint = if (anchorKey.isPressed) pressedKeyPaint else toolbarBackgroundPaint
                canvas.drawRoundRect(anchorKey.bounds, cornerRadius, cornerRadius, bgPaint)
                if (layout.isIncognitoActive) {
                    val badgeRadius = minOf(anchorKey.bounds.width(), anchorKey.bounds.height()) / 2f
                    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.parseColor("#334155")
                    }
                    canvas.drawCircle(anchorKey.bounds.centerX(), anchorKey.bounds.centerY(), badgeRadius * 0.85f, badgePaint)
                    drawVectorIcon(canvas, anchorKey.bounds, R.drawable.ic_incognito, 18f * density, Color.WHITE)
                } else {
                    val textY = anchorKey.bounds.centerY() - ((toolbarTextPaint.descent() + toolbarTextPaint.ascent()) / 2)
                    canvas.drawText(anchorKey.label, anchorKey.bounds.centerX(), textY, toolbarTextPaint)
                }
            }

            // B. Clip and translate scrollable tools tray
            canvas.save()
            canvas.clipRect(layout.toolbarScrollBounds)
            canvas.translate(-layout.toolbarScrollOffset, 0f)

            for (key in layout.toolbarKeys) {
                if (key.type == KeyType.ACTION_EXPAND) continue
                val bgPaint = if (key.isPressed) pressedKeyPaint else toolbarBackgroundPaint
                canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, bgPaint)

                if (key.type == KeyType.TOOLBAR_TOOL) {
                    key.tool?.let { tool ->
                        drawVectorIcon(canvas, key.bounds, tool.iconResId, 20f * density, theme.textColor)
                    }
                } else {
                    val textY = key.bounds.centerY() - ((toolbarTextPaint.descent() + toolbarTextPaint.ascent()) / 2)
                    canvas.drawText(key.label, key.bounds.centerX(), textY, toolbarTextPaint)
                }
            }
            canvas.restore()
        } else {
            for (key in layout.toolbarKeys) {
                val bgPaint = if (key.isPressed) pressedKeyPaint else toolbarBackgroundPaint
                canvas.drawRoundRect(key.bounds, cornerRadius, cornerRadius, bgPaint)

                if (key.type == KeyType.ACTION_EXPAND) {
                    if (layout.isIncognitoActive) {
                        // Draw Incognito Pill / Badge with sunglasses & hat icon
                        val badgeRadius = minOf(key.bounds.width(), key.bounds.height()) / 2f
                        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#334155")
                        }
                        canvas.drawCircle(key.bounds.centerX(), key.bounds.centerY(), badgeRadius * 0.85f, badgePaint)
                        drawVectorIcon(canvas, key.bounds, R.drawable.ic_incognito, 18f * density, Color.WHITE)
                    } else {
                        val textY = key.bounds.centerY() - ((toolbarTextPaint.descent() + toolbarTextPaint.ascent()) / 2)
                        canvas.drawText(key.label, key.bounds.centerX(), textY, toolbarTextPaint)
                    }
                } else if (key.type == KeyType.TOOLBAR_TOOL) {
                    key.tool?.let { tool ->
                        drawVectorIcon(canvas, key.bounds, tool.iconResId, 20f * density, theme.textColor)
                    }
                } else {
                    val textY = key.bounds.centerY() - ((toolbarTextPaint.descent() + toolbarTextPaint.ascent()) / 2)
                    canvas.drawText(key.label, key.bounds.centerX(), textY, toolbarTextPaint)
                }
            }
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

            // Determine corner radius: special functional keys are noticeably rounder (capsule/stadium curves) matching HeliBoard & screenshot
            val isSpecialKey = key.type == KeyType.SHIFT ||
                               key.type == KeyType.SYMBOLS_TOGGLE ||
                               key.type == KeyType.SYMBOLS_MORE_TOGGLE ||
                               key.type == KeyType.NUMPAD_TOGGLE ||
                               key.type == KeyType.COMMA ||
                               key.type == KeyType.PERIOD ||
                               key.type == KeyType.DELETE ||
                               key.type == KeyType.ENTER

            val currentRadius = if (isSpecialKey) {
                // Rounder stadium radius, up to half the key height/width
                (cornerRadius * 1.85f).coerceAtMost(minOf(key.bounds.width(), key.bounds.height()) / 2f)
            } else {
                cornerRadius
            }

            // Key background rect with customizable corner radius
            canvas.drawRoundRect(key.bounds, currentRadius, currentRadius, currentBgPaint)

            // Key border if set
            if (hasBorder) {
                canvas.drawRoundRect(key.bounds, currentRadius, currentRadius, borderPaint)
            }

            // Key label or custom vector icon
            when (key.type) {
                KeyType.DELETE -> {
                    drawBackspaceIcon(canvas, key.bounds, density)
                }
                KeyType.SHIFT -> {
                    drawShiftChevronIcon(canvas, key.bounds, density, layout.shiftState)
                }
                KeyType.ENTER -> {
                    drawEnterReturnIcon(canvas, key.bounds, density)
                }
                else -> {
                    val paintToUse = when {
                        key.type == KeyType.CHARACTER || key.type == KeyType.SPACE || key.type == KeyType.COMMA || key.type == KeyType.PERIOD -> textPaint
                        else -> actionTextPaint
                    }

                    if (key.label.contains("\n")) {
                        val lines = key.label.split("\n")
                        val totalH = (lines.size * 12f * density)
                        var lineY = key.bounds.centerY() - (totalH / 2) + (8f * density)
                        for (line in lines) {
                            canvas.drawText(line, key.bounds.centerX(), lineY, paintToUse)
                            lineY += (12f * density)
                        }
                    } else {
                        val textY = key.bounds.centerY() - ((paintToUse.descent() + paintToUse.ascent()) / 2)
                        canvas.drawText(key.label, key.bounds.centerX(), textY, paintToUse)
                    }
                }
            }

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
                isLongPressTriggered = false
                isToolbarScrolling = false
                toolbarTouchStartX = x
                toolbarInitialScrollOffset = layout.toolbarScrollOffset

                val key = layout.findKeyAt(x, y)
                if (key != null) {
                    activePressedKey = key
                    key.isPressed = true
                    isRepeatingBackspace = false
                    isMultiPopupActive = false
                    isCommaGridPopupActive = false

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
                if (isCommaGridPopupActive) {
                    popupWindow.updateSelection(event.rawX, event.rawY)
                } else if (isMultiPopupActive) {
                    popupWindow.updateSelection(event.rawX, event.rawY)
                } else {
                    // Check if dragging/scrolling on the expanded toolbar
                    if (layout.isToolbarExpanded && layout.toolbarScrollBounds.contains(toolbarTouchStartX, y) && layout.maxToolbarScrollOffset > 0f) {
                        val deltaX = x - toolbarTouchStartX
                        if (isToolbarScrolling || Math.abs(deltaX) > touchSlop) {
                            isToolbarScrolling = true
                            mainHandler.removeCallbacks(longPressRunnable)
                            activePressedKey?.isPressed = false
                            activePressedKey = null

                            val newOffset = (toolbarInitialScrollOffset - deltaX).coerceIn(0f, layout.maxToolbarScrollOffset)
                            if (newOffset != layout.toolbarScrollOffset) {
                                layout.toolbarScrollOffset = newOffset
                                invalidate()
                            }
                            return true
                        }
                    }

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

                if (isToolbarScrolling) {
                    isToolbarScrolling = false
                    activePressedKey?.isPressed = false
                    activePressedKey = null
                    invalidate()
                    return true
                }

                if (isCommaGridPopupActive) {
                    val selected = popupWindow.getSelectedItem()
                    if (!selected.isNullOrEmpty()) {
                        onCommaPopupSelected?.invoke(selected)
                    }
                } else if (isMultiPopupActive) {
                    val selected = popupWindow.getSelectedItem()
                    if (!selected.isNullOrEmpty()) {
                        onTextCommit?.invoke(selected)
                    }
                } else {
                    val key = activePressedKey
                    if (key != null && !isRepeatingBackspace && !isLongPressTriggered) {
                        handleKeySelection(key)
                    }
                }

                popupWindow.dismiss()
                activePressedKey?.isPressed = false
                activePressedKey = null
                isRepeatingBackspace = false
                isMultiPopupActive = false
                isCommaGridPopupActive = false
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
                isCommaGridPopupActive = false
                isToolbarScrolling = false
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

            KeyType.NUMPAD_TOGGLE -> {
                layout.mode = KeyboardMode.NUMPAD
                val density = resources.displayMetrics.density
                layout.buildLayout(width.toFloat(), height.toFloat(), theme, density, bottomNavInsetPx)
                invalidate()
            }

            KeyType.TOOLBAR_TOOL -> {
                key.tool?.let { onToolbarToolClick?.invoke(it) }
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

    // Vector Icon Renderers (Matching HeliBoard & Screenshot)

    /**
     * Backspace Tag Icon (⌫):
     * Pointed tag outline pointing to the left with a centered '✕'
     */
    private fun drawBackspaceIcon(canvas: Canvas, bounds: RectF, density: Float) {
        val iconW = 20f * density
        val iconH = 14f * density
        val cx = bounds.centerX()
        val cy = bounds.centerY()
        val left = cx - (iconW / 2)
        val top = cy - (iconH / 2)
        val right = cx + (iconW / 2)
        val bottom = cy + (iconH / 2)
        val arrowW = 6.5f * density

        pathHelper.reset()
        pathHelper.moveTo(left, cy)
        pathHelper.lineTo(left + arrowW, top)
        pathHelper.lineTo(right, top)
        pathHelper.lineTo(right, bottom)
        pathHelper.lineTo(left + arrowW, bottom)
        pathHelper.close()
        canvas.drawPath(pathHelper, iconStrokePaint)

        // Draw inner ✕
        val crossHalf = 3.2f * density
        val crossCx = cx + (arrowW * 0.28f)
        canvas.drawLine(crossCx - crossHalf, cy - crossHalf, crossCx + crossHalf, cy + crossHalf, iconStrokePaint)
        canvas.drawLine(crossCx + crossHalf, cy - crossHalf, crossCx - crossHalf, cy + crossHalf, iconStrokePaint)
    }

    /**
     * Shift Chevron Icon:
     * Clean upward chevron (^).
     * OFF: stroke outline matching text
     * ON: illuminated / thicker stroke or accent fill
     * CAPS_LOCK: illuminated chevron with horizontal lock underline
     */
    private fun drawShiftChevronIcon(canvas: Canvas, bounds: RectF, density: Float, state: ShiftState) {
        val chevronW = 14f * density
        val chevronH = 8.5f * density
        val cx = bounds.centerX()
        val cy = bounds.centerY() - (if (state == ShiftState.CAPS_LOCK) 2f * density else 0f)

        val left = cx - (chevronW / 2)
        val right = cx + (chevronW / 2)
        val top = cy - (chevronH / 2)
        val bottom = cy + (chevronH / 2)

        when (state) {
            ShiftState.OFF -> {
                pathHelper.reset()
                pathHelper.moveTo(left, bottom)
                pathHelper.lineTo(cx, top)
                pathHelper.lineTo(right, bottom)
                canvas.drawPath(pathHelper, iconStrokePaint)
            }
            ShiftState.ON -> {
                // Active single shift - illuminated bold chevron
                val activePaint = Paint(iconStrokePaint).apply {
                    color = theme.accentColor
                    strokeWidth = 3.2f * density
                }
                pathHelper.reset()
                pathHelper.moveTo(left, bottom)
                pathHelper.lineTo(cx, top)
                pathHelper.lineTo(right, bottom)
                canvas.drawPath(pathHelper, activePaint)
            }
            ShiftState.CAPS_LOCK -> {
                // Caps Lock - illuminated chevron with lock bar underneath
                val lockPaint = Paint(iconStrokePaint).apply {
                    color = theme.accentColor
                    strokeWidth = 3.0f * density
                }
                pathHelper.reset()
                pathHelper.moveTo(left, bottom)
                pathHelper.lineTo(cx, top)
                pathHelper.lineTo(right, bottom)
                canvas.drawPath(pathHelper, lockPaint)

                // Lock horizontal bar beneath chevron
                val barY = bottom + (5f * density)
                canvas.drawLine(cx - (5.5f * density), barY, cx + (5.5f * density), barY, lockPaint)
            }
        }
    }

    /**
     * Enter / Return Elbow Icon (↵):
     * White return path entering from right, going left, ending in arrow
     */
    private fun drawEnterReturnIcon(canvas: Canvas, bounds: RectF, density: Float) {
        val iconW = 18f * density
        val iconH = 13f * density
        val cx = bounds.centerX()
        val cy = bounds.centerY()

        val left = cx - (iconW / 2)
        val right = cx + (iconW / 2)
        val top = cy - (iconH / 2)
        val bottom = cy + (iconH / 2)
        val arrowSize = 4.5f * density

        pathHelper.reset()
        // Start top right, come down, turn left
        pathHelper.moveTo(right, top)
        pathHelper.lineTo(right, bottom)
        pathHelper.lineTo(left, bottom)
        canvas.drawPath(pathHelper, enterIconStrokePaint)

        // Draw left arrow head
        pathHelper.reset()
        pathHelper.moveTo(left, bottom)
        pathHelper.lineTo(left + arrowSize, bottom - arrowSize)
        pathHelper.moveTo(left, bottom)
        pathHelper.lineTo(left + arrowSize, bottom + arrowSize)
        canvas.drawPath(pathHelper, enterIconStrokePaint)
    }

    private fun drawVectorIcon(canvas: Canvas, bounds: RectF, resId: Int, sizePx: Float, tintColor: Int) {
        val drawable = iconCache.getOrPut(resId) {
            ContextCompat.getDrawable(context, resId) ?: return
        }
        val left = (bounds.centerX() - (sizePx / 2f)).toInt()
        val top = (bounds.centerY() - (sizePx / 2f)).toInt()
        val right = (left + sizePx).toInt()
        val bottom = (top + sizePx).toInt()
        drawable.setBounds(left, top, right, bottom)
        drawable.setTint(tintColor)
        drawable.draw(canvas)
    }
}
