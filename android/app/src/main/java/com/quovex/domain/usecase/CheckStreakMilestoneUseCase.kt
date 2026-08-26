package com.quovex.domain.usecase

import com.quovex.domain.model.StreakMilestone
import com.quovex.domain.repository.StreakRepository
import javax.inject.Inject

class CheckStreakMilestoneUseCase @Inject constructor(
    private val streakRepository: StreakRepository
) {
    operator fun invoke(currentStreak: Int): List<StreakMilestone> {
        return streakRepository.getMilestones(currentStreak)
    }
}
