package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.entity.StreakEntity
import com.quovex.data.repository.StreakRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogStreakReflectionUseCaseTest {

    private lateinit var streakDao: FakeStreakDao
    private lateinit var sessionDao: FakeSessionDao
    private lateinit var userStatsDao: FakeUserStatsDao
    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var streakRepository: StreakRepositoryImpl
    private lateinit var logStreakReflectionUseCase: LogStreakReflectionUseCase

    @Before
    fun setUp() {
        streakDao = FakeStreakDao()
        sessionDao = FakeSessionDao()
        userStatsDao = FakeUserStatsDao()
        userPreferencesManager = UserPreferencesManager(null)
        streakRepository = StreakRepositoryImpl(streakDao, sessionDao, userStatsDao, userPreferencesManager)
        logStreakReflectionUseCase = LogStreakReflectionUseCase(streakRepository)
    }

    @Test
    fun `invoke updates reflection note on cemetery tombstone`() = runBlocking {
        val id = streakDao.insertStreak(
            StreakEntity(
                streakDays = 21,
                startDate = System.currentTimeMillis() - 86400000L * 21,
                endDate = System.currentTimeMillis(),
                isBroken = true,
                causeOfDeath = "Missed daily focus goal",
                reflectionNote = null,
                tokensUsed = 0
            )
        )

        val success = logStreakReflectionUseCase(id, "Burned out after physics mock test. Need to pace better.")
        assertTrue(success)

        val tombstones = streakRepository.getBrokenStreaks().first()
        assertEquals(1, tombstones.size)
        assertEquals("Burned out after physics mock test. Need to pace better.", tombstones[0].reflectionNote)
    }

    @Test
    fun `invoke fails for blank note or invalid id`() = runBlocking {
        assertFalse(logStreakReflectionUseCase(-1L, "Valid text"))
        assertFalse(logStreakReflectionUseCase(1L, "   "))
    }
}
