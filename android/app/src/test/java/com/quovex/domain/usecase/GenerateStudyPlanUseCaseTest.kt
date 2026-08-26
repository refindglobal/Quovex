package com.quovex.domain.usecase

import com.quovex.data.local.dao.StudyPlanDao
import com.quovex.data.local.entity.StudyPlanEntity
import com.quovex.data.local.entity.StudyTaskEntity
import com.quovex.domain.model.PlanStatus
import com.quovex.domain.model.StudyTaskType
import com.quovex.domain.repository.AIRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class GenerateStudyPlanUseCaseTest {

    private lateinit var aiRepository: AIRepository
    private lateinit var studyPlanDao: StudyPlanDao
    private lateinit var useCase: GenerateStudyPlanUseCase

    @Before
    fun setUp() {
        aiRepository = mockk(relaxed = true)
        studyPlanDao = mockk(relaxed = true)
        useCase = GenerateStudyPlanUseCase(aiRepository, studyPlanDao)
    }

    @Test
    fun `execute generates plan, archives old plans, and saves tasks to Room`() = runTest {
        coEvery { aiRepository.generateStudyPlan(any(), any(), any(), any()) } returns Result.success("Focus on Mechanics and Electromagnetism")
        coEvery { studyPlanDao.insertPlan(any()) } returns 42L
        coEvery { studyPlanDao.insertTasks(any()) } returns Unit

        val futureDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30)
        val result = useCase.execute(
            examName = "JEE Advanced",
            examDateMillis = futureDate,
            dailyStudyHours = 4.0f,
            subjects = listOf("Physics", "Chemistry", "Mathematics"),
            weakTopics = listOf("Rotational Motion"),
            customDays = 10
        )

        assertTrue(result.isSuccess)
        val plan = result.getOrNull()
        assertNotNull(plan)
        assertEquals(42L, plan?.id)
        assertEquals("JEE Advanced Mastery Plan", plan?.title)
        assertEquals("JEE Advanced", plan?.targetExam)
        assertEquals(10, plan?.totalDays)
        assertEquals(PlanStatus.ACTIVE, plan?.status)

        coVerify { studyPlanDao.archiveOtherActivePlans(0) }

        val taskSlot = slot<List<StudyTaskEntity>>()
        coVerify { studyPlanDao.insertTasks(capture(taskSlot)) }
        assertEquals(40, taskSlot.captured.size) // 10 days * 4 tasks/day = 40 tasks
        assertTrue(taskSlot.captured.all { it.planId == 42L })
    }

    @Test
    fun `execute succeeds with fallback generation when AI fails`() = runTest {
        coEvery { aiRepository.generateStudyPlan(any(), any(), any(), any()) } returns Result.failure(Exception("AI Gateway timeout"))
        coEvery { studyPlanDao.insertPlan(any()) } returns 99L

        val futureDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14)
        val result = useCase.execute(
            examName = "NEET UG",
            examDateMillis = futureDate,
            dailyStudyHours = 3.0f,
            subjects = listOf("Biology", "Physics", "Chemistry"),
            customDays = 5
        )

        assertTrue(result.isSuccess)
        val plan = result.getOrNull()
        assertNotNull(plan)
        assertEquals(99L, plan?.id)
        assertEquals(5, plan?.totalDays)

        val taskSlot = slot<List<StudyTaskEntity>>()
        coVerify { studyPlanDao.insertTasks(capture(taskSlot)) }
        assertEquals(20, taskSlot.captured.size) // 5 days * 4 tasks/day
    }

    @Test
    fun `execute correctly distributes task types across day`() = runTest {
        coEvery { aiRepository.generateStudyPlan(any(), any(), any(), any()) } returns Result.success("OK")
        coEvery { studyPlanDao.insertPlan(any()) } returns 1L

        val taskSlot = slot<List<StudyTaskEntity>>()
        coEvery { studyPlanDao.insertTasks(capture(taskSlot)) } returns Unit

        val futureDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
        val result = useCase.execute(
            examName = "SAT",
            examDateMillis = futureDate,
            dailyStudyHours = 2.0f,
            subjects = listOf("Math", "Reading"),
            customDays = 1
        )

        assertTrue(result.isSuccess)
        val tasks = taskSlot.captured
        assertEquals(4, tasks.size)
        assertEquals(StudyTaskType.STUDY_CHAPTER.name, tasks[0].taskType)
        assertEquals(StudyTaskType.DEEP_WORK_PRACTICE.name, tasks[1].taskType)
        assertEquals(StudyTaskType.REVISE_FLASHCARDS.name, tasks[2].taskType)
        assertEquals(StudyTaskType.TAKE_QUIZ.name, tasks[3].taskType)
    }
}
