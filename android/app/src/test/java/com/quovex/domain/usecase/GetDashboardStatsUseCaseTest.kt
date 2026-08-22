package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.DeckStats
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.model.RecentActivityItem
import com.quovex.domain.model.UserProfile
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class GetDashboardStatsUseCaseTest {

    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var fakeUserPrefs: UserPreferencesManager
    private lateinit var useCase: GetDashboardStatsUseCase

    private val testZone = ZoneId.of("UTC")

    @Before
    fun setup() {
        fakeRepository = FakeQuovexRepository()
        fakeUserPrefs = UserPreferencesManager(null)
        useCase = GetDashboardStatsUseCase(fakeRepository, fakeUserPrefs)
    }

    private fun setUserProfile(dailyGoalHours: Float, streak: Int = 5, xp: Int = 1000) {
        fakeUserPrefs.saveUserProfile(
            UserProfile(
                name = "Test Aspirant",
                dailyGoalHours = dailyGoalHours,
                streakDays = streak,
                xp = xp,
                targetExam = "JEE Advanced"
            )
        )
    }

    @Test
    fun testZeroGoalHandledSafely() = runBlocking {
        setUserProfile(dailyGoalHours = 0f)
        fakeRepository.todaySeconds = 3600L // 60 mins studied

        val result = useCase(testZone)

        assertEquals(0, result.targetMinutes)
        assertEquals(60, result.todayFocusMinutes)
        assertEquals(0f, result.progressPercent, 0.001f)
        assertFalse(result.hasGoal)
        assertFalse(result.isGoalCompleted)
        assertFalse(result.isGoalExceeded)
    }

    @Test
    fun testPartialProgressCalculation() = runBlocking {
        setUserProfile(dailyGoalHours = 2.0f) // 120 mins target
        fakeRepository.todaySeconds = 3600L // 60 mins studied

        val result = useCase(testZone)

        assertEquals(120, result.targetMinutes)
        assertEquals(60, result.todayFocusMinutes)
        assertEquals(0.5f, result.progressPercent, 0.001f)
        assertTrue(result.hasGoal)
        assertFalse(result.isGoalCompleted)
        assertFalse(result.isGoalExceeded)
    }

    @Test
    fun testGoalExactlyCompleted() = runBlocking {
        setUserProfile(dailyGoalHours = 2.0f) // 120 mins target
        fakeRepository.todaySeconds = 7200L // 120 mins studied

        val result = useCase(testZone)

        assertEquals(120, result.targetMinutes)
        assertEquals(120, result.todayFocusMinutes)
        assertEquals(1.0f, result.progressPercent, 0.001f)
        assertTrue(result.hasGoal)
        assertTrue(result.isGoalCompleted)
        assertFalse(result.isGoalExceeded)
    }

    @Test
    fun testGoalExceeded() = runBlocking {
        setUserProfile(dailyGoalHours = 2.0f) // 120 mins target
        fakeRepository.todaySeconds = 10800L // 180 mins studied

        val result = useCase(testZone)

        assertEquals(120, result.targetMinutes)
        assertEquals(180, result.todayFocusMinutes)
        assertEquals(1.5f, result.progressPercent, 0.001f)
        assertTrue(result.hasGoal)
        assertTrue(result.isGoalCompleted)
        assertTrue(result.isGoalExceeded)
    }

    @Test
    fun testNoActivityDashboard() = runBlocking {
        setUserProfile(dailyGoalHours = 3.0f)
        fakeRepository.todaySeconds = 0L
        fakeRepository.mostRecentDeck = null
        fakeRepository.totalDueFlashcards = 0

        val result = useCase(testZone)

        assertEquals(0, result.todayFocusMinutes)
        assertEquals(0f, result.progressPercent, 0.001f)
        assertNull(result.jumpBackInItem)
        assertEquals(0, result.dueFlashcards.totalDueCount)
        assertEquals(7, result.weeklyProgress.size)
    }

    @Test
    fun testWeeklyMondayToSundayMapping() = runBlocking {
        setUserProfile(dailyGoalHours = 1.0f) // 60 mins daily target
        val today = LocalDate.now(testZone)
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        fakeRepository.weeklyMap = mapOf(
            1 to 60,  // Monday: 60 mins -> Goal met
            3 to 30,  // Wednesday: 30 mins -> Partial
            7 to 90   // Sunday: 90 mins -> Goal met
        )

        val result = useCase(testZone)

        assertEquals(7, result.weeklyProgress.size)

        val mondayProgress = result.weeklyProgress.find { it.dayOfWeek == 1 }!!
        assertEquals(60, mondayProgress.minutesStudied)
        assertTrue(mondayProgress.isGoalCompleted)
        assertEquals("M", mondayProgress.dayShort)

        val wednesdayProgress = result.weeklyProgress.find { it.dayOfWeek == 3 }!!
        assertEquals(30, wednesdayProgress.minutesStudied)
        assertFalse(wednesdayProgress.isGoalCompleted)
        assertEquals("W", wednesdayProgress.dayShort)

        val tuesdayProgress = result.weeklyProgress.find { it.dayOfWeek == 2 }!!
        assertEquals(0, tuesdayProgress.minutesStudied)
        assertFalse(tuesdayProgress.isGoalCompleted)

        val sundayProgress = result.weeklyProgress.find { it.dayOfWeek == 7 }!!
        assertEquals(90, sundayProgress.minutesStudied)
        assertTrue(sundayProgress.isGoalCompleted)
    }

    @Test
    fun testDueFlashcardsAndJumpBackIn() = runBlocking {
        setUserProfile(dailyGoalHours = 2.0f)
        fakeRepository.totalDueFlashcards = 14
        fakeRepository.mostRecentDeck = DeckItem(
            id = 101,
            title = "Thermodynamics & Heat",
            subject = "Physics",
            totalCards = 30,
            createdAt = System.currentTimeMillis()
        )
        fakeRepository.deckDueCount = 14

        val result = useCase(testZone)

        assertEquals(14, result.dueFlashcards.totalDueCount)
        assertEquals(101, result.dueFlashcards.primaryDeckId)
        assertEquals("Thermodynamics & Heat", result.dueFlashcards.primaryDeckTitle)

        assertNotNull(result.jumpBackInItem)
        assertEquals("Thermodynamics & Heat", result.jumpBackInItem?.title)
        assertEquals(14, result.jumpBackInItem?.dueCount)
        assertEquals(53, result.jumpBackInItem?.masteryPercent)
    }

    @Test
    fun testActiveSessionDetection() = runBlocking {
        setUserProfile(dailyGoalHours = 2.0f)
        fakeRepository.activeSessionFlow.value = ActiveSessionState(
            isActive = true,
            remainingSeconds = 1200,
            totalSeconds = 1500,
            subject = "Organic Chemistry"
        )

        val result = useCase(testZone)

        assertTrue(result.activeSession.isActive)
        assertEquals("Organic Chemistry", result.activeSession.subject)
        assertEquals(1200, result.activeSession.remainingSeconds)
    }
}
