package com.quovex.domain.util

/**
 * Pure Kotlin mathematical engine for focus timer state calculations.
 *
 * All time calculations are derived from absolute epoch timestamps (startTimeMillis and endTimeMillis)
 * rather than counting ticks, preventing timer drift across backgrounding, doze mode, and process recreation.
 */
object FocusTimerEngine {

    /**
     * Calculates remaining seconds from the scheduled end time and current epoch timestamp.
     * Always clamped to >= 0.
     */
    fun calculateRemainingSeconds(endTimeMillis: Long, currentTimeMillis: Long): Int {
        val diffMillis = endTimeMillis - currentTimeMillis
        return if (diffMillis > 0) {
            // Ceiling division so fractional seconds round up into the remaining second count
            ((diffMillis + 999) / 1000).toInt()
        } else {
            0
        }
    }

    /**
     * Calculates elapsed minutes from the session start time to the current/finish time.
     * Minimum 1 minute if session lasted at least 30 seconds; 0 if cancelled immediately.
     */
    fun calculateElapsedMinutes(startTimeMillis: Long, endTimeMillis: Long): Int {
        val elapsedMillis = (endTimeMillis - startTimeMillis).coerceAtLeast(0)
        val elapsedSeconds = elapsedMillis / 1000
        return when {
            elapsedSeconds < 30 -> 0
            else -> ((elapsedSeconds + 30) / 60).toInt().coerceAtLeast(1)
        }
    }

    /**
     * Calculates normalized progress (0.0 to 1.0) of time remaining.
     */
    fun calculateProgress(remainingSeconds: Int, totalSeconds: Int): Float {
        return if (totalSeconds > 0) {
            (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
    }

    /**
     * Returns true when the current time has reached or exceeded the end time.
     */
    fun isTimerFinished(endTimeMillis: Long, currentTimeMillis: Long): Boolean {
        return currentTimeMillis >= endTimeMillis
    }
}
