package com.quovex.domain.usecase

import com.quovex.data.local.dao.StudyPlanDao
import com.quovex.data.local.entity.StudyPlanEntity
import com.quovex.data.local.entity.StudyTaskEntity
import com.quovex.domain.model.DailyStudyTask
import com.quovex.domain.model.PlanStatus
import com.quovex.domain.model.StudyPlan
import com.quovex.domain.model.StudyTaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Observes the active study plan and daily task schedules from Room.
 */
class ObserveDailyScheduleUseCase @Inject constructor(
    private val studyPlanDao: StudyPlanDao
) {

    /**
     * Observes the currently active study plan as a reactive Flow.
     */
    fun observeActivePlan(): Flow<StudyPlan?> {
        return studyPlanDao.observeActivePlan().map { entity ->
            entity?.toDomain()
        }
    }

    /**
     * Observes today's tasks based on the current calendar date.
     */
    fun observeTodayTasks(): Flow<List<DailyStudyTask>> {
        val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfToday = startOfToday + TimeUnit.DAYS.toMillis(1) - 1

        return studyPlanDao.observeTasksForDateRange(startOfToday, endOfToday).map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * Observes tasks for a specific day in a study plan.
     */
    fun observeTasksForDay(planId: Long, dayNumber: Int): Flow<List<DailyStudyTask>> {
        return studyPlanDao.observeTasksForDay(planId, dayNumber).map { list ->
            list.map { it.toDomain() }
        }
    }

    /**
     * Observes all tasks belonging to a study plan.
     */
    fun observeTasksForPlan(planId: Long): Flow<List<DailyStudyTask>> {
        return studyPlanDao.observeTasksForPlan(planId).map { list ->
            list.map { it.toDomain() }
        }
    }

    private fun StudyPlanEntity.toDomain(): StudyPlan {
        return StudyPlan(
            id = id,
            title = title,
            targetExam = targetExam,
            examDateMillis = examDateMillis,
            dailyStudyHours = dailyStudyHours,
            targetSubjects = if (targetSubjectsCsv.isBlank()) emptyList() else targetSubjectsCsv.split(","),
            weakTopics = if (weakTopicsCsv.isBlank()) emptyList() else weakTopicsCsv.split(","),
            totalDays = totalDays,
            currentDay = currentDay,
            status = try {
                PlanStatus.valueOf(status)
            } catch (_: Exception) {
                PlanStatus.ACTIVE
            },
            createdAtMillis = createdAtMillis
        )
    }

    private fun StudyTaskEntity.toDomain(): DailyStudyTask {
        return DailyStudyTask(
            id = id,
            planId = planId,
            dayNumber = dayNumber,
            dateMillis = dateMillis,
            subject = subject,
            topic = topic,
            taskType = try {
                StudyTaskType.valueOf(taskType)
            } catch (_: Exception) {
                StudyTaskType.STUDY_CHAPTER
            },
            estimatedMinutes = estimatedMinutes,
            completedMinutes = completedMinutes,
            isCompleted = isCompleted,
            notes = notes
        )
    }
}
