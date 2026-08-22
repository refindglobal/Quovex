package com.quovex.data.local.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quovex.data.local.entity.QuizMistakeEntity
import com.quovex.data.local.entity.QuizQuestionEntity
import com.quovex.data.local.entity.QuizResultEntity
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.QuizResult

private val gson = Gson()
private val stringListType = object : TypeToken<List<String>>() {}.type

fun QuizQuestionEntity.toDomain(): QuizQuestion {
    val optionsList: List<String> = try {
        gson.fromJson(optionsJson, stringListType) ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
    return QuizQuestion(
        id = id,
        materialId = materialId,
        question = question,
        options = optionsList,
        correctIndex = correctIndex,
        explanation = explanation,
        relatedConcept = relatedConcept,
        difficulty = difficulty
    )
}

fun QuizQuestion.toEntity(): QuizQuestionEntity {
    return QuizQuestionEntity(
        id = id,
        materialId = materialId,
        question = question,
        optionsJson = gson.toJson(options),
        correctIndex = correctIndex,
        explanation = explanation,
        relatedConcept = relatedConcept,
        difficulty = difficulty
    )
}

fun QuizResultEntity.toDomain(mistakes: List<QuizMistake> = emptyList()): QuizResult {
    return QuizResult(
        id = id,
        materialId = materialId,
        takenAt = takenAt,
        score = score,
        totalQuestions = totalQuestions,
        accuracyPercent = accuracyPercent,
        mistakes = mistakes
    )
}

fun QuizResult.toEntity(): QuizResultEntity {
    return QuizResultEntity(
        id = id,
        materialId = materialId,
        takenAt = takenAt,
        score = score,
        totalQuestions = totalQuestions,
        accuracyPercent = accuracyPercent
    )
}

fun QuizMistakeEntity.toDomain(): QuizMistake {
    return QuizMistake(
        id = id,
        resultId = resultId,
        questionId = questionId,
        questionText = questionText,
        studentAnswer = studentAnswer,
        correctAnswer = correctAnswer,
        explanation = explanation,
        concept = concept,
        remedialCardId = remedialCardId
    )
}

fun QuizMistake.toEntity(resultId: Long): QuizMistakeEntity {
    return QuizMistakeEntity(
        id = id,
        resultId = resultId,
        questionId = questionId,
        questionText = questionText,
        studentAnswer = studentAnswer,
        correctAnswer = correctAnswer,
        explanation = explanation,
        concept = concept,
        remedialCardId = remedialCardId
    )
}
