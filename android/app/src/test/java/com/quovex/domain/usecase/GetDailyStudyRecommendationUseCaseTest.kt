package com.quovex.domain.usecase

import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.dao.StudyPlanDao
import com.quovex.data.local.entity.QuizMistakeEntity
import com.quovex.data.local.entity.StudyPlanEntity
import com.quovex.data.local.entity.StudyTaskEntity
import com.quovex.domain.model.StudyTaskType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDailyStudyRecommendationUseCaseTest {

    private lateinit var studyPlanDao: StudyPlanDao
    private lateinit var quizDao: QuizDao
    private lateinit var useCase: GetDailyStudyRecommendationUseCase

    @Before
    fun setUp() {
        studyPlanDao = mockk(relaxed = true)
        quizDao = mockk(relaxed = true)
        useCase = GetDailyStudyRecommendationUseCase(studyPlanDao, quizDao)
    }

    @Test
    fun `execute prioritizes pending study plan task for today`() = runTest {
        val plan = StudyPlanEntity(
            id = 1L, title = "JEE Plan", targetExam = "JEE Advanced",
            examDateMillis = 2000000000L, dailyStudyHours = 4f, targetSubjectsCsv = "Physics",
            weakTopicsCsv = "", totalDays = 30, currentDay = 1, status = "ACTIVE", createdAtMillis = 1000L
        )
        val task = StudyTaskEntity(
            id = 10L, planId = 1L, dayNumber = 1, dateMillis = System.currentTimeMillis(),
            subject = "Physics", topic = "Rotational Mechanics", taskType = "DEEP_WORK_PRACTICE",
            estimatedMinutes = 50, completedMinutes = 0, isCompleted = false, notes = ""
        )
        coEvery { studyPlanDao.getActivePlan() } returns plan
        every { studyPlanDao.observeTasksForDateRange(any(), any()) } returns flowOf(listOf(task))

        val rec = useCase.execute(fallbackSubject = "Physics")

        assertNotNull(rec)
        assertEquals("Physics", rec.recommendedSubject)
        assertEquals("Rotational Mechanics", rec.recommendedTopic)
        assertEquals(50, rec.estimatedMinutes)
        assertEquals(StudyTaskType.DEEP_WORK_PRACTICE, rec.suggestedActionType)
        assertTrue(rec.reason.contains("JEE Advanced"))
    }

    @Test
    fun `execute falls back to recent quiz mistake when no active plan task exists`() = runTest {
        every { studyPlanDao.observeTasksForDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { studyPlanDao.getActivePlan() } returns null
        coEvery { quizDao.getRecentMistakes(any()) } returns listOf(
            QuizMistakeEntity(
                id = 1L,
                resultId = 10L,
                questionId = 1L,
                questionText = "What is Snell's Law?",
                studentAnswer = "n1/sin1 = n2/sin2",
                correctAnswer = "n1 sin1 = n2 sin2",
                explanation = "Refraction formula",
                concept = "Optics Snell's Law"
            )
        )

        val rec = useCase.execute(fallbackSubject = "Physics")

        assertNotNull(rec)
        assertEquals("Optics Snell's Law", rec.recommendedTopic)
        assertEquals(StudyTaskType.REVISE_FLASHCARDS, rec.suggestedActionType)
        assertTrue(rec.reason.contains("practice quiz"))
    }

    @Test
    fun `execute returns general focus recommendation when no tasks or mistakes exist`() = runTest {
        every { studyPlanDao.observeTasksForDateRange(any(), any()) } returns flowOf(emptyList())
        coEvery { studyPlanDao.getActivePlan() } returns null
        coEvery { quizDao.getRecentMistakes(any()) } returns emptyList()

        val rec = useCase.execute(fallbackSubject = "Chemistry")

        assertNotNull(rec)
        assertEquals("Chemistry", rec.recommendedSubject)
        assertEquals("Core Problem Practice", rec.recommendedTopic)
        assertEquals(45, rec.estimatedMinutes)
    }
}
