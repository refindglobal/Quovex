package com.quovex.domain.usecase

import com.quovex.domain.model.NoteItem
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class GenerateFlashcardsFromNoteUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val repository: QuovexRepository
) {
    suspend operator fun invoke(note: NoteItem): Result<Int> {
        val rawText = "${note.title}\n\n${note.content}\n\nKey Points:\n" +
                note.keyPoints.joinToString("\n") { "- $it" }

        val summarizeResult = aiRepository.summarizeNote(
            rawText = rawText,
            subject = note.subject
        )

        return summarizeResult.mapCatching { aiSummary ->
            if (aiSummary.flashcards.isEmpty()) {
                0
            } else {
                val deckId = repository.insertDeck(
                    title = "Flashcards: ${note.title}",
                    subject = note.subject
                )

                var count = 0
                aiSummary.flashcards.forEach { cardDto ->
                    val formulaSuffix = if (!cardDto.formula.isNullOrBlank()) {
                        "\n\nFormula: ${cardDto.formula}"
                    } else ""

                    repository.insertFlashcard(
                        deckId = deckId.toInt(),
                        frontContent = cardDto.question,
                        backContent = "${cardDto.answer}$formulaSuffix"
                    )
                    count++
                }

                // Update the note's flashcard count
                repository.updateNote(
                    note.copy(
                        flashcardCount = note.flashcardCount + count,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                count
            }
        }
    }
}
