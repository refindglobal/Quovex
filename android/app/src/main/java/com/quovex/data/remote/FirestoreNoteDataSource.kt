package com.quovex.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.quovex.domain.model.NoteItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore data source for note metadata synchronization.
 *
 * Architecture: Firestore = canonical cloud note metadata, Room = local offline cache.
 *
 * Document path: users/{uid}/notes/{localId}
 *
 * Large source files (PDFs, scanned images) are stored in Firebase Storage
 * referenced via [NoteItem.storageRef] — never in Firestore.
 *
 * All operations are best-effort: failures are logged and returned as Result.failure
 * but MUST NOT block local Room operations. The caller (QuovexRepositoryImpl) decides
 * whether to propagate or swallow the error.
 */
@Singleton
class FirestoreNoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun currentUid(): String? = auth.currentUser?.uid

    private fun notesCollection(uid: String) =
        firestore.collection("users").document(uid).collection("notes")

    /**
     * Upsert a note to Firestore.
     * Call after a successful Room insert/update.
     * @param note The domain NoteItem (local id used as Firestore doc id for simplicity)
     */
    suspend fun saveNote(note: NoteItem): Result<Unit> {
        val uid = currentUid() ?: return Result.failure(
            IllegalStateException("User not authenticated — note will sync after sign-in")
        )
        return try {
            val data = hashMapOf(
                "localId" to note.id,
                "cloudId" to note.cloudId,
                "title" to note.title,
                "subject" to note.subject,
                "content" to note.content.take(8000), // Firestore 1MB doc limit guard
                "status" to note.status.name,
                "inputType" to note.inputType.name,
                "sourceUrl" to note.sourceUrl,
                "storageRef" to note.storageRef,
                "keyPoints" to note.keyPoints,
                "flashcardCount" to note.flashcardCount,
                "createdAt" to note.createdAt,
                "updatedAt" to note.updatedAt,
                "uid" to uid
            )
            notesCollection(uid)
                .document(note.id.toString())
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a note document from Firestore.
     * Call after a successful Room delete.
     */
    suspend fun deleteNote(noteId: Long): Result<Unit> {
        val uid = currentUid() ?: return Result.failure(
            IllegalStateException("User not authenticated — skipping Firestore delete")
        )
        return try {
            notesCollection(uid)
                .document(noteId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch all notes for the current user from Firestore.
     * Used for initial sync from cloud to Room on new device sign-in.
     * Returns empty list on any error (Room cache is used instead).
     */
    suspend fun fetchAllNotes(): List<NoteItem> {
        val uid = currentUid() ?: return emptyList()
        return try {
            val snapshot = notesCollection(uid).get().await()
            snapshot.documents.mapNotNull { doc ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    val keyPoints = (doc.get("keyPoints") as? List<String>) ?: emptyList()
                    NoteItem(
                        id = doc.getLong("localId") ?: 0L,
                        cloudId = doc.getString("cloudId"),
                        title = doc.getString("title") ?: return@mapNotNull null,
                        subject = doc.getString("subject") ?: "General",
                        content = doc.getString("content") ?: "",
                        status = enumValueOrDefault("status", doc.getString("status")),
                        inputType = enumValueOrDefault("inputType", doc.getString("inputType")),
                        sourceUrl = doc.getString("sourceUrl"),
                        storageRef = doc.getString("storageRef"),
                        keyPoints = keyPoints,
                        flashcardCount = (doc.getLong("flashcardCount") ?: 0L).toInt(),
                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                        updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                    )
                } catch (_: Exception) {
                    null // Skip malformed documents
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("FirestoreNoteDS", "fetchAllNotes failed — using Room cache", e)
            emptyList()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private inline fun <reified T : Enum<T>> enumValueOrDefault(
        fieldName: String,
        value: String?
    ): T {
        return try {
            enumValueOf<T>(value ?: "")
        } catch (_: Exception) {
            // Return first value (assumed to be the safe default: READY / TEXT)
            enumValues<T>().first()
        }
    }
}
