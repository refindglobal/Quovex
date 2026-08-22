package com.quovex.ui.timer

import com.quovex.data.local.SessionStateManager
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.FocusMode
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.StartFocusSessionUseCase
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
class TimerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var sessionStateManager: SessionStateManager
    private lateinit var getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase
    private lateinit var startFocusSessionUseCase: StartFocusSessionUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuovexRepository()
        sessionStateManager = SessionStateManager()
        getConfiguredSubjectsUseCase = GetConfiguredSubjectsUseCase(fakeRepository)
        startFocusSessionUseCase = StartFocusSessionUseCase(sessionStateManager)

        fakeRepository.decksList = listOf(
            DeckItem(1, "Thermodynamics", "Physics", 20),
            DeckItem(2, "Organic", "Chemistry", 10)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel(): TimerViewModel {
        return TimerViewModel(
            getConfiguredSubjectsUseCase = getConfiguredSubjectsUseCase,
            startFocusSessionUseCase = startFocusSessionUseCase,
            sessionStateManager = sessionStateManager
        )
    }

    @Test
    fun `initial screen state is SETUP`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TimerScreenState.SETUP, vm.uiState.value.screenState)
        assertEquals("Physics", vm.uiState.value.selectedSubject)
        assertEquals(FocusMode.Pomodoro, vm.uiState.value.selectedMode)
        assertTrue(vm.uiState.value.strictFocusEnabled)
    }

    @Test
    fun `selecting subject updates state`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSubject("Chemistry")
        assertEquals("Chemistry", vm.uiState.value.selectedSubject)
    }

    @Test
    fun `selecting mode updates state`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectMode(FocusMode.DeepWork)
        assertEquals(FocusMode.DeepWork, vm.uiState.value.selectedMode)
    }

    @Test
    fun `custom duration dialog updates mode correctly`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.openCustomDurationDialog()
        assertTrue(vm.uiState.value.isCustomDurationDialogOpen)

        vm.setCustomDuration(40, 8)
        assertFalse(vm.uiState.value.isCustomDurationDialogOpen)
        val mode = vm.uiState.value.selectedMode as FocusMode.Custom
        assertEquals(40, mode.customFocusMinutes)
        assertEquals(8, mode.customBreakMinutes)
    }

    @Test
    fun `toggling strict focus updates state`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleStrictFocus()
        assertFalse(vm.uiState.value.strictFocusEnabled)
        vm.toggleStrictFocus()
        assertTrue(vm.uiState.value.strictFocusEnabled)
    }

    @Test
    fun `screen state transitions to ACTIVE when session starts`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        sessionStateManager.startSession("Physics", "Pomodoro", 25, true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TimerScreenState.ACTIVE, vm.uiState.value.screenState)
        assertEquals("25:00", vm.uiState.value.formattedRemainingTime)
    }

    @Test
    fun `screen state transitions to SUMMARY when session completes`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        sessionStateManager.startSession("Physics", "Pomodoro", 25, true)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TimerScreenState.ACTIVE, vm.uiState.value.screenState)

        sessionStateManager.markCompleted()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TimerScreenState.SUMMARY, vm.uiState.value.screenState)
        assertEquals("Physics", vm.uiState.value.latestSummary?.subject)
        assertTrue(vm.uiState.value.latestSummary?.isCompleted == true)
    }

    @Test
    fun `dismissing summary returns to SETUP`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        sessionStateManager.startSession("Physics", "Pomodoro", 25, true)
        sessionStateManager.markCompleted()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(TimerScreenState.SUMMARY, vm.uiState.value.screenState)

        vm.dismissSummary()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(TimerScreenState.SETUP, vm.uiState.value.screenState)
    }
}
