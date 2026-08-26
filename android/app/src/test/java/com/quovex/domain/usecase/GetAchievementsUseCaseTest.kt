package com.quovex.domain.usecase

import com.quovex.data.local.entity.SessionEntity
import com.quovex.data.local.entity.UserStatsEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAchievementsUseCaseTest {

    private lateinit var fakeSessionDao: FakeSessionDao
    private lateinit var fakeFlashcardDao: FakeFlashcardDao
    private lateinit var fakeQuizDao: FakeQuizDao
    private lateinit var fakeUserStatsDao: FakeUserStatsDao
    private lateinit var useCase: GetAchievementsUseCase

    @Before
    fun setUp() {
        fakeSessionDao = FakeSessionDao()
        fakeFlashcardDao = FakeFlashcardDao()
        fakeQuizDao = FakeQuizDao()
        fakeUserStatsDao = FakeUserStatsDao()
        useCase = GetAchievementsUseCase(
            fakeSessionDao,
            fakeFlashcardDao,
            fakeQuizDao,
            fakeUserStatsDao
        )
    }

    @Test
    fun `initial user has no unlocked badges`() = runBlocking {
        val badges = useCase()

        assertEquals(7, badges.size)
        assertTrue(badges.all { !it.isUnlocked })
    }

    @Test
    fun `user unlocking first session and 7-day streak`() = runBlocking {
        fakeSessionDao.insertSession(
            SessionEntity(id = 1, startTime = 1000L, endTime = 2000L, durationMinutes = 25, focusScore = 0, appBlockViolations = 0)
        )
        fakeUserStatsDao.insertOrUpdate(
            UserStatsEntity(id = 1, currentStreak = 7, longestStreak = 7, rescueTokens = 1, totalXp = 500L, scholarLevel = 2)
        )

        val badges = useCase()

        val firstSession = badges.find { it.id == "FIRST_SESSION" }!!
        assertTrue(firstSession.isUnlocked)
        assertEquals("Unlocked", firstSession.progressText)

        val streak7 = badges.find { it.id == "STREAK_7" }!!
        assertTrue(streak7.isUnlocked)
        assertEquals("Unlocked", streak7.progressText)

        val streak30 = badges.find { it.id == "STREAK_30" }!!
        assertFalse(streak30.isUnlocked)
    }
}
