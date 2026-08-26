package com.quovex.domain.model

/**
 * Achievement badge data model representing unlocked or pending milestones.
 */
data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: String, // "STREAK", "MASTERY", "HOURS", "QUIZ"
    val isUnlocked: Boolean,
    val progress: Float = 0f, // 0.0f to 1.0f
    val progressText: String = "",
    val unlockedAtMillis: Long? = null
)
