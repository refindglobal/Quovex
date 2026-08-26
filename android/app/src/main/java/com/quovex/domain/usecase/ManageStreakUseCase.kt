package com.quovex.domain.usecase

import com.quovex.domain.model.CemeteryTombstone
import com.quovex.domain.model.StreakInfo
import com.quovex.domain.model.StreakMilestone
import com.quovex.domain.model.StreakProtectionResult
import com.quovex.domain.repository.StreakRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageStreakUseCase @Inject constructor(
    private val streakRepository: StreakRepository
) {
    fun getStreakInfo(): Flow<StreakInfo> {
        return streakRepository.getStreakInfo()
    }

    fun getCemeteryTombstones(): Flow<List<CemeteryTombstone>> {
        return streakRepository.getBrokenStreaks()
    }

    suspend fun spendRescueToken(): StreakProtectionResult {
        return streakRepository.spendRescueTokenToProtectToday()
    }

    fun getMilestones(currentStreak: Int): List<StreakMilestone> {
        return streakRepository.getMilestones(currentStreak)
    }

    suspend fun checkAndRecordMissedStreaks(): Boolean {
        return streakRepository.recordBrokenStreakIfMissed()
    }
}
