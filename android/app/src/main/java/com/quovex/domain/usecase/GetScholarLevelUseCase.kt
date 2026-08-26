package com.quovex.domain.usecase

import com.quovex.domain.model.ScholarLevelInfo
import com.quovex.domain.model.ScholarRank
import javax.inject.Inject

/**
 * Calculates the user's ScholarLevel progression based on cumulative XP.
 */
class GetScholarLevelUseCase @Inject constructor() {

    operator fun invoke(totalXp: Long): ScholarLevelInfo {
        val currentRank = ScholarRank.fromXp(totalXp)
        val nextRank = ScholarRank.entries.firstOrNull { it.level == currentRank.level + 1 }

        val xpInCurrentLevel = (totalXp - currentRank.minXp).coerceAtLeast(0L)
        val levelSpan = if (nextRank != null) (currentRank.maxXp - currentRank.minXp + 1L) else 10000L
        val xpRequiredForNextLevel = if (nextRank != null) (currentRank.maxXp - totalXp + 1L).coerceAtLeast(0L) else 0L

        val progressPercent = if (nextRank != null && levelSpan > 0) {
            (xpInCurrentLevel.toFloat() / levelSpan).coerceIn(0f, 1f)
        } else {
            1.0f
        }

        return ScholarLevelInfo(
            rank = currentRank,
            currentXp = totalXp,
            xpInCurrentLevel = xpInCurrentLevel,
            xpRequiredForNextLevel = xpRequiredForNextLevel,
            progressPercent = progressPercent,
            nextRank = nextRank
        )
    }
}
