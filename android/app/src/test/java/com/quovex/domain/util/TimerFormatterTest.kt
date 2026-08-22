package com.quovex.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimerFormatterTest {

    @Test
    fun `formats time under 1 hour as MM_SS`() {
        assertEquals("25:00", TimerFormatter.formatRemainingTime(25 * 60))
        assertEquals("04:15", TimerFormatter.formatRemainingTime(4 * 60 + 15))
        assertEquals("00:59", TimerFormatter.formatRemainingTime(59))
        assertEquals("00:00", TimerFormatter.formatRemainingTime(0))
    }

    @Test
    fun `formats time 1 hour and above as HH_MM_SS`() {
        assertEquals("01:00:00", TimerFormatter.formatRemainingTime(60 * 60))
        assertEquals("01:30:25", TimerFormatter.formatRemainingTime(90 * 60 + 25))
        assertEquals("02:15:00", TimerFormatter.formatRemainingTime(135 * 60))
    }

    @Test
    fun `clamps negative seconds to 00_00`() {
        assertEquals("00:00", TimerFormatter.formatRemainingTime(-1))
        assertEquals("00:00", TimerFormatter.formatRemainingTime(-500))
    }

    @Test
    fun `formats duration minutes correctly`() {
        assertEquals("25m", TimerFormatter.formatDurationMinutes(25))
        assertEquals("1h", TimerFormatter.formatDurationMinutes(60))
        assertEquals("1h 30m", TimerFormatter.formatDurationMinutes(90))
        assertEquals("0m", TimerFormatter.formatDurationMinutes(0))
    }
}
