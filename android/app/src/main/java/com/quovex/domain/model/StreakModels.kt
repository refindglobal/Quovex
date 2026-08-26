package com.quovex.domain.model

enum class StreakStatus {
    ACTIVE,
    AT_RISK,
    PROTECTED,
    FROZEN
}

data class CemeteryTombstone(
    val id: Long,
    val streakLength: Int,
    val startDate: Long,
    val endDate: Long,
    val dateRangeFormatted: String,
    val causeOfDeath: String,
    val reflectionNote: String?,
    val tokensUsed: Int
)

data class StreakMilestone(
    val milestoneDays: Int,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val xpBonus: Int,
    val isUnlocked: Boolean,
    val isCurrentTarget: Boolean
)

data class StreakProtectionResult(
    val isSuccess: Boolean,
    val message: String,
    val remainingTokens: Int
)
