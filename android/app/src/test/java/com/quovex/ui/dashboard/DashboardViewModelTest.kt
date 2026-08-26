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
    private lateinit var fakeUserStatsDao: com.quovex.domain.usecase.FakeUserStatsDao
    private lateinit var fakeSessionDao: com.quovex.domain.usecase.FakeSessionDao
    private lateinit var calculateStreakUseCase: com.quovex.domain.usecase.CalculateStreakUseCase
    private lateinit var getScholarLevelUseCase: com.quovex.domain.usecase.GetScholarLevelUseCase
    private lateinit var studyAnalyticsUseCase: com.quovex.domain.usecase.StudyAnalyticsUseCase
    private lateinit var useRescueTokenUseCase: com.quovex.domain.usecase.UseRescueTokenUseCase
    private lateinit var sessionStateManager: SessionStateManager
    private lateinit var getDashboardStatsUseCase: GetDashboardStatsUseCase
    private lateinit var observeDailyScheduleUseCase: com.quovex.domain.usecase.ObserveDailyScheduleUseCase
    private lateinit var getDailyStudyRecommendationUseCase: com.quovex.domain.usecase.GetDailyStudyRecommendationUseCase
    private lateinit var observeUserEntitlementUseCase: com.quovex.domain.usecase.ObserveUserEntitlementUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeQuovexRepository()
        fakeUserPrefs = UserPreferencesManager(null)
        fakeUserStatsDao = com.quovex.domain.usecase.FakeUserStatsDao()
        fakeSessionDao = com.quovex.domain.usecase.FakeSessionDao()
        calculateStreakUseCase = com.quovex.domain.usecase.CalculateStreakUseCase(fakeUserStatsDao, fakeUserPrefs)
        getScholarLevelUseCase = com.quovex.domain.usecase.GetScholarLevelUseCase()
        studyAnalyticsUseCase = com.quovex.domain.usecase.StudyAnalyticsUseCase(fakeSessionDao, fakeUserPrefs)
        useRescueTokenUseCase = com.quovex.domain.usecase.UseRescueTokenUseCase(fakeUserStatsDao, calculateStreakUseCase)
        sessionStateManager = SessionStateManager()
        observeDailyScheduleUseCase = io.mockk.mockk(relaxed = true)
        getDailyStudyRecommendationUseCase = io.mockk.mockk(relaxed = true)
        observeUserEntitlementUseCase = io.mockk.mockk(relaxed = true)

        io.mockk.every { observeDailyScheduleUseCase.observeTodayTasks() } returns kotlinx.coroutines.flow.flowOf(emptyList())
        io.mockk.every { observeDailyScheduleUseCase.observeActivePlan() } returns kotlinx.coroutines.flow.flowOf(null)
        io.mockk.every { observeUserEntitlementUseCase.invoke() } returns kotlinx.coroutines.flow.MutableStateFlow(com.quovex.domain.model.UserEntitlement())
        io.mockk.coEvery { getDailyStudyRecommendationUseCase.execute(any()) } returns com.quovex.domain.model.StudyRecommendation(
            "Physics", "Core Concepts", 45, "Daily Focus", com.quovex.domain.model.StudyTaskType.DEEP_WORK_PRACTICE
        )

        getDashboardStatsUseCase = GetDashboardStatsUseCase(
            fakeRepository,
            fakeUserPrefs,
            calculateStreakUseCase,
            getScholarLevelUseCase,
            studyAnalyticsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testGreetingCalculation() {
        val viewModel = DashboardViewModel(
            getDashboardStatsUseCase,
            useRescueTokenUseCase,
            fakeUserPrefs,
            sessionStateManager,
            observeDailyScheduleUseCase,
            getDailyStudyRecommendationUseCase,
            observeUserEntitlementUseCase
        )

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
        fakeUserStatsDao.insertOrUpdate(
            com.quovex.data.local.entity.UserStatsEntity(id = 1, currentStreak = 7, longestStreak = 7, rescueTokens = 1, lastStudyDateMillis = System.currentTimeMillis())
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

        val viewModel = DashboardViewModel(
            getDashboardStatsUseCase,
            useRescueTokenUseCase,
            fakeUserPrefs,
            sessionStateManager,
            observeDailyScheduleUseCase,
            getDailyStudyRecommendationUseCase,
            observeUserEntitlementUseCase
        )
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

        val viewModel = DashboardViewModel(
            getDashboardStatsUseCase,
            useRescueTokenUseCase,
            fakeUserPrefs,
            sessionStateManager,
            observeDailyScheduleUseCase,
            getDailyStudyRecommendationUseCase,
            observeUserEntitlementUseCase
        )
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
