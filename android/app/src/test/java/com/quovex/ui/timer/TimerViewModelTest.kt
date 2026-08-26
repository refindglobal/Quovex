package com.quovex.ui.timer

import com.quovex.data.local.SessionStateManager
import com.quovex.domain.model.AppCategory
import com.quovex.domain.model.DistractionShieldState
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.FocusFrameResult
import com.quovex.domain.model.FocusMode
import com.quovex.domain.model.FocusTrackingState
import com.quovex.domain.model.SoundscapePresets
import com.quovex.domain.model.SoundscapeState
import com.quovex.domain.repository.DistractionBlockerRepository
import com.quovex.domain.repository.FocusDetectionRepository
import com.quovex.domain.repository.SoundscapeRepository
import com.quovex.domain.usecase.ControlFocusDetectionUseCase
import com.quovex.domain.usecase.ControlSoundscapeUseCase
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.GetInstalledAppsUseCase
import com.quovex.domain.usecase.ObserveBlockedAppsUseCase
import com.quovex.domain.usecase.ObserveFocusDetectionUseCase
import com.quovex.domain.usecase.ObserveSoundscapeUseCase
import com.quovex.domain.usecase.StartFocusSessionUseCase
import com.quovex.domain.usecase.ToggleBlockedAppUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
    private lateinit var soundscapeRepository: SoundscapeRepository
    private lateinit var observeSoundscapeUseCase: ObserveSoundscapeUseCase
    private lateinit var controlSoundscapeUseCase: ControlSoundscapeUseCase
    private lateinit var focusDetectionRepository: FocusDetectionRepository
    private lateinit var observeFocusDetectionUseCase: ObserveFocusDetectionUseCase
    private lateinit var controlFocusDetectionUseCase: ControlFocusDetectionUseCase
    private lateinit var distractionBlockerRepository: DistractionBlockerRepository
    private lateinit var observeBlockedAppsUseCase: ObserveBlockedAppsUseCase
    private lateinit var toggleBlockedAppUseCase: ToggleBlockedAppUseCase
    private lateinit var getInstalledAppsUseCase: GetInstalledAppsUseCase

    private val soundscapeStateFlow = MutableStateFlow(SoundscapeState())
    private val focusTrackingStateFlow = MutableStateFlow(FocusTrackingState())
    private val shieldStateFlow = MutableStateFlow(DistractionShieldState())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuovexRepository()
        sessionStateManager = SessionStateManager()
        getConfiguredSubjectsUseCase = GetConfiguredSubjectsUseCase(fakeRepository)
        startFocusSessionUseCase = StartFocusSessionUseCase(sessionStateManager)

        soundscapeRepository = mockk(relaxed = true)
        every { soundscapeRepository.soundscapeState } returns soundscapeStateFlow
        observeSoundscapeUseCase = ObserveSoundscapeUseCase(soundscapeRepository)
        controlSoundscapeUseCase = ControlSoundscapeUseCase(soundscapeRepository)

        focusDetectionRepository = mockk(relaxed = true)
        every { focusDetectionRepository.focusTrackingState } returns focusTrackingStateFlow
        observeFocusDetectionUseCase = ObserveFocusDetectionUseCase(focusDetectionRepository)
        controlFocusDetectionUseCase = ControlFocusDetectionUseCase(focusDetectionRepository)

        distractionBlockerRepository = mockk(relaxed = true)
        every { distractionBlockerRepository.shieldState } returns shieldStateFlow
        observeBlockedAppsUseCase = ObserveBlockedAppsUseCase(distractionBlockerRepository)
        toggleBlockedAppUseCase = ToggleBlockedAppUseCase(distractionBlockerRepository)
        getInstalledAppsUseCase = GetInstalledAppsUseCase(distractionBlockerRepository)

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
            sessionStateManager = sessionStateManager,
            observeSoundscapeUseCase = observeSoundscapeUseCase,
            controlSoundscapeUseCase = controlSoundscapeUseCase,
            observeFocusDetectionUseCase = observeFocusDetectionUseCase,
            controlFocusDetectionUseCase = controlFocusDetectionUseCase,
            observeBlockedAppsUseCase = observeBlockedAppsUseCase,
            toggleBlockedAppUseCase = toggleBlockedAppUseCase,
            getInstalledAppsUseCase = getInstalledAppsUseCase
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
        assertFalse(vm.uiState.value.focusTrackingState.isEnabled)
    }

    @Test
    fun `selectSubject updates selected subject when inactive`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectSubject("Chemistry")
        assertEquals("Chemistry", vm.uiState.value.selectedSubject)
    }

    @Test
    fun `selectMode updates selected mode when inactive`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.selectMode(FocusMode.DeepWork)
        assertEquals(FocusMode.DeepWork, vm.uiState.value.selectedMode)
    }

    @Test
    fun `setCustomDuration applies custom mode and closes dialog`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.openCustomDurationDialog()
        assertTrue(vm.uiState.value.isCustomDurationDialogOpen)

        vm.setCustomDuration(45, 10)
        assertFalse(vm.uiState.value.isCustomDurationDialogOpen)
        val mode = vm.uiState.value.selectedMode as FocusMode.Custom
        assertEquals(45, mode.customFocusMinutes)
        assertEquals(10, mode.customBreakMinutes)
    }

    @Test
    fun `toggleStrictFocus flips strict focus flag`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.strictFocusEnabled)
        vm.toggleStrictFocus()
        assertFalse(vm.uiState.value.strictFocusEnabled)
    }

    @Test
    fun `toggleCameraFocusDetection delegates to use case`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleCameraFocusDetection()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { focusDetectionRepository.setTrackingEnabled(true) }
    }

    @Test
    fun `open and close distraction shield sheet`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.uiState.value.isDistractionShieldSheetOpen)
        vm.openDistractionShieldSheet()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value.isDistractionShieldSheetOpen)
        coVerify { distractionBlockerRepository.refreshInstalledApps() }

        vm.closeDistractionShieldSheet()
        assertFalse(vm.uiState.value.isDistractionShieldSheetOpen)
    }

    @Test
    fun `toggleAppBlocked and toggleCategoryBlocked delegate to repository`() = runTest {
        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.toggleAppBlocked("com.instagram.android", true)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { distractionBlockerRepository.toggleAppBlocked("com.instagram.android", true) }

        vm.toggleCategoryBlocked(AppCategory.GAMING, false)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { distractionBlockerRepository.setCategoryBlocked(AppCategory.GAMING, false) }
    }
}
