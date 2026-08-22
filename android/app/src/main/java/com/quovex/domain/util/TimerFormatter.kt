package com.quovex.domain.util

/**
 * Pure Kotlin formatter for focus timer displays and notifications.
 *
 * Rules:
 * - Negative inputs are clamped to 0.
 * - For durations under 1 hour (0 .. 3599 seconds): format as MM:SS (e.g. "25:00", "04:15", "00:00").
 * - For durations 1 hour or greater (>= 3600 seconds): format as HH:MM:SS (e.g. "01:30:00").
 */
object TimerFormatter {

    fun formatRemainingTime(seconds: Int): String {
        val safeSeconds = seconds.coerceAtLeast(0)
        val hours = safeSeconds / 3600
        val minutes = (safeSeconds % 3600) / 60
        val remainingSecs = safeSeconds % 60

        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, remainingSecs)
        } else {
            "%02d:%02d".format(minutes, remainingSecs)
        }
    }

    fun formatDurationMinutes(minutes: Int): String {
        val safeMinutes = minutes.coerceAtLeast(0)
        val hours = safeMinutes / 60
        val remainingMins = safeMinutes % 60

        return when {
            hours > 0 && remainingMins > 0 -> "${hours}h ${remainingMins}m"
            hours > 0 -> "${hours}h"
            else -> "${remainingMins}m"
        }
    }
}
