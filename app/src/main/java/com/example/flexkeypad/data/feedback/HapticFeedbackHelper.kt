package com.example.flexkeypad.data.feedback

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticFeedbackHelper(context: Context) {

    private val vibratorManager: VibratorManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
    } else null

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    // Hardware/Physical feedback attributes bypass the system "TOUCH=OFF" user setting
    private val hardwareVibrationAttributes: VibrationAttributes? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            VibrationAttributes.Builder()
                .setUsage(VibrationAttributes.USAGE_HARDWARE_FEEDBACK)
                .build()
        } else null

    private val gameAudioAttributes: AudioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_GAME)
        .build()

    private fun playVibration(effect: VibrationEffect, fallbackDurationMs: Long) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                hardwareVibrationAttributes?.let { attrs ->
                    vib.vibrate(effect, attrs)
                } ?: run {
                    vib.vibrate(effect, gameAudioAttributes)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = vibratorManager
                val attrs = hardwareVibrationAttributes
                if (vm != null && attrs != null) {
                    vm.vibrate(CombinedVibration.createParallel(effect), attrs)
                } else {
                    vib.vibrate(effect, gameAudioAttributes)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(effect, gameAudioAttributes)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(fallbackDurationMs)
            }
        } catch (_: Exception) {
            // Silently ignore if vibration error
        }
    }

    /**
     * Triggers a subtle tactile mechanical switch click feeling.
     */
    fun performKeyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            playVibration(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), 15L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playVibration(VibrationEffect.createOneShot(15L, VibrationEffect.DEFAULT_AMPLITUDE), 15L)
        } else {
            vibrator?.let {
                if (it.hasVibrator()) {
                    @Suppress("DEPRECATION")
                    it.vibrate(15L)
                }
            }
        }
    }

    /**
     * Triggers a light tick for slider / resize handle adjustments.
     */
    fun performTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            playVibration(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK), 8L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playVibration(VibrationEffect.createOneShot(8L, VibrationEffect.DEFAULT_AMPLITUDE), 8L)
        } else {
            vibrator?.let {
                if (it.hasVibrator()) {
                    @Suppress("DEPRECATION")
                    it.vibrate(8L)
                }
            }
        }
    }

    /**
     * Triggers a distinct tactile confirmation for successful connection.
     */
    fun performSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            playVibration(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK), 35L)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playVibration(VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 40), -1), 35L)
        } else {
            vibrator?.let {
                if (it.hasVibrator()) {
                    @Suppress("DEPRECATION")
                    it.vibrate(longArrayOf(0, 30, 60, 40), -1)
                }
            }
        }
    }
}
