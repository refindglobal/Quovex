package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.DeckStatsProjection
import com.quovex.data.local.entity.FlashcardEntity
import com.quovex.data.local.entity.NoteEntity
import com.quovex.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuovexDao {

    // --- Notes ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity): Int

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE subject = :subject ORDER BY updatedAt DESC")
    fun getNotesBySubject(subject: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteEntity?

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long): Int

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNotesCount(): Int

    // --- Decks ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Int): DeckEntity?

    @Query("SELECT * FROM decks ORDER BY createdAt DESC LIMIT 1")
    suspend fun getMostRecentDeck(): DeckEntity?

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun getDeckCount(): Int

    @Query("UPDATE decks SET totalCards = totalCards + 1 WHERE id = :deckId")
    suspend fun incrementDeckCardCount(deckId: Int): Int

    // --- Flashcards ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Update
    suspend fun updateFlashcard(flashcard: FlashcardEntity): Int

    @Query("SELECT * FROM flashcards WHERE id = :cardId")
    suspend fun getFlashcardById(cardId: Int): FlashcardEntity?

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY id ASC")
    fun getFlashcardsForDeck(deckId: Int): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND nextReviewDate <= :currentTimeMillis ORDER BY nextReviewDate ASC")
    fun getDueFlashcardsFlow(deckId: Int, currentTimeMillis: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId AND nextReviewDate <= :currentTimeMillis LIMIT :limit")
    suspend fun getDueFlashcards(deckId: Int, currentTimeMillis: Long, limit: Int = 50): List<FlashcardEntity>

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewDate <= :currentTimeMillis")
    suspend fun getTotalDueFlashcardsCount(currentTimeMillis: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId AND nextReviewDate <= :currentTimeMillis")
    suspend fun getDeckDueCount(deckId: Int, currentTimeMillis: Long): Int

    /**
     * Aggregated deck statistics across ALL decks in a single SQL query.
     *
     * Uses conditional aggregation (SUM + CASE WHEN) to compute dueCards and masteredCards
     * without a separate query per deck. This eliminates the N+1 pattern.
     *
     * masteredCards = cards with repetitions >= 2 (successful interval spacing begun)
     * dueCards      = cards with nextReviewDate <= currentTimeMillis
     * totalCards    = COUNT of flashcards rows per deck (live count, not DeckEntity.totalCards)
     *
     * LEFT JOIN ensures decks with zero flashcards are still included.
     */
    @Query("""
        SELECT
            d.id        AS deckId,
            d.title     AS title,
            d.subject   AS subject,
            d.xpValue   AS xpValue,
            COUNT(f.id) AS totalCards,
            SUM(CASE WHEN f.nextReviewDate <= :currentTimeMillis THEN 1 ELSE 0 END) AS dueCards,
            SUM(CASE WHEN f.repetitions >= 2 THEN 1 ELSE 0 END) AS masteredCards
        FROM decks d
        LEFT JOIN flashcards f ON f.deckId = d.id
        GROUP BY d.id
        ORDER BY d.createdAt DESC
    """)
    fun getDeckStatsProjections(currentTimeMillis: Long): Flow<List<DeckStatsProjection>>

    /**
     * Aggregated statistics for a single deck — used by Deck Overview screen.
     */
    @Query("""
        SELECT
            d.id        AS deckId,
            d.title     AS title,
            d.subject   AS subject,
            d.xpValue   AS xpValue,
            COUNT(f.id) AS totalCards,
            SUM(CASE WHEN f.nextReviewDate <= :currentTimeMillis THEN 1 ELSE 0 END) AS dueCards,
            SUM(CASE WHEN f.repetitions >= 2 THEN 1 ELSE 0 END) AS masteredCards
        FROM decks d
        LEFT JOIN flashcards f ON f.deckId = d.id
        WHERE d.id = :deckId
        GROUP BY d.id
    """)
    suspend fun getDeckStatsProjection(deckId: Int, currentTimeMillis: Long): DeckStatsProjection?

    // --- Sessions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessionsList(limit: Int = 10): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE startTime >= :startTime AND startTime <= :endTime ORDER BY startTime ASC")
    suspend fun getSessionsBetween(startTime: Long, endTime: Long): List<SessionEntity>

    @Query("SELECT SUM(durationMinutes) FROM sessions WHERE startTime >= :startTime")
    suspend fun getTotalStudyMinutesSince(startTime: Long): Int?

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getTotalSessionsCount(): Int
}
