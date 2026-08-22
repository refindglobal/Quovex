package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    indices = [Index(value = ["startTime"])]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val focusScore: Int, // 0-100 score based on distractions
    val appBlockViolations: Int,
    val isMultiplayer: Boolean = false,
    val roomId: String? = null, // Firebase Room ID if it was a multiplayer session
    val subject: String = ""
)
