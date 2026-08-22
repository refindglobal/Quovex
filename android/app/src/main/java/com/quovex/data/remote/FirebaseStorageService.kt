package com.quovex.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File

import javax.inject.Inject
import javax.inject.Singleton

/**
 * FirebaseStorageService — handles all cloud file storage for Quovex
 */
@Singleton
class FirebaseStorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadNotePdf(userId: String, noteId: String, pdfFile: File): Result<String> {
        return try {
            val ref = storage.reference.child("notes/$userId/$noteId/original.pdf")
            ref.putFile(Uri.fromFile(pdfFile)).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
