package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.repository.StreakRepositoryImpl
import com.quovex.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ManageStreakUseCaseTest {

    private lateinit var streakDao: FakeStreakDao
    private lateinit var sessionDao: FakeSessionDao
    private lateinit var userStatsDao: FakeUserStatsDao
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var streakRepository: StreakRepositoryImpl
    private lateinit var manageStreakUseCase: ManageStreakUseCase

    @Before
    fun setUp() {
        streakDao = FakeStreakDao()
        sessionDao = FakeSessionDao()
        userStatsDao = FakeUserStatsDao()
        userPreferencesManager = UserPreferencesManager(null)
        streakRepository = StreakRepositoryImpl(streakDao, sessionDao, userStatsDao, userPreferencesManager)
        manageStreakUseCase = ManageStreakUseCase(streakRepository)
    }

    @Test
    fun `getStreakInfo computes correct streak days and milestones`() = runBlocking {
        userPreferencesManager.saveUserProfile(
            UserProfile(
                streakDays = 14,
                rescueTokens = 2
            )
        )

        val info = manageStreakUseCase.getStreakInfo().first()

        assertEquals(14, info.currentStreak)
        assertEquals(2, info.rescueTokens)
        assertTrue(info.isProtectedToday)
        assertEquals(30, info.nextMilestoneDays)
        assertTrue(info.milestoneProgress > 0f)
    }

    @Test
    fun `spendRescueToken decrements token count when available`() = runBlocking {
        userPreferencesManager.saveUserProfile(
            UserProfile(
                streakDays = 5,
                rescueTokens = 1
            )
        )

        val result = manageStreakUseCase.spendRescueToken()

        assertTrue(result.isSuccess)
        assertEquals(0, result.remainingTokens)

        val profileAfter = userPreferencesManager.userProfile.value
        assertEquals(0, profileAfter.rescueTokens)

        // Attempting to spend when 0 tokens left returns failure
        val failResult = manageStreakUseCase.spendRescueToken()
        assertFalse(failResult.isSuccess)
    }

    @Test
    fun `getMilestones identifies unlocked vs locked achievements`() {
        val milestones = manageStreakUseCase.getMilestones(currentStreak = 14)

        assertEquals(4, milestones.size)
        // 7-day milestone should be unlocked
        val weekMilestone = milestones.find { it.milestoneDays == 7 }
        assertNotNull(weekMilestone)
        assertTrue(weekMilestone!!.isUnlocked)

        // 30-day milestone should be locked and current target
        val monthMilestone = milestones.find { it.milestoneDays == 30 }
        assertNotNull(monthMilestone)
        assertFalse(monthMilestone!!.isUnlocked)
        assertTrue(monthMilestone.isCurrentTarget)
    }
}
