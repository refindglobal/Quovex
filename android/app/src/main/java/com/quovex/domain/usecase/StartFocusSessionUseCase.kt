package com.quovex.domain.usecase

import com.quovex.data.local.SessionStateManager
import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.FocusMode
import javax.inject.Inject

class StartFocusSessionUseCase @Inject constructor(
    private val sessionStateManager: SessionStateManager
) {

    operator fun invoke(
        subject: String,
        mode: FocusMode,
        strictFocusEnabled: Boolean,
        startTimeMillis: Long = System.currentTimeMillis()
    ): Result<ActiveSessionState> {
        val trimmedSubject = subject.trim()
        if (trimmedSubject.isBlank()) {
            return Result.failure(IllegalArgumentException("Subject cannot be empty"))
        }

        val durationMinutes = mode.focusDurationMinutes
        if (durationMinutes <= 0) {
            return Result.failure(IllegalArgumentException("Duration must be greater than 0"))
        }

        val endTimeMillis = startTimeMillis + (durationMinutes * 60 * 1000L)
        sessionStateManager.startSession(
            subject = trimmedSubject,
            modeName = mode.title,
            durationMinutes = durationMinutes,
            strictFocusEnabled = strictFocusEnabled,
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis
        )

        return Result.success(sessionStateManager.activeSession.value)
    }
}
