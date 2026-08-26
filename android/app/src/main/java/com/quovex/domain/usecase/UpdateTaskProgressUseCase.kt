package com.quovex.domain.usecase

import com.quovex.data.local.dao.StudyPlanDao
import com.quovex.domain.model.PlanStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Updates the completion state and logged minutes of a [DailyStudyTask],
 * and recalculates overall study plan progress.
 */
class UpdateTaskProgressUseCase @Inject constructor(
    private val studyPlanDao: StudyPlanDao
) {

    suspend fun execute(
        taskId: Long,
        isCompleted: Boolean,
        customCompletedMinutes: Int? = null
    ): Result<Unit> {
        return try {
            val task = studyPlanDao.getTaskById(taskId)
                ?: return Result.failure(IllegalArgumentException("Task $taskId not found"))

            val minutes = customCompletedMinutes
                ?: if (isCompleted) task.estimatedMinutes else 0

            studyPlanDao.updateTaskCompletion(
                taskId = taskId,
                isCompleted = isCompleted,
                completedMinutes = minutes
            )

            // Check if all tasks in the plan are completed
            val allTasks = studyPlanDao.observeTasksForPlan(task.planId).first()
            if (allTasks.isNotEmpty() && allTasks.all { it.isCompleted }) {
                studyPlanDao.updatePlanStatus(
                    planId = task.planId,
                    status = PlanStatus.COMPLETED.name,
                    currentDay = task.dayNumber
                )
            } else {
                // Update currentDay based on max completed task day
                val maxDay = allTasks.filter { it.isCompleted }.maxOfOrNull { it.dayNumber } ?: 1
                studyPlanDao.updatePlanStatus(
                    planId = task.planId,
                    status = PlanStatus.ACTIVE.name,
                    currentDay = maxDay
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
