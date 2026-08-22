package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_results",
    indices = [Index(value = ["materialId"])]
)
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val materialId: Long,
    val takenAt: Long = System.currentTimeMillis(),
    val score: Int,
    val totalQuestions: Int,
    val accuracyPercent: Float
)
