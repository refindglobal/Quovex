package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity): Long

    @Query("SELECT * FROM decks ORDER BY createdAt DESC")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE id = :deckId")
    suspend fun getDeckById(deckId: Int): DeckEntity?

    @Query("SELECT * FROM decks WHERE sourceMaterialId = :materialId LIMIT 1")
    suspend fun getDeckByMaterialId(materialId: Long): DeckEntity?

    @Query("SELECT * FROM decks ORDER BY createdAt DESC LIMIT 1")
    suspend fun getMostRecentDeck(): DeckEntity?

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun getDeckCount(): Int

    @Query("UPDATE decks SET totalCards = totalCards + 1 WHERE id = :deckId")
    suspend fun incrementDeckCardCount(deckId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<FlashcardEntity>): List<Long>

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

    @Query("SELECT * FROM flashcards WHERE isRemedial = 1 AND nextReviewDate <= :currentTimeMillis")
    suspend fun getRemedialCardsDue(currentTimeMillis: Long): List<FlashcardEntity>
}
