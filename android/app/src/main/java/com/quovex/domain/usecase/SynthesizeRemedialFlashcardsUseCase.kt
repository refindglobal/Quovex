package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.FlashcardDao
import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.FlashcardEntity
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.repository.DiagnosticQuizRepository
import javax.inject.Inject

class SynthesizeRemedialFlashcardsUseCase @Inject constructor(
    private val diagnosticQuizRepository: DiagnosticQuizRepository,
    private val flashcardDao: FlashcardDao,
    private val quizDao: QuizDao,
    private val userPreferencesManager: UserPreferencesManager
) {

    suspend operator fun invoke(
        mistakes: List<QuizMistake>,
        preferredDeckId: Int? = null
    ): Result<List<Long>> {
        if (mistakes.isEmpty()) return Result.success(emptyList())

        val profile = userPreferencesManager.userProfile.value
        val targetExam = profile.targetExam.ifBlank { "General Competitive" }

        // Find or create dedicated Remedial deck if none provided
        val deckId = preferredDeckId ?: getOrCreateRemedialDeck()

        val synthesisResult = diagnosticQuizRepository.synthesizeRemedialFlashcards(mistakes, targetExam)
        val synthesizedCards = synthesisResult.getOrElse {
            // Deterministic offline fallback if AI network call fails
            mistakes.map { m ->
                com.quovex.domain.model.RemedialCardSynthesis(
                    questionText = m.questionText,
                    studentSelectedOption = m.studentAnswer,
                    correctOption = m.correctAnswer,
                    concept = m.concept.ifBlank { "Core Concept" },
                    frontPrompt = "📌 Concept Check: ${m.questionText}",
                    backExplanation = "✅ Correct: ${m.correctAnswer}\n\n💡 Explanation: ${m.explanation}",
                    commonTrapAlert = "Avoid selecting '${m.studentAnswer}' — review the core definition."
                )
            }
        }

        val insertedIds = mutableListOf<Long>()
        val tomorrowMillis = System.currentTimeMillis() + 86400000L // 24 hours (SM-2 interval 1 day)

        synthesizedCards.forEachIndexed { index, synth ->
            val correspondingMistake = mistakes.getOrNull(index)

            val entity = FlashcardEntity(
                deckId = deckId,
                frontContent = synth.frontPrompt,
                backContent = "${synth.backExplanation}\n\n⚠️ Common Trap: ${synth.commonTrapAlert}",
                easeFactor = 2.0f,
                intervalDays = 1,
                repetitions = 0,
                nextReviewDate = tomorrowMillis,
                tags = "remedial,${synth.concept}",
                isRemedial = true,
                difficulty = 4
            )

            val cardId = flashcardDao.insertFlashcard(entity)
            flashcardDao.incrementDeckCardCount(deckId)
            insertedIds.add(cardId)

            if (correspondingMistake != null && correspondingMistake.id > 0) {
                quizDao.updateRemedialCardId(correspondingMistake.id, cardId)
            }
        }

        return Result.success(insertedIds)
    }

    private suspend fun getOrCreateRemedialDeck(): Int {
        val allDecks = flashcardDao.getMostRecentDeck()
        val existingDeck = if (allDecks?.subject.equals("Remedial", ignoreCase = true)) {
            allDecks
        } else {
            null
        }

        if (existingDeck != null) {
            return existingDeck.id
        }

        val newDeck = DeckEntity(
            title = "🎯 Remedial Concepts & Traps",
            subject = "Remedial",
            totalCards = 0,
            xpValue = 200
        )
        return flashcardDao.insertDeck(newDeck).toInt()
    }
}
