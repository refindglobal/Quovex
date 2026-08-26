package com.quovex.domain.repository

import com.quovex.domain.model.CemeteryTombstone
import com.quovex.domain.model.StreakInfo
import com.quovex.domain.model.StreakMilestone
import com.quovex.domain.model.StreakProtectionResult
import kotlinx.coroutines.flow.Flow

interface StreakRepository {
    fun getStreakInfo(): Flow<StreakInfo>
    fun getBrokenStreaks(): Flow<List<CemeteryTombstone>>
    suspend fun spendRescueTokenToProtectToday(): StreakProtectionResult
    suspend fun logCemeteryReflection(streakId: Long, note: String): Boolean
    fun getMilestones(currentStreak: Int): List<StreakMilestone>
    suspend fun recordBrokenStreakIfMissed(): Boolean
}
