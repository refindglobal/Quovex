package com.quovex.domain.usecase

import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.originals.OriginalChapter
import com.quovex.domain.model.originals.QuovexOriginalBook
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class OriginalStudyAidsResult(
    val materialId: Long,
    val deckId: Long
)

/**
 * UseCase that ingests an Original Chapter's flashcards and quiz questions
 * directly into the student's local Room database, enabling seamless practice
 * with the existing Quiz engine and SM-2 Flashcard player without duplicating code.
 */
class PrepareOriginalChapterStudyAidsUseCase @Inject constructor(
    private val quovexRepository: QuovexRepository,
    private val saveNoteUseCase: SaveNoteUseCase
) {
    suspend operator fun invoke(book: QuovexOriginalBook, chapter: OriginalChapter): Result<OriginalStudyAidsResult> {
        return try {
            val title = "${book.title}: Ch ${chapter.chapterNumber} - ${chapter.title}"

            // 1. Check if NoteItem already exists for this book chapter
            val existingMaterials = quovexRepository.getMaterials().firstOrNull() ?: emptyList()
            var material = existingMaterials.find { it.title == title && it.subject == book.subject }
            val noteId: Long

            if (material != null) {
                noteId = material.id
            } else {
                noteId = saveNoteUseCase(
                    NoteItem(
                        title = title,
                        subject = book.subject,
                        content = chapter.summary.ifBlank { "Quovex Original Study Context: ${book.title} Chapter ${chapter.chapterNumber}: ${chapter.title}" },
                        inputType = NoteInputType.TEXT,
                        status = NoteProcessingStatus.READY,
                        keyPoints = chapter.sections.flatMap { it.summaryPoints }.take(10),
                        flashcardCount = chapter.flashcards.size
                    )
                )
            }

            // 2. Ingest Flashcards into Room Deck if present
            var deck = quovexRepository.getDeckByMaterialId(noteId)
            val deckId: Long
            if (deck != null) {
                deckId = deck.id.toLong()
            } else {
                deckId = quovexRepository.insertDeck(
                    title = "${book.title} - Ch ${chapter.chapterNumber}",
                    subject = book.subject,
                    sourceMaterialId = noteId
                )
                if (chapter.flashcards.isNotEmpty()) {
                    val cardsToInsert = chapter.flashcards.map {
                        Pair(it.frontPrompt, it.backAnswer)
                    }
                    quovexRepository.insertFlashcards(deckId.toInt(), cardsToInsert)
                }
            }

            // 3. Ingest Quiz Questions into Room if present
            if (chapter.quizQuestions.isNotEmpty()) {
                val existingQuestions = quovexRepository.getQuizQuestionsList(noteId)
                if (existingQuestions.isEmpty()) {
                    val questionsToInsert = chapter.quizQuestions.map { q ->
                        QuizQuestion(
                            materialId = noteId,
                            question = q.question,
                            options = q.options,
                            correctIndex = q.correctIndex,
                            explanation = q.pedagogicalExplanation,
                            relatedConcept = q.formulaReference ?: chapter.title,
                            difficulty = 3
                        )
                    }
                    quovexRepository.saveQuizQuestions(questionsToInsert)
                }
            }

            Result.success(OriginalStudyAidsResult(materialId = noteId, deckId = deckId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
