package com.quovex.domain.usecase

import com.quovex.domain.model.FocusFrameResult
import com.quovex.domain.repository.FocusDetectionRepository
import javax.inject.Inject

class ControlFocusDetectionUseCase @Inject constructor(
    private val focusDetectionRepository: FocusDetectionRepository
) {
    suspend fun setTrackingEnabled(enabled: Boolean) {
        focusDetectionRepository.setTrackingEnabled(enabled)
    }

    fun updateCameraPermission(granted: Boolean) {
        focusDetectionRepository.updateCameraPermission(granted)
    }

    fun setCameraActive(active: Boolean) {
        focusDetectionRepository.setCameraActive(active)
    }

    fun processFrame(result: FocusFrameResult) {
        focusDetectionRepository.processFrame(result)
    }

    fun resetSession() {
        focusDetectionRepository.resetSession()
    }
}
