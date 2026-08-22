package com.quovex.data.local

import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.SessionStatus
import com.quovex.domain.model.SessionSummary
import com.quovex.domain.util.FocusTimerEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStateManager @Inject constructor() {

    private val _activeSession = MutableStateFlow(ActiveSessionState())
    val activeSession: StateFlow<ActiveSessionState> = _activeSession.asStateFlow()

    private val _latestSummary = MutableStateFlow<SessionSummary?>(null)
    val latestSummary: StateFlow<SessionSummary?> = _latestSummary.asStateFlow()

    private val isCompletingOrStopping = AtomicBoolean(false)

    /**
     * Initializes a new running focus session with absolute timestamps.
     */
    fun startSession(
        subject: String,
        modeName: String,
        durationMinutes: Int,
        strictFocusEnabled: Boolean,
        startTimeMillis: Long = System.currentTimeMillis(),
        endTimeMillis: Long = startTimeMillis + (durationMinutes * 60 * 1000L)
    ) {
        isCompletingOrStopping.set(false)
        _latestSummary.value = null
        val totalSecs = durationMinutes * 60

        _activeSession.value = ActiveSessionState(
            isActive = true,
            subject = subject,
            modeName = modeName,
            totalSeconds = totalSecs,
            remainingSeconds = totalSecs,
            startedAtMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
            strictFocusEnabled = strictFocusEnabled,
            status = SessionStatus.RUNNING
        )
    }

    /**
     * Updates the remaining time based on the absolute scheduled end timestamp.
     * Guaranteed to never produce negative time.
     */
    fun updateTick(currentTimeMillis: Long = System.currentTimeMillis()): Int {
        val current = _activeSession.value
        if (!current.isActive || current.status != SessionStatus.RUNNING) return current.remainingSeconds

        val remaining = FocusTimerEngine.calculateRemainingSeconds(current.endTimeMillis, currentTimeMillis)
        _activeSession.value = current.copy(remainingSeconds = remaining)
        return remaining
    }

    /**
     * Attempts to atomically claim completion, ensuring completion logic executes exactly once.
     */
    fun tryClaimCompletion(): Boolean {
        return isCompletingOrStopping.compareAndSet(false, true)
    }

    /**
     * Marks the session as completed and publishes the factual session summary.
     */
    fun markCompleted(endTimeMillis: Long = System.currentTimeMillis()): SessionSummary {
        val current = _activeSession.value
        val actualMinutes = FocusTimerEngine.calculateElapsedMinutes(current.startedAtMillis, endTimeMillis)
            .coerceAtLeast(current.totalSeconds / 60)

        val summary = SessionSummary(
            subject = current.subject,
            modeName = current.modeName,
            plannedDurationMinutes = current.totalSeconds / 60,
            actualDurationMinutes = actualMinutes,
            startTimeMillis = current.startedAtMillis,
            endTimeMillis = endTimeMillis,
            isCompleted = true,
            strictFocusEnabled = current.strictFocusEnabled
        )

        _latestSummary.value = summary
        _activeSession.value = current.copy(
            isActive = false,
            remainingSeconds = 0,
            status = SessionStatus.COMPLETED
        )
        return summary
    }

    /**
     * Marks the session as ended early / cancelled and publishes the factual session summary.
     */
    fun markCancelled(endTimeMillis: Long = System.currentTimeMillis()): SessionSummary {
        val current = _activeSession.value
        val actualMinutes = FocusTimerEngine.calculateElapsedMinutes(current.startedAtMillis, endTimeMillis)

        val summary = SessionSummary(
            subject = current.subject,
            modeName = current.modeName,
            plannedDurationMinutes = current.totalSeconds / 60,
            actualDurationMinutes = actualMinutes,
            startTimeMillis = current.startedAtMillis,
            endTimeMillis = endTimeMillis,
            isCompleted = false,
            strictFocusEnabled = current.strictFocusEnabled
        )

        _latestSummary.value = summary
        _activeSession.value = current.copy(
            isActive = false,
            status = SessionStatus.CANCELLED
        )
        return summary
    }

    /**
     * Backward-compatible helper for legacy callers.
     */
    fun updateActiveSession(
        isActive: Boolean,
        remainingSeconds: Int,
        totalSeconds: Int,
        subject: String
    ) {
        val current = _activeSession.value
        _activeSession.value = current.copy(
            isActive = isActive,
            remainingSeconds = remainingSeconds,
            totalSeconds = totalSeconds,
            subject = subject,
            status = if (isActive) SessionStatus.RUNNING else SessionStatus.IDLE
        )
    }

    fun clearSession() {
        isCompletingOrStopping.set(false)
        _activeSession.value = ActiveSessionState()
    }

    fun clearSummary() {
        _latestSummary.value = null
    }
}
