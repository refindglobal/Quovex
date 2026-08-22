package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["subject"]),
        Index(value = ["createdAt"])
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cloudId: String? = null,
    val title: String,
    val subject: String,
    val content: String,
    val status: String = "READY",
    val inputType: String = "TEXT",
    val sourceUrl: String? = null,
    val storageRef: String? = null,
    val keyPointsJson: String? = null,
    val flashcardCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Extended fields for Learning Transformation System
    val topic: String? = null,
    val summary: String? = null,
    val formulasJson: String? = null,
    val inferredSubject: String? = null,
    val inferredTopic: String? = null,
    val inferredConfidence: Float = 0f,
    val flashcardDeckId: Int? = null,
    val quizGenerated: Boolean = false,
    val syncStatus: String = "PENDING_SYNC"
)
