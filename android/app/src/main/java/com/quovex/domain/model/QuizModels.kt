package com.quovex.domain.model

data class QuizQuestion(
    val id: Long = 0,
    val materialId: Long,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val relatedConcept: String,
    val difficulty: Int = 3
)

data class QuizResult(
    val id: Long = 0,
    val materialId: Long,
    val takenAt: Long = System.currentTimeMillis(),
    val score: Int,
    val totalQuestions: Int,
    val accuracyPercent: Float,
    val mistakes: List<QuizMistake> = emptyList()
)

data class QuizMistake(
    val id: Long = 0,
    val resultId: Long = 0,
    val questionId: Long = 0,
    val questionText: String,
    val studentAnswer: String,
    val correctAnswer: String,
    val explanation: String,
    val concept: String,
    val remedialCardId: Long? = null
)
