package com.quovex.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Quovex Tactile Haptic Micro-Feedback Engine
 * Provides subtle, sensory haptic pulses for key academic & focus interactions:
 * - Light tick (timer dial, chip tap)
 * - Medium snap (card flip, mode change)
 * - Success double pulse (session complete, quiz correct)
 * - Warning pulse (distraction detected, error)
 */
class HapticHelper(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /** Subtle 10ms tick for rotary dials, sliders, and chips */
    fun tick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }

    /** Crisper 25ms snap for card flips, tab transitions, and CTA clicks */
    fun snap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(25)
        }
    }

    /** Double pulse for session complete, streak preservation, or quiz victory */
    fun victory() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 40, 60, 80)
            val amplitudes = intArrayOf(0, 180, 0, 255)
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 40, 60, 80), -1)
        }
    }

    /** Distinct triple micro-vibe for distraction / warning triggers */
    fun warning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 40, 30, 40, 50)
            val amplitudes = intArrayOf(0, 150, 0, 150, 0, 200)
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 30, 40, 30, 40, 50), -1)
        }
    }
}

@Composable
fun rememberHapticHelper(): HapticHelper {
    val context = LocalContext.current
    return remember(context) { HapticHelper(context) }
}
