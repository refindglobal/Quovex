package com.quovex.data.local.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quovex.data.local.entity.NoteEntity
import com.quovex.domain.model.FormulaItem
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus

private val gson = Gson()
private val stringListType = object : TypeToken<List<String>>() {}.type
private val formulaListType = object : TypeToken<List<FormulaItem>>() {}.type

fun NoteEntity.toDomain(): NoteItem {
    val keyPointsList: List<String> = try {
        if (!keyPointsJson.isNullOrBlank()) {
            gson.fromJson(keyPointsJson, stringListType) ?: emptyList()
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }

    val inputTypeEnum = try {
        NoteInputType.valueOf(inputType)
    } catch (_: Exception) {
        NoteInputType.TEXT
    }

    val statusEnum = try {
        NoteProcessingStatus.valueOf(status)
    } catch (_: Exception) {
        NoteProcessingStatus.READY
    }

    return NoteItem(
        id = id,
        cloudId = cloudId,
        title = title,
        subject = subject,
        content = content,
        status = statusEnum,
        inputType = inputTypeEnum,
        sourceUrl = sourceUrl,
        storageRef = storageRef,
        keyPoints = keyPointsList,
        flashcardCount = flashcardCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun NoteItem.toEntity(): NoteEntity {
    val keyPointsJson = if (keyPoints.isNotEmpty()) {
        gson.toJson(keyPoints)
    } else {
        null
    }

    return NoteEntity(
        id = id,
        cloudId = cloudId,
        title = title,
        subject = subject,
        content = content,
        status = status.name,
        inputType = inputType.name,
        sourceUrl = sourceUrl,
        storageRef = storageRef,
        keyPointsJson = keyPointsJson,
        flashcardCount = flashcardCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun NoteEntity.toLearningMaterial(): LearningMaterial {
    val keyPointsList: List<String> = try {
        if (!keyPointsJson.isNullOrBlank()) {
            gson.fromJson(keyPointsJson, stringListType) ?: emptyList()
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }

    val formulasList: List<FormulaItem> = try {
        if (!formulasJson.isNullOrBlank()) {
            gson.fromJson(formulasJson, formulaListType) ?: emptyList()
        } else {
            emptyList()
        }
    } catch (_: Exception) {
        emptyList()
    }

    val inputTypeEnum = try {
        NoteInputType.valueOf(inputType)
    } catch (_: Exception) {
        NoteInputType.TEXT
    }

    val statusEnum = try {
        NoteProcessingStatus.valueOf(status)
    } catch (_: Exception) {
        NoteProcessingStatus.READY
    }

    return LearningMaterial(
        id = id,
        cloudId = cloudId,
        title = title,
        subject = subject,
        topic = topic ?: "",
        subtopic = "",
        summary = summary ?: "",
        keyPoints = keyPointsList,
        formulas = formulasList,
        inputType = inputTypeEnum,
        status = statusEnum,
        sourceUrl = sourceUrl,
        storageRef = storageRef,
        flashcardDeckId = flashcardDeckId,
        flashcardCount = flashcardCount,
        quizGenerated = quizGenerated,
        inferredSubject = inferredSubject,
        inferredTopic = inferredTopic,
        inferredConfidence = inferredConfidence,
        syncStatus = syncStatus,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun LearningMaterial.toEntity(): NoteEntity {
    val keyPointsJson = if (keyPoints.isNotEmpty()) {
        gson.toJson(keyPoints)
    } else {
        null
    }

    val formulasJson = if (formulas.isNotEmpty()) {
        gson.toJson(formulas)
    } else {
        null
    }

    return NoteEntity(
        id = id,
        cloudId = cloudId,
        title = title,
        subject = subject,
        content = summary.ifBlank { title },
        status = status.name,
        inputType = inputType.name,
        sourceUrl = sourceUrl,
        storageRef = storageRef,
        keyPointsJson = keyPointsJson,
        flashcardCount = flashcardCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        topic = topic.ifBlank { null },
        summary = summary.ifBlank { null },
        formulasJson = formulasJson,
        inferredSubject = inferredSubject,
        inferredTopic = inferredTopic,
        inferredConfidence = inferredConfidence,
        flashcardDeckId = flashcardDeckId,
        quizGenerated = quizGenerated,
        syncStatus = syncStatus
    )
}
