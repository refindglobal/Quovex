package com.quovex.domain.usecase

import com.quovex.domain.repository.StreakRepository
import javax.inject.Inject

class LogStreakReflectionUseCase @Inject constructor(
    private val streakRepository: StreakRepository
) {
    suspend operator fun invoke(streakId: Long, note: String): Boolean {
        if (streakId <= 0 || note.isBlank()) return false
        return streakRepository.logCemeteryReflection(streakId, note.trim())
    }
}
