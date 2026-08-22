package com.quovex.domain.model

data class WeeklyDayProgress(
    val dayOfWeek: Int, // 1 = Monday .. 7 = Sunday (java.time.DayOfWeek standard)
    val dayName: String,
    val dayShort: String,
    val dateMillis: Long,
    val minutesStudied: Int,
    val targetMinutes: Int,
    val isGoalCompleted: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)

data class RecentActivityItem(
    val id: Int,
    val title: String,
    val subject: String,
    val durationMinutes: Int,
    val focusScore: Int,
    val timestamp: Long
)

data class DueFlashcardsSummary(
    val totalDueCount: Int,
    val primaryDeckId: Int? = null,
    val primaryDeckTitle: String? = null
)

enum class SessionStatus { IDLE, RUNNING, COMPLETED, CANCELLED }

data class ActiveSessionState(
    val isActive: Boolean = false,
    val subject: String = "",
    val modeName: String = "Pomodoro",
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val startedAtMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val strictFocusEnabled: Boolean = false,
    val status: SessionStatus = SessionStatus.IDLE
)

data class JumpBackInItem(
    val deckId: Int,
    val title: String,
    val subject: String,
    val dueCount: Int,
    val totalCards: Int,
    val masteryPercent: Int
)

data class DeckItem(
    val id: Int,
    val title: String,
    val subject: String,
    val totalCards: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val xpValue: Int = 100
)
