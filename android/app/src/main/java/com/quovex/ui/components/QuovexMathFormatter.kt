package com.quovex.ui.components

object QuovexMathFormatter {

    private val GREEK_MAP = mapOf(
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\delta" to "δ",
        "\\epsilon" to "ε",
        "\\varepsilon" to "ε",
        "\\zeta" to "ζ",
        "\\eta" to "η",
        "\\theta" to "θ",
        "\\vartheta" to "θ",
        "\\iota" to "ι",
        "\\kappa" to "κ",
        "\\lambda" to "λ",
        "\\mu" to "μ",
        "\\nu" to "ν",
        "\\xi" to "ξ",
        "\\pi" to "π",
        "\\varpi" to "ϖ",
        "\\rho" to "ρ",
        "\\varrho" to "ρ",
        "\\sigma" to "σ",
        "\\varsigma" to "ς",
        "\\tau" to "τ",
        "\\upsilon" to "υ",
        "\\phi" to "φ",
        "\\varphi" to "φ",
        "\\chi" to "χ",
        "\\psi" to "ψ",
        "\\omega" to "ω",
        "\\Gamma" to "Γ",
        "\\Delta" to "Δ",
        "\\Theta" to "Θ",
        "\\Lambda" to "Λ",
        "\\Xi" to "Ξ",
        "\\Pi" to "Π",
        "\\Sigma" to "Σ",
        "\\Upsilon" to "Υ",
        "\\Phi" to "Φ",
        "\\Psi" to "Ψ",
        "\\Omega" to "Ω"
    )

    private val OPERATORS_MAP = mapOf(
        "\\cdot" to "·",
        "\\times" to "×",
        "\\div" to "÷",
        "\\pm" to "±",
        "\\mp" to "∓",
        "\\le" to "≤",
        "\\leq" to "≤",
        "\\ge" to "≥",
        "\\geq" to "≥",
        "\\ne" to "≠",
        "\\neq" to "≠",
        "\\approx" to "≈",
        "\\equiv" to "≡",
        "\\to" to "→",
        "\\rightarrow" to "→",
        "\\implies" to "→",
        "\\leftarrow" to "←",
        "\\longleftarrow" to "←",
        "\\leftrightarrow" to "↔",
        "\\iff" to "↔",
        "\\infty" to "∞",
        "\\partial" to "∂",
        "\\nabla" to "∇",
        "\\propto" to "∝",
        "\\in" to "∈",
        "\\notin" to "∉",
        "\\subset" to "⊂",
        "\\subseteq" to "⊆",
        "\\cap" to "∩",
        "\\cup" to "∪",
        "\\forall" to "∀",
        "\\exists" to "∃",
        "\\deg" to "°",
        "\\circ" to "°",
        "^{\\circ}" to "°"
    )

