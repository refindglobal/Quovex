package com.quovex.domain.repository

import com.quovex.domain.model.FocusFrameResult
import com.quovex.domain.model.FocusTrackingState
import kotlinx.coroutines.flow.StateFlow

interface FocusDetectionRepository {
    /**
     * Observable stream of real-time camera tracking state, scores, and attentiveness.
     */
    val focusTrackingState: StateFlow<FocusTrackingState>

    /**
     * Enables or disables Camera AI focus detection for future sessions.
     */
    suspend fun setTrackingEnabled(enabled: Boolean)

    /**
     * Updates camera permission grant status.
     */
    fun updateCameraPermission(granted: Boolean)

    /**
     * Sets whether camera preview/analysis is currently actively streaming.
     */
    fun setCameraActive(active: Boolean)

    /**
     * Processes an analyzed frame result from ML Kit and updates attentiveness & scores.
     */
    fun processFrame(result: FocusFrameResult)

    /**
     * Resets tracking counters for a newly initiated focus session.
     */
    fun resetSession()
}
