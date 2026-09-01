package com.example.ime.keyboard

import android.content.Context
import android.content.SharedPreferences

data class KeyboardTheme(
    val backgroundColor: Int = 0xFFF1F5F9.toInt(),      // Light Slate Canvas
    val keyBackgroundColor: Int = 0xFFFFFFFF.toInt(),   // Pure White Keycaps
    val actionKeyColor: Int = 0xFFE2E8F0.toInt(),       // Soft Slate Action Keys
    val enterKeyColor: Int = 0xFF64748B.toInt(),        // Slate 500 for Pill Enter
    val accentColor: Int = 0xFF0284C7.toInt(),          // Sky 600
    val textColor: Int = 0xFF0F172A.toInt(),            // Slate 900 High Contrast
    val enterTextColor: Int = 0xFFFFFFFF.toInt(),       // White text on Enter
    val hintColor: Int = 0xFF64748B.toInt(),            // Slate 500 Hint Labels
    val borderColor: Int = 0x00000000,                  // Borderless clean M3
    val pressedKeyColor: Int = 0xFFCBD5E1.toInt(),      // Highlighted Keycap
    val popupBackgroundColor: Int = 0xFFCBD5E1.toInt(),  // Elevated Bubble (Matching Screenshot)
    val popupTextColor: Int = 0xFF0F172A.toInt(),
    
    // Metrics in DP
    val keyHeightDp: Float = 46f,
    val toolbarHeightDp: Float = 40f,
    val keyCornerRadiusDp: Float = 8f,
    val borderWidthDp: Float = 0f,
    val horizontalGapDp: Float = 4f,
    val verticalGapDp: Float = 5f,
    val showPopups: Boolean = true,
    val showHints: Boolean = true
) {
    companion object {
        private const val PREFS_NAME = "vian_appearance_prefs"
        const val KEY_HEIGHT = "key_height"
        const val KEY_CORNER_RADIUS = "key_corner_radius"
        const val KEY_BORDER_WIDTH = "key_border_width"
        const val KEY_HORIZONTAL_GAP = "key_horizontal_gap"
        const val KEY_VERTICAL_GAP = "key_vertical_gap"
        const val KEY_SHOW_POPUPS = "key_show_popups"
        const val KEY_SHOW_HINTS = "key_show_hints"

        fun loadFromPrefs(context: Context): KeyboardTheme {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return KeyboardTheme(
                keyHeightDp = prefs.getFloat(KEY_HEIGHT, 46f),
                keyCornerRadiusDp = prefs.getFloat(KEY_CORNER_RADIUS, 8f),
                borderWidthDp = prefs.getFloat(KEY_BORDER_WIDTH, 0f),
                horizontalGapDp = prefs.getFloat(KEY_HORIZONTAL_GAP, 4f),
                verticalGapDp = prefs.getFloat(KEY_VERTICAL_GAP, 5f),
                showPopups = prefs.getBoolean(KEY_SHOW_POPUPS, true),
                showHints = prefs.getBoolean(KEY_SHOW_HINTS, true)
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
                .putBoolean(KEY_SHOW_POPUPS, theme.showPopups)
                .putBoolean(KEY_SHOW_HINTS, theme.showHints)
                .apply()
        }
    }
}
