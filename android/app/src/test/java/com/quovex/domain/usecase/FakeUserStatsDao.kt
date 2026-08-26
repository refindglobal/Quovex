package com.quovex.domain.usecase

import com.quovex.data.local.dao.UserStatsDao
import com.quovex.data.local.entity.UserStatsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserStatsDao : UserStatsDao {
    private val statsFlow = MutableStateFlow<UserStatsEntity?>(
        UserStatsEntity(id = 1, currentStreak = 1, longestStreak = 1, rescueTokens = 1, totalXp = 0L, scholarLevel = 1, lastStudyDateMillis = 0L)
    )

    override suspend fun insertOrUpdate(stats: UserStatsEntity) {
        statsFlow.value = stats
    }

    override suspend fun getUserStats(): UserStatsEntity? {
        return statsFlow.value
    }

    override fun observeUserStats(): Flow<UserStatsEntity?> {
        return statsFlow.asStateFlow()
    }

    override suspend fun updateStreak(streak: Int, longest: Int, lastDate: Long) {
        val current = statsFlow.value ?: UserStatsEntity()
        statsFlow.value = current.copy(currentStreak = streak, longestStreak = longest, lastStudyDateMillis = lastDate)
    }

    override suspend fun updateRescueTokens(tokens: Int) {
        val current = statsFlow.value ?: UserStatsEntity()
        statsFlow.value = current.copy(rescueTokens = tokens)
    }

    override suspend fun addXp(xpToAdd: Long, newLevel: Int) {
        val current = statsFlow.value ?: UserStatsEntity()
        statsFlow.value = current.copy(totalXp = current.totalXp + xpToAdd, scholarLevel = newLevel)
    }
}
