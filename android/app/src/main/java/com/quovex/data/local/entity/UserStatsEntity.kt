package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 1,
    val longestStreak: Int = 1,
    val rescueTokens: Int = 1,
    val totalXp: Long = 0L,
    val scholarLevel: Int = 1,
    val lastStudyDateMillis: Long = 0L
)