    private val SUPERSCRIPT_MAP = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'y' to 'ʸ'
    )

    private val SUBSCRIPT_MAP = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ',
        'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ',
        'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ',
        'v' to 'ᵥ', 'x' to 'ₓ'
    )

    /**
     * Converts raw LaTeX, mathematical expressions, and chemical formulas into human-readable Unicode text.
     */
    fun formatMathAndLatex(raw: String): String {
        if (raw.isBlank()) return raw

        var text = raw

        // 1. Remove LaTeX math delimiters \[ \] and \( \)
        text = text.replace("\\[", "\n").replace("\\]", "\n")
        text = text.replace("\\(", "").replace("\\)", "")
        text = text.replace("$$", "\n").replace("$", "")

        // 2. Remove LaTeX formatting commands
        text = text.replace("\\displaystyle", "")
        text = text.replace("\\limits", "")
        text = text.replace("\\nolimits", "")

        // 3. Clean \boxed{...}, \text{...}, \mathrm{...}, \mathbf{...}, \mathit{...}
        text = cleanSingleArgCommands(text, "boxed")
        text = cleanSingleArgCommands(text, "text")
        text = cleanSingleArgCommands(text, "mathrm")
        text = cleanSingleArgCommands(text, "mathbf")
        text = cleanSingleArgCommands(text, "mathit")
        text = cleanSingleArgCommands(text, "boldsymbol")
        text = cleanSingleArgCommands(text, "overline")

        // 4. Fractions: \frac{a}{b} -> (a / b) or a/b
        text = cleanFractions(text)

        // 5. Roots: \sqrt[n]{x} -> ⁿ√(x) and \sqrt{x} -> √(x)
        text = cleanRoots(text)

        // 6. Integrals, Sums, Products
        text = cleanCalculus(text)

        // 7. Greek letters
        for ((k, v) in GREEK_MAP) {
            text = text.replace(k, v)
        }

        // 8. Operators and symbols
        for ((k, v) in OPERATORS_MAP) {
            text = text.replace(k, v)
        }

        // 9. Spacing commands
        text = text.replace("\\,", " ")
            .replace("\\;", " ")
            .replace("\\:", " ")
            .replace("\\!", "")
            .replace("\\quad", "   ")
            .replace("\\qquad", "      ")

        // 10. Superscripts: x^{2} or x^2 -> x²
        text = formatSuperscripts(text)

        // 11. Subscripts: x_{0} or x_0 -> x₀
        text = formatSubscripts(text)

        // 12. Chemistry formulas: H2O -> H₂O, H2SO4 -> H₂SO₄, CO2 -> CO₂
        text = formatChemistry(text)

        // 13. Clean lingering LaTeX backslashes before plain letters
        text = text.replace(Regex("""\\([a-zA-Z]+)""")) { match ->
            match.groupValues[1]
        }

        // 14. Normalize multiple spaces and lines
        text = text.replace(Regex("""[ \t]{2,}"""), " ")
        text = text.replace(Regex("""\n{3,}"""), "\n\n")

        return text.trim()
    }

    private fun cleanSingleArgCommands(input: String, command: String): String {
        val regex = Regex("""\\?$command\s*\{""")
        var result = input
        while (regex.containsMatchIn(result)) {
            val match = regex.find(result) ?: break
            val openBrace = match.range.last
            val startIdx = match.range.first
            val closeBrace = findMatchingBrace(result, openBrace)
            if (closeBrace != -1) {
                val content = result.substring(openBrace + 1, closeBrace).trim()
                result = result.substring(0, startIdx) + content + result.substring(closeBrace + 1)
            } else {
                result = result.replaceFirst(regex, "")
            }
        }
        return result
    }

    private fun cleanFractions(input: String): String {
        var result = input
        val fracPattern = "\\frac{"
        while (result.contains(fracPattern)) {
            val startIdx = result.indexOf(fracPattern)
            val numOpen = startIdx + fracPattern.length - 1
            val numClose = findMatchingBrace(result, numOpen)
            if (numClose != -1 && numClose + 1 < result.length && result[numClose + 1] == '{') {
                val denOpen = numClose + 1
                val denClose = findMatchingBrace(result, denOpen)
                if (denClose != -1) {
                    val num = formatMathAndLatex(result.substring(numOpen + 1, numClose).trim())
                    val den = formatMathAndLatex(result.substring(denOpen + 1, denClose).trim())
                    
                    val formattedFraction = if (num.length <= 4 && den.length <= 4 && !num.contains(" ") && !den.contains(" ")) {
                        "$num/$den"
                    } else {
                        "($num / $den)"
                    }
                    result = result.substring(0, startIdx) + formattedFraction + result.substring(denClose + 1)
                    continue
                }
            }
            result = result.replaceFirst(fracPattern, "")
        }
        return result
    }

    private fun cleanRoots(input: String): String {
        var result = input
        // \sqrt[n]{x}
        val nthRootRegex = Regex("""\\sqrt\[(.*?)\]\{(.*?)\}""")
        result = nthRootRegex.replace(result) { match ->
            val n = match.groupValues[1].trim()
            val x = match.groupValues[2].trim()
            val nSuper = toSuperscript(n)
            "${nSuper}√($x)"
        }

        // \sqrt{x}
        val sqrtPattern = "\\sqrt{"
        while (result.contains(sqrtPattern)) {
            val startIdx = result.indexOf(sqrtPattern)
            val openBrace = startIdx + sqrtPattern.length - 1
            val closeBrace = findMatchingBrace(result, openBrace)
            if (closeBrace != -1) {
                val content = result.substring(openBrace + 1, closeBrace).trim()
                result = result.substring(0, startIdx) + "√($content)" + result.substring(closeBrace + 1)
            } else {
                result = result.replaceFirst(sqrtPattern, "√")
            }
        }
        return result
    }

    private fun cleanCalculus(input: String): String {
        var result = input
        // \int_{a}^{b} -> ∫[a to b] or ∫ₐᵇ
        val intLimitsRegex = Regex("""\\int_\{?(.*?)\}?\^\{?(.*?)\}?""")
        result = intLimitsRegex.replace(result) { match ->
            val lower = match.groupValues[1].trim()
            val upper = match.groupValues[2].trim()
            "∫[$lower to $upper] "
        }
        result = result.replace("\\int", "∫ ")

        // \sum_{a}^{b} -> ∑[a to b]
        val sumLimitsRegex = Regex("""\\sum_\{?(.*?)\}?\^\{?(.*?)\}?""")
        result = sumLimitsRegex.replace(result) { match ->
            val lower = match.groupValues[1].trim()
            val upper = match.groupValues[2].trim()
            "∑[$lower to $upper] "
        }
        result = result.replace("\\sum", "∑ ")

        // \prod_{a}^{b} -> ∏[a to b]
        val prodLimitsRegex = Regex("""\\prod_\{?(.*?)\}?\^\{?(.*?)\}?""")
        result = prodLimitsRegex.replace(result) { match ->
            val lower = match.groupValues[1].trim()
            val upper = match.groupValues[2].trim()
            "∏[$lower to $upper] "
        }
        result = result.replace("\\prod", "∏ ")

        return result
    }

    private fun formatSuperscripts(input: String): String {
        var result = input
        // Form: ^{123}
        val braceSuperRegex = Regex("""\^\{([0-9+\-nixy=()]+)\}""")
        result = braceSuperRegex.replace(result) { match ->
            toSuperscript(match.groupValues[1])
        }

        // Form: ^2, ^3, ^-1
        val simpleSuperRegex = Regex("""\^([0-9n])""")
        result = simpleSuperRegex.replace(result) { match ->
            toSuperscript(match.groupValues[1])
        }

        val negSuperRegex = Regex("""\^(-[0-9]+)""")
        result = negSuperRegex.replace(result) { match ->
            toSuperscript(match.groupValues[1])
        }

        return result
    }

    private fun formatSubscripts(input: String): String {
        var result = input
        // Form: _{123} (digits only)
        val numSubRegex = Regex("""_\{([0-9+\-=()]+)\}""")
        result = numSubRegex.replace(result) { match ->
            toSubscript(match.groupValues[1])
        }

        // Form: _{word} -> _word
        val wordSubRegex = Regex("""_\{([a-zA-Z0-9_]+)\}""")
        result = wordSubRegex.replace(result) { match ->
            "_${match.groupValues[1]}"
        }

        // Form: _0, _1, _9 (single digit)
        val simpleSubRegex = Regex("""_([0-9])""")
        result = simpleSubRegex.replace(result) { match ->
            val ch = match.groupValues[1][0]
            SUBSCRIPT_MAP[ch]?.toString() ?: "_${ch}"
        }

        return result
    }

    private fun formatChemistry(input: String): String {
        // Chemical formulas: H2O -> H₂O, H2SO4 -> H₂SO₄, CO2 -> CO₂, CaCO3 -> CaCO₃, NO3 -> NO₃, CH4 -> CH₄, Na2SO4 -> Na₂SO₄
        val elementNumberRegex = Regex("""([A-Z][a-z]?)([0-9]+)""")
        return elementNumberRegex.replace(input) { match ->
            val element = match.groupValues[1]
            val count = match.groupValues[2]
            element + toSubscript(count)
        }
    }

    private fun toSuperscript(str: String): String {
        val sb = StringBuilder()
        for (c in str) {
            sb.append(SUPERSCRIPT_MAP[c] ?: c)
        }
        return sb.toString()
    }

    private fun toSubscript(str: String): String {
        val sb = StringBuilder()
        for (c in str) {
            sb.append(SUBSCRIPT_MAP[c] ?: c)
        }
        return sb.toString()
    }

    private fun findMatchingBrace(text: String, openBraceIdx: Int): Int {
        var depth = 1
        for (i in (openBraceIdx + 1) until text.length) {
            if (text[i] == '{') {
                depth++
            } else if (text[i] == '}') {
                depth--
                if (depth == 0) return i
            }
        }
        return -1
    }
}
