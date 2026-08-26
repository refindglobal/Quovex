package com.quovex.domain.model

/**
 * Hourly focus metrics computed across 24 hours of the day (0..23).
 */
data class HourlyProductivity(
    val hourOfDay: Int,
    val totalMinutes: Int,
    val sessionCount: Int,
    val averageFocusScore: Int,
    val formattedHour: String
)

/**
 * High-level AI cognitive performance telemetry calculated from real study session history.
 */
data class PerformanceInsights(
    val bestDayOfWeek: String,
    val bestHourWindow: String,
    val weeklyTotalMinutes: Int,
    val monthlyTotalMinutes: Int,
    val focusScoreAvg: Int,
    val distractionResistanceRate: Float,
    val totalSessionsCompleted: Int,
    val aiInsight: String
)

/**
 * Encapsulates full study telemetry required to generate a printable PDF report.
 */
data class StudyReportData(
    val studentName: String,
    val targetExam: String,
    val generatedDateFormatted: String,
    val weeklyTotalMinutes: Int,
    val dailyAverageMinutes: Int,
    val focusScoreAvg: Int,
    val totalSessions: Int,
    val bestFocusWindow: String,
    val currentStreak: Int,
    val subjects: List<SubjectStudyTime>,
    val hourlyProductivity: List<HourlyProductivity>,
    val aiRecommendation: String
)
