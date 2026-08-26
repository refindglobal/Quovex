package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.data.local.dao.SessionDao
import com.quovex.domain.model.ExamCountdown
import com.quovex.domain.model.HeatmapDay
import com.quovex.domain.model.SubjectStudyTime
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

/**
 * Aggregates advanced student telemetry, heatmap contribution grid,
 * subject distribution, and exam countdowns.
 */
class StudyAnalyticsUseCase @Inject constructor(
    private val sessionDao: SessionDao,
    private val userPreferencesManager: UserPreferencesManager
) {
    suspend fun getHeatmapGrid(
        zoneId: ZoneId = ZoneId.systemDefault(),
        weeksCount: Int = 4
    ): List<HeatmapDay> {
        val today = LocalDate.now(zoneId)
        val totalDays = weeksCount * 7
        val startDate = today.minusDays((totalDays - 1).toLong())

        val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        val sessions = sessionDao.getSessionsBetween(startMillis, endMillis)
        val minutesPerDate = mutableMapOf<LocalDate, Int>()

        for (session in sessions) {
            val sessionDate = Instant.ofEpochMilli(session.startTime).atZone(zoneId).toLocalDate()
            val current = minutesPerDate.getOrDefault(sessionDate, 0)
            minutesPerDate[sessionDate] = current + session.durationMinutes
        }

        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

        return (0 until totalDays).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            val minutes = minutesPerDate.getOrDefault(date, 0)
            val intensity = when {
                minutes <= 0 -> 0
                minutes < 30 -> 1
                minutes < 60 -> 2
                minutes < 120 -> 3
                else -> 4
            }
            HeatmapDay(
                dateMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                dayOfWeek = date.dayOfWeek.value,
                dayOfMonth = date.dayOfMonth,
                monthShort = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                minutesStudied = minutes,
                intensityLevel = intensity,
                isToday = date.isEqual(today),
                formattedDate = date.format(dateFormatter)
            )
        }
    }

    suspend fun getSubjectBreakdown(
        zoneId: ZoneId = ZoneId.systemDefault(),
        days: Int = 30
    ): List<SubjectStudyTime> {
        val today = LocalDate.now(zoneId)
        val startDate = today.minusDays(days.toLong())
        val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        val sessions = sessionDao.getSessionsBetween(startMillis, endMillis)
        if (sessions.isEmpty()) return emptyList()

        val subjectMap = mutableMapOf<String, Pair<Int, Int>>() // subject -> (totalMinutes, count)
        var grandTotalMinutes = 0

        for (s in sessions) {
            val subjectName = s.subject.ifBlank { "General Focus" }
            val existing = subjectMap.getOrDefault(subjectName, Pair(0, 0))
            subjectMap[subjectName] = Pair(existing.first + s.durationMinutes, existing.second + 1)
            grandTotalMinutes += s.durationMinutes
        }

        if (grandTotalMinutes == 0) return emptyList()

        return subjectMap.map { (subj, pair) ->
            SubjectStudyTime(
                subject = subj,
                totalMinutes = pair.first,
                percentage = (pair.first.toFloat() / grandTotalMinutes).coerceIn(0f, 1f),
                sessionCount = pair.second
            )
        }.sortedByDescending { it.totalMinutes }
    }

    fun getExamCountdown(zoneId: ZoneId = ZoneId.systemDefault()): ExamCountdown {
        val profile = userPreferencesManager.userProfile.value
        val today = LocalDate.now(zoneId)

        // Estimated major exam target dates
        val targetDate = when {
            profile.targetExam.contains("JEE", ignoreCase = true) -> LocalDate.of(today.year + (if (today.monthValue > 5) 1 else 0), Month.MAY, 24)
            profile.targetExam.contains("NEET", ignoreCase = true) -> LocalDate.of(today.year + (if (today.monthValue > 5) 1 else 0), Month.MAY, 3)
            profile.targetExam.contains("UPSC", ignoreCase = true) -> LocalDate.of(today.year + (if (today.monthValue > 6) 1 else 0), Month.JUNE, 1)
            profile.targetExam.contains("SAT", ignoreCase = true) -> LocalDate.of(today.year + (if (today.monthValue > 10) 1 else 0), Month.NOVEMBER, 7)
            else -> LocalDate.of(today.year + (if (today.monthValue > 4) 1 else 0), Month.MAY, 15)
        }

        val daysRemaining = ChronoUnit.DAYS.between(today, targetDate).toInt().coerceAtLeast(0)
        val formattedTargetDate = targetDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))

        val quote = when {
            daysRemaining > 180 -> "Building strong conceptual foundations each day."
            daysRemaining > 60 -> "Consistency beats intensity. Keep the momentum going."
            daysRemaining > 30 -> "Peak revision phase. Sharpen your active recall."
            else -> "Final stretch. Trust your preparation and focus on mocks."
        }

        return ExamCountdown(
            targetExam = profile.targetExam,
            daysRemaining = daysRemaining,
            targetDateFormatted = formattedTargetDate,
            motivationalQuote = quote
        )
    }

    suspend fun getHourlyProductivity(
        zoneId: ZoneId = ZoneId.systemDefault(),
        days: Int = 30
    ): List<com.quovex.domain.model.HourlyProductivity> {
        val today = LocalDate.now(zoneId)
        val startDate = today.minusDays(days.toLong())
        val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endMillis = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        val sessions = sessionDao.getSessionsBetween(startMillis, endMillis)
        val hourlyMinutes = IntArray(24)
        val hourlyCount = IntArray(24)
        val hourlyScoreSum = IntArray(24)

        for (s in sessions) {
            val hour = Instant.ofEpochMilli(s.startTime).atZone(zoneId).hour
            hourlyMinutes[hour] += s.durationMinutes
            hourlyCount[hour] += 1
            hourlyScoreSum[hour] += s.focusScore
        }

        return (0..23).map { hour ->
            val count = hourlyCount[hour]
            val totalMins = hourlyMinutes[hour]
            val avgScore = if (count > 0) (hourlyScoreSum[hour] / count).coerceIn(0, 100) else 0
            val formatted = String.format(Locale.US, "%02d:00", hour)
            com.quovex.domain.model.HourlyProductivity(
                hourOfDay = hour,
                totalMinutes = totalMins,
                sessionCount = count,
                averageFocusScore = avgScore,
                formattedHour = formatted
            )
        }
    }

    suspend fun getPerformanceInsights(
        zoneId: ZoneId = ZoneId.systemDefault()
    ): com.quovex.domain.model.PerformanceInsights {
        val today = LocalDate.now(zoneId)
        val weekStart = today.minusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthStart = today.minusDays(30).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val nowMillis = today.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        val weekSessions = sessionDao.getSessionsBetween(weekStart, nowMillis)
        val monthSessions = sessionDao.getSessionsBetween(monthStart, nowMillis)

        val weeklyTotalMinutes = weekSessions.sumOf { it.durationMinutes }
        val monthlyTotalMinutes = monthSessions.sumOf { it.durationMinutes }
        val totalSessions = monthSessions.size

        val focusScoreAvg = if (monthSessions.isNotEmpty()) {
            (monthSessions.sumOf { it.focusScore } / monthSessions.size).coerceIn(0, 100)
        } else {
            0
        }

        val totalDistractions = monthSessions.sumOf { it.appBlockViolations }
        val distractionResistanceRate = if (totalSessions > 0) {
            val maxPossibleDistractionPenalty = totalSessions * 10f
            ((maxPossibleDistractionPenalty - totalDistractions.coerceAtMost(maxPossibleDistractionPenalty.toInt())) / maxPossibleDistractionPenalty).coerceIn(0f, 1f)
        } else {
            1.0f
        }

        // Calculate Best Day of the Week
        val dayMinutesMap = mutableMapOf<java.time.DayOfWeek, Int>()
        for (s in monthSessions) {
            val dayOfWeek = Instant.ofEpochMilli(s.startTime).atZone(zoneId).dayOfWeek
            dayMinutesMap[dayOfWeek] = dayMinutesMap.getOrDefault(dayOfWeek, 0) + s.durationMinutes
        }
        val bestDay = dayMinutesMap.maxByOrNull { it.value }?.key?.getDisplayName(TextStyle.FULL, Locale.getDefault())
            ?: if (totalSessions > 0) "Consistent" else "Start Today"

        // Calculate Best Hourly Focus Window
        val hourlyMap = IntArray(24)
        for (s in monthSessions) {
            val hour = Instant.ofEpochMilli(s.startTime).atZone(zoneId).hour
            hourlyMap[hour] += s.durationMinutes
        }
        val maxHour = hourlyMap.indices.maxByOrNull { hourlyMap[it] } ?: 9
        val bestWindow = if (hourlyMap[maxHour] > 0) {
            val nextHour = (maxHour + 2) % 24
            String.format(Locale.US, "%02d:00 – %02d:00", maxHour, nextHour)
        } else {
            "08:00 – 11:00 AM"
        }

        // Generate dynamic insight based on real telemetry
        val aiInsight = when {
            totalSessions == 0 -> "Complete your first focus session today to unlock personalized cognitive curve telemetry and AI recommendations."
            focusScoreAvg >= 85 -> "Outstanding cognitive stamina! Your peak focus window is $bestWindow with an impressive average focus score of $focusScoreAvg%."
            focusScoreAvg in 70..84 -> "Consistent focus maintained. Studying during $bestWindow yields your highest retention and lowest distraction rate."
            else -> "Try using shorter 25-minute Pomodoro intervals during $bestWindow to reduce mental fatigue and build momentum."
        }

        return com.quovex.domain.model.PerformanceInsights(
            bestDayOfWeek = bestDay,
            bestHourWindow = bestWindow,
            weeklyTotalMinutes = weeklyTotalMinutes,
            monthlyTotalMinutes = monthlyTotalMinutes,
            focusScoreAvg = focusScoreAvg,
            distractionResistanceRate = distractionResistanceRate,
            totalSessionsCompleted = totalSessions,
            aiInsight = aiInsight
        )
    }
}
