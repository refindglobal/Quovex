package com.quovex.domain.usecase

import com.quovex.domain.model.NoteItem
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class GetNoteByIdUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    suspend operator fun invoke(id: Long): NoteItem? {
        return repository.getNoteById(id)
    }
}
