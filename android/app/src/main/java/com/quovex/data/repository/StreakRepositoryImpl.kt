package com.quovex.data.repository

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.SessionDao
import com.quovex.data.local.dao.StreakDao
import com.quovex.data.local.dao.UserStatsDao
import com.quovex.data.local.entity.StreakEntity
import com.quovex.domain.model.CemeteryTombstone
import com.quovex.domain.model.StreakInfo
import com.quovex.domain.model.StreakMilestone
import com.quovex.domain.model.StreakProtectionResult
import com.quovex.domain.model.StreakStatus
import com.quovex.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepositoryImpl @Inject constructor(
    private val streakDao: StreakDao,
    private val sessionDao: SessionDao,
    private val userStatsDao: UserStatsDao,
    private val userPreferencesManager: UserPreferencesManager
) : StreakRepository {

    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    override fun getStreakInfo(): Flow<StreakInfo> {
        return userPreferencesManager.userProfile.map { profile ->
            val stats = userStatsDao.getUserStats()
            val currentStreak = profile.streakDays.coerceAtLeast(stats?.currentStreak ?: 1)
            val longestCemetery = streakDao.getLongestCemeteryStreak() ?: 0
            val longestStreak = maxOf(currentStreak, stats?.longestStreak ?: 1, longestCemetery)

            // Determine milestone target
            val (nextMilestone, progress) = calculateMilestoneProgress(currentStreak)

            // Determine streak health status
            val status = when {
                profile.rescueTokens > 0 -> StreakStatus.ACTIVE
                currentStreak > 0 -> StreakStatus.AT_RISK
                else -> StreakStatus.FROZEN
            }

            StreakInfo(
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                rescueTokens = profile.rescueTokens,
                isProtectedToday = profile.rescueTokens > 0,
                streakStatus = status,
                nextMilestoneDays = nextMilestone,
                milestoneProgress = progress,
                daysUntilRisk = 1,
                canUseRescueToken = profile.rescueTokens > 0
            )
        }
    }

    override fun getBrokenStreaks(): Flow<List<CemeteryTombstone>> {
        return streakDao.getAllBrokenStreaks().map { entities ->
            entities.map { entity ->
                val startStr = dateFormat.format(Date(entity.startDate))
                val endStr = dateFormat.format(Date(entity.endDate))
                CemeteryTombstone(
                    id = entity.id,
                    streakLength = entity.streakDays,
                    startDate = entity.startDate,
                    endDate = entity.endDate,
                    dateRangeFormatted = "$startStr – $endStr",
                    causeOfDeath = entity.causeOfDeath,
                    reflectionNote = entity.reflectionNote,
                    tokensUsed = entity.tokensUsed
                )
            }
        }
    }

    override suspend fun spendRescueTokenToProtectToday(): StreakProtectionResult {
        val currentProfile = userPreferencesManager.userProfile.value
        if (currentProfile.rescueTokens <= 0) {
            return StreakProtectionResult(
                isSuccess = false,
                message = "No Streak Rescue Tokens remaining. Study consistently for 7 days to earn a new token!",
                remainingTokens = 0
            )
        }

        val success = userPreferencesManager.spendRescueToken()
        return if (success) {
            val updatedTokens = userPreferencesManager.userProfile.value.rescueTokens
            StreakProtectionResult(
                isSuccess = true,
                message = "Streak Protected! You redeemed 1 Rescue Token to preserve your streak.",
                remainingTokens = updatedTokens
            )
        } else {
            StreakProtectionResult(
                isSuccess = false,
                message = "Could not redeem token. Please try again.",
                remainingTokens = currentProfile.rescueTokens
            )
        }
    }

    override suspend fun logCemeteryReflection(streakId: Long, note: String): Boolean {
        val updatedRows = streakDao.updateReflection(streakId, note)
        return updatedRows > 0
    }

    override fun getMilestones(currentStreak: Int): List<StreakMilestone> {
        val list = listOf(
            StreakMilestone(
                milestoneDays = 7,
                title = "Week of Fire",
                description = "7 consecutive days of laser focus",
                iconEmoji = "🔥",
                xpBonus = 500,
                isUnlocked = currentStreak >= 7,
                isCurrentTarget = currentStreak < 7
            ),
            StreakMilestone(
                milestoneDays = 30,
                title = "Iron Discipline",
                description = "30 days of unbroken consistency",
                iconEmoji = "⚡",
                xpBonus = 2000,
                isUnlocked = currentStreak >= 30,
                isCurrentTarget = currentStreak in 7..29
            ),
            StreakMilestone(
                milestoneDays = 100,
                title = "Centurion Scholar",
                description = "100-day relentless academic mastery",
                iconEmoji = "👑",
                xpBonus = 10000,
                isUnlocked = currentStreak >= 100,
                isCurrentTarget = currentStreak in 30..99
            ),
            StreakMilestone(
                milestoneDays = 365,
                title = "Immortal Focus",
                description = "1 full year of transcendent study",
                iconEmoji = "🏛️",
                xpBonus = 50000,
                isUnlocked = currentStreak >= 365,
                isCurrentTarget = currentStreak in 100..364
            )
        )
        return list
    }

    override suspend fun recordBrokenStreakIfMissed(): Boolean {
        val profile = userPreferencesManager.userProfile.value
        if (profile.streakDays > 1) {
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val startDate = now - (profile.streakDays.toLong() * dayMillis)

            streakDao.insertStreak(
                StreakEntity(
                    streakDays = profile.streakDays,
                    startDate = startDate,
                    endDate = now,
                    isBroken = true,
                    causeOfDeath = "Missed daily focus goal",
                    reflectionNote = null,
                    tokensUsed = 0
                )
            )

            // Reset current streak to 1 in profile
            userPreferencesManager.saveUserProfile(profile.copy(streakDays = 1))
            return true
        }
        return false
    }

    private fun calculateMilestoneProgress(current: Int): Pair<Int, Float> {
        return when {
            current < 7 -> 7 to (current / 7f).coerceIn(0f, 1f)
            current < 30 -> 30 to ((current - 7) / 23f).coerceIn(0f, 1f)
            current < 100 -> 100 to ((current - 30) / 70f).coerceIn(0f, 1f)
            else -> 365 to ((current - 100) / 265f).coerceIn(0f, 1f)
        }
    }
}
