package com.quovex.domain.usecase

import com.quovex.data.local.SessionStateManager
import com.quovex.domain.model.SessionSummary
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class EndFocusSessionUseCase @Inject constructor(
    private val sessionStateManager: SessionStateManager,
    private val repository: QuovexRepository,
    private val awardXpUseCase: AwardXpUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase
) {

    suspend operator fun invoke(
        isCompleted: Boolean,
        endTimeMillis: Long = System.currentTimeMillis(),
        focusScore: Int? = null,
        distractionsCount: Int = 0,
        drowsinessCount: Int = 0,
        cameraTrackingEnabled: Boolean = false
    ): SessionSummary {
        val summary = if (isCompleted) {
            sessionStateManager.markCompleted(
                endTimeMillis = endTimeMillis,
                focusScore = focusScore,
                distractionsCount = distractionsCount,
                drowsinessCount = drowsinessCount,
                cameraTrackingEnabled = cameraTrackingEnabled
            )
        } else {
            sessionStateManager.markCancelled(
                endTimeMillis = endTimeMillis,
                focusScore = focusScore,
                distractionsCount = distractionsCount,
                drowsinessCount = drowsinessCount,
                cameraTrackingEnabled = cameraTrackingEnabled
            )
        }

        // Persist session record to Room database and award XP
        if (summary.actualDurationMinutes > 0 || isCompleted) {
            repository.recordSession(
                startTime = summary.startTimeMillis,
                endTime = summary.endTimeMillis,
                durationMinutes = summary.actualDurationMinutes,
                focusScore = focusScore ?: 0,
                appBlockViolations = 0,
                subject = summary.subject
            )

            // Award XP: 2 XP per minute of focus + bonus 50 XP for laser camera focus score (>= 85)
            val baseBonus = (summary.actualDurationMinutes * 2L).coerceAtLeast(10L)
            val focusBonus = if (focusScore != null && focusScore >= 85) 50L else 0L
            awardXpUseCase(baseBonus + focusBonus)

            // Update streak
            calculateStreakUseCase(currentTimeMillis = endTimeMillis, recordStudyActivity = true)
        }

        return summary
    }
}
