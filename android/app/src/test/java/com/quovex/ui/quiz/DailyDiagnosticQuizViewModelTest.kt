package com.quovex.ui.quiz

import com.quovex.domain.model.DiagnosticQuestion
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.GenerateDailyDiagnosticQuizUseCase
import com.quovex.domain.usecase.SynthesizeRemedialFlashcardsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DailyDiagnosticQuizViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val generateDailyDiagnosticQuizUseCase = mockk<GenerateDailyDiagnosticQuizUseCase>()
    private val synthesizeRemedialFlashcardsUseCase = mockk<SynthesizeRemedialFlashcardsUseCase>()
    private val quovexRepository = mockk<QuovexRepository>()

    private lateinit var viewModel: DailyDiagnosticQuizViewModel

    private val sampleQuestions = listOf(
        DiagnosticQuestion(
            id = 1,
            questionText = "Question 1?",
            options = listOf("Option A", "Option B", "Option C", "Option D"),
            correctOptionIndex = 0,
            subject = "Physics",
            concept = "Mechanics",
            explanation = "Explanation 1"
        ),
        DiagnosticQuestion(
            id = 2,
            questionText = "Question 2?",
            options = listOf("Option A", "Option B", "Option C", "Option D"),
            correctOptionIndex = 2,
            subject = "Chemistry",
            concept = "Thermodynamics",
            explanation = "Explanation 2"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { generateDailyDiagnosticQuizUseCase() } returns Result.success(sampleQuestions)
        coEvery { quovexRepository.recordQuizResult(any()) } returns 1L
        coEvery { synthesizeRemedialFlashcardsUseCase(any()) } returns Result.success(listOf(101L))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads diagnostic questions`() = runTest {
        viewModel = DailyDiagnosticQuizViewModel(
            generateDailyDiagnosticQuizUseCase = generateDailyDiagnosticQuizUseCase,
            synthesizeRemedialFlashcardsUseCase = synthesizeRemedialFlashcardsUseCase,
            quovexRepository = quovexRepository
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.questions.size)
        assertEquals(2, state.totalQuestions)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun `submitAnswer and nextQuestion transitions to finished and triggers remedial synthesis on mistakes`() = runTest {
        viewModel = DailyDiagnosticQuizViewModel(
            generateDailyDiagnosticQuizUseCase = generateDailyDiagnosticQuizUseCase,
            synthesizeRemedialFlashcardsUseCase = synthesizeRemedialFlashcardsUseCase,
            quovexRepository = quovexRepository
        )
        advanceUntilIdle()

        // Question 1: choose correct option (0)
        viewModel.selectOption(0)
        viewModel.submitAnswer()
        assertTrue(viewModel.uiState.value.isSubmitted)
        viewModel.nextQuestion()

        // Question 2: choose wrong option (1 instead of 2)
        viewModel.selectOption(1)
        viewModel.submitAnswer()
        viewModel.nextQuestion() // should finish quiz

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFinished)
        assertEquals(50, state.score) // 1 of 2 correct = 50%
        assertEquals(1, state.mistakes.size)
        assertEquals("Question 2?", state.mistakes.first().questionText)

        coVerify { quovexRepository.recordQuizResult(any()) }
        coVerify { synthesizeRemedialFlashcardsUseCase(any()) }
        assertEquals(1, state.remedialCardsCreated)
    }
}
