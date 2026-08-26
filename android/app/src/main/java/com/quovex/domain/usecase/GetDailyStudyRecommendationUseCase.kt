package com.quovex.domain.usecase

import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.dao.StudyPlanDao
import com.quovex.data.local.entity.QuizMistakeEntity
import com.quovex.data.local.entity.StudyTaskEntity
import com.quovex.domain.model.StudyRecommendation
import com.quovex.domain.model.StudyTaskType
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Computes a smart contextual study recommendation ("What should I study today?").
 *
 * Prioritizes:
 * 1. Uncompleted tasks from the active Study Plan for today.
 * 2. Recent quiz mistakes / low-mastery concepts requiring active recall.
 * 3. Default foundational deep work on core subjects.
 */
class GetDailyStudyRecommendationUseCase @Inject constructor(
    private val studyPlanDao: StudyPlanDao,
    private val quizDao: QuizDao
) {

    suspend fun execute(fallbackSubject: String = "Physics"): StudyRecommendation {
        val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfToday = startOfToday + TimeUnit.DAYS.toMillis(1) - 1

        // 1. Check active plan tasks for today
        val activePlan = studyPlanDao.getActivePlan()

        val tasksList: List<StudyTaskEntity> = try {
            studyPlanDao.observeTasksForDateRange(startOfToday, endOfToday).first()
        } catch (_: Exception) {
            emptyList()
        }

        val firstUncompleted = tasksList.firstOrNull { !it.isCompleted }
        if (firstUncompleted != null && activePlan != null) {
            val taskType = try {
                StudyTaskType.valueOf(firstUncompleted.taskType)
            } catch (_: Exception) {
                StudyTaskType.DEEP_WORK_PRACTICE
            }

            return StudyRecommendation(
                recommendedSubject = firstUncompleted.subject,
                recommendedTopic = firstUncompleted.topic,
                estimatedMinutes = firstUncompleted.estimatedMinutes,
                reason = "Scheduled today in your ${activePlan.targetExam} revision plan (Day ${firstUncompleted.dayNumber})",
                suggestedActionType = taskType
            )
        }

        // 2. Check recent quiz mistakes for weak areas
        val recentMistakes: List<QuizMistakeEntity> = quizDao.getRecentMistakes(5)
        if (recentMistakes.isNotEmpty()) {
            val mistake = recentMistakes.first()
            return StudyRecommendation(
                recommendedSubject = fallbackSubject,
                recommendedTopic = mistake.concept.ifBlank { "Recent Quiz Review" },
                estimatedMinutes = 25,
                reason = "Reinforce concept missed in recent practice quiz",
                suggestedActionType = StudyTaskType.REVISE_FLASHCARDS
            )
        }

        // 3. Fallback to foundational deep focus session
        return StudyRecommendation(
            recommendedSubject = fallbackSubject,
            recommendedTopic = "Core Problem Practice",
            estimatedMinutes = 45,
            reason = "Recommended daily deep focus session to build consistency",
            suggestedActionType = StudyTaskType.DEEP_WORK_PRACTICE
        )
    }
}
