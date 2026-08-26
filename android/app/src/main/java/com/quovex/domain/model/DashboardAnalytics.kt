package com.quovex.domain.model

/**
 * Granular daily contribution data point for the consistency heatmap calendar.
 */
data class HeatmapDay(
    val dateMillis: Long,
    val dayOfWeek: Int, // 1 = Mon .. 7 = Sun
    val dayOfMonth: Int,
    val monthShort: String,
    val minutesStudied: Int,
    val intensityLevel: Int, // 0 = 0m, 1 = 1-29m, 2 = 30-59m, 3 = 60-119m, 4 = 120m+
    val isToday: Boolean,
    val formattedDate: String
)

/**
 * Subject-wise time breakdown for the analytics visualizer.
 */
data class SubjectStudyTime(
    val subject: String,
    val totalMinutes: Int,
    val percentage: Float, // 0.0 to 1.0
    val sessionCount: Int
)

/**
 * Dynamic exam countdown tracker.
 */
data class ExamCountdown(
    val targetExam: String,
    val daysRemaining: Int,
    val targetDateFormatted: String,
    val motivationalQuote: String
)
