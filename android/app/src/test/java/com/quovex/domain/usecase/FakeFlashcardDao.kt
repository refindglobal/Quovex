package com.quovex.domain.usecase

import com.quovex.data.local.dao.FlashcardDao
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.FlashcardEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFlashcardDao : FlashcardDao {
    var deckCount: Int = 0

    override suspend fun insertDeck(deck: DeckEntity): Long = 1L
    override fun getAllDecks(): Flow<List<DeckEntity>> = flowOf(emptyList())
    override suspend fun getDeckById(deckId: Int): DeckEntity? = null
    override suspend fun getDeckByMaterialId(materialId: Long): DeckEntity? = null
    override suspend fun getMostRecentDeck(): DeckEntity? = null
    override suspend fun getDeckCount(): Int = deckCount
    override suspend fun incrementDeckCardCount(deckId: Int): Int = 1
    override suspend fun insertFlashcard(flashcard: FlashcardEntity): Long = 1L
    override suspend fun insertFlashcards(flashcards: List<FlashcardEntity>): List<Long> = emptyList()
    override suspend fun updateFlashcard(flashcard: FlashcardEntity): Int = 1
    override suspend fun getFlashcardById(cardId: Int): FlashcardEntity? = null
    override fun getFlashcardsForDeck(deckId: Int): Flow<List<FlashcardEntity>> = flowOf(emptyList())
    override fun getDueFlashcardsFlow(deckId: Int, currentTimeMillis: Long): Flow<List<FlashcardEntity>> = flowOf(emptyList())
    override suspend fun getDueFlashcards(deckId: Int, currentTimeMillis: Long, limit: Int): List<FlashcardEntity> = emptyList()
    override suspend fun getTotalDueFlashcardsCount(currentTimeMillis: Long): Int = 0
    override suspend fun getDeckDueCount(deckId: Int, currentTimeMillis: Long): Int = 0
    override suspend fun getRemedialCardsDue(currentTimeMillis: Long): List<FlashcardEntity> = emptyList()
}
