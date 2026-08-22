package com.quovex.domain.usecase

import com.quovex.data.storage.FirebaseStorageHelper

/**
 * Test double for [FirebaseStorageHelper]. Records invocations without touching Firebase.
 * Extends the open no-arg constructor to avoid Android runtime dependencies.
 */
class FakeFirebaseStorageHelper : FirebaseStorageHelper(null, null) {

    val deletedPaths = mutableListOf<String>()
    var shouldFailUpload: Boolean = false

    override suspend fun uploadPdf(
        noteId: String,
        fileUri: android.net.Uri,
        fileName: String
    ): Result<String> {
        return if (shouldFailUpload) {
            Result.failure(IllegalStateException("Storage upload failed (fake)"))
        } else {
            Result.success("notes/fake-user/$noteId/$fileName")
        }
    }

    override suspend fun uploadImageBytes(
        noteId: String,
        bytes: ByteArray,
        fileName: String
    ): Result<String> {
        return if (shouldFailUpload) {
            Result.failure(IllegalStateException("Image upload failed (fake)"))
        } else {
            Result.success("notes/fake-user/$noteId/$fileName")
        }
    }

    override suspend fun deleteFile(storagePath: String): Result<Unit> {
        deletedPaths.add(storagePath)
        return Result.success(Unit)
    }
}
