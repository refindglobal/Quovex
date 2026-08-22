package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_questions",
    indices = [Index(value = ["materialId"])]
)
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val materialId: Long,
    val question: String,
    val optionsJson: String,
    val correctIndex: Int,
    val explanation: String,
    val relatedConcept: String,
    val difficulty: Int = 3
)
