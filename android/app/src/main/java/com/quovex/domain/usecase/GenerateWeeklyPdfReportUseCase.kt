package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.domain.manager.PdfReportGenerator
import com.quovex.domain.model.StudyReportData
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GenerateWeeklyPdfReportUseCase @Inject constructor(
    private val studyAnalyticsUseCase: StudyAnalyticsUseCase,
    private val userPreferencesManager: UserPreferencesManager,
    private val pdfReportGenerator: PdfReportGenerator
) {
    suspend operator fun invoke(): Result<File> {
        val profile = userPreferencesManager.userProfile.value
        val streak = profile.streakDays
        val insights = studyAnalyticsUseCase.getPerformanceInsights()
        val subjects = studyAnalyticsUseCase.getSubjectBreakdown(days = 7)
        val hourly = studyAnalyticsUseCase.getHourlyProductivity(days = 7)

        val todayFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
        val dailyAverageMinutes = if (insights.weeklyTotalMinutes > 0) insights.weeklyTotalMinutes / 7 else 0

        val reportData = StudyReportData(
            studentName = profile.name.ifBlank { "Quovex Scholar" },
            targetExam = profile.targetExam.ifBlank { "Academic Excellence" },
            generatedDateFormatted = todayFormatted,
            weeklyTotalMinutes = insights.weeklyTotalMinutes,
            dailyAverageMinutes = dailyAverageMinutes,
            focusScoreAvg = insights.focusScoreAvg,
            totalSessions = insights.totalSessionsCompleted,
            bestFocusWindow = insights.bestHourWindow,
            currentStreak = streak,
            subjects = subjects,
            hourlyProductivity = hourly,
            aiRecommendation = insights.aiInsight
        )

        return pdfReportGenerator.generateWeeklyReportPdf(reportData)
    }
}
