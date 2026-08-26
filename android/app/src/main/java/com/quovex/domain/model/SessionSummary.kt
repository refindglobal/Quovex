package com.quovex.domain.model

/**
 * Domain model representing the summary of a finished or early-ended focus session.
 * Displays purely factual, verified metrics (duration, subject, outcome, strict focus status, focus score).
 * Never contains fabricated XP or fake focus scores.
 */
data class SessionSummary(
    val subject: String,
    val modeName: String,
    val plannedDurationMinutes: Int,
    val actualDurationMinutes: Int,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isCompleted: Boolean,
    val strictFocusEnabled: Boolean,
    val focusScore: Int? = null, // 0-100 calculated by on-device Camera ML Kit, or null if camera tracking was disabled
    val distractionsCount: Int = 0,
    val drowsinessCount: Int = 0,
    val cameraTrackingEnabled: Boolean = false
)
