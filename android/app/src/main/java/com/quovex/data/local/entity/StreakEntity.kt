package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "streaks_cemetery")
data class StreakEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val streakDays: Int,
    val startDate: Long,
    val endDate: Long,
    val isBroken: Boolean = true,
    val causeOfDeath: String = "Missed daily focus goal",
    val reflectionNote: String? = null,
    val tokensUsed: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
