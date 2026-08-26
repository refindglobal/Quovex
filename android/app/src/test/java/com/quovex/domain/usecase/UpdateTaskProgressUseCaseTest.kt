package com.quovex.domain.usecase

import com.quovex.data.local.dao.StudyPlanDao
import com.quovex.data.local.entity.StudyTaskEntity
import com.quovex.domain.model.PlanStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateTaskProgressUseCaseTest {

    private lateinit var studyPlanDao: StudyPlanDao
    private lateinit var useCase: UpdateTaskProgressUseCase

    @Before
    fun setUp() {
        studyPlanDao = mockk(relaxed = true)
        useCase = UpdateTaskProgressUseCase(studyPlanDao)
    }

    @Test
    fun `execute marks task completed and accumulates estimated minutes`() = runTest {
        val task = StudyTaskEntity(
            id = 7L, planId = 1L, dayNumber = 2, dateMillis = 1000L,
            subject = "Math", topic = "Derivatives", taskType = "DEEP_WORK_PRACTICE",
            estimatedMinutes = 45, completedMinutes = 0, isCompleted = false, notes = ""
        )
        coEvery { studyPlanDao.getTaskById(7L) } returns task
        coEvery { studyPlanDao.observeTasksForPlan(1L) } returns flowOf(listOf(task.copy(isCompleted = true)))

        val result = useCase.execute(taskId = 7L, isCompleted = true)

        assertTrue(result.isSuccess)
        coVerify {
            studyPlanDao.updateTaskCompletion(
                taskId = 7L,
                isCompleted = true,
                completedMinutes = 45
            )
        }
    }

    @Test
    fun `execute updates plan to COMPLETED when all plan tasks are done`() = runTest {
        val task1 = StudyTaskEntity(
            id = 1L, planId = 3L, dayNumber = 1, dateMillis = 1000L,
            subject = "Physics", topic = "T1", taskType = "STUDY_CHAPTER",
            estimatedMinutes = 30, completedMinutes = 30, isCompleted = true, notes = ""
        )
        val task2 = StudyTaskEntity(
            id = 2L, planId = 3L, dayNumber = 1, dateMillis = 1000L,
            subject = "Physics", topic = "T2", taskType = "TAKE_QUIZ",
            estimatedMinutes = 20, completedMinutes = 0, isCompleted = false, notes = ""
        )
        coEvery { studyPlanDao.getTaskById(2L) } returns task2
        coEvery { studyPlanDao.observeTasksForPlan(3L) } returns flowOf(
            listOf(task1, task2.copy(isCompleted = true))
        )

        val result = useCase.execute(taskId = 2L, isCompleted = true)

        assertTrue(result.isSuccess)
        coVerify {
            studyPlanDao.updatePlanStatus(
                planId = 3L,
                status = PlanStatus.COMPLETED.name,
                currentDay = 1
            )
        }
    }

    @Test
    fun `execute returns failure when task does not exist`() = runTest {
        coEvery { studyPlanDao.getTaskById(999L) } returns null

        val result = useCase.execute(taskId = 999L, isCompleted = true)

        assertTrue(result.isFailure)
    }
}
