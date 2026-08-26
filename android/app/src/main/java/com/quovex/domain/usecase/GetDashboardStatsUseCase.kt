package com.quovex.domain.usecase

import com.quovex.data.local.UserPreferencesManager
import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.DueFlashcardsSummary
import com.quovex.domain.model.ExamCountdown
import com.quovex.domain.model.HeatmapDay
import com.quovex.domain.model.JumpBackInItem
import com.quovex.domain.model.RecentActivityItem
import com.quovex.domain.model.ScholarLevelInfo
import com.quovex.domain.model.StreakInfo
import com.quovex.domain.model.SubjectStudyTime
import com.quovex.domain.model.UserProfile
import com.quovex.domain.model.WeeklyDayProgress
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

data class DashboardData(
    val userProfile: UserProfile,
    val todayFocusMinutes: Int,
    val targetMinutes: Int,
    val progressPercent: Float,
    val isGoalCompleted: Boolean,
    val isGoalExceeded: Boolean,
    val hasGoal: Boolean,
    val streakDays: Int,
    val totalXp: Long,
    val weeklyProgress: List<WeeklyDayProgress>,
    val dueFlashcards: DueFlashcardsSummary,
    val jumpBackInItem: JumpBackInItem?,
    val recentActivities: List<RecentActivityItem>,
    val activeSession: ActiveSessionState,
    val streakInfo: StreakInfo,
    val scholarLevelInfo: ScholarLevelInfo,
    val heatmapGrid: List<HeatmapDay>,
    val subjectBreakdown: List<SubjectStudyTime>,
    val examCountdown: ExamCountdown
)

class GetDashboardStatsUseCase @Inject constructor(
    private val repository: QuovexRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val calculateStreakUseCase: CalculateStreakUseCase,
    private val getScholarLevelUseCase: GetScholarLevelUseCase,
    private val studyAnalyticsUseCase: StudyAnalyticsUseCase
) {
    suspend operator fun invoke(zoneId: ZoneId = ZoneId.systemDefault()): DashboardData {
        val profile = userPreferencesManager.userProfile.value
        val today = LocalDate.now(zoneId)
        val currentTime = System.currentTimeMillis()

        // Today's Goal Calculations
        val todaySeconds = repository.getTodayFocusSeconds()
        val todayMinutes = (todaySeconds / 60).toInt()
        val targetMinutes = (profile.dailyGoalHours * 60).toInt()
        val hasGoal = targetMinutes > 0
        val progress = if (hasGoal) (todayMinutes.toFloat() / targetMinutes) else 0f
        val isGoalCompleted = hasGoal && todayMinutes >= targetMinutes
        val isGoalExceeded = hasGoal && todayMinutes > targetMinutes
        val totalXp = profile.xp.toLong() + repository.getTotalXp()

        // Week Boundaries (Monday to Sunday in local timezone)
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val startOfWeekMillis = startOfWeek.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfWeekMillis = endOfWeek.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()

        val weeklyMinutesMap = repository.getWeeklySessionMinutes(startOfWeekMillis, endOfWeekMillis)
        val weeklyProgress = (1..7).map { dayIndex ->
            val dayDate = startOfWeek.plusDays((dayIndex - 1).toLong())
            val minutes = weeklyMinutesMap[dayIndex] ?: 0
            val isToday = dayDate.isEqual(today)
            val isFuture = dayDate.isAfter(today)
            val dayGoalCompleted = hasGoal && minutes >= targetMinutes
            WeeklyDayProgress(
                dayOfWeek = dayIndex,
                dayName = dayDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                dayShort = when (dayDate.dayOfWeek) {
                    DayOfWeek.MONDAY -> "M"
                    DayOfWeek.TUESDAY -> "T"
                    DayOfWeek.WEDNESDAY -> "W"
                    DayOfWeek.THURSDAY -> "T"
                    DayOfWeek.FRIDAY -> "F"
                    DayOfWeek.SATURDAY -> "S"
                    DayOfWeek.SUNDAY -> "S"
                },
                dateMillis = dayDate.atStartOfDay(zoneId).toInstant().toEpochMilli(),
                minutesStudied = minutes,
                targetMinutes = targetMinutes,
                isGoalCompleted = dayGoalCompleted,
                isToday = isToday,
                isFuture = isFuture
            )
        }

        // Flashcards Due Summary
        val totalDue = repository.getTotalDueFlashcardsCount(currentTime)
        val mostRecentDeck = repository.getMostRecentDeck()
        val dueSummary = DueFlashcardsSummary(
            totalDueCount = totalDue,
            primaryDeckId = mostRecentDeck?.id,
            primaryDeckTitle = mostRecentDeck?.title
        )

        // Jump Back In Item (Priority: Most recent deck/activity)
        val jumpBackInItem = if (mostRecentDeck != null) {
            val dueForDeck = repository.getDeckDueCount(mostRecentDeck.id, currentTime)
            val mastery = if (mostRecentDeck.totalCards > 0) {
                (((mostRecentDeck.totalCards - dueForDeck).coerceAtLeast(0) * 100) / mostRecentDeck.totalCards)
            } else 0
            JumpBackInItem(
                deckId = mostRecentDeck.id,
                title = mostRecentDeck.title,
                subject = mostRecentDeck.subject,
                dueCount = dueForDeck,
                totalCards = mostRecentDeck.totalCards,
                masteryPercent = mastery
            )
        } else null

        // Recent Activities
        val recentSessions = repository.getRecentSessionsList(limit = 3)

        // Active Session
        val activeSession = repository.getActiveSessionState().first()

        // Phase 12 Analytics, Streak & Scholar Progression
        val streakInfo = calculateStreakUseCase(currentTimeMillis = currentTime, zoneId = zoneId)
        val scholarLevelInfo = getScholarLevelUseCase(totalXp)
        val heatmapGrid = studyAnalyticsUseCase.getHeatmapGrid(zoneId = zoneId, weeksCount = 4)
        val subjectBreakdown = studyAnalyticsUseCase.getSubjectBreakdown(zoneId = zoneId, days = 30)
        val examCountdown = studyAnalyticsUseCase.getExamCountdown(zoneId = zoneId)

        return DashboardData(
            userProfile = profile,
            todayFocusMinutes = todayMinutes,
            targetMinutes = targetMinutes,
            progressPercent = progress,
            isGoalCompleted = isGoalCompleted,
            isGoalExceeded = isGoalExceeded,
            hasGoal = hasGoal,
            streakDays = streakInfo.currentStreak,
            totalXp = totalXp,
            weeklyProgress = weeklyProgress,
            dueFlashcards = dueSummary,
            jumpBackInItem = jumpBackInItem,
            recentActivities = recentSessions,
            activeSession = activeSession,
            streakInfo = streakInfo,
            scholarLevelInfo = scholarLevelInfo,
            heatmapGrid = heatmapGrid,
            subjectBreakdown = subjectBreakdown,
            examCountdown = examCountdown
        )
    }
}
