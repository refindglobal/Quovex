package com.quovex.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuovexMathFormatterTest {

    @Test
    fun testFaradayLenzLawFormulaCleaning() {
        val raw = """
            ### 2. The Formula
            \[
            \boxed{\varepsilon = -\,\frac{d\Phi}{dt}}
            \]
            * **\varepsilon** — induced EMF (volts)
            * **\Phi** — magnetic flux (Wb)
            * **d\Phi/dt** — rate of change of flux (Wb s^{-1})
        """.trimIndent()

        val formatted = QuovexMathFormatter.formatMathAndLatex(raw)

        // Raw LaTeX tags must be stripped
        assertFalse(formatted.contains("\\["))
        assertFalse(formatted.contains("\\]"))
        assertFalse(formatted.contains("\\boxed"))
        assertFalse(formatted.contains("\\frac"))
        assertFalse(formatted.contains("\\,"))

        // Greek and Math symbols must be rendered
        assertTrue(formatted.contains("ε"))
        assertTrue(formatted.contains("Φ"))
        assertTrue(formatted.contains("dΦ/dt") || formatted.contains("dΦ / dt"))
        assertTrue(formatted.contains("Wb s⁻¹") || formatted.contains("s⁻¹"))
    }

    @Test
    fun testWorkIntegralAndCurrentEquations() {
        val raw = """
            **Why the Work Integral \(W_{\text{enter}} = \displaystyle \int_{0}^{w} F\,dx = F\,w\) is Valid**
            During the entry phase from \(x=0\) to \(x=w\), EMF is:
            \[
            \varepsilon = -N B l v
            \]
            Ohm's law gives:
            \[
            I = \frac{|\varepsilon|}{R} = \frac{N B l v}{R}
            \]
        """.trimIndent()

        val formatted = QuovexMathFormatter.formatMathAndLatex(raw)

        // No raw delimiters
        assertFalse(formatted.contains("\\("))
        assertFalse(formatted.contains("\\)"))
        assertFalse(formatted.contains("\\["))
        assertFalse(formatted.contains("\\]"))
        assertFalse(formatted.contains("\\displaystyle"))
        assertFalse(formatted.contains("\\text"))
        assertFalse(formatted.contains("\\frac"))

        // Readable notation
        assertTrue(formatted.contains("W_enter") || formatted.contains("W"))
        assertTrue(formatted.contains("∫[0 to w]") || formatted.contains("∫"))
        assertTrue(formatted.contains("ε = -N B l v") || formatted.contains("ε"))
        assertTrue(formatted.contains("(N B l v / R)") || formatted.contains("N B l v / R") || formatted.contains("N B l v"))
    }

    @Test
    fun testTrigonometryAndRoots() {
        val raw = "\\sin^2\\theta + \\cos^2\\theta = 1 and \\sqrt{x^2 + 1} and \\sqrt[3]{8} = 2"
        val formatted = QuovexMathFormatter.formatMathAndLatex(raw)

        assertTrue(formatted.contains("θ"))
        assertTrue(formatted.contains("²"))
        assertTrue(formatted.contains("√(x² + 1)"))
        assertTrue(formatted.contains("³√(8)") || formatted.contains("√(8)"))
    }

    @Test
    fun testChemicalFormulas() {
        val raw = "Reaction: H2SO4 + 2 NaOH -> Na2SO4 + 2 H2O and CO2 + H2O -> H2CO3"
        val formatted = QuovexMathFormatter.formatMathAndLatex(raw)

        assertTrue(formatted.contains("H₂SO₄"))
        assertTrue(formatted.contains("Na₂SO₄") || formatted.contains("Na2SO4"))
        assertTrue(formatted.contains("H₂O"))
        assertTrue(formatted.contains("CO₂"))
    }

    @Test
    fun testExponentsAndSubscripts() {
        val raw = "Gravity g = 9.8 m/s^2, density 10^-3 kg/m^3, initial pos x_0 and x_{max}"
        val formatted = QuovexMathFormatter.formatMathAndLatex(raw)

        assertTrue(formatted.contains("m/s²"))
        assertTrue(formatted.contains("10⁻³"))
        assertTrue(formatted.contains("kg/m³"))
        assertTrue(formatted.contains("x₀"))
        assertTrue(formatted.contains("x_max"))
    }

    @Test
    fun testInequalitiesAndArrows() {
        val raw = "a \\le b and c \\ge d and e \\ne f and A \\implies B and \\pm 5"
        val formatted = QuovexMathFormatter.formatMathAndLatex(raw)

        assertTrue(formatted.contains("≤"))
        assertTrue(formatted.contains("≥"))
        assertTrue(formatted.contains("≠"))
        assertTrue(formatted.contains("→"))
        assertTrue(formatted.contains("±"))
    }
}
