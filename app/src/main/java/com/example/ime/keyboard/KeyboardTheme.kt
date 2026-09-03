package com.example.ime.keyboard

import android.content.Context
import android.content.SharedPreferences

data class KeyboardTheme(
    val backgroundColor: Int = 0xFFDDE3E6.toInt(),      // Grayish-white container canvas
    val keyBackgroundColor: Int = 0xFFFFFFFF.toInt(),   // Crisp white letter/number/space keycaps
    val actionKeyColor: Int = 0xFFC6CFD6.toInt(),       // Soft grey special keys (shift, ?123, comma, period, backspace)
    val enterKeyColor: Int = 0xFF4A6572.toInt(),        // Muted teal-slate enter key
    val accentColor: Int = 0xFF0284C7.toInt(),          // Sky 600
    val textColor: Int = 0xFF0F172A.toInt(),            // High contrast text
    val enterTextColor: Int = 0xFFFFFFFF.toInt(),       // White icon/text on Enter
    val hintColor: Int = 0xFF64748B.toInt(),            // Slate 500 hints
    val borderColor: Int = 0x00000000,
    val pressedKeyColor: Int = 0xFFCBD5E1.toInt(),      // Pressed state
    val popupBackgroundColor: Int = 0xFFCBD5E1.toInt(),  // Popup bubble
    val popupTextColor: Int = 0xFF0F172A.toInt(),
    
    // Sliders
    val keyHeightDp: Float = 46f,
    val toolbarHeightDp: Float = 40f,
    val keyCornerRadiusDp: Float = 8f,
    val borderWidthDp: Float = 0f,
    val horizontalGapDp: Float = 4f,
    val verticalGapDp: Float = 5f,
    val actionKeyGrayProgress: Int = 40,   // 0 to 100 for special key grey slider
    val enterKeyColorProgress: Int = 50,   // 0 to 100 for enter key color slider
    val showPopups: Boolean = true,
    val showHints: Boolean = true
) {
    companion object {
        const val PREFS_NAME = "vian_appearance_prefs"
        const val KEY_HEIGHT = "key_height"
        const val KEY_CORNER_RADIUS = "key_corner_radius"
        const val KEY_BORDER_WIDTH = "key_border_width"
        const val KEY_HORIZONTAL_GAP = "key_horizontal_gap"
        const val KEY_VERTICAL_GAP = "key_vertical_gap"
        const val KEY_ACTION_GRAY = "key_action_gray"
        const val KEY_ENTER_COLOR = "key_enter_color"
        const val KEY_SHOW_POPUPS = "key_show_popups"
        const val KEY_SHOW_HINTS = "key_show_hints"

        fun calculateActionKeyColor(grayProgress: Int): Int {
            // 0 = #F8FAFC (very light), 40 = #C6CFD6 (screenshot grey), 100 = #8E9CA8 (darker grey)
            val factor = grayProgress.coerceIn(0, 100) / 100f
            val startR = 248; val startG = 250; val startB = 252
            val endR = 142; val endG = 156; val endB = 168
            val r = (startR + (endR - startR) * factor).toInt()
            val g = (startG + (endG - startG) * factor).toInt()
            val b = (startB + (endB - startB) * factor).toInt()
            return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }

        fun calculateEnterKeyColor(progress: Int): Int {
            // 0 = #64748B (Slate), 50 = #4A6572 (Teal Slate), 100 = #0F172A (Deep Dark)
            val factor = progress.coerceIn(0, 100)
            return if (factor <= 50) {
                val f = factor / 50f
                val r = (100 + (74 - 100) * f).toInt()
                val g = (116 + (101 - 116) * f).toInt()
                val b = (139 + (114 - 139) * f).toInt()
                (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            } else {
                val f = (factor - 50) / 50f
                val r = (74 + (15 - 74) * f).toInt()
                val g = (101 + (23 - 101) * f).toInt()
                val b = (114 + (42 - 114) * f).toInt()
                (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        fun loadFromPrefs(context: Context): KeyboardTheme {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val keyHeight = prefs.getFloat(KEY_HEIGHT, 46f)
            val cornerRadius = prefs.getFloat(KEY_CORNER_RADIUS, 8f)
            val hGap = prefs.getFloat(KEY_HORIZONTAL_GAP, 4f)
            val vGap = prefs.getFloat(KEY_VERTICAL_GAP, 5f)
            val actionGray = prefs.getInt(KEY_ACTION_GRAY, 40)
            val enterColorProgress = prefs.getInt(KEY_ENTER_COLOR, 50)
            val showPopups = prefs.getBoolean(KEY_SHOW_POPUPS, true)
            val showHints = prefs.getBoolean(KEY_SHOW_HINTS, true)

            return KeyboardTheme(
                keyHeightDp = keyHeight,
                keyCornerRadiusDp = cornerRadius,
                horizontalGapDp = hGap,
                verticalGapDp = vGap,
                actionKeyGrayProgress = actionGray,
                enterKeyColorProgress = enterColorProgress,
                actionKeyColor = calculateActionKeyColor(actionGray),
                enterKeyColor = calculateEnterKeyColor(enterColorProgress),
                showPopups = showPopups,
                showHints = showHints
            )
        }

        fun saveToPrefs(context: Context, theme: KeyboardTheme) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putFloat(KEY_HEIGHT, theme.keyHeightDp)
                .putFloat(KEY_CORNER_RADIUS, theme.keyCornerRadiusDp)
                .putFloat(KEY_BORDER_WIDTH, theme.borderWidthDp)
                .putFloat(KEY_HORIZONTAL_GAP, theme.horizontalGapDp)
                .putFloat(KEY_VERTICAL_GAP, theme.verticalGapDp)
                .putInt(KEY_ACTION_GRAY, theme.actionKeyGrayProgress)
                .putInt(KEY_ENTER_COLOR, theme.enterKeyColorProgress)
                .putBoolean(KEY_SHOW_POPUPS, theme.showPopups)
                .putBoolean(KEY_SHOW_HINTS, theme.showHints)
                .apply()
        }
    }
}
