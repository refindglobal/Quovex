package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.UserStatsDao
import com.quovex.data.local.entity.UserStatsEntity
import com.quovex.domain.model.StreakInfo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Calculates current streak status, gap detection, and rescue token availability
 * according to the Anti-Duolingo streak system rules (PRD Module F1).
 */
class CalculateStreakUseCase @Inject constructor(
    private val userStatsDao: UserStatsDao,
    private val userPreferencesManager: UserPreferencesManager
) {
    suspend operator fun invoke(
        currentTimeMillis: Long = System.currentTimeMillis(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        recordStudyActivity: Boolean = false
    ): StreakInfo {
        var stats = userStatsDao.getUserStats()
        if (stats == null) {
            stats = UserStatsEntity(
                id = 1,
                currentStreak = 1,
                longestStreak = 1,
                rescueTokens = 1,
                totalXp = 0L,
                scholarLevel = 1,
                lastStudyDateMillis = currentTimeMillis
            )
            userStatsDao.insertOrUpdate(stats)
        }

        val today = Instant.ofEpochMilli(currentTimeMillis).atZone(zoneId).toLocalDate()
        val lastDate = if (stats.lastStudyDateMillis > 0) {
            Instant.ofEpochMilli(stats.lastStudyDateMillis).atZone(zoneId).toLocalDate()
        } else {
            today
        }

        val daysDifference = ChronoUnit.DAYS.between(lastDate, today)

        var currentStreak = stats.currentStreak
        var longestStreak = stats.longestStreak
        var rescueTokens = stats.rescueTokens
        var isStreakActiveToday = (daysDifference == 0L && stats.lastStudyDateMillis > 0)
        var canUseRescueToken = false

        if (recordStudyActivity) {
            if (daysDifference == 0L) {
                // Already studied today
                isStreakActiveToday = true
            } else if (daysDifference == 1L) {
                // Studied yesterday -> increment streak!
                currentStreak += 1
                if (currentStreak > longestStreak) {
                    longestStreak = currentStreak
                }
                isStreakActiveToday = true
                userStatsDao.updateStreak(currentStreak, longestStreak, currentTimeMillis)
            } else if (daysDifference == 2L && rescueTokens > 0) {
                // Missed yesterday, but has a rescue token!
                canUseRescueToken = true
            } else {
                // Streak broken, reset to 1
                currentStreak = 1
                isStreakActiveToday = true
                userStatsDao.updateStreak(currentStreak, longestStreak, currentTimeMillis)
            }
        } else {
            // Read-only evaluation
            if (daysDifference > 1L) {
                if (daysDifference == 2L && rescueTokens > 0) {
                    canUseRescueToken = true
                } else if (daysDifference > 2L) {
                    currentStreak = 0
                }
            }
        }

        // Sync with UserPreferencesManager
        val profile = userPreferencesManager.userProfile.value
        if (profile.streakDays != currentStreak) {
            userPreferencesManager.saveUserProfile(profile.copy(streakDays = currentStreak))
        }

        val streakInfo = StreakInfo(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            rescueTokens = rescueTokens,
            lastStudyDateMillis = stats.lastStudyDateMillis,
            isStreakActiveToday = isStreakActiveToday,
            canUseRescueToken = canUseRescueToken
        )

        return streakInfo.copy(milestoneTitle = streakInfo.calculateMilestoneBadge())
    }
}
