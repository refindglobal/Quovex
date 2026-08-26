package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.SessionDao
import com.quovex.data.local.entity.SessionEntity
import com.quovex.domain.model.DiagnosticQuestion
import com.quovex.domain.model.DiagnosticQuizRequest
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
import java.time.ZoneId

class GenerateDailyDiagnosticQuizUseCaseTest {

    private val sessionDao = mockk<SessionDao>()
    private val userPreferencesManager = mockk<UserPreferencesManager>()
    private val diagnosticQuizRepository = mockk<DiagnosticQuizRepository>()

    private lateinit var useCase: GenerateDailyDiagnosticQuizUseCase

    private val fakeProfileFlow = MutableStateFlow(
        UserProfile(
            name = "Aspirant",
            targetExam = "JEE Advanced",
            dailyGoalHours = 2.0f
        )
    )

    @Before
    fun setup() {
        every { userPreferencesManager.userProfile } returns fakeProfileFlow
        useCase = GenerateDailyDiagnosticQuizUseCase(
            sessionDao = sessionDao,
            userPreferencesManager = userPreferencesManager,
            diagnosticQuizRepository = diagnosticQuizRepository
        )
    }

    @Test
    fun `invoke gathers today sessions and generates 5-question quiz`() = runTest {
        val now = System.currentTimeMillis()
        val todaySessions = listOf(
            SessionEntity(
                id = 1,
                subject = "Physics",
                startTime = now - 3600000L,
                endTime = now - 900000L,
                durationMinutes = 45,
                focusScore = 85,
                appBlockViolations = 0
            ),
            SessionEntity(
                id = 2,
                subject = "Mathematics",
                startTime = now - 1800000L,
                endTime = now,
                durationMinutes = 30,
                focusScore = 90,
                appBlockViolations = 0
            )
        )

        coEvery { sessionDao.getSessionsBetween(any(), any()) } returns todaySessions

        val mockQuestions = listOf(
            DiagnosticQuestion(
                id = 1,
                questionText = "What is the work done in an adiabatic process?",
                options = listOf("W = -dU", "W = dQ", "W = 0", "W = PdV"),
                correctOptionIndex = 0,
                subject = "Physics",
                concept = "Thermodynamics",
                explanation = "In adiabatic process, dQ = 0, hence dU = -dW."
            )
        )

        val requestSlot = slot<DiagnosticQuizRequest>()
        coEvery { diagnosticQuizRepository.generateDailyDiagnosticQuiz(capture(requestSlot)) } returns Result.success(mockQuestions)

        val result = useCase(ZoneId.of("UTC"))

        assertTrue(result.isSuccess)
        val questions = result.getOrNull()
        assertEquals(1, questions?.size)
        assertEquals("Physics", questions?.first()?.subject)

        val captured = requestSlot.captured
        assertEquals("JEE Advanced", captured.targetExam)
        assertEquals(2, captured.topics.size)
        assertTrue(captured.topics.any { it.subject == "Physics" && it.minutesStudied == 45 })
        assertTrue(captured.topics.any { it.subject == "Mathematics" && it.minutesStudied == 30 })
    }

    @Test
    fun `invoke falls back to default exam topics if no sessions studied today`() = runTest {
        coEvery { sessionDao.getSessionsBetween(any(), any()) } returns emptyList()

        val requestSlot = slot<DiagnosticQuizRequest>()
        coEvery { diagnosticQuizRepository.generateDailyDiagnosticQuiz(capture(requestSlot)) } returns Result.success(emptyList())

        val result = useCase(ZoneId.of("UTC"))

        assertTrue(result.isSuccess)
        val captured = requestSlot.captured
        assertEquals("JEE Advanced", captured.targetExam)
        assertTrue(captured.topics.isNotEmpty())
        assertTrue(captured.topics.any { it.subject == "Physics" })
        assertTrue(captured.topics.any { it.subject == "Chemistry" })
        assertTrue(captured.topics.any { it.subject == "Mathematics" })
    }
}
