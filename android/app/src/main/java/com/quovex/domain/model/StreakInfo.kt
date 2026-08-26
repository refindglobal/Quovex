package com.quovex.domain.model

/**
 * Domain representation of user streak status, rescue token availability, milestone progress, and cemetery telemetry.
 */
data class StreakInfo(
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val rescueTokens: Int = 1,
    val isProtectedToday: Boolean = false,
    val streakStatus: StreakStatus = StreakStatus.ACTIVE,
    val nextMilestoneDays: Int = 7,
    val milestoneProgress: Float = 0.14f,
    val daysUntilRisk: Int = 1,
    val lastStudyDateMillis: Long = 0L,
    val isStreakActiveToday: Boolean = false,
    val canUseRescueToken: Boolean = false,
    val milestoneTitle: String? = null
) {
    val displayStreakText: String get() = "$currentStreak Day Streak"

    /**
     * Determines milestone recognition based on current streak count (7, 30, 100, 365 days).
     */
    fun calculateMilestoneBadge(): String? {
        return when (currentStreak) {
            in 7..29 -> "🔥 7-Day Flame"
            in 30..99 -> "⚡ 30-Day Master"
            in 100..364 -> "🏆 100-Day Legend"
            in 365..Int.MAX_VALUE -> "👑 365-Day Grandmaster"
            else -> null
        }
    }
}
