package com.quovex.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusTimerEngineTest {

    @Test
    fun `calculates remaining seconds accurately with ceiling rounding`() {
        val now = 1000000L
        val endTime = now + 150000L // 150 seconds

        val remaining = FocusTimerEngine.calculateRemainingSeconds(endTime, now)
        assertEquals(150, remaining)
    }

    @Test
    fun `fractional millisecond rounds up into remaining second count`() {
        val now = 1000000L
        val endTime = now + 1001L // 1.001 seconds -> 2 remaining seconds

        val remaining = FocusTimerEngine.calculateRemainingSeconds(endTime, now)
        assertEquals(2, remaining)
    }

    @Test
    fun `returns zero when current time equals or exceeds end time`() {
        val now = 1000000L
        assertEquals(0, FocusTimerEngine.calculateRemainingSeconds(now, now))
        assertEquals(0, FocusTimerEngine.calculateRemainingSeconds(now - 5000L, now))
    }

    @Test
    fun `calculates elapsed minutes correctly`() {
        val start = 1000000L
        val end = start + (25 * 60 * 1000L) // 25 mins

        assertEquals(25, FocusTimerEngine.calculateElapsedMinutes(start, end))
    }

    @Test
    fun `calculates zero elapsed minutes for immediate cancellation`() {
        val start = 1000000L
        val end = start + 10000L // 10 seconds

        assertEquals(0, FocusTimerEngine.calculateElapsedMinutes(start, end))
    }

    @Test
    fun `calculates progress accurately`() {
        assertEquals(0.5f, FocusTimerEngine.calculateProgress(1500, 3000), 0.001f)
        assertEquals(1.0f, FocusTimerEngine.calculateProgress(3000, 3000), 0.001f)
        assertEquals(0.0f, FocusTimerEngine.calculateProgress(0, 3000), 0.001f)
    }

    @Test
    fun `isTimerFinished accurately detects expiration`() {
        val now = 1000000L
        assertFalse(FocusTimerEngine.isTimerFinished(now + 1000L, now))
        assertTrue(FocusTimerEngine.isTimerFinished(now, now))
        assertTrue(FocusTimerEngine.isTimerFinished(now - 1000L, now))
    }
}
