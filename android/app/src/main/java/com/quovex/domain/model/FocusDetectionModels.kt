package com.quovex.domain.model

/**
 * Real-time attentiveness classification determined by on-device ML Kit Face Detection.
 */
enum class AttentivenessState(val label: String, val emoji: String) {
    ATTENTIVE("Laser Focused", "🎯"),
    LOOKING_AWAY("Looking Away", "👀"),
    DROWSY_EYES_CLOSED("Drowsy (Eyes Closed)", "😴"),
    NO_FACE_DETECTED("No Face Detected", "❓"),
    CAMERA_OFF("Camera Off", "📷")
}

/**
 * Immutable domain state for Camera AI focus & drowsiness tracking.
 */
data class FocusTrackingState(
    val isEnabled: Boolean = false,
    val isCameraActive: Boolean = false,
    val hasCameraPermission: Boolean = false,
    val attentivenessState: AttentivenessState = AttentivenessState.CAMERA_OFF,
    val focusScore: Int = 100, // 0 to 100%
    val distractionsCount: Int = 0,
    val drowsinessCount: Int = 0,
    val totalSamplesAnalyzed: Int = 0,
    val attentiveSamplesCount: Int = 0,
    val isWarningActive: Boolean = false,
    val warningMessage: String? = null
)

/**
 * Telemetry result analyzed from a single camera frame by ML Kit Face Detection.
 */
data class FocusFrameResult(
    val isFaceDetected: Boolean,
    val isDrowsy: Boolean, // Eyes closed (probabilities < 0.25)
    val isLookingAway: Boolean, // Euler Y or X > 25 degrees
    val timestampMillis: Long = System.currentTimeMillis()
)
