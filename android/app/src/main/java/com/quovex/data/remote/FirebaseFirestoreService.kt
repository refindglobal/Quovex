package com.quovex.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quovex.data.local.entity.SessionEntity
import com.quovex.domain.model.StudyRoomModel
import com.quovex.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFirestoreService @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /**
     * Real-time listener for user profile at users/{uid}
     */
    fun getUserProfileFlow(uid: String): Flow<UserProfile?> = callbackFlow {
        val docRef = firestore.collection("users").document(uid)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(null)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val profile = UserProfile(
                    id = snapshot.id,
                    name = snapshot.getString("name") ?: "Aspirant",
                    avatarId = (snapshot.getLong("avatarId") ?: 1L).toInt(),
                    targetExam = snapshot.getString("examName") ?: "JEE Advanced",
                    dailyGoalHours = (snapshot.getDouble("dailyHours") ?: 4.0).toFloat(),
                    streakDays = (snapshot.getLong("streakCount") ?: 1L).toInt(),
                    xp = (snapshot.getLong("xp") ?: 0L).toInt(),
                    level = (snapshot.getLong("level") ?: 1L).toInt(),
                    isOnboarded = snapshot.getBoolean("isOnboarded") ?: true,
                    email = snapshot.getString("email") ?: ""
                )
                trySend(profile)
            } else {
                trySend(null)
            }
        }

        awaitClose { listener.remove() }
    }

    /**
     * Write user profile to users/{uid}
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            val data = hashMapOf(
                "uid" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "avatarId" to profile.avatarId,
                "examName" to profile.targetExam,
                "dailyHours" to profile.dailyGoalHours,
                "streakCount" to profile.streakDays,
                "xp" to profile.xp,
                "level" to profile.level,
                "isOnboarded" to profile.isOnboarded,
                "lastActiveAt" to System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(profile.id)
                .set(data, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sync focus session to users/{uid}/sessions/{sessionId}
     */
    suspend fun syncSession(uid: String, session: SessionEntity): Result<Unit> {
        return try {
            val sessionData = hashMapOf(
                "startTime" to session.startTime,
                "endTime" to session.endTime,
                "durationMinutes" to session.durationMinutes,
                "focusScore" to session.focusScore,
                "appBlockViolations" to session.appBlockViolations,
                "isMultiplayer" to session.isMultiplayer,
                "roomId" to session.roomId
            )

            firestore.collection("users")
                .document(uid)
                .collection("sessions")
                .document(session.id.toString())
                .set(sessionData, SetOptions.merge())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Real-time listener for study rooms at study_rooms/
     */
    fun getStudyRoomsFlow(): Flow<List<StudyRoomModel>> = callbackFlow {
        val collectionRef = firestore.collection("study_rooms")
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val rooms = snapshot.documents.mapNotNull { doc ->
                StudyRoomModel(
                    id = doc.id,
                    name = doc.getString("name") ?: "Study Room",
                    subject = doc.getString("subject") ?: "General",
                    activeMembers = (doc.getLong("activeMembers") ?: 1L).toInt(),
                    targetExam = doc.getString("targetExam") ?: "All Exams",
                    avatars = listOf(1, 2, 3),
                    createdBy = doc.getString("createdBy") ?: "system"
                )
            }
            trySend(rooms)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Seed initial public study rooms to Firestore if collection is empty
     */
    suspend fun seedInitialRoomsIfEmpty() {
        try {
            val snapshot = firestore.collection("study_rooms").limit(1).get().await()
            if (snapshot.isEmpty) {
                val sampleRooms = listOf(
                    hashMapOf(
                        "name" to "JEE 2027 • Mechanics Deep Work",
                        "subject" to "Physics",
                        "activeMembers" to 42,
                        "targetExam" to "JEE Advanced",
                        "createdBy" to "system",
                        "maxMembers" to 30,
                        "backgroundTheme" to "emerald",
                        "isPublic" to true
                    ),
                    hashMapOf(
                        "name" to "Organic Chemistry Mechanisms",
                        "subject" to "Chemistry",
                        "activeMembers" to 28,
                        "targetExam" to "NEET / JEE",
                        "createdBy" to "system",
                        "maxMembers" to 30,
                        "backgroundTheme" to "emerald",
                        "isPublic" to true
                    ),
                    hashMapOf(
                        "name" to "Calculus & Differential Equations",
                        "subject" to "Maths",
                        "activeMembers" to 35,
                        "targetExam" to "JEE Advanced",
                        "createdBy" to "system",
                        "maxMembers" to 30,
                        "backgroundTheme" to "emerald",
                        "isPublic" to true
                    ),
                    hashMapOf(
                        "name" to "Cell Biology & Genetics Flashcards",
                        "subject" to "Biology",
                        "activeMembers" to 19,
                        "targetExam" to "NEET UG",
                        "createdBy" to "system",
                        "maxMembers" to 30,
                        "backgroundTheme" to "emerald",
                        "isPublic" to true
                    )
                )

                sampleRooms.forEach { roomData ->
                    firestore.collection("study_rooms").add(roomData).await()
                }
            }
        } catch (_: Exception) {}
    }

    // ---- Phase 13: Community & Social ----

    /**
     * Real-time listener for live members inside a single study room.
     */
    fun getRoomMembersFlow(roomId: String): Flow<List<com.quovex.domain.model.RoomMember>> = callbackFlow {
        val ref = firestore.collection("study_rooms").document(roomId).collection("members")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) { trySend(emptyList()); return@addSnapshotListener }
            val members = snapshot.documents.mapNotNull { doc ->
                com.quovex.domain.model.RoomMember(
                    userId = doc.id,
                    userName = doc.getString("userName") ?: "Scholar",
                    avatarId = (doc.getLong("avatarId") ?: 1L).toInt(),
                    scholarRank = doc.getString("scholarRank") ?: "Novice Scholar",
                    currentSessionMinutes = (doc.getLong("currentSessionMinutes") ?: 0L).toInt(),
                    isStudying = doc.getBoolean("isStudying") ?: true,
                    focusScore = (doc.getLong("focusScore") ?: 0L).toInt()
                )
            }
            trySend(members)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Join a study room by writing presence to study_rooms/{roomId}/members/{userId}
     */
    suspend fun joinStudyRoom(roomId: String, member: com.quovex.domain.model.RoomMember): Result<Unit> {
        return try {
            val data = hashMapOf(
                "userName" to member.userName,
                "avatarId" to member.avatarId,
                "scholarRank" to member.scholarRank,
                "currentSessionMinutes" to member.currentSessionMinutes,
                "isStudying" to member.isStudying,
                "focusScore" to member.focusScore,
                "joinedAtMillis" to System.currentTimeMillis()
            )
            firestore.collection("study_rooms").document(roomId)
                .collection("members").document(member.userId)
                .set(data, SetOptions.merge()).await()
            // Increment activeMembers count
            firestore.collection("study_rooms").document(roomId)
                .update("activeMembers", com.google.firebase.firestore.FieldValue.increment(1)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Leave a study room by deleting presence from study_rooms/{roomId}/members/{userId}
     */
    suspend fun leaveStudyRoom(roomId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("study_rooms").document(roomId)
                .collection("members").document(userId).delete().await()
            firestore.collection("study_rooms").document(roomId)
                .update("activeMembers", com.google.firebase.firestore.FieldValue.increment(-1)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Real-time listener for break-time chat messages in a study room.
     */
    fun getRoomChatFlow(roomId: String): Flow<List<com.quovex.domain.model.RoomChatMessage>> = callbackFlow {
        val ref = firestore.collection("study_rooms").document(roomId)
            .collection("chat").orderBy("timestampMillis")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) { trySend(emptyList()); return@addSnapshotListener }
            val messages = snapshot.documents.mapNotNull { doc ->
                com.quovex.domain.model.RoomChatMessage(
                    messageId = doc.id,
                    roomId = roomId,
                    senderId = doc.getString("senderId") ?: "",
                    senderName = doc.getString("senderName") ?: "Scholar",
                    messageText = doc.getString("messageText") ?: "",
                    timestampMillis = doc.getLong("timestampMillis") ?: System.currentTimeMillis(),
                    isSystemMessage = doc.getBoolean("isSystemMessage") ?: false,
                    reactionCount = (doc.getLong("reactionCount") ?: 0L).toInt()
                )
            }
            trySend(messages)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Send a break-time chat message to a study room.
     */
    suspend fun sendRoomChatMessage(roomId: String, message: com.quovex.domain.model.RoomChatMessage): Result<Unit> {
        return try {
            val data = hashMapOf(
                "senderId" to message.senderId,
                "senderName" to message.senderName,
                "messageText" to message.messageText,
                "timestampMillis" to message.timestampMillis,
                "isSystemMessage" to message.isSystemMessage,
                "reactionCount" to message.reactionCount
            )
            firestore.collection("study_rooms").document(roomId)
                .collection("chat").add(data).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    /**
     * Fetch weekly leaderboard entries for a given type (GLOBAL / FRIENDS / SUBJECT).
     * Falls back to empty list on error for offline resilience.
     */
    suspend fun getWeeklyLeaderboard(
        type: com.quovex.domain.model.LeaderboardType,
        subjectFilter: String = "ALL",
        currentUserId: String = ""
    ): List<com.quovex.domain.model.LeaderboardEntry> {
        return try {
            val collectionPath = when (type) {
                com.quovex.domain.model.LeaderboardType.GLOBAL -> "weekly_leaderboard_global"
                com.quovex.domain.model.LeaderboardType.FRIENDS -> "weekly_leaderboard_friends"
                com.quovex.domain.model.LeaderboardType.SUBJECT -> "weekly_leaderboard_subject_${subjectFilter.lowercase()}"
            }
            val snapshot = firestore.collection(collectionPath)
                .orderBy("studyMinutes", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get().await()
            snapshot.documents.mapIndexedNotNull { index, doc ->
                com.quovex.domain.model.LeaderboardEntry(
                    userId = doc.id,
                    userName = doc.getString("userName") ?: "Scholar",
                    avatarId = (doc.getLong("avatarId") ?: 1L).toInt(),
                    scholarRank = doc.getString("scholarRank") ?: "Novice Scholar",
                    studyMinutes = (doc.getLong("studyMinutes") ?: 0L).toInt(),
                    xp = (doc.getLong("xp") ?: 0L).toInt(),
                    rank = index + 1,
                    isCurrentUser = doc.id == currentUserId,
                    trend = com.quovex.domain.model.RankTrend.SAME
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    /**
     * Real-time listener for a user's friends list.
     */
    fun getFriendsFlow(userId: String): Flow<List<com.quovex.domain.model.FriendProfile>> = callbackFlow {
        val ref = firestore.collection("users").document(userId).collection("friends")
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) { trySend(emptyList()); return@addSnapshotListener }
            val friends = snapshot.documents.mapNotNull { doc ->
                com.quovex.domain.model.FriendProfile(
                    friendId = doc.id,
                    username = doc.getString("username") ?: "",
                    displayName = doc.getString("displayName") ?: "Scholar",
                    avatarId = (doc.getLong("avatarId") ?: 1L).toInt(),
                    scholarRank = doc.getString("scholarRank") ?: "Novice Scholar",
                    streakDays = (doc.getLong("streakDays") ?: 0L).toInt(),
                    totalStudyHours = (doc.getDouble("totalStudyHours") ?: 0.0).toFloat(),
                    topSubject = doc.getString("topSubject") ?: "General",
                    isStudyingNow = doc.getBoolean("isStudyingNow") ?: false
                )
            }
            trySend(friends)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Real-time listener for active study battles involving a user.
     */
    fun getStudyBattlesFlow(userId: String): Flow<List<com.quovex.domain.model.StudyBattle>> = callbackFlow {
        val ref = firestore.collection("study_battles")
            .whereEqualTo("challengerId", userId)
        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) { trySend(emptyList()); return@addSnapshotListener }
            val battles = snapshot.documents.mapNotNull { doc ->
                com.quovex.domain.model.StudyBattle(
                    battleId = doc.id,
                    challengerId = doc.getString("challengerId") ?: "",
                    challengerName = doc.getString("challengerName") ?: "",
                    challengerAvatarId = (doc.getLong("challengerAvatarId") ?: 1L).toInt(),
                    opponentId = doc.getString("opponentId") ?: "",
                    opponentName = doc.getString("opponentName") ?: "",
                    opponentAvatarId = (doc.getLong("opponentAvatarId") ?: 2L).toInt(),
                    targetExam = doc.getString("targetExam") ?: "JEE Advanced",
                    challengerMinutes = (doc.getLong("challengerMinutes") ?: 0L).toInt(),
                    opponentMinutes = (doc.getLong("opponentMinutes") ?: 0L).toInt(),
                    startDateMillis = doc.getLong("startDateMillis") ?: System.currentTimeMillis(),
                    endDateMillis = doc.getLong("endDateMillis") ?: System.currentTimeMillis(),
                    status = try {
                        com.quovex.domain.model.BattleStatus.valueOf(doc.getString("status") ?: "PENDING")
                    } catch (_: Exception) { com.quovex.domain.model.BattleStatus.PENDING }
                )
            }
            trySend(battles)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Create a new 1v1 study battle document in Firestore.
     */
    suspend fun createStudyBattle(battle: com.quovex.domain.model.StudyBattle): Result<String> {
        return try {
            val data = hashMapOf(
                "challengerId" to battle.challengerId,
                "challengerName" to battle.challengerName,
                "challengerAvatarId" to battle.challengerAvatarId,
                "opponentId" to battle.opponentId,
                "opponentName" to battle.opponentName,
                "opponentAvatarId" to battle.opponentAvatarId,
                "targetExam" to battle.targetExam,
                "challengerMinutes" to battle.challengerMinutes,
                "opponentMinutes" to battle.opponentMinutes,
                "startDateMillis" to battle.startDateMillis,
                "endDateMillis" to battle.endDateMillis,
                "status" to battle.status.name
            )
            val docRef = firestore.collection("study_battles").add(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) { Result.failure(e) }
    }
}

