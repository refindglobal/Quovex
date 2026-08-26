package com.quovex.ui.planner

import com.quovex.domain.model.DailyStudyTask
import com.quovex.domain.model.PlanStatus
import com.quovex.domain.model.StudyPlan
import com.quovex.domain.model.StudyTaskType
import com.quovex.domain.usecase.GenerateStudyPlanUseCase
import com.quovex.domain.usecase.ObserveDailyScheduleUseCase
import com.quovex.domain.usecase.UpdateTaskProgressUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudyPlannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var generateStudyPlanUseCase: GenerateStudyPlanUseCase
    private lateinit var observeDailyScheduleUseCase: ObserveDailyScheduleUseCase
    private lateinit var updateTaskProgressUseCase: UpdateTaskProgressUseCase
    private lateinit var viewModel: StudyPlannerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        generateStudyPlanUseCase = mockk(relaxed = true)
        observeDailyScheduleUseCase = mockk(relaxed = true)
        updateTaskProgressUseCase = mockk(relaxed = true)

        every { observeDailyScheduleUseCase.observeActivePlan() } returns flowOf(null)
        every { observeDailyScheduleUseCase.observeTasksForPlan(any()) } returns flowOf(emptyList())

        viewModel = StudyPlannerViewModel(
            generateStudyPlanUseCase,
            observeDailyScheduleUseCase,
            updateTaskProgressUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init shows wizard when no active plan exists`() = runTest {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(null, state.activePlan)
        assertTrue(state.showWizard)
        assertEquals(1, state.wizardStep)
    }

    @Test
    fun `wizard step transitions update uiState`() = runTest {
        viewModel.setWizardStep(2)
        assertEquals(2, viewModel.uiState.value.wizardStep)

        viewModel.setWizardStep(3)
        assertEquals(3, viewModel.uiState.value.wizardStep)

        viewModel.setWizardStep(4)
        assertEquals(4, viewModel.uiState.value.wizardStep)
    }

    @Test
    fun `exam and subject selection updates uiState`() = runTest {
        viewModel.selectExam("NEET UG")
        assertEquals("NEET UG", viewModel.uiState.value.selectedExam)

        viewModel.setDailyHours(6.0f)
        assertEquals(6.0f, viewModel.uiState.value.dailyHours, 0.01f)

        viewModel.toggleSubject("Biology")
        assertTrue(viewModel.uiState.value.selectedSubjects.contains("Biology"))

        viewModel.setWeakTopics("Genetics, Optics")
        assertEquals("Genetics, Optics", viewModel.uiState.value.weakTopicsInput)
    }

    @Test
    fun `generatePlan calls use case and sets active plan on success`() = runTest {
        val plan = StudyPlan(
            id = 50L,
            title = "JEE Advanced Mastery Plan",
            targetExam = "JEE Advanced",
            examDateMillis = 1900000000L,
            dailyStudyHours = 4.0f,
            targetSubjects = listOf("Physics", "Chemistry", "Mathematics"),
            weakTopics = listOf("Calculus"),
            totalDays = 30,
            currentDay = 1,
            status = PlanStatus.ACTIVE
        )
        coEvery {
            generateStudyPlanUseCase.execute(any(), any(), any(), any(), any(), any())
        } returns Result.success(plan)

        viewModel.selectExam("JEE Advanced")
        viewModel.generatePlan()

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isGenerating)
        assertFalse(state.showWizard)
        assertEquals(50L, state.activePlan?.id)
        assertEquals("JEE Advanced Mastery Plan", state.activePlan?.title)
    }

    @Test
    fun `toggleTask calls updateTaskProgressUseCase`() = runTest {
        coEvery { updateTaskProgressUseCase.execute(any(), any(), any()) } returns Result.success(Unit)

        viewModel.toggleTask(taskId = 12L, isCompleted = true)

        advanceUntilIdle()
        coVerify { updateTaskProgressUseCase.execute(12L, true) }
    }

    @Test
    fun `selectDay filters tasks for selected day`() = runTest {
        val taskDay1 = DailyStudyTask(
            id = 1L, planId = 10L, dayNumber = 1, dateMillis = 1000L,
            subject = "Physics", topic = "T1", taskType = StudyTaskType.STUDY_CHAPTER,
            estimatedMinutes = 30
        )
        val taskDay2 = DailyStudyTask(
            id = 2L, planId = 10L, dayNumber = 2, dateMillis = 2000L,
            subject = "Math", topic = "T2", taskType = StudyTaskType.DEEP_WORK_PRACTICE,
            estimatedMinutes = 45
        )
        val plan = StudyPlan(
            id = 10L, title = "Plan", targetExam = "JEE", examDateMillis = 1000L,
            dailyStudyHours = 2f, targetSubjects = listOf("Physics"), weakTopics = emptyList(),
            totalDays = 5
        )

        every { observeDailyScheduleUseCase.observeActivePlan() } returns flowOf(plan)
        every { observeDailyScheduleUseCase.observeTasksForPlan(10L) } returns flowOf(listOf(taskDay1, taskDay2))

        val vm = StudyPlannerViewModel(generateStudyPlanUseCase, observeDailyScheduleUseCase, updateTaskProgressUseCase)
        advanceUntilIdle()

        vm.selectDay(2)
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.selectedDay)
        assertEquals(1, vm.uiState.value.tasksForSelectedDay.size)
        assertEquals("T2", vm.uiState.value.tasksForSelectedDay[0].topic)
    }
}
