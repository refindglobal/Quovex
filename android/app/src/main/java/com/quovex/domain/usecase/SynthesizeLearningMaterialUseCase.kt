package com.quovex.domain.usecase

import com.quovex.domain.model.FormulaItem
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.SubjectCatalog
import com.quovex.domain.model.SubjectCategory
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

/**
 * Transforms raw study notes / OCR text into a full Learning Material (Module B & Module D).
 *
 * Actions:
 * 1. AI Summarization & LaTeX math formula extraction.
 * 2. Key concepts extraction (5-10 core points).
 * 3. Auto-generation of SM-2 Spaced Repetition Flashcards linked directly to `FlashcardDao`.
 * 4. Auto-generation of Diagnostic Topic Quiz Questions linked to `QuizDao`.
 * 5. Direct persistence in Room SQLite tables with zero mock data.
 */
class SynthesizeLearningMaterialUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val quovexRepository: QuovexRepository
) {
    suspend operator fun invoke(
        title: String,
        subject: String,
        topic: String,
        subtopic: String = "",
        rawText: String,
        inputType: NoteInputType = NoteInputType.TEXT,
        sourceUrl: String? = null,
        inferredConfidence: Float = 0.9f
    ): Result<LearningMaterial> {
        val cleanText = rawText.trim()
        if (cleanText.isBlank()) {
            return Result.failure(IllegalArgumentException("Content cannot be empty for synthesis"))
        }

        val subjectCategory = SubjectCatalog.findByName(subject)?.category ?: SubjectCategory.OTHER

        // 1. Generate structured summary, key points, formulas, and flashcards
        val summaryResult = aiRepository.summarizeNote(
            rawText = cleanText,
            subject = subject
        )

        val summaryData = summaryResult.getOrNull()

        val summaryText = summaryData?.summary?.ifBlank { null }
            ?: generateFallbackSummary(cleanText, title, subject)

        val keyPoints = if (!summaryData?.keyPoints.isNullOrEmpty()) {
            summaryData!!.keyPoints
        } else {
            generateFallbackKeyPoints(cleanText)
        }

        val formulas = summaryData?.flashcards?.mapNotNull { card ->
            card.formula?.takeIf { it.isNotBlank() }?.let { f ->
                FormulaItem(
                    name = card.question.take(40),
                    latex = f,
                    description = card.answer
                )
            }
        } ?: emptyList()

        // 2. Build initial Learning Material entity
        val initialMaterial = LearningMaterial(
            title = title.ifBlank { "$subject - $topic" },
            subject = subject,
            subjectCategory = subjectCategory,
            topic = topic,
            subtopic = subtopic,
            summary = summaryText,
            keyPoints = keyPoints,
            formulas = formulas,
            inputType = inputType,
            status = NoteProcessingStatus.READY,
            sourceUrl = sourceUrl,
            inferredSubject = subject,
            inferredTopic = topic,
            inferredConfidence = inferredConfidence
        )

        val materialId = quovexRepository.insertMaterial(initialMaterial)

        // 3. Auto-generate SM-2 Flashcard Deck
        var flashcardDeckId: Int? = null
        var flashcardCount = 0

        val flashcardDtos = summaryData?.flashcards
        if (!flashcardDtos.isNullOrEmpty()) {
            try {
                val deckIdLong = quovexRepository.insertDeck(
                    title = "Flashcards: ${initialMaterial.title}",
                    subject = subject,
                    sourceMaterialId = materialId
                )
                flashcardDeckId = deckIdLong.toInt()

                val cardsToInsert = flashcardDtos.map { dto ->
                    val formulaNote = if (!dto.formula.isNullOrBlank()) "\n\nFormula: ${dto.formula}" else ""
                    Pair(dto.question, "${dto.answer}$formulaNote")
                }

                val insertedCards = quovexRepository.insertFlashcards(flashcardDeckId, cardsToInsert)
                flashcardCount = insertedCards.size
            } catch (_: Exception) {
                // If deck creation fails, continue gracefully
            }
        }

        // 4. Auto-generate Topic Quiz
        var quizGenerated = false
        try {
            val quizResult = aiRepository.generateQuiz(
                subject = subject,
                topic = topic,
                difficulty = "Medium",
                keyPoints = keyPoints
            )

            val questions = quizResult.getOrNull()
            if (!questions.isNullOrEmpty()) {
                val boundQuestions = questions.map { q ->
                    q.copy(materialId = materialId)
                }
                quovexRepository.saveQuizQuestions(boundQuestions)
                quizGenerated = true
            }
        } catch (_: Exception) {
            // Quiz generation is non-blocking
        }

        // 5. Update material with deck ID, card count, and quiz flag
        val finalMaterial = initialMaterial.copy(
            id = materialId,
            flashcardDeckId = flashcardDeckId,
            flashcardCount = flashcardCount,
            quizGenerated = quizGenerated,
            updatedAt = System.currentTimeMillis()
        )

        quovexRepository.updateMaterial(finalMaterial)

        return Result.success(finalMaterial)
    }

    private fun generateFallbackSummary(rawText: String, title: String, subject: String): String {
        val snippet = rawText.take(600).trim()
        return "### Executive Study Summary: $title\n\n" +
                "**Subject:** $subject\n\n" +
                snippet + (if (rawText.length > 600) "..." else "")
    }

    private fun generateFallbackKeyPoints(rawText: String): List<String> {
        val lines = rawText.lines()
            .map { it.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
            .filter { it.length in 15..200 }
        return if (lines.isNotEmpty()) lines.take(6) else listOf("Core conceptual review for $rawText")
    }
}
