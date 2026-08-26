package com.quovex.domain.model

/**
 * Scholar progression ranks according to the PRD RPG Progression System (Module F4).
 */
enum class ScholarRank(
    val level: Int,
    val title: String,
    val minXp: Long,
    val maxXp: Long,
    val badgeIconName: String
) {
    NOVICE(1, "Novice Scholar", 0L, 499L, "ic_scholar_novice"),
    APPRENTICE(2, "Apprentice", 500L, 1499L, "ic_scholar_apprentice"),
    SCHOLAR(3, "Scholar", 1500L, 3499L, "ic_scholar_scholar"),
    EXPERT(4, "Expert", 3500L, 7499L, "ic_scholar_expert"),
    MASTER(5, "Grandmaster", 7500L, Long.MAX_VALUE, "ic_scholar_master");

    companion object {
        fun fromXp(xp: Long): ScholarRank {
            return when {
                xp < 500L -> NOVICE
                xp < 1500L -> APPRENTICE
                xp < 3500L -> SCHOLAR
                xp < 7500L -> EXPERT
                else -> MASTER
            }
        }

        fun fromLevel(level: Int): ScholarRank {
            return entries.firstOrNull { it.level == level } ?: NOVICE
        }
    }
}

/**
 * Comprehensive details regarding the user's RPG level and progress towards the next milestone.
 */
data class ScholarLevelInfo(
    val rank: ScholarRank,
    val currentXp: Long,
    val xpInCurrentLevel: Long,
    val xpRequiredForNextLevel: Long,
    val progressPercent: Float,
    val nextRank: ScholarRank?
)
