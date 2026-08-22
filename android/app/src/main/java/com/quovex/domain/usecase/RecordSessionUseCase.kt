package com.quovex.domain.usecase

import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class RecordSessionUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    suspend operator fun invoke(
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        focusScore: Int,
        appBlockViolations: Int = 0
    ): Long {
        return repository.recordSession(
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            focusScore = focusScore,
            appBlockViolations = appBlockViolations
        )
    }
}
