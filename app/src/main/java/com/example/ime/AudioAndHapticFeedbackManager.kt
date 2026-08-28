package com.example.ime

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import com.example.settings.GeneralSettings

/**
 * AudioAndHapticFeedbackManager: Manages keypress click sounds and customized vibration feedback.
 * Aligned with HeliBoard AudioAndHapticFeedbackManager.
 */
class AudioAndHapticFeedbackManager(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun performKeyPressFeedback(
        view: View?,
        settings: GeneralSettings,
        isSpecialKey: Boolean = false
    ) {
        // Haptic feedback
        if (settings.vibrateOnKeyPress) {
            if (settings.vibrationStrengthMs > 0 && vibrator?.hasVibrator() == true) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val amplitude = if (isSpecialKey) 200 else 120
                        vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                settings.vibrationStrengthMs.toLong().coerceIn(1L, 100L),
                                amplitude
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(settings.vibrationStrengthMs.toLong().coerceIn(1L, 100L))
                    }
                } catch (_: Exception) {
                    performFallbackHaptic(view)
                }
            } else {
                performFallbackHaptic(view)
            }
        }

        // Audio feedback
        if (settings.soundOnKeyPress && audioManager != null) {
            try {
                val soundEffect = if (isSpecialKey) {
                    AudioManager.FX_KEYPRESS_DELETE
                } else {
                    AudioManager.FX_KEYPRESS_STANDARD
                }
                val volume = (settings.soundVolume / 100f).coerceIn(0f, 1f)
                audioManager.playSoundEffect(soundEffect, volume)
            } catch (_: Exception) {}
        }
    }

    private fun performFallbackHaptic(view: View?) {
        try {
            view?.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } catch (_: Exception) {}
    }

    fun performConfirmHaptic(view: View?) {
        try {
            view?.performHapticFeedback(
                HapticFeedbackConstants.CONFIRM,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        } catch (_: Exception) {}
    }
}
