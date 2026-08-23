package com.quovex.domain.usecase

import com.quovex.domain.model.FormulaItem
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * UseCase to transform an NCERT official chapter into an interactive Quovex Learning Material.
 * Generates original Quovex study aids (Summary, Key Concepts, Formulas, Flashcards, Quiz)
 * without duplicating or copying copyrighted textbook text.
 */
class StudyNcertChapterWithAiUseCase @Inject constructor(
    private val quovexRepository: QuovexRepository,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val summarizeNoteUseCase: SummarizeNoteUseCase
) {
    suspend operator fun invoke(chapter: NcertChapter): Result<Long> {
        return try {
            val title = "${chapter.bookTitle}: Ch ${chapter.chapterNumber} - ${chapter.chapterTitle}"

            // 1. Check if material already created for this chapter
            val existingMaterials = quovexRepository.getMaterials().firstOrNull() ?: emptyList()
            val existing = existingMaterials.find {
                it.sourceUrl == chapter.officialSourceUrl || (it.title == title && it.subject == chapter.subject)
            }
            if (existing != null) {
                return Result.success(existing.id)
            }

            // 2. Create initial material entity
            val placeholderContent = "Official NCERT Study Context: Class ${chapter.classLevel} ${chapter.subject}, ${chapter.bookTitle}, Chapter ${chapter.chapterNumber}: ${chapter.chapterTitle}. Curriculum: ${chapter.curriculum}."
            val noteId = saveNoteUseCase(
                NoteItem(
                    title = title,
                    subject = chapter.subject,
                    content = placeholderContent,
                    inputType = NoteInputType.URL,
                    sourceUrl = chapter.officialSourceUrl,
                    status = NoteProcessingStatus.PROCESSING
                )
            )

            // 3. Generate original study aids with Quovex AI
            val aiContextPrompt = "Analyze and create structured comprehensive study aids for NCERT Class ${chapter.classLevel} ${chapter.subject} - Chapter ${chapter.chapterNumber}: ${chapter.chapterTitle} (${chapter.bookTitle}). Include comprehensive chapter overview, key concepts, formulas/laws, flashcards, and exam takeaways."
            val summaryResult = summarizeNoteUseCase(aiContextPrompt, chapter.subject)

            summaryResult.fold(
                onSuccess = { summaryData ->
                    val flashcardsCount = summaryData.flashcards?.size ?: 0
                    val updatedItem = NoteItem(
                        id = noteId,
                        title = title,
                        subject = chapter.subject,
                        content = summaryData.summary.ifBlank { placeholderContent },
                        status = NoteProcessingStatus.READY,
                        inputType = NoteInputType.URL,
                        sourceUrl = chapter.officialSourceUrl,
                        keyPoints = summaryData.keyPoints ?: emptyList(),
                        flashcardCount = flashcardsCount
                    )
                    saveNoteUseCase(updatedItem)
                    Result.success(noteId)
                },
                onFailure = {
                    // Fallback to ready state with basic chapter overview
                    val fallbackItem = NoteItem(
                        id = noteId,
                        title = title,
                        subject = chapter.subject,
                        content = placeholderContent,
                        keyPoints = listOf("Chapter ${chapter.chapterNumber}: ${chapter.chapterTitle}", "Official NCERT Curriculum: ${chapter.curriculum}"),
                        status = NoteProcessingStatus.READY,
                        inputType = NoteInputType.URL,
                        sourceUrl = chapter.officialSourceUrl
                    )
                    saveNoteUseCase(fallbackItem)
                    Result.success(noteId)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
