package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.SessionStateManager
import com.quovex.domain.model.FocusMode
import com.quovex.domain.model.SessionStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartAndEndFocusSessionUseCaseTest {

    private lateinit var sessionStateManager: SessionStateManager
    private lateinit var fakeRepository: FakeQuovexRepository
    private lateinit var fakeUserStatsDao: FakeUserStatsDao
    private lateinit var fakeUserPrefs: UserPreferencesManager
    private lateinit var awardXpUseCase: AwardXpUseCase
    private lateinit var calculateStreakUseCase: CalculateStreakUseCase
    private lateinit var startUseCase: StartFocusSessionUseCase
    private lateinit var endUseCase: EndFocusSessionUseCase

    @Before
    fun setUp() {
        sessionStateManager = SessionStateManager()
        fakeRepository = FakeQuovexRepository()
        fakeUserStatsDao = FakeUserStatsDao()
        fakeUserPrefs = UserPreferencesManager(null)
        awardXpUseCase = AwardXpUseCase(fakeUserStatsDao, fakeUserPrefs)
        calculateStreakUseCase = CalculateStreakUseCase(fakeUserStatsDao, fakeUserPrefs)
        startUseCase = StartFocusSessionUseCase(sessionStateManager)
        endUseCase = EndFocusSessionUseCase(sessionStateManager, fakeRepository, awardXpUseCase, calculateStreakUseCase)
    }

    @Test
    fun `startFocusSession validates empty subject`() {
        val result = startUseCase(
            subject = "   ",
            mode = FocusMode.Pomodoro,
            strictFocusEnabled = true
        )

        assertTrue(result.isFailure)
        assertEquals("Subject cannot be empty", result.exceptionOrNull()?.message)
    }

    @Test
    fun `startFocusSession initializes state with absolute timestamps`() {
        val now = 1000000L
        val result = startUseCase(
            subject = "Physics",
            mode = FocusMode.Pomodoro,
            strictFocusEnabled = true,
            startTimeMillis = now
        )

        assertTrue(result.isSuccess)
        val state = sessionStateManager.activeSession.value
        assertTrue(state.isActive)
        assertEquals("Physics", state.subject)
        assertEquals(25 * 60, state.totalSeconds)
        assertEquals(25 * 60, state.remainingSeconds)
        assertEquals(now, state.startedAtMillis)
        assertEquals(now + (25 * 60 * 1000L), state.endTimeMillis)
        assertEquals(SessionStatus.RUNNING, state.status)
        assertTrue(state.strictFocusEnabled)
    }

    @Test
    fun `endFocusSession marks completed and records session in repository`() = runTest {
        val start = 1000000L
        startUseCase("Chemistry", FocusMode.DeepWork, false, start)

        val end = start + (50 * 60 * 1000L)
        val summary = endUseCase(isCompleted = true, endTimeMillis = end)

        assertTrue(summary.isCompleted)
        assertEquals(50, summary.actualDurationMinutes)
        assertEquals(50, summary.plannedDurationMinutes)
        assertEquals("Chemistry", summary.subject)
        assertFalse(sessionStateManager.activeSession.value.isActive)
        assertEquals(SessionStatus.COMPLETED, sessionStateManager.activeSession.value.status)
    }

    @Test
    fun `endFocusSession marks cancelled when ended early`() = runTest {
        val start = 1000000L
        startUseCase("Maths", FocusMode.Pomodoro, true, start)

        val end = start + (10 * 60 * 1000L) // only 10 mins elapsed
        val summary = endUseCase(isCompleted = false, endTimeMillis = end)

        assertFalse(summary.isCompleted)
        assertEquals(10, summary.actualDurationMinutes)
        assertEquals(25, summary.plannedDurationMinutes)
        assertEquals(SessionStatus.CANCELLED, sessionStateManager.activeSession.value.status)
    }

    @Test
    fun `SessionStateManager atomic completion claim works exactly once`() {
        sessionStateManager.startSession("Physics", "Pomodoro", 25, true)

        assertTrue(sessionStateManager.tryClaimCompletion())
        assertFalse(sessionStateManager.tryClaimCompletion()) // second attempt rejected
    }

    @Test
    fun `SessionStateManager updateTick derives remaining seconds accurately`() {
        val start = 1000000L
        val durationMinutes = 25
        sessionStateManager.startSession("Physics", "Pomodoro", durationMinutes, true, start, start + (25 * 60 * 1000L))

        // 10 minutes in -> 15 minutes remaining (900 seconds)
        val remaining = sessionStateManager.updateTick(start + (10 * 60 * 1000L))
        assertEquals(900, remaining)
    }
}
