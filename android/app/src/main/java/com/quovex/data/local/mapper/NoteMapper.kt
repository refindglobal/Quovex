package com.quovex.data.local.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quovex.data.local.entity.NoteEntity
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus

private val gson = Gson()
private val stringListType = object : TypeToken<List<String>>() {}.type

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
