package com.quovex.ui.dashboard

import com.quovex.data.local.SessionStateManager
import com.quovex.data.local.UserPreferencesManager
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.UserProfile
import com.quovex.domain.usecase.FakeQuovexRepository
import com.quovex.domain.usecase.GetDashboardStatsUseCase
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
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var fakeUserPrefs: UserPreferencesManager
    private lateinit var sessionStateManager: SessionStateManager
    private lateinit var getDashboardStatsUseCase: GetDashboardStatsUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuovexRepository()
        fakeUserPrefs = UserPreferencesManager(null)
        sessionStateManager = SessionStateManager()
        getDashboardStatsUseCase = GetDashboardStatsUseCase(fakeRepository, fakeUserPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGreetingCalculation() {
        val viewModel = DashboardViewModel(getDashboardStatsUseCase, fakeUserPrefs, sessionStateManager)

        assertEquals("Good Morning", viewModel.calculateGreeting(5))
        assertEquals("Good Morning", viewModel.calculateGreeting(11))
        assertEquals("Good Afternoon", viewModel.calculateGreeting(12))
        assertEquals("Good Afternoon", viewModel.calculateGreeting(16))
        assertEquals("Good Evening", viewModel.calculateGreeting(17))
        assertEquals("Good Evening", viewModel.calculateGreeting(23))
        assertEquals("Good Evening", viewModel.calculateGreeting(2))
    }

    @Test
    fun testDashboardUiStateSuccessEmission() = runTest {
        fakeUserPrefs.saveUserProfile(
            UserProfile(name = "Aarav", dailyGoalHours = 3.0f, streakDays = 7, targetExam = "JEE Advanced")
        )
        fakeRepository.todaySeconds = 5400L // 90 mins
        fakeRepository.totalDueFlashcards = 5
        fakeRepository.mostRecentDeck = DeckItem(
            id = 1,
            title = "Mechanics",
            subject = "Physics",
            totalCards = 20
        )
        fakeRepository.deckDueCount = 5

        val viewModel = DashboardViewModel(getDashboardStatsUseCase, fakeUserPrefs, sessionStateManager)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DashboardUiStatus.Success, state.status)
        assertEquals("Aarav", state.userProfile.name)
        assertEquals(90, state.todayFocusMinutes)
        assertEquals(180, state.targetMinutes)
        assertEquals(0.5f, state.progressPercent, 0.001f)
        assertEquals(7, state.streakDays)
        assertEquals(5, state.dueFlashcards.totalDueCount)
        assertEquals(1, state.jumpBackInItem?.deckId)
        assertEquals("Mechanics", state.jumpBackInItem?.title)
        assertFalse(state.activeSession.isActive)
    }

    @Test
    fun testActiveSessionSync() = runTest {
        fakeUserPrefs.saveUserProfile(UserProfile(name = "Aarav"))

        val viewModel = DashboardViewModel(getDashboardStatsUseCase, fakeUserPrefs, sessionStateManager)
        advanceUntilIdle()

        // Update active session in SessionStateManager
        sessionStateManager.updateActiveSession(
            isActive = true,
            remainingSeconds = 900,
            totalSeconds = 1500,
            subject = "Physics: Solenoids"
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.activeSession.isActive)
        assertEquals("Physics: Solenoids", state.activeSession.subject)
        assertEquals(900, state.activeSession.remainingSeconds)
    }
}
