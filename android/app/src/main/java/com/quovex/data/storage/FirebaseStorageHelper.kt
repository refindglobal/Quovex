package com.quovex.data.storage

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class FirebaseStorageHelper @Inject constructor(
    private val storage: FirebaseStorage?,
    private val auth: FirebaseAuth?
) {
    constructor() : this(null, null)

    open suspend fun uploadPdf(
        noteId: String,
        fileUri: Uri,
        fileName: String = "original.pdf"
    ): Result<String> {
        return try {
            val userId = auth?.currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated for storage upload"))

            val storagePath = "notes/$userId/$noteId/$fileName"
            val ref = storage?.reference?.child(storagePath)
                ?: return Result.failure(IllegalStateException("Storage reference unavailable"))

            val metadata = StorageMetadata.Builder()
                .setContentType("application/pdf")
                .build()

            ref.putFile(fileUri, metadata).await()
            Result.success(storagePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun uploadImageBytes(
        noteId: String,
        bytes: ByteArray,
        fileName: String = "scan.jpg"
    ): Result<String> {
        return try {
            val userId = auth?.currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not authenticated for storage upload"))

            val storagePath = "notes/$userId/$noteId/$fileName"
            val ref = storage?.reference?.child(storagePath)
                ?: return Result.failure(IllegalStateException("Storage reference unavailable"))

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            ref.putBytes(bytes, metadata).await()
            Result.success(storagePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun deleteFile(storagePath: String): Result<Unit> {
        return try {
            if (storagePath.isNotBlank() && storage != null) {
                storage.reference.child(storagePath).delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
