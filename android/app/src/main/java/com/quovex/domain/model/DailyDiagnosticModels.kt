package com.quovex.domain.model

/**
 * Topic studied today, extracted dynamically from Room SessionDao or MaterialDao.
 */
data class DailyDiagnosticTopic(
    val topicName: String,
    val subject: String,
    val minutesStudied: Int,
    val sourceMaterialId: Long? = null
)

/**
 * Request payload for generating a 5-question daily diagnostic quiz.
 */
data class DiagnosticQuizRequest(
    val targetExam: String,
    val topics: List<DailyDiagnosticTopic>,
    val questionCount: Int = 5
)

/**
 * Single question generated for the daily diagnostic quiz.
 */
data class DiagnosticQuestion(
    val id: Long = 0,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val subject: String,
    val concept: String,
    val explanation: String
)

/**
 * Structured result of an AI remedial flashcard synthesis for a quiz mistake.
 */
data class RemedialCardSynthesis(
    val questionText: String,
    val studentSelectedOption: String,
    val correctOption: String,
    val concept: String,
    val frontPrompt: String,
    val backExplanation: String,
    val commonTrapAlert: String
)
