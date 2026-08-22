package com.quovex.domain.usecase

import com.quovex.domain.model.StudySession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Sm2CalculatorTest {

    @Test
    fun `calculate quality 0 resets repetitions and interval to 1`() {
        val result = Sm2Calculator.calculate(
            quality = 0,
            currentRepetitions = 3,
            currentIntervalDays = 10,
            currentEf = 2.5f
        )

        assertEquals(0, result.repetitions)
        assertEquals(1, result.intervalDays)
    }

    @Test
    fun `calculate quality 5 first review gives 1 day interval and 1 rep`() {
        val result = Sm2Calculator.calculate(
            quality = 5,
            currentRepetitions = 0,
            currentIntervalDays = 0,
            currentEf = 2.5f
        )

        assertEquals(1, result.repetitions)
        assertEquals(1, result.intervalDays)
        assertTrue(result.easinessFactor >= 2.5f)
    }

    @Test
    fun `calculate quality 4 second review gives 6 days interval`() {
        val result = Sm2Calculator.calculate(
            quality = 4,
            currentRepetitions = 1,
            currentIntervalDays = 1,
            currentEf = 2.5f
        )

        assertEquals(2, result.repetitions)
        assertEquals(6, result.intervalDays)
    }

    @Test
    fun `easiness factor never drops below 1_3`() {
        var ef = 1.3f
        for (i in 1..5) {
            val result = Sm2Calculator.calculate(
                quality = 0,
                currentRepetitions = 0,
                currentIntervalDays = 1,
                currentEf = ef
            )
            ef = result.easinessFactor
        }

        assertTrue(ef >= 1.3f)
    }

    @Test
    fun `quality 3 increments repetitions`() {
        val result = Sm2Calculator.calculate(
            quality = 3,
            currentRepetitions = 0,
            currentIntervalDays = 0,
            currentEf = 2.5f
        )
        assertEquals(1, result.repetitions)
        assertEquals(1, result.intervalDays)
    }

    @Test
    fun `quality 5 third review scales interval using EF`() {
        // After 2 reps with interval=6, next interval = round(6 * ef)
        val ef = 2.5f
        val result = Sm2Calculator.calculate(
            quality = 5,
            currentRepetitions = 2,
            currentIntervalDays = 6,
            currentEf = ef
        )
        val expected = (6 * ef).toInt()
        assertEquals(expected, result.intervalDays)
        assertEquals(3, result.repetitions)
    }

    @Test
    fun `nextReviewDate is strictly after current time`() {
        val now = 1000000L
        val result = Sm2Calculator.calculate(
            quality = 4,
            currentRepetitions = 1,
            currentIntervalDays = 1,
            currentEf = 2.5f,
            currentTimeMillis = now
        )
        assertTrue(result.nextReviewAtMillis > now)
    }

    // ── StudySession model tests ───────────────────────────────────────────────

    @Test
    fun `StudySession recordReview increments correct counter`() {
        val session = StudySession(totalCards = 5)
        val after = session.recordReview(4) // Good

        assertEquals(1, after.reviewedCount)
        assertEquals(1, after.goodCount)
        assertEquals(0, after.againCount)
        assertEquals(0, after.hardCount)
        assertEquals(0, after.easyCount)
    }

    @Test
    fun `StudySession accuracyPercent is correct`() {
        val session = StudySession(totalCards = 4)
        val after = session
            .recordReview(4)  // good
            .recordReview(5)  // easy
            .recordReview(0)  // again
            .recordReview(3)  // hard

        // 2 good/easy out of 4 = 50%
        assertEquals(50, after.accuracyPercent)
    }

    @Test
    fun `StudySession accuracyPercent is 0 when nothing reviewed`() {
        assertEquals(0, StudySession().accuracyPercent)
    }
}
