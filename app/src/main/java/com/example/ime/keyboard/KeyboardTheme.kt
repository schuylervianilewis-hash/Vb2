package com.example.ime.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color

data class KeyboardTheme(
    val backgroundColor: Int = 0xFF0F172A.toInt(),      // Deep Slate 900
    val keyBackgroundColor: Int = 0xFF1E293B.toInt(),   // Slate 800
    val actionKeyColor: Int = 0xFF334155.toInt(),       // Slate 700
    val accentColor: Int = 0xFF0284C7.toInt(),          // Sky 600
    val textColor: Int = 0xFFF8FAFC.toInt(),            // Slate 50
    val hintColor: Int = 0xFF94A3B8.toInt(),            // Slate 400
    val borderColor: Int = 0xFF334155.toInt(),          // Slate 700
    val pressedKeyColor: Int = 0xFF0284C7.toInt(),      // Highlight Sky 600
    val popupBackgroundColor: Int = 0xFF1E293B.toInt(),
    val popupTextColor: Int = 0xFFFFFFFF.toInt(),
    
    // Metrics in DP
    val keyHeightDp: Float = 52f,
    val keyCornerRadiusDp: Float = 10f,
    val borderWidthDp: Float = 1.0f,
    val horizontalGapDp: Float = 4f,
    val verticalGapDp: Float = 6f,
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
                keyHeightDp = prefs.getFloat(KEY_HEIGHT, 52f),
                keyCornerRadiusDp = prefs.getFloat(KEY_CORNER_RADIUS, 10f),
                borderWidthDp = prefs.getFloat(KEY_BORDER_WIDTH, 1.0f),
                horizontalGapDp = prefs.getFloat(KEY_HORIZONTAL_GAP, 4f),
                verticalGapDp = prefs.getFloat(KEY_VERTICAL_GAP, 6f),
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
