package com.quovex.domain.model

/**
 * Represents a rank entry in a weekly leaderboard.
 */
data class LeaderboardEntry(
    val userId: String = "",
    val userName: String = "",
    val avatarId: Int = 1,
    val scholarRank: String = "Novice Scholar",
    val studyMinutes: Int = 0,
    val xp: Int = 0,
    val rank: Int = 0,
    val isCurrentUser: Boolean = false,
    val trend: RankTrend = RankTrend.SAME
)

enum class RankTrend { UP, DOWN, SAME }

enum class LeaderboardType { GLOBAL, FRIENDS, SUBJECT }

/**
 * A live study room with real-time member presence.
 */
data class StudyRoomSession(
    val roomId: String = "",
    val roomTitle: String = "",
    val subject: String = "General",
    val targetExam: String = "JEE Advanced",
    val activeMemberCount: Int = 0,
    val maxMembers: Int = 30,
    val backgroundTheme: String = "emerald",
    val members: List<RoomMember> = emptyList(),
    val createdBy: String = "system",
    val isPublic: Boolean = true
)

/**
 * A single participant inside a live study room.
 */
data class RoomMember(
    val userId: String = "",
    val userName: String = "",
    val avatarId: Int = 1,
    val scholarRank: String = "Novice Scholar",
    val currentSessionMinutes: Int = 0,
    val isStudying: Boolean = true,
    val focusScore: Int = 0
)

/**
 * A break-time chat message posted inside a study room.
 */
data class RoomChatMessage(
    val messageId: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val messageText: String = "",
    val timestampMillis: Long = System.currentTimeMillis(),
    val isSystemMessage: Boolean = false,
    val reactionCount: Int = 0
)

/**
 * A 1v1 weekly study-hour battle between two users.
 */
data class StudyBattle(
    val battleId: String = "",
    val challengerId: String = "",
    val challengerName: String = "",
    val challengerAvatarId: Int = 1,
    val opponentId: String = "",
    val opponentName: String = "",
    val opponentAvatarId: Int = 2,
    val targetExam: String = "JEE Advanced",
    val challengerMinutes: Int = 0,
    val opponentMinutes: Int = 0,
    val startDateMillis: Long = System.currentTimeMillis(),
    val endDateMillis: Long = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L,
    val status: BattleStatus = BattleStatus.PENDING
)

enum class BattleStatus { PENDING, ACTIVE, COMPLETED }

/**
 * A friend's public profile summary for the Friends tab.
 */
data class FriendProfile(
    val friendId: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarId: Int = 1,
    val scholarRank: String = "Novice Scholar",
    val streakDays: Int = 0,
    val totalStudyHours: Float = 0f,
    val topSubject: String = "General",
    val isStudyingNow: Boolean = false
)
