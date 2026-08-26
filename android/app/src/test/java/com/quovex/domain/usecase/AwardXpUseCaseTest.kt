package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.entity.UserStatsEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AwardXpUseCaseTest {

    private lateinit var fakeDao: FakeUserStatsDao
    private lateinit var fakeUserPrefs: UserPreferencesManager
    private lateinit var useCase: AwardXpUseCase

    @Before
    fun setUp() {
        fakeDao = FakeUserStatsDao()
        fakeUserPrefs = UserPreferencesManager(null)
        useCase = AwardXpUseCase(fakeDao, fakeUserPrefs)
    }

    @Test
    fun `awarding XP increments total XP and syncs user preferences`() = runBlocking {
        fakeDao.insertOrUpdate(
            UserStatsEntity(id = 1, currentStreak = 1, longestStreak = 1, rescueTokens = 1, totalXp = 200L, scholarLevel = 1)
        )

        val updatedXp = useCase(150L)

        assertEquals(350L, updatedXp)
        val stats = fakeDao.getUserStats()
        assertEquals(350L, stats?.totalXp)
        assertEquals(1, stats?.scholarLevel)
    }

    @Test
    fun `awarding XP past 500 elevates scholar level`() = runBlocking {
        fakeDao.insertOrUpdate(
            UserStatsEntity(id = 1, currentStreak = 1, longestStreak = 1, rescueTokens = 1, totalXp = 450L, scholarLevel = 1)
        )

        val updatedXp = useCase(100L)

        assertEquals(550L, updatedXp)
        val stats = fakeDao.getUserStats()
        assertEquals(550L, stats?.totalXp)
        assertEquals(2, stats?.scholarLevel)
    }
}
