package com.quovex.domain.usecase

import com.quovex.domain.model.SubjectCatalog
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Provides configured subjects for study sessions.
 *
 * Priority:
 * 1. Subjects from the user's actual flashcard decks (personalized to their stream)
 * 2. Full universal catalog from [SubjectCatalog] (ALL streams — Science, Commerce, Humanities, Languages)
 *
 * IMPORTANT: Never hardcode subjects in UI files.
 * All subject lists are sourced from this use case or directly from [SubjectCatalog].
 */
class GetConfiguredSubjectsUseCase @Inject constructor(
    private val repository: QuovexRepository
) {

    operator fun invoke(): Flow<List<String>> {
        return repository.getDecks().map { decks ->
            val userSubjects = decks
                .map { it.subject.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            if (userSubjects.isNotEmpty()) {
                // User has decks — include their subjects first, then supplement with full catalog
                val supplemented = userSubjects + SubjectCatalog.chatSelectorNames
                    .filterNot { it in userSubjects }
                supplemented
            } else {
                // No decks yet — show full universal catalog (not Science-only!)
                SubjectCatalog.chatSelectorNames
            }
        }
    }
}

