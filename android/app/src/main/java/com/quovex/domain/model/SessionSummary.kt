package com.quovex.domain.model

/**
 * Domain model representing the summary of a finished or early-ended focus session.
 * Displays purely factual, verified metrics (duration, subject, outcome, strict focus status).
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
    val strictFocusEnabled: Boolean
)
