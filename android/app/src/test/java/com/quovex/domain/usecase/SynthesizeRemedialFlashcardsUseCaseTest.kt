package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.FlashcardDao
import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.FlashcardEntity
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.model.RemedialCardSynthesis
import com.quovex.domain.model.UserProfile
import com.quovex.domain.repository.DiagnosticQuizRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SynthesizeRemedialFlashcardsUseCaseTest {

    private val diagnosticQuizRepository = mockk<DiagnosticQuizRepository>()
    private val flashcardDao = mockk<FlashcardDao>()
    private val quizDao = mockk<QuizDao>()
    private val userPreferencesManager = mockk<UserPreferencesManager>()

    private lateinit var useCase: SynthesizeRemedialFlashcardsUseCase

    private val fakeProfileFlow = MutableStateFlow(
        UserProfile(
            name = "Test Student",
            targetExam = "NEET"
        )
    )

    @Before
    fun setup() {
        every { userPreferencesManager.userProfile } returns fakeProfileFlow
        useCase = SynthesizeRemedialFlashcardsUseCase(
            diagnosticQuizRepository = diagnosticQuizRepository,
            flashcardDao = flashcardDao,
            quizDao = quizDao,
            userPreferencesManager = userPreferencesManager
        )
    }

    @Test
    fun `invoke synthesizes remedial cards and inserts into remedial deck with SM-2 interval 1`() = runTest {
        val mistakes = listOf(
            QuizMistake(
                id = 101,
                resultId = 5,
                questionText = "Which organelle synthesizes ATP in eukaryotes?",
                studentAnswer = "Ribosome",
                correctAnswer = "Mitochondria",
                explanation = "Mitochondria carry out oxidative phosphorylation to produce ATP.",
                concept = "Cellular Respiration"
            )
        )

        val existingDeck = DeckEntity(
            id = 7,
            title = "🎯 Remedial Concepts & Traps",
            subject = "Remedial",
            totalCards = 2
        )
        coEvery { flashcardDao.getMostRecentDeck() } returns existingDeck
        coEvery { flashcardDao.incrementDeckCardCount(7) } returns 1

        val synthesizedList = listOf(
            RemedialCardSynthesis(
                questionText = mistakes[0].questionText,
                studentSelectedOption = mistakes[0].studentAnswer,
                correctOption = mistakes[0].correctAnswer,
                concept = "Cellular Respiration",
                frontPrompt = "📌 Concept Check: ATP Generation Site",
                backExplanation = "Mitochondria synthesize ATP via oxidative phosphorylation.",
                commonTrapAlert = "Ribosomes perform translation/protein synthesis, not ATP synthesis."
            )
        )

        coEvery { diagnosticQuizRepository.synthesizeRemedialFlashcards(mistakes, "NEET") } returns Result.success(synthesizedList)

        val insertedCardSlot = slot<FlashcardEntity>()
        coEvery { flashcardDao.insertFlashcard(capture(insertedCardSlot)) } returns 999L
        coEvery { quizDao.updateRemedialCardId(101, 999L) } returns 1

        val result = useCase(mistakes)

        assertTrue(result.isSuccess)
        val ids = result.getOrNull()
        assertEquals(listOf(999L), ids)

        val card = insertedCardSlot.captured
        assertEquals(7, card.deckId)
        assertTrue(card.isRemedial)
        assertEquals(1, card.intervalDays) // Scheduled for tomorrow via SM-2!
        assertEquals(2.0f, card.easeFactor, 0.01f)
        assertTrue(card.frontContent.contains("ATP Generation Site"))
        assertTrue(card.backContent.contains("Ribosomes perform translation"))

        coVerify { quizDao.updateRemedialCardId(101, 999L) }
        coVerify { flashcardDao.incrementDeckCardCount(7) }
    }

    @Test
    fun `invoke returns empty list when no mistakes given`() = runTest {
        val result = useCase(emptyList())
        assertTrue(result.isSuccess)
        assertEquals(emptyList<Long>(), result.getOrNull())
    }
}
