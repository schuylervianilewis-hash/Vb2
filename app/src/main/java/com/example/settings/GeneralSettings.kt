package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.foundation.common.Constants

/**
 * Encapsulates the General Settings preferences (Phase 1).
 */
data class GeneralSettings(
    val vibrateOnKeyPress: Boolean = true,
    val vibrationStrengthMs: Int = 15,
    val soundOnKeyPress: Boolean = false,
    val soundVolume: Int = 50,
    val autoCapitalization: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val spacebarCursorGlide: Boolean = true,
    val backspaceSwipeDelete: Boolean = true,
    val smartMultiplyMorph: Boolean = true,
    val autoSpaceAfterPunctuation: Boolean = true,
    val heightScale: Float = 1.0f,
    val bottomInsetPaddingDp: Int = 0,
    val showNumberRow: Boolean = true,
    val keyCornerRadiusDp: Float = 6f,
    val keyHorizontalGapDp: Float = 2.5f,
    val keyVerticalGapDp: Float = 3.5f,
    val keyBorderWidthDp: Float = 0.75f,
    val keyOutlineEnabled: Boolean = true
)

/**
 * Manager for General Settings persistence.
 */
class GeneralSettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_GENERAL, Context.MODE_PRIVATE)

    fun load(): GeneralSettings {
        return GeneralSettings(
            vibrateOnKeyPress = prefs.getBoolean(KEY_VIBRATE, true),
            vibrationStrengthMs = prefs.getInt(KEY_VIBRATION_STRENGTH, 15),
            soundOnKeyPress = prefs.getBoolean(KEY_SOUND, false),
            soundVolume = prefs.getInt(KEY_SOUND_VOLUME, 50),
            autoCapitalization = prefs.getBoolean(KEY_AUTO_CAP, true),
            doubleSpacePeriod = prefs.getBoolean(KEY_DOUBLE_SPACE_PERIOD, true),
            spacebarCursorGlide = prefs.getBoolean(KEY_SPACEBAR_GLIDE, true),
            backspaceSwipeDelete = prefs.getBoolean(KEY_SWIPE_DELETE, true),
            smartMultiplyMorph = prefs.getBoolean(KEY_SMART_MULTIPLY, true),
            autoSpaceAfterPunctuation = prefs.getBoolean(KEY_AUTO_SPACE_PUNCT, true),
            heightScale = prefs.getFloat(KEY_HEIGHT_SCALE, 1.0f),
            bottomInsetPaddingDp = prefs.getInt(KEY_BOTTOM_INSET, 0),
            showNumberRow = prefs.getBoolean(KEY_SHOW_NUMBER_ROW, true),
            keyCornerRadiusDp = prefs.getFloat(KEY_KEY_CORNER_RADIUS, 6f),
            keyHorizontalGapDp = prefs.getFloat(KEY_KEY_HORIZONTAL_GAP, 2.5f),
            keyVerticalGapDp = prefs.getFloat(KEY_KEY_VERTICAL_GAP, 3.5f),
            keyBorderWidthDp = prefs.getFloat(KEY_KEY_BORDER_WIDTH, 0.75f),
            keyOutlineEnabled = prefs.getBoolean(KEY_KEY_OUTLINE_ENABLED, true)
        )
    }

    fun save(settings: GeneralSettings) {
        prefs.edit()
            .putBoolean(KEY_VIBRATE, settings.vibrateOnKeyPress)
            .putInt(KEY_VIBRATION_STRENGTH, settings.vibrationStrengthMs)
            .putBoolean(KEY_SOUND, settings.soundOnKeyPress)
            .putInt(KEY_SOUND_VOLUME, settings.soundVolume)
            .putBoolean(KEY_AUTO_CAP, settings.autoCapitalization)
            .putBoolean(KEY_DOUBLE_SPACE_PERIOD, settings.doubleSpacePeriod)
            .putBoolean(KEY_SPACEBAR_GLIDE, settings.spacebarCursorGlide)
            .putBoolean(KEY_SWIPE_DELETE, settings.backspaceSwipeDelete)
            .putBoolean(KEY_SMART_MULTIPLY, settings.smartMultiplyMorph)
            .putBoolean(KEY_AUTO_SPACE_PUNCT, settings.autoSpaceAfterPunctuation)
            .putFloat(KEY_HEIGHT_SCALE, settings.heightScale)
            .putInt(KEY_BOTTOM_INSET, settings.bottomInsetPaddingDp)
            .putBoolean(KEY_SHOW_NUMBER_ROW, settings.showNumberRow)
            .putFloat(KEY_KEY_CORNER_RADIUS, settings.keyCornerRadiusDp)
            .putFloat(KEY_KEY_HORIZONTAL_GAP, settings.keyHorizontalGapDp)
            .putFloat(KEY_KEY_VERTICAL_GAP, settings.keyVerticalGapDp)
            .putFloat(KEY_KEY_BORDER_WIDTH, settings.keyBorderWidthDp)
            .putBoolean(KEY_KEY_OUTLINE_ENABLED, settings.keyOutlineEnabled)
            .apply()
    }

    companion object {
        private const val PREFS_GENERAL = "prefs_general"
        private const val KEY_VIBRATE = "pref_vibrate_on_keypress"
        private const val KEY_VIBRATION_STRENGTH = "pref_vibration_strength"
        private const val KEY_SOUND = "pref_sound_on_keypress"
        private const val KEY_SOUND_VOLUME = "pref_sound_volume"
        private const val KEY_AUTO_CAP = "pref_auto_cap"
        private const val KEY_DOUBLE_SPACE_PERIOD = "pref_double_space_period"
        private const val KEY_SPACEBAR_GLIDE = "pref_spacebar_glide"
        private const val KEY_SWIPE_DELETE = "pref_swipe_delete"
        private const val KEY_SMART_MULTIPLY = "pref_smart_multiply"
        private const val KEY_AUTO_SPACE_PUNCT = "pref_auto_space_punct"
        private const val KEY_HEIGHT_SCALE = "pref_height_scale"
        private const val KEY_BOTTOM_INSET = "pref_bottom_inset"
        private const val KEY_SHOW_NUMBER_ROW = "pref_show_number_row"
        private const val KEY_KEY_CORNER_RADIUS = "pref_key_corner_radius"
        private const val KEY_KEY_HORIZONTAL_GAP = "pref_key_horizontal_gap"
        private const val KEY_KEY_VERTICAL_GAP = "pref_key_vertical_gap"
        private const val KEY_KEY_BORDER_WIDTH = "pref_key_border_width"
        private const val KEY_KEY_OUTLINE_ENABLED = "pref_key_outline_enabled"
    }
}
