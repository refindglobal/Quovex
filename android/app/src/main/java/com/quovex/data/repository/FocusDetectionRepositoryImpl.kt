package com.quovex.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.quovex.domain.model.AttentivenessState
import com.quovex.domain.model.FocusFrameResult
import com.quovex.domain.model.FocusTrackingState
import com.quovex.domain.repository.FocusDetectionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FocusDetectionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FocusDetectionRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("quovex_focus_detection_prefs", Context.MODE_PRIVATE)
    }

    private val _focusTrackingState = MutableStateFlow(FocusTrackingState())
    override val focusTrackingState: StateFlow<FocusTrackingState> = _focusTrackingState.asStateFlow()

    private var consecutiveDrowsyFrames = 0
    private var consecutiveLookingAwayFrames = 0
    private var consecutiveNoFaceFrames = 0

    init {
        val isEnabled = prefs.getBoolean(KEY_TRACKING_ENABLED, false)
        _focusTrackingState.value = FocusTrackingState(
            isEnabled = isEnabled,
            attentivenessState = if (isEnabled) AttentivenessState.CAMERA_OFF else AttentivenessState.CAMERA_OFF
        )
    }

    override suspend fun setTrackingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TRACKING_ENABLED, enabled).apply()
        val current = _focusTrackingState.value
        _focusTrackingState.value = current.copy(
            isEnabled = enabled,
            attentivenessState = if (!enabled) AttentivenessState.CAMERA_OFF else current.attentivenessState
        )
    }

    override fun updateCameraPermission(granted: Boolean) {
        val current = _focusTrackingState.value
        _focusTrackingState.value = current.copy(hasCameraPermission = granted)
    }

    override fun setCameraActive(active: Boolean) {
        val current = _focusTrackingState.value
        _focusTrackingState.value = current.copy(
            isCameraActive = active,
            attentivenessState = if (active) AttentivenessState.ATTENTIVE else AttentivenessState.CAMERA_OFF
        )
    }

    override fun processFrame(result: FocusFrameResult) {
        val current = _focusTrackingState.value
        if (!current.isEnabled || !current.isCameraActive) return

        val newTotalSamples = current.totalSamplesAnalyzed + 1
        var newAttentiveSamples = current.attentiveSamplesCount
        var newDistractions = current.distractionsCount
        var newDrowsiness = current.drowsinessCount
        var newAttentiveness = AttentivenessState.ATTENTIVE
        var warningActive = false
        var warningMsg: String? = null

        if (!result.isFaceDetected) {
            consecutiveNoFaceFrames++
            consecutiveDrowsyFrames = 0
            consecutiveLookingAwayFrames = 0

            if (consecutiveNoFaceFrames >= 3) {
                newAttentiveness = AttentivenessState.NO_FACE_DETECTED
                warningActive = true
                warningMsg = "No face detected in study frame"
                if (consecutiveNoFaceFrames == 3) newDistractions++
            }
        } else if (result.isDrowsy) {
            consecutiveDrowsyFrames++
            consecutiveLookingAwayFrames = 0
            consecutiveNoFaceFrames = 0

            if (consecutiveDrowsyFrames >= 3) {
                newAttentiveness = AttentivenessState.DROWSY_EYES_CLOSED
                warningActive = true
                warningMsg = "Drowsiness alert: Take a breath or stretch!"
                if (consecutiveDrowsyFrames == 3) newDrowsiness++
            } else {
                newAttentiveSamples++
            }
        } else if (result.isLookingAway) {
            consecutiveLookingAwayFrames++
            consecutiveDrowsyFrames = 0
            consecutiveNoFaceFrames = 0

            if (consecutiveLookingAwayFrames >= 2) {
                newAttentiveness = AttentivenessState.LOOKING_AWAY
                warningActive = true
                warningMsg = "Focus reminder: Eyes on study material!"
                if (consecutiveLookingAwayFrames == 2) newDistractions++
            } else {
                newAttentiveSamples++
            }
        } else {
            // Attentive frame
            consecutiveDrowsyFrames = 0
            consecutiveLookingAwayFrames = 0
            consecutiveNoFaceFrames = 0
            newAttentiveSamples++
            newAttentiveness = AttentivenessState.ATTENTIVE
        }

        val calculatedScore = if (newTotalSamples > 0) {
            ((newAttentiveSamples.toFloat() / newTotalSamples.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else {
            100
        }

        _focusTrackingState.value = current.copy(
            totalSamplesAnalyzed = newTotalSamples,
            attentiveSamplesCount = newAttentiveSamples,
            focusScore = calculatedScore,
            distractionsCount = newDistractions,
            drowsinessCount = newDrowsiness,
            attentivenessState = newAttentiveness,
            isWarningActive = warningActive,
            warningMessage = warningMsg
        )
    }

    override fun resetSession() {
        consecutiveDrowsyFrames = 0
        consecutiveLookingAwayFrames = 0
        consecutiveNoFaceFrames = 0

        val current = _focusTrackingState.value
        _focusTrackingState.value = current.copy(
            focusScore = 100,
            distractionsCount = 0,
            drowsinessCount = 0,
            totalSamplesAnalyzed = 0,
            attentiveSamplesCount = 0,
            isWarningActive = false,
            warningMessage = null,
            attentivenessState = if (current.isEnabled && current.isCameraActive) AttentivenessState.ATTENTIVE else AttentivenessState.CAMERA_OFF
        )
    }

    companion object {
        private const val KEY_TRACKING_ENABLED = "is_camera_focus_enabled"
    }
}
