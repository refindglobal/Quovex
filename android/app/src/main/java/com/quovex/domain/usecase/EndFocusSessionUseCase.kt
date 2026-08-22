package com.quovex.domain.usecase

import com.quovex.data.local.SessionStateManager
import com.quovex.domain.model.SessionSummary
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class EndFocusSessionUseCase @Inject constructor(
    private val sessionStateManager: SessionStateManager,
    private val repository: QuovexRepository
) {

    suspend operator fun invoke(
        isCompleted: Boolean,
        endTimeMillis: Long = System.currentTimeMillis()
    ): SessionSummary {
        val summary = if (isCompleted) {
            sessionStateManager.markCompleted(endTimeMillis)
        } else {
            sessionStateManager.markCancelled(endTimeMillis)
        }

        // Persist session record to Room database
        if (summary.actualDurationMinutes > 0 || isCompleted) {
            repository.recordSession(
                startTime = summary.startTimeMillis,
                endTime = summary.endTimeMillis,
                durationMinutes = summary.actualDurationMinutes,
                focusScore = 0, // 0 = not measured (no fake focus score)
                appBlockViolations = 0
            )
        }

        return summary
    }
}
