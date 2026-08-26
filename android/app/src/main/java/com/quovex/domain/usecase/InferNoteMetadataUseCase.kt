package com.quovex.domain.usecase

import com.quovex.domain.model.SubjectCatalog
import com.quovex.domain.model.SubjectCategory
import com.quovex.domain.model.SubjectInference
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

/**
 * AI-First Subject & Topic Inference Use Case (Module B: L-010 to L-016).
 *
 * Infers:
 * - Subject (Physics, Chemistry, Maths, Biology, History, Economy, etc.)
 * - Chapter / Topic (e.g. "Thermodynamics", "Newton's Laws", "Organic Reaction Mechanisms")
 * - Subtopic (e.g. "Carnot Cycle", "Free Body Diagrams")
 * - Exam Relevance (e.g. "JEE Main & Advanced", "NEET-UG", "CBSE Class 12", "UPSC CSE")
 * - Confidence score (0.0 to 1.0)
 *
 * Subject selection on import is optional — student may import without pre-selecting.
 */
class InferNoteMetadataUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        rawText: String,
        fileName: String? = null,
        userSubjectHint: String? = null
    ): Result<SubjectInference> {
        val cleanText = rawText.trim()
        if (cleanText.isBlank()) {
            return Result.failure(IllegalArgumentException("Text content cannot be blank for inference"))
        }

        val textSample = cleanText.take(2500)
        val aiResult = aiRepository.classifyMaterial(textSample, fileName)

        return aiResult.mapCatching { inference ->
            // If AI succeeded with high confidence, normalize and return
            val detectedSubject = if (!userSubjectHint.isNullOrBlank() && userSubjectHint != "General") {
                userSubjectHint
            } else {
                inference.subject.ifBlank { "General" }
            }

            val detectedTopic = inference.topic.ifBlank {
                extractFirstHeading(cleanText) ?: "Study Notes"
            }

            val examRelevance = if (inference.examRelevance.isNotEmpty()) {
                inference.examRelevance
            } else {
                inferExamsFromSubject(detectedSubject)
            }

            inference.copy(
                subject = detectedSubject,
                topic = detectedTopic,
                subtopic = inference.subtopic?.ifBlank { null },
                examRelevance = examRelevance,
                confidence = if (inference.confidence > 0f) inference.confidence else 0.85f
            )
        }.recoverCatching {
            // Deterministic offline fallback if AI gateway is offline or unavailable
            fallbackInference(cleanText, fileName, userSubjectHint)
        }
    }

    private fun extractFirstHeading(text: String): String? {
        val lines = text.lines()
        val firstLine = lines.firstOrNull { it.isNotBlank() }?.trim() ?: return null
        return firstLine.removePrefix("#").removePrefix("##").trim().take(50)
    }

    private fun fallbackInference(
        text: String,
        fileName: String?,
        userSubjectHint: String?
    ): SubjectInference {
        val lower = text.lowercase()
        val detectedSubject = when {
            !userSubjectHint.isNullOrBlank() && userSubjectHint != "General" -> userSubjectHint
            lower.contains("force") || lower.contains("velocity") || lower.contains("acceleration") || lower.contains("momentum") || lower.contains("thermodynamics") -> "Physics"
            lower.contains("reaction") || lower.contains("molecule") || lower.contains("acid") || lower.contains("base") || lower.contains("orbital") || lower.contains("equilibrium") -> "Chemistry"
            lower.contains("integral") || lower.contains("derivative") || lower.contains("matrix") || lower.contains("probability") || lower.contains("equation") || lower.contains("triangle") -> "Mathematics"
            lower.contains("cell") || lower.contains("dna") || lower.contains("protein") || lower.contains("photosynthesis") || lower.contains("respiration") || lower.contains("organism") -> "Biology"
            lower.contains("revenue") || lower.contains("gdp") || lower.contains("inflation") || lower.contains("market") || lower.contains("demand") || lower.contains("supply") -> "Economics"
            lower.contains("debit") || lower.contains("credit") || lower.contains("ledger") || lower.contains("balance sheet") || lower.contains("journal") -> "Accountancy"
            lower.contains("constitution") || lower.contains("dynasty") || lower.contains("treaty") || lower.contains("empire") || lower.contains("revolution") -> "History"
            else -> "General"
        }

        val topic = fileName?.removeSuffix(".pdf")?.removeSuffix(".txt")
            ?: extractFirstHeading(text)
            ?: "$detectedSubject Notes"

        return SubjectInference(
            subject = detectedSubject,
            topic = topic,
            subtopic = null,
            examRelevance = inferExamsFromSubject(detectedSubject),
            confidence = 0.75f
        )
    }

    private fun inferExamsFromSubject(subject: String): List<String> {
        val category = SubjectCatalog.findByName(subject)?.category ?: SubjectCategory.OTHER
        return when (category) {
            SubjectCategory.SCIENCE, SubjectCategory.MATHEMATICS -> listOf("JEE Main & Advanced", "NEET-UG", "CBSE Class 12")
            SubjectCategory.COMMERCE -> listOf("CA Foundation", "CUET-UG", "CBSE Commerce")
            SubjectCategory.HUMANITIES -> listOf("UPSC CSE", "CUET-UG", "State PSC")
            else -> listOf("CBSE / Board Exams", "Competitive Exams")
        }
    }
}
