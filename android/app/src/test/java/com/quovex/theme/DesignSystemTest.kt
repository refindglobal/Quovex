package com.quovex.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignSystemTest {

    @Test
    fun testDarkColorsTokens() {
        val colors = DarkQuovexColors
        assertNotNull(colors.primary)
        assertNotNull(colors.background)
        assertNotNull(colors.surface)
        assertNotNull(colors.textPrimary)
        assertNotNull(colors.error)
        assertEquals(PrimaryEmerald, colors.primary)
    }

    @Test
    fun testLightColorsTokens() {
        val colors = LightQuovexColors
        assertNotNull(colors.primary)
        assertNotNull(colors.background)
        assertNotNull(colors.surface)
        assertNotNull(colors.textPrimary)
        assertNotNull(colors.error)
        assertEquals(PrimaryEmeraldDark, colors.primary)
    }

    @Test
    fun testSpacingConstantsScale() {
        val spacing = QuovexSpacing()
        assertTrue(spacing.xxs.value < spacing.xs.value)
        assertTrue(spacing.xs.value < spacing.sm.value)
        assertTrue(spacing.sm.value < spacing.md.value)
        assertTrue(spacing.md.value < spacing.base.value)
        assertTrue(spacing.base.value < spacing.lg.value)
        assertTrue(spacing.lg.value < spacing.xl.value)
        assertTrue(spacing.xl.value < spacing.xxl.value)
        assertTrue(spacing.xxl.value < spacing.xxxl.value)
    }

    @Test
    fun testTouchTargetMinimum() {
        assertEquals(48f, QuovexTouchTarget.minimum.value, 0.01f)
    }
}
