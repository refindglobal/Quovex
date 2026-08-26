package com.quovex.domain.model

/**
 * Status of a student's study plan.
 */
enum class PlanStatus {
    ACTIVE,
    COMPLETED,
    ARCHIVED
}

/**
 * High-yield educational task types in a daily study schedule.
 */
enum class StudyTaskType(val label: String, val icon: String) {
    STUDY_CHAPTER("Study Chapter", "📚"),
    REVISE_FLASHCARDS("Revise Flashcards", "⚡"),
    TAKE_QUIZ("Practice Quiz", "📝"),
    DEEP_WORK_PRACTICE("Deep Problem Practice", "🎯")
}

/**
 * Domain representation of a structured, multi-day exam revision plan.
 */
data class StudyPlan(
    val id: Long = 0,
    val title: String,
    val targetExam: String,
    val examDateMillis: Long,
    val dailyStudyHours: Float,
    val targetSubjects: List<String>,
    val weakTopics: List<String>,
    val totalDays: Int,
    val currentDay: Int = 1,
    val status: PlanStatus = PlanStatus.ACTIVE,
    val createdAtMillis: Long = System.currentTimeMillis()
)

/**
 * A single granular daily learning objective generated as part of a [StudyPlan].
 */
data class DailyStudyTask(
    val id: Long = 0,
    val planId: Long,
    val dayNumber: Int,
    val dateMillis: Long,
    val subject: String,
    val topic: String,
    val taskType: StudyTaskType,
    val estimatedMinutes: Int,
    val completedMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

/**
 * Real-time dynamic recommendation ("What should I study today?").
 */
data class StudyRecommendation(
    val recommendedSubject: String,
    val recommendedTopic: String,
    val estimatedMinutes: Int,
    val reason: String,
    val suggestedActionType: StudyTaskType
)
