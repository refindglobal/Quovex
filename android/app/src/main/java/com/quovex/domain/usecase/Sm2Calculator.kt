package com.quovex.domain.usecase

import kotlin.math.roundToInt

data class Sm2CalculationResult(
    val repetitions: Int,
    val intervalDays: Int,
    val easinessFactor: Float,
    val nextReviewAtMillis: Long
)

object Sm2Calculator {

    /**
     * Calculates the next spaced repetition schedule using the SM-2 algorithm.
     *
     * @param quality Rating of recall quality:
     *   0 = Again (<1m / Forgot)
     *   3 = Hard (Difficult recall)
     *   4 = Good (Correct recall with slight hesitation)
     *   5 = Easy (Instant, perfect recall)
     * @param currentRepetitions Number of consecutive successful recalls
     * @param currentIntervalDays Current interval in days
     * @param currentEf Current Easiness Factor (default 2.5f)
     * @param currentTimeMillis Current timestamp in milliseconds
     */
    fun calculate(
        quality: Int,
        currentRepetitions: Int,
        currentIntervalDays: Int,
        currentEf: Float = 2.5f,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Sm2CalculationResult {
        val q = quality.coerceIn(0, 5)

        val newRepetitions: Int
        val newIntervalDays: Int

        if (q < 3) {
            // Failed recall - restart repetitions
            newRepetitions = 0
            newIntervalDays = 1
        } else {
            // Successful recall
            newRepetitions = currentRepetitions + 1
            newIntervalDays = when (currentRepetitions) {
                0 -> 1
                1 -> 6
                else -> (currentIntervalDays * currentEf).roundToInt().coerceAtLeast(1)
            }
        }

        // Calculate new Easiness Factor (EF)
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        val efDelta = 0.1f - (5 - q) * (0.08f + (5 - q) * 0.02f)
        val newEf = (currentEf + efDelta).coerceAtLeast(1.3f)

        val oneDayMillis = 24L * 60L * 60L * 1000L
        val nextReviewAt = currentTimeMillis + (newIntervalDays * oneDayMillis)

        return Sm2CalculationResult(
            repetitions = newRepetitions,
            intervalDays = newIntervalDays,
            easinessFactor = newEf,
            nextReviewAtMillis = nextReviewAt
        )
    }
}
