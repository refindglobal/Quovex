package com.quovex.domain.usecase

import com.quovex.data.remote.dto.AiSummaryResult
import com.quovex.data.remote.dto.GeneratedFlashcardDto
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.repository.QuovexRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SynthesizeLearningMaterialUseCaseTest {

    private val aiRepository = mockk<AIRepository>()
    private val quovexRepository = mockk<QuovexRepository>()
    private lateinit var useCase: SynthesizeLearningMaterialUseCase

    @Before
    fun setup() {
        useCase = SynthesizeLearningMaterialUseCase(aiRepository, quovexRepository)
    }

    @Test
    fun synthesize_validContent_createsFlashcardsAndSavesMaterial() = runTest {
        val rawText = "Ohm's Law states that V = IR across a conductor."
        val summaryResponse = AiSummaryResult(
            summary = "Summary of Ohm's Law and circuit resistance.",
            keyPoints = listOf("V is voltage", "I is current", "R is resistance"),
            flashcards = listOf(
                GeneratedFlashcardDto(
                    question = "State Ohm's Law",
                    answer = "V = IR",
                    formula = "V = I * R"
                )
            )
        )

        coEvery { aiRepository.summarizeNote(any(), "Physics") } returns Result.success(summaryResponse)
        coEvery { quovexRepository.insertMaterial(any()) } returns 77L
        coEvery { quovexRepository.insertDeck(any(), any(), any()) } returns 1L
        coEvery { quovexRepository.insertFlashcards(1, any()) } returns listOf(101L)
        coEvery { aiRepository.generateQuiz("Physics", "Current Electricity", any(), any()) } returns Result.success(
            listOf(
                QuizQuestion(
                    id = 1,
                    materialId = 77L,
                    question = "What does V represent?",
                    options = listOf("Voltage", "Velocity", "Volume", "Vector"),
                    correctIndex = 0,
                    explanation = "V stands for voltage",
                    relatedConcept = "Ohm's Law",
                    difficulty = 3
                )
            )
        )
        coEvery { quovexRepository.saveQuizQuestions(any()) } returns listOf(1L)
        coEvery { quovexRepository.updateMaterial(any()) } returns 1

        val result = useCase(
            title = "Ohm's Law",
            subject = "Physics",
            topic = "Current Electricity",
            rawText = rawText,
            inputType = NoteInputType.TEXT,
            inferredConfidence = 0.95f
        )

        assertTrue(result.isSuccess)
        val material = result.getOrNull()
        assertEquals(77L, material?.id)
        assertEquals("Physics", material?.subject)
        assertEquals(1, material?.formulas?.size)
        assertEquals(3, material?.keyPoints?.size)
        assertEquals(1, material?.flashcardDeckId)
        assertTrue(material?.quizGenerated == true)
    }

    @Test
    fun synthesize_blankText_returnsFailure() = runTest {
        val result = useCase(
            title = "Empty",
            subject = "Mathematics",
            topic = "Algebra",
            rawText = "   ",
            inputType = NoteInputType.TEXT
        )

        assertTrue(result.isFailure)
        assertEquals("Content cannot be empty for synthesis", result.exceptionOrNull()?.message)
    }
}

