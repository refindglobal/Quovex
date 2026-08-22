package com.quovex.domain.repository

import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.DeckStats
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.QuizMistake
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.model.QuizResult
import com.quovex.domain.model.RecentActivityItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer repository contract.
 * Must NOT import any Room entity or data-layer type.
 */
interface QuovexRepository {

    // --- Learning Materials (Learning Transformation System) ---
    fun getMaterials(): Flow<List<LearningMaterial>>
    fun getMaterialsBySubject(subject: String): Flow<List<LearningMaterial>>
    suspend fun getMaterialById(id: Long): LearningMaterial?
    suspend fun insertMaterial(material: LearningMaterial): Long
    suspend fun updateMaterial(material: LearningMaterial): Int
    suspend fun deleteMaterial(id: Long): Int
    fun getDistinctMaterialSubjects(): Flow<List<String>>

    // --- Legacy Notes (for backward compatibility during migration) ---
    fun getNotes(): Flow<List<NoteItem>>
    fun getNotesBySubject(subject: String): Flow<List<NoteItem>>
    suspend fun getNoteById(id: Long): NoteItem?
    suspend fun insertNote(note: NoteItem): Long
    suspend fun updateNote(note: NoteItem): Int
    suspend fun deleteNote(id: Long): Int

    // --- Decks (Domain Models) ---
    fun getDecks(): Flow<List<DeckItem>>
    suspend fun getDeckById(deckId: Long): DeckItem?
    suspend fun getDeckByMaterialId(materialId: Long): DeckItem?
    suspend fun insertDeck(title: String, subject: String, sourceMaterialId: Long? = null): Long
    suspend fun getMostRecentDeck(): DeckItem?
    suspend fun getDeckDueCount(deckId: Int, currentTimeMillis: Long): Int

    // --- Deck Stats (Aggregated — no N+1) ---
    fun getDeckStatsForAllDecks(currentTimeMillis: Long): Flow<List<DeckStats>>
    suspend fun getDeckStats(deckId: Int, currentTimeMillis: Long): DeckStats?

    // --- Flashcards ---
    fun getDueFlashcards(deckId: Long, currentTimeMillis: Long): Flow<List<FlashcardItem>>
    fun getAllFlashcardsForDeck(deckId: Long): Flow<List<FlashcardItem>>
    suspend fun processCardReview(cardId: Long, quality: Int): FlashcardItem?
    suspend fun insertFlashcard(deckId: Int, frontContent: String, backContent: String): Long
    suspend fun insertFlashcards(deckId: Int, cards: List<Pair<String, String>>): List<Long>
    suspend fun getTotalDueFlashcardsCount(currentTimeMillis: Long): Int
    suspend fun createRemedialFlashcard(mistake: QuizMistake, deckId: Int): Long

    // --- Quiz Engine ---
    suspend fun saveQuizQuestions(questions: List<QuizQuestion>): List<Long>
    fun getQuizQuestionsForMaterial(materialId: Long): Flow<List<QuizQuestion>>
    suspend fun getQuizQuestionsList(materialId: Long): List<QuizQuestion>
    suspend fun recordQuizResult(result: QuizResult): Long
    fun getQuizResultsForMaterial(materialId: Long): Flow<List<QuizResult>>
    suspend fun getRecentMistakes(limit: Int = 20): List<QuizMistake>

    // --- Sessions & Dashboard Stats ---
    fun getRecentSessions(limit: Int = 10): Flow<List<RecentActivityItem>>
    suspend fun getRecentSessionsList(limit: Int = 10): List<RecentActivityItem>
    suspend fun recordSession(
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        focusScore: Int,
        appBlockViolations: Int,
        subject: String = ""
    ): Long
    suspend fun getTodayFocusSeconds(): Long
    suspend fun getTotalXp(): Long
    suspend fun getWeeklySessionMinutes(startOfWeekMillis: Long, endOfWeekMillis: Long): Map<Int, Int>

    // --- Active Session State ---
    fun getActiveSessionState(): Flow<ActiveSessionState>
    fun updateActiveSessionState(isActive: Boolean, remainingSeconds: Int, totalSeconds: Int, subject: String)
}
