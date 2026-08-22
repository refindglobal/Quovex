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
                        "createdBy" to "system"
                    ),
                    hashMapOf(
                        "name" to "Organic Chemistry Mechanisms",
                        "subject" to "Chemistry",
                        "activeMembers" to 28,
                        "targetExam" to "NEET / JEE",
                        "createdBy" to "system"
                    ),
                    hashMapOf(
                        "name" to "Calculus & Differential Equations",
                        "subject" to "Maths",
                        "activeMembers" to 35,
                        "targetExam" to "JEE Advanced",
                        "createdBy" to "system"
                    ),
                    hashMapOf(
                        "name" to "Cell Biology & Genetics Flashcards",
                        "subject" to "Biology",
                        "activeMembers" to 19,
                        "targetExam" to "NEET UG",
                        "createdBy" to "system"
                    )
                )

                sampleRooms.forEach { roomData ->
                    firestore.collection("study_rooms").add(roomData).await()
                }
            }
        } catch (_: Exception) {}
    }
}
