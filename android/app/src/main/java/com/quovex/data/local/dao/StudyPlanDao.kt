package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quovex.data.local.entity.StudyPlanEntity
import com.quovex.data.local.entity.StudyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: StudyPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Query("SELECT * FROM study_plans WHERE status = 'ACTIVE' ORDER BY createdAtMillis DESC LIMIT 1")
    fun observeActivePlan(): Flow<StudyPlanEntity?>

    @Query("SELECT * FROM study_plans WHERE status = 'ACTIVE' ORDER BY createdAtMillis DESC LIMIT 1")
    suspend fun getActivePlan(): StudyPlanEntity?

    @Query("SELECT * FROM study_plans WHERE id = :planId")
    suspend fun getPlanById(planId: Long): StudyPlanEntity?

    @Query("SELECT * FROM study_tasks WHERE planId = :planId ORDER BY dayNumber ASC, id ASC")
    fun observeTasksForPlan(planId: Long): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE planId = :planId AND dayNumber = :dayNumber ORDER BY id ASC")
    fun observeTasksForDay(planId: Long, dayNumber: Int): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY isCompleted ASC, id ASC")
    fun observeTasksForDateRange(startMillis: Long, endMillis: Long): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): StudyTaskEntity?

    @Query("UPDATE study_tasks SET isCompleted = :isCompleted, completedMinutes = :completedMinutes WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Long, isCompleted: Boolean, completedMinutes: Int)

    @Query("UPDATE study_plans SET status = 'ARCHIVED' WHERE id != :exceptId AND status = 'ACTIVE'")
    suspend fun archiveOtherActivePlans(exceptId: Long)

    @Query("UPDATE study_plans SET status = :status, currentDay = :currentDay WHERE id = :planId")
    suspend fun updatePlanStatus(planId: Long, status: String, currentDay: Int)

    @Query("DELETE FROM study_plans WHERE id = :planId")
    suspend fun deletePlan(planId: Long)

    @Query("DELETE FROM study_tasks WHERE planId = :planId")
    suspend fun deleteTasksForPlan(planId: Long)
}
