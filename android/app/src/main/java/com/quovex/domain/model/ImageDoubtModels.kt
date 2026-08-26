package com.quovex.domain.model

/**
 * Domain-safe image input abstraction.
 * Encapsulates raw image binary data without leaking Android-specific
 * Bitmap or Context objects into the domain layer.
 */
data class DomainImageInput(
    val bytes: ByteArray,
    val mimeType: String = "image/jpeg"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DomainImageInput

        if (!bytes.contentEquals(other.bytes)) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Domain model representing the step-by-step solution from the vision AI tutor.
 */
data class ImageDoubtSolution(
    val solution: String,
    val provider: String = "groq",
    val model: String? = null
) {
    fun toStructured(subject: String = "General"): StructuredDoubtSolution {
        return parseDoubtMarkdown(solution, subject)
    }
}

/**
 * 6-tier structured pedagogical breakdown of a visual academic problem (Module B3: L-032, L-033).
 */
data class StructuredDoubtSolution(
    val problemSummary: String,
    val coreConcept: String,
    val steps: List<String>,
    val formulas: List<FormulaItem>,
    val finalAnswer: String,
    val commonMistakes: List<String>,
    val rawMarkdown: String
)

data class DoubtFollowUpMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

private fun parseDoubtMarkdown(raw: String, defaultSubject: String): StructuredDoubtSolution {
    val lines = raw.lines()
    var currentSection = "summary"
    val summaryLines = mutableListOf<String>()
    val conceptLines = mutableListOf<String>()
    val stepLines = mutableListOf<String>()
    val formulaLines = mutableListOf<String>()
    val answerLines = mutableListOf<String>()
    val mistakeLines = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trim()
        val lower = trimmed.lowercase()
        val isHeader = trimmed.startsWith("#") ||
                (trimmed.startsWith("**") && trimmed.endsWith("**")) ||
                (trimmed.length < 50 && (trimmed.endsWith(":") ||
                        lower.startsWith("problem") ||
                        lower.startsWith("core concept") ||
                        lower.startsWith("key concept") ||
                        lower.startsWith("step-by-step") ||
                        lower.startsWith("solution steps") ||
                        lower.startsWith("key formula") ||
                        lower.startsWith("formulas") ||
                        lower.startsWith("final answer") ||
                        lower.startsWith("common mistake") ||
                        lower.startsWith("pitfall")))

        if (isHeader) {
            when {
                lower.contains("problem") || lower.contains("given data") -> {
                    currentSection = "summary"
                }
                lower.contains("concept") || lower.contains("principle") || lower.contains("governing law") -> {
                    currentSection = "concept"
                }
                lower.contains("step") || lower.contains("derivation") || lower.contains("solution") -> {
                    currentSection = "steps"
                }
                lower.contains("formula") || lower.contains("equation") || lower.contains("theorem") -> {
                    currentSection = "formula"
                }
                lower.contains("final answer") || lower.contains("result") || lower.contains("conclusion") -> {
                    currentSection = "answer"
                }
                lower.contains("mistake") || lower.contains("pitfall") || lower.contains("trap") || lower.contains("misconception") -> {
                    currentSection = "mistakes"
                }
            }
        } else {
            when (currentSection) {
                "summary" -> summaryLines.add(line)
                "concept" -> conceptLines.add(line)
                "steps" -> stepLines.add(line)
                "formula" -> formulaLines.add(line)
                "answer" -> answerLines.add(line)
                "mistakes" -> mistakeLines.add(line)
            }
        }
    }

    val summary = summaryLines.joinToString("\n").trim().ifBlank {
        raw.take(300).trim()
    }
    val concept = conceptLines.joinToString("\n").trim().ifBlank {
        "$defaultSubject Fundamental Concept & Problem Solving Principle"
    }
    val steps = if (stepLines.isNotEmpty()) {
        val nonBlank = stepLines.map { it.trim() }.filter { it.isNotBlank() }
        if (nonBlank.isNotEmpty() && nonBlank.all { it.matches(Regex("^(\\d+\\.|[-•*]|Step\\s*\\d+:?).*")) }) {
            nonBlank.map { it.removePrefix("-").removePrefix("•").removePrefix("*").trim() }
        } else {
            stepLines.joinToString("\n").split(Regex("\n(?=\\d+\\.|###|Step\\s*\\d+)"))
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
    } else {
        listOf(raw.trim())
    }

    val formulas = mutableListOf<FormulaItem>()
    formulaLines.forEach { fLine ->
        val trimmed = fLine.trim().removePrefix("-").removePrefix("•").trim()
        if (trimmed.isNotBlank() && (trimmed.contains("=") || trimmed.contains("\\") || trimmed.contains("+") || trimmed.length < 100)) {
            formulas.add(
                FormulaItem(
                    name = "$defaultSubject Formula",
                    latex = trimmed,
                    description = "Governing equation for this problem"
                )
            )
        }
    }

    val finalAnswer = answerLines.joinToString("\n").trim().ifBlank {
        "Verified solution based on step-by-step reasoning."
    }

    val commonMistakes = mistakeLines.map { it.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
        .filter { it.isNotBlank() && it.length > 8 }

    return StructuredDoubtSolution(
        problemSummary = summary,
        coreConcept = concept,
        steps = if (steps.isNotEmpty()) steps else listOf(raw),
        formulas = formulas,
        finalAnswer = finalAnswer,
        commonMistakes = if (commonMistakes.isNotEmpty()) commonMistakes else listOf(
            "Be careful with sign conventions and unit conversions (e.g. converting cm to meters or minutes to seconds).",
            "Verify all boundary conditions and assumptions before applying formulas directly."
        ),
        rawMarkdown = raw
    )
}
