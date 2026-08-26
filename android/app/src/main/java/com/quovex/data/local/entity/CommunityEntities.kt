package com.quovex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Local cache of a friend relationship.
 * Populated from Firestore friendships/{userId}/friends collection.
 */
@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey val friendId: String,
    val username: String,
    val displayName: String,
    val avatarId: Int,
    val scholarRank: String,
    val streakDays: Int,
    val totalStudyHours: Float,
    val topSubject: String,
    val isStudyingNow: Boolean,
    val cachedAtMillis: Long = System.currentTimeMillis()
)

/**
 * Local cache of a 1v1 study battle.
 * Populated from Firestore study_battles collection.
 */
@Entity(
    tableName = "study_battles",
    indices = [
        Index("challengerId"),
        Index("opponentId")
    ]
)
data class StudyBattleEntity(
    @PrimaryKey val battleId: String,
    val challengerId: String,
    val challengerName: String,
    val challengerAvatarId: Int,
    val opponentId: String,
    val opponentName: String,
    val opponentAvatarId: Int,
    val targetExam: String,
    val challengerMinutes: Int,
    val opponentMinutes: Int,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val status: String,   // "PENDING" | "ACTIVE" | "COMPLETED"
    val cachedAtMillis: Long = System.currentTimeMillis()
)

/**
 * Local cache of a leaderboard entry row.
 * Populated from Firestore weekly_leaderboards collection.
 */
@Entity(tableName = "leaderboard_cache", primaryKeys = ["leaderboardType", "userId"])
data class LeaderboardCacheEntity(
    val leaderboardType: String,   // "GLOBAL" | "FRIENDS" | "SUBJECT"
    val subjectFilter: String = "ALL",
    val userId: String,
    val userName: String,
    val avatarId: Int,
    val scholarRank: String,
    val studyMinutes: Int,
    val xp: Int,
    val rank: Int,
    val isCurrentUser: Boolean,
    val trend: String,             // "UP" | "DOWN" | "SAME"
    val weekKey: String,           // e.g. "2026-W35"
    val cachedAtMillis: Long = System.currentTimeMillis()
)
