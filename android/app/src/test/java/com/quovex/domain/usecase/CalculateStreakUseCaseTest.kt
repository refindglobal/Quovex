package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.entity.UserStatsEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class CalculateStreakUseCaseTest {

    private lateinit var fakeDao: FakeUserStatsDao
    private lateinit var fakeUserPrefs: UserPreferencesManager
    private lateinit var useCase: CalculateStreakUseCase
    private val zoneId = ZoneId.of("UTC")

    @Before
    fun setUp() {
        fakeDao = FakeUserStatsDao()
        fakeUserPrefs = UserPreferencesManager(null)
        useCase = CalculateStreakUseCase(fakeDao, fakeUserPrefs)
    }

    @Test
    fun `when user studies today, streak continues active`() = runBlocking {
        val now = System.currentTimeMillis()
        fakeDao.insertOrUpdate(
            UserStatsEntity(id = 1, currentStreak = 5, longestStreak = 5, rescueTokens = 1, lastStudyDateMillis = now)
        )

        val result = useCase(currentTimeMillis = now, zoneId = zoneId, recordStudyActivity = false)

        assertEquals(5, result.currentStreak)
        assertTrue(result.isStreakActiveToday)
        assertFalse(result.canUseRescueToken)
    }

    @Test
    fun `when user studies after studying yesterday, streak increments by 1`() = runBlocking {
        val today = LocalDate.of(2026, 8, 25)
        val yesterday = today.minusDays(1)
        val yesterdayMillis = yesterday.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

        fakeDao.insertOrUpdate(
            UserStatsEntity(id = 1, currentStreak = 6, longestStreak = 6, rescueTokens = 1, lastStudyDateMillis = yesterdayMillis)
        )

        val result = useCase(currentTimeMillis = todayMillis, zoneId = zoneId, recordStudyActivity = true)

        assertEquals(7, result.currentStreak)
        assertEquals(7, result.longestStreak)
        assertEquals("🔥 7-Day Flame", result.milestoneTitle)
        assertTrue(result.isStreakActiveToday)
    }

    @Test
    fun `when user missed 1 day and has rescue token, canUseRescueToken is true`() = runBlocking {
        val today = LocalDate.of(2026, 8, 25)
        val twoDaysAgo = today.minusDays(2)
        val twoDaysAgoMillis = twoDaysAgo.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

        fakeDao.insertOrUpdate(
            UserStatsEntity(id = 1, currentStreak = 10, longestStreak = 10, rescueTokens = 1, lastStudyDateMillis = twoDaysAgoMillis)
        )

        val result = useCase(currentTimeMillis = todayMillis, zoneId = zoneId, recordStudyActivity = false)

        assertTrue(result.canUseRescueToken)
    }

    @Test
    fun `when user missed 3 days, streak resets`() = runBlocking {
        val today = LocalDate.of(2026, 8, 25)
        val fourDaysAgo = today.minusDays(4)
        val fourDaysAgoMillis = fourDaysAgo.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayMillis = today.atStartOfDay(zoneId).toInstant().toEpochMilli()

        fakeDao.insertOrUpdate(
            UserStatsEntity(id = 1, currentStreak = 12, longestStreak = 12, rescueTokens = 1, lastStudyDateMillis = fourDaysAgoMillis)
        )

        val result = useCase(currentTimeMillis = todayMillis, zoneId = zoneId, recordStudyActivity = true)

        assertEquals(1, result.currentStreak)
        assertEquals(12, result.longestStreak)
        assertTrue(result.isStreakActiveToday)
    }
}
