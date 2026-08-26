package com.quovex.domain.usecase

import com.quovex.data.local.dao.UserStatsDao
import com.quovex.domain.model.StreakInfo
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

/**
 * Consumes 1 Streak Rescue Token to protect the student's study streak after a missed day.
 */
class UseRescueTokenUseCase @Inject constructor(
    private val userStatsDao: UserStatsDao,
    private val calculateStreakUseCase: CalculateStreakUseCase
) {
    suspend operator fun invoke(
        currentTimeMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): StreakInfo {
        val stats = userStatsDao.getUserStats() ?: return calculateStreakUseCase(currentTimeMillis, zoneId)

        if (stats.rescueTokens > 0) {
            val newTokens = stats.rescueTokens - 1
            userStatsDao.updateRescueTokens(newTokens)

            // Bridge yesterday's date
            val yesterdayMillis = currentTimeMillis - (24 * 60 * 60 * 1000L)
            userStatsDao.updateStreak(stats.currentStreak, stats.longestStreak, yesterdayMillis)
        }

        return calculateStreakUseCase(currentTimeMillis, zoneId, recordStudyActivity = true)
    }
}
