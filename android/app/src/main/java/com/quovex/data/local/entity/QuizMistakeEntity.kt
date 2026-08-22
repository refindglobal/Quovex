package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_mistakes",
    indices = [Index(value = ["resultId"])]
)
data class QuizMistakeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resultId: Long,
    val questionId: Long = 0,
    val questionText: String,
    val studentAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val concept: String,
    val remedialCardId: Long? = null
)
