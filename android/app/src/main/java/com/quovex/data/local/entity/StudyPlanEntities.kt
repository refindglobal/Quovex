package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local Room entity representing a generated or active study plan.
 */
@Entity(tableName = "study_plans")
data class StudyPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetExam: String,
    val examDateMillis: Long,
    val dailyStudyHours: Float,
    val targetSubjectsCsv: String,
    val weakTopicsCsv: String,
    val totalDays: Int,
    val currentDay: Int,
    val status: String, // "ACTIVE" | "COMPLETED" | "ARCHIVED"
    val createdAtMillis: Long = System.currentTimeMillis()
)

/**
 * Local Room entity representing a single day's study task.
 */
@Entity(
    tableName = "study_tasks",
    indices = [
        Index("planId"),
        Index("dateMillis")
    ]
)
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: Long,
    val dayNumber: Int,
    val dateMillis: Long,
    val subject: String,
    val topic: String,
    val taskType: String,
    val estimatedMinutes: Int,
    val completedMinutes: Int,
    val isCompleted: Boolean,
    val notes: String
)
