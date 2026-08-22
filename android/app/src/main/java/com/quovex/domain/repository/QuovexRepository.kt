package com.quovex.domain.repository

import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.DeckStats
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.RecentActivityItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain-layer repository contract.
 * Must NOT import any Room entity or data-layer type.
 */
interface QuovexRepository {

    // --- Notes (Domain Models) ---
    fun getNotes(): Flow<List<NoteItem>>
    fun getNotesBySubject(subject: String): Flow<List<NoteItem>>
    suspend fun getNoteById(id: Long): NoteItem?
    suspend fun insertNote(note: NoteItem): Long
    suspend fun updateNote(note: NoteItem): Int
    suspend fun deleteNote(id: Long): Int

    // --- Decks (Domain Models) ---
    fun getDecks(): Flow<List<DeckItem>>
    suspend fun getDeckById(deckId: Long): DeckItem?
    suspend fun insertDeck(title: String, subject: String): Long
    suspend fun getMostRecentDeck(): DeckItem?
    suspend fun getDeckDueCount(deckId: Int, currentTimeMillis: Long): Int

    // --- Deck Stats (Aggregated — no N+1) ---
    /**
     * Returns aggregated stats for ALL decks in a single query.
     * Backed by a SQL LEFT JOIN + conditional aggregation in the data layer.
     */
    fun getDeckStatsForAllDecks(currentTimeMillis: Long): Flow<List<DeckStats>>

    /**
     * Returns aggregated stats for a single deck. Used by Deck Overview screen.
     */
    suspend fun getDeckStats(deckId: Int, currentTimeMillis: Long): DeckStats?

    // --- Flashcards (Domain Models — no FlashcardEntity leakage) ---
    fun getDueFlashcards(deckId: Long, currentTimeMillis: Long): Flow<List<FlashcardItem>>
    fun getAllFlashcardsForDeck(deckId: Long): Flow<List<FlashcardItem>>
    suspend fun processCardReview(cardId: Long, quality: Int): FlashcardItem?
    suspend fun insertFlashcard(deckId: Int, frontContent: String, backContent: String): Long
    suspend fun getTotalDueFlashcardsCount(currentTimeMillis: Long): Int

    // --- Sessions & Dashboard Stats ---
    fun getRecentSessions(limit: Int = 10): Flow<List<RecentActivityItem>>
    suspend fun getRecentSessionsList(limit: Int = 10): List<RecentActivityItem>
    suspend fun recordSession(
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        focusScore: Int,
        appBlockViolations: Int
    ): Long
    suspend fun getTodayFocusSeconds(): Long
    suspend fun getTotalXp(): Long
    suspend fun getWeeklySessionMinutes(startOfWeekMillis: Long, endOfWeekMillis: Long): Map<Int, Int>

    // --- Active Session State ---
    fun getActiveSessionState(): Flow<ActiveSessionState>
    fun updateActiveSessionState(isActive: Boolean, remainingSeconds: Int, totalSeconds: Int, subject: String)
}
