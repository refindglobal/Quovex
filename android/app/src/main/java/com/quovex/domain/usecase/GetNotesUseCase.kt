package com.quovex.domain.usecase

import com.quovex.domain.model.NoteItem
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetNotesUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    operator fun invoke(
        selectedSubject: String = "All",
        searchQuery: String = ""
    ): Flow<List<NoteItem>> {
        val baseFlow = if (selectedSubject.equals("All", ignoreCase = true)) {
            repository.getNotes()
        } else {
            repository.getNotesBySubject(selectedSubject)
        }

        return baseFlow.map { notes ->
            if (searchQuery.isBlank()) {
                notes
            } else {
                val query = searchQuery.trim().lowercase()
                notes.filter { note ->
                    note.title.lowercase().contains(query) ||
                    note.content.lowercase().contains(query) ||
                    note.subject.lowercase().contains(query) ||
                    note.keyPoints.any { it.lowercase().contains(query) }
                }
            }
        }
    }
}
