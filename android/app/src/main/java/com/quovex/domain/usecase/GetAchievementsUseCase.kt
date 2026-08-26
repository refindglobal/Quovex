package com.quovex.domain.usecase

import com.quovex.data.local.dao.FlashcardDao
import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.dao.SessionDao
import com.quovex.data.local.dao.UserStatsDao
import com.quovex.domain.model.AchievementBadge
import javax.inject.Inject

/**
 * Computes unlock status and progress for all student achievement badges.
 */
class GetAchievementsUseCase @Inject constructor(
    private val sessionDao: SessionDao,
    private val flashcardDao: FlashcardDao,
    private val quizDao: QuizDao,
    private val userStatsDao: UserStatsDao
) {
    suspend operator fun invoke(): List<AchievementBadge> {
        val stats = userStatsDao.getUserStats()
        val currentStreak = stats?.currentStreak ?: 0
        val totalSessions = sessionDao.getTotalSessionsCount()
        val totalMinutes = sessionDao.getTotalStudyMinutesSince(0L) ?: 0
        val totalHours = totalMinutes / 60.0
        val totalQuizzes = quizDao.getTotalQuizCount()
        val totalDecks = flashcardDao.getDeckCount()

        return listOf(
            AchievementBadge(
                id = "FIRST_SESSION",
                title = "First Step",
                description = "Complete your first deep focus study session",
                iconEmoji = "🎯",
                category = "HOURS",
                isUnlocked = totalSessions >= 1,
                progress = (totalSessions / 1f).coerceIn(0f, 1f),
                progressText = if (totalSessions >= 1) "Unlocked" else "0 / 1 session"
            ),
            AchievementBadge(
                id = "STREAK_7",
                title = "Week of Fire",
                description = "Maintain a 7-day active study streak",
                iconEmoji = "🔥",
                category = "STREAK",
                isUnlocked = currentStreak >= 7,
                progress = (currentStreak / 7f).coerceIn(0f, 1f),
                progressText = if (currentStreak >= 7) "Unlocked" else "$currentStreak / 7 days"
            ),
            AchievementBadge(
                id = "STREAK_30",
                title = "Iron Discipline",
                description = "Maintain a 30-day active study streak",
                iconEmoji = "⚡",
                category = "STREAK",
                isUnlocked = currentStreak >= 30,
                progress = (currentStreak / 30f).coerceIn(0f, 1f),
                progressText = if (currentStreak >= 30) "Unlocked" else "$currentStreak / 30 days"
            ),
            AchievementBadge(
                id = "HOURS_10",
                title = "10-Hour Scholar",
                description = "Log 10 hours of focused deep study",
                iconEmoji = "⏳",
                category = "HOURS",
                isUnlocked = totalHours >= 10.0,
                progress = (totalHours / 10.0f).toFloat().coerceIn(0f, 1f),
                progressText = if (totalHours >= 10.0) "Unlocked" else "${String.format("%.1f", totalHours)} / 10h"
            ),
            AchievementBadge(
                id = "HOURS_50",
                title = "Deep Work Titan",
                description = "Log 50 hours of focused deep study",
                iconEmoji = "🏛️",
                category = "HOURS",
                isUnlocked = totalHours >= 50.0,
                progress = (totalHours / 50.0f).toFloat().coerceIn(0f, 1f),
                progressText = if (totalHours >= 50.0) "Unlocked" else "${String.format("%.1f", totalHours)} / 50h"
            ),
            AchievementBadge(
                id = "DECK_BUILDER",
                title = "Deck Architect",
                description = "Create or transform 3 flashcard decks",
                iconEmoji = "🃏",
                category = "MASTERY",
                isUnlocked = totalDecks >= 3,
                progress = (totalDecks / 3f).coerceIn(0f, 1f),
                progressText = if (totalDecks >= 3) "Unlocked" else "$totalDecks / 3 decks"
            ),
            AchievementBadge(
                id = "QUIZ_WARRIOR",
                title = "Active Recall Master",
                description = "Complete 5 active recall quizzes",
                iconEmoji = "🧠",
                category = "QUIZ",
                isUnlocked = totalQuizzes >= 5,
                progress = (totalQuizzes / 5f).coerceIn(0f, 1f),
                progressText = if (totalQuizzes >= 5) "Unlocked" else "$totalQuizzes / 5 quizzes"
            )
        )
    }
}
