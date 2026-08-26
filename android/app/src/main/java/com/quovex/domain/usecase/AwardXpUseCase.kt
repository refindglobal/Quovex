package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.UserStatsDao
import com.quovex.data.local.entity.UserStatsEntity
import com.quovex.domain.model.ScholarRank
import javax.inject.Inject

/**
 * Awards XP to the student according to the PRD RPG progression rules (Module F4),
 * persisting the update to Room DB and local preferences.
 */
class AwardXpUseCase @Inject constructor(
    private val userStatsDao: UserStatsDao,
    private val userPreferencesManager: UserPreferencesManager
) {
    suspend operator fun invoke(xpGained: Long): Long {
        if (xpGained <= 0) return 0L

        var stats = userStatsDao.getUserStats()
        if (stats == null) {
            stats = UserStatsEntity(id = 1, currentStreak = 1, longestStreak = 1, rescueTokens = 1, totalXp = 0L, scholarLevel = 1)
            userStatsDao.insertOrUpdate(stats)
        }

        val updatedXp = stats.totalXp + xpGained
        val newRank = ScholarRank.fromXp(updatedXp)

        userStatsDao.addXp(xpGained, newRank.level)
        userPreferencesManager.addXpAndSession(focusMinutes = 0, gainedXp = xpGained.toInt())

        return updatedXp
    }
}
