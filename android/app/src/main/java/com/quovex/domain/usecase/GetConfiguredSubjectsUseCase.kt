package com.quovex.domain.usecase

import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Provides configured subjects for study sessions.
 * Prioritizes subjects found in the user's actual decks, and supplements with the
 * standard academic catalog when the user has no decks or few subjects configured.
 */
class GetConfiguredSubjectsUseCase @Inject constructor(
    private val repository: QuovexRepository
) {

    companion object {
        val defaultSubjectCatalog = listOf(
            "Physics",
            "Chemistry",
            "Mathematics",
            "Biology",
            "History",
            "Computer Science"
        )
    }

    operator fun invoke(): Flow<List<String>> {
        return repository.getDecks().map { decks ->
            val userSubjects = decks.map { it.subject.trim() }.filter { it.isNotBlank() }.distinct()
            if (userSubjects.isNotEmpty()) {
                userSubjects
            } else {
                defaultSubjectCatalog
            }
        }
    }
}
