package com.quovex.domain.usecase

import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.QuizResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LearningMaterialAndQuizUseCasesTest {

    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var fakeAiRepository: FakeAIRepository

    private lateinit var classifyMaterialUseCase: ClassifyMaterialUseCase
    private lateinit var confirmMaterialSubjectUseCase: ConfirmMaterialSubjectUseCase
    private lateinit var generateQuizUseCase: GenerateQuizUseCase
    private lateinit var recordQuizResultUseCase: RecordQuizResultUseCase
    private lateinit var getQuizResultsUseCase: GetQuizResultsUseCase
    private lateinit var createRemedialFlashcardsUseCase: CreateRemedialFlashcardsUseCase
    private lateinit var processScanAndSummarizeUseCase: ProcessScanAndSummarizeUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeQuovexRepository()
        fakeAiRepository = FakeAIRepository()

        classifyMaterialUseCase = ClassifyMaterialUseCase(fakeAiRepository)
        confirmMaterialSubjectUseCase = ConfirmMaterialSubjectUseCase(fakeRepository)
        generateQuizUseCase = GenerateQuizUseCase(fakeAiRepository, fakeRepository)
        recordQuizResultUseCase = RecordQuizResultUseCase(fakeRepository)
        getQuizResultsUseCase = GetQuizResultsUseCase(fakeRepository)
        createRemedialFlashcardsUseCase = CreateRemedialFlashcardsUseCase(fakeRepository)
        processScanAndSummarizeUseCase = ProcessScanAndSummarizeUseCase(fakeAiRepository)
    }

    @Test
    fun `classifyMaterialUseCase returns inferred subject and topic`() = runBlocking {
        val result = classifyMaterialUseCase("Force is mass times acceleration.")
        assertTrue(result.isSuccess)
        val inference = result.getOrNull()
        assertNotNull(inference)
        assertEquals("Physics", inference?.subject)
        assertEquals("Newton's Laws of Motion", inference?.topic)
        assertTrue((inference?.confidence ?: 0f) >= 0.9f)
    }

    @Test
    fun `confirmMaterialSubjectUseCase updates subject and topic and saves material`() = runBlocking {
        val material = LearningMaterial(
            id = 1L,
            title = "Mechanics",
            subject = "General",
            topic = ""
        )
        fakeRepository.insertMaterial(material)

        val updateResult = confirmMaterialSubjectUseCase(
            materialId = 1L,
            confirmedSubject = "Physics",
            confirmedTopic = "Newton's Laws",
            title = "Mechanics Chapter"
        )

        assertTrue(updateResult.isSuccess)
        val updated = updateResult.getOrThrow()
        assertEquals("Physics", updated.subject)
        assertEquals("Newton's Laws", updated.topic)
        assertEquals("Mechanics Chapter", updated.title)

        val persisted = fakeRepository.getMaterialById(1L)
        assertEquals("Physics", persisted?.subject)
    }

    @Test
    fun `generateQuizUseCase generates questions from keypoints and persists to repository`() = runBlocking {
        val material = LearningMaterial(
            id = 10L,
            title = "Kinematics",
            subject = "Physics",
            topic = "Motion",
            keyPoints = listOf("Velocity formula", "Acceleration definition")
        )
        fakeRepository.insertMaterial(material)

        val result = generateQuizUseCase(
            materialId = 10L,
            subject = "Physics",
            topic = "Motion",
            keyPoints = material.keyPoints
        )
        assertTrue(result.isSuccess)
        val questions = result.getOrNull()
        assertNotNull(questions)
        assertTrue(questions!!.isNotEmpty())
        assertEquals("What is F in F=ma?", questions[0].question)

        // Verify stored in DB
        val saved = fakeRepository.getQuizQuestionsForMaterial(10L).first()
        assertEquals(1, saved.size)
    }

    @Test
    fun `recordQuizResultUseCase records result score and mistakes`() = runBlocking {
        val mistakes = listOf(
            QuizMistake(
                id = 0L,
                resultId = 0L,
                questionText = "What is the speed of light?",
                studentAnswer = "3x10^6 m/s",
                correctAnswer = "3x10^8 m/s",
                explanation = "c is approximately 300,000 km/s in vacuum.",
                concept = "Electromagnetism"
            )
        )
        val result = QuizResult(
            id = 0L,
            materialId = 5L,
            totalQuestions = 5,
            score = 80,
            accuracyPercent = 80.0f,
            mistakes = mistakes
        )

        val resultId = recordQuizResultUseCase(result, deckId = 1)
        assertTrue(resultId > 0)

        val history = fakeRepository.getQuizResultsForMaterial(5L).first()
        assertEquals(1, history.size)
        assertEquals(80, history[0].score)
    }

    @Test
    fun `createRemedialFlashcardsUseCase turns quiz mistakes into high priority flashcards`() = runBlocking {
        val mistakes = listOf(
            QuizMistake(
                id = 1L,
                resultId = 100L,
                questionText = "State Lenz's Law.",
                studentAnswer = "Current opposes resistance",
                correctAnswer = "Induced EMF opposes the change in magnetic flux.",
                explanation = "Conservation of energy in electromagnetic systems.",
                concept = "Electromagnetic Induction"
            )
        )

        val cardIds = createRemedialFlashcardsUseCase(
            mistakes = mistakes,
            deckId = 1
        )

        assertEquals(1, cardIds.size)
        assertEquals(100L, cardIds[0])
    }

    @Test
    fun `processScanAndSummarizeUseCase processes OCR text into structured summary and flashcards`() = runBlocking {
        val result = processScanAndSummarizeUseCase(
            rawOcrText = "Photosynthesis requires sunlight, water, and CO2.",
            subjectHint = "Biology"
        )

        assertTrue(result.isSuccess)
        val scanResult = result.getOrNull()
        assertNotNull(scanResult)
        assertEquals("Structured summary generated by AI.", scanResult?.summaryResult?.summary)
        assertEquals("Physics", scanResult?.inference?.subject)
    }
}
