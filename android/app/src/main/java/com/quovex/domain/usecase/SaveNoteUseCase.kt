package com.quovex.domain.usecase

import com.quovex.domain.model.NoteItem
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class SaveNoteUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    suspend operator fun invoke(note: NoteItem): Long {
        return if (note.id == 0L) {
            repository.insertNote(
                note.copy(
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            repository.updateNote(
                note.copy(
                    updatedAt = System.currentTimeMillis()
                )
            )
            note.id
        }
    }
}
