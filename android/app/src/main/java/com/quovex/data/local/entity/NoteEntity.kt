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
    val updatedAt: Long = System.currentTimeMillis()
)
