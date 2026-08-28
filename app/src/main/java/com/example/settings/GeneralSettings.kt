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
    val backspaceSwipeDelete: Boolean = true
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
            backspaceSwipeDelete = prefs.getBoolean(KEY_SWIPE_DELETE, true)
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
    }
}
