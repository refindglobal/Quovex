package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.entity.UserStatsEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class UseRescueTokenUseCaseTest {

    private lateinit var fakeDao: FakeUserStatsDao
    private lateinit var fakeUserPrefs: UserPreferencesManager
    private lateinit var calculateStreakUseCase: CalculateStreakUseCase
    private lateinit var useCase: UseRescueTokenUseCase
    private val zoneId = ZoneId.of("UTC")

    @Before
    fun setUp() {
        fakeDao = FakeUserStatsDao()
        fakeUserPrefs = UserPreferencesManager(null)
        calculateStreakUseCase = CalculateStreakUseCase(fakeDao, fakeUserPrefs)
        useCase = UseRescueTokenUseCase(fakeDao, calculateStreakUseCase)
    }

    @Test
    fun `spending rescue token decreases tokens and protects streak`() = runBlocking {
        val today = LocalDate.of(2026, 8, 25)
        val twoDaysAgo = today.minusDays(2)
        val twoDaysAgoMillis = twoDaysAgo.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

        fakeDao.insertOrUpdate(
            UserStatsEntity(id = 1, currentStreak = 14, longestStreak = 14, rescueTokens = 1, lastStudyDateMillis = twoDaysAgoMillis)
        )

        val result = useCase(currentTimeMillis = todayMillis, zoneId = zoneId)

        assertEquals(0, result.rescueTokens)
        assertEquals(15, result.currentStreak)
        assertTrue(result.isStreakActiveToday)
    }
}
