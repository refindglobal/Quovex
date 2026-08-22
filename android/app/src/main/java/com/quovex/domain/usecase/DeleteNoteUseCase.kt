package com.quovex.domain.usecase

import com.quovex.data.storage.FirebaseStorageHelper
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class DeleteNoteUseCase @Inject constructor(
    private val repository: QuovexRepository,
    private val storageHelper: FirebaseStorageHelper
) {
    suspend operator fun invoke(id: Long, storageRef: String? = null): Int {
        // Clean up associated file in Firebase Storage if attached
        if (!storageRef.isNullOrBlank()) {
            try {
                storageHelper.deleteFile(storageRef)
            } catch (_: Exception) {
                // Ignore storage deletion errors to ensure local DB cleanup succeeds
            }
        }
        return repository.deleteNote(id)
    }
}
