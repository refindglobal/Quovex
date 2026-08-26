package com.quovex.domain.usecase

import com.quovex.data.local.dao.StudyPlanDao
import com.quovex.data.local.entity.StudyPlanEntity
import com.quovex.data.local.entity.StudyTaskEntity
import com.quovex.domain.model.PlanStatus
import com.quovex.domain.model.StudyTaskType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ObserveDailyScheduleUseCaseTest {

    private lateinit var studyPlanDao: StudyPlanDao
    private lateinit var useCase: ObserveDailyScheduleUseCase

    @Before
    fun setUp() {
        studyPlanDao = mockk(relaxed = true)
        useCase = ObserveDailyScheduleUseCase(studyPlanDao)
    }

    @Test
    fun `observeActivePlan maps entity to domain model correctly`() = runTest {
        val entity = StudyPlanEntity(
            id = 10L,
            title = "JEE Plan",
            targetExam = "JEE Advanced",
            examDateMillis = 1800000000L,
            dailyStudyHours = 4.0f,
            targetSubjectsCsv = "Physics,Chemistry",
            weakTopicsCsv = "Calculus",
            totalDays = 30,
            currentDay = 3,
            status = "ACTIVE",
            createdAtMillis = 1700000000L
        )
        every { studyPlanDao.observeActivePlan() } returns flowOf(entity)

        val plan = useCase.observeActivePlan().first()
        assertNotNull(plan)
        assertEquals(10L, plan?.id)
        assertEquals("JEE Plan", plan?.title)
        assertEquals(listOf("Physics", "Chemistry"), plan?.targetSubjects)
        assertEquals(listOf("Calculus"), plan?.weakTopics)
        assertEquals(PlanStatus.ACTIVE, plan?.status)
    }

    @Test
    fun `observeActivePlan emits null when no active plan exists`() = runTest {
        every { studyPlanDao.observeActivePlan() } returns flowOf(null)

        val plan = useCase.observeActivePlan().first()
        assertNull(plan)
    }

    @Test
    fun `observeTasksForDay maps entities to domain tasks correctly`() = runTest {
        val entities = listOf(
            StudyTaskEntity(
                id = 1L, planId = 5L, dayNumber = 1, dateMillis = 1000L,
                subject = "Physics", topic = "Kinematics", taskType = "STUDY_CHAPTER",
                estimatedMinutes = 60, completedMinutes = 60, isCompleted = true, notes = "Notes"
            ),
            StudyTaskEntity(
                id = 2L, planId = 5L, dayNumber = 1, dateMillis = 1000L,
                subject = "Physics", topic = "Problem Set", taskType = "DEEP_WORK_PRACTICE",
                estimatedMinutes = 45, completedMinutes = 0, isCompleted = false, notes = ""
            )
        )
        every { studyPlanDao.observeTasksForDay(5L, 1) } returns flowOf(entities)

        val tasks = useCase.observeTasksForDay(5L, 1).first()
        assertEquals(2, tasks.size)
        assertEquals("Kinematics", tasks[0].topic)
        assertEquals(StudyTaskType.STUDY_CHAPTER, tasks[0].taskType)
        assertEquals(true, tasks[0].isCompleted)
        assertEquals(StudyTaskType.DEEP_WORK_PRACTICE, tasks[1].taskType)
        assertEquals(false, tasks[1].isCompleted)
    }
}
