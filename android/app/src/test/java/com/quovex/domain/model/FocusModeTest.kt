package com.quovex.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusModeTest {

    @Test
    fun `pomodoro preset has 25 focus and 5 break`() {
        val mode = FocusMode.Pomodoro
        assertEquals(25, mode.focusDurationMinutes)
        assertEquals(5, mode.breakDurationMinutes)
        assertEquals("Pomodoro", mode.title)
    }

    @Test
    fun `deep work preset has 50 focus and 10 break`() {
        val mode = FocusMode.DeepWork
        assertEquals(50, mode.focusDurationMinutes)
        assertEquals(10, mode.breakDurationMinutes)
        assertEquals("Deep Work", mode.title)
    }

    @Test
    fun `long deep work preset has 90 focus and 20 break`() {
        val mode = FocusMode.LongDeepWork
        assertEquals(90, mode.focusDurationMinutes)
        assertEquals(20, mode.breakDurationMinutes)
        assertEquals("Long Deep Work", mode.title)
    }

    @Test
    fun `custom preset retains user defined values`() {
        val mode = FocusMode.Custom(customFocusMinutes = 45, customBreakMinutes = 15)
        assertEquals(45, mode.focusDurationMinutes)
        assertEquals(15, mode.breakDurationMinutes)
        assertEquals("Custom", mode.title)
    }
}
