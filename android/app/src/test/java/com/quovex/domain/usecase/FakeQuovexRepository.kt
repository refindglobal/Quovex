package com.quovex.domain.usecase

import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.DeckStats
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.RecentActivityItem
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class FakeQuovexRepository : QuovexRepository {
    var todaySeconds: Long = 0L
    var totalDueFlashcards: Int = 0
    var mostRecentDeck: DeckItem? = null
    var deckDueCount: Int = 0
    var weeklyMap: Map<Int, Int> = emptyMap()
    val activeSessionFlow = MutableStateFlow(ActiveSessionState())

    // Test fixtures for notes
    val notesMap = mutableMapOf<Long, NoteItem>()
    var nextNoteId: Long = 1L

    override fun getNotes(): Flow<List<NoteItem>> = flowOf(notesMap.values.toList().sortedByDescending { it.updatedAt })
    override fun getNotesBySubject(subject: String): Flow<List<NoteItem>> =
        flowOf(notesMap.values.filter { it.subject.equals(subject, ignoreCase = true) }.sortedByDescending { it.updatedAt })

    override suspend fun getNoteById(id: Long): NoteItem? = notesMap[id]
    override suspend fun insertNote(note: NoteItem): Long {
        val id = if (note.id == 0L) nextNoteId++ else note.id
        notesMap[id] = note.copy(id = id)
        return id
    }
    override suspend fun updateNote(note: NoteItem): Int {
        notesMap[note.id] = note
        return 1
    }
    override suspend fun deleteNote(id: Long): Int {
        return if (notesMap.remove(id) != null) 1 else 0
    }

    // Test fixtures for deck stats & flashcards
    var decksList: List<DeckItem> = emptyList()
    var deckStatsList: List<DeckStats> = emptyList()
    val deckStatsMap = mutableMapOf<Int, DeckStats>()
    val dueCardsMap = mutableMapOf<Long, List<FlashcardItem>>()
    val allCardsMap = mutableMapOf<Long, List<FlashcardItem>>()
    val reviewedCardMap = mutableMapOf<Long, FlashcardItem>()
    var lastReviewedQuality: Int? = null
    var shouldThrowOnDeckStats: Boolean = false
    var shouldThrowOnCards: Boolean = false
    var shouldThrowOnReview: Boolean = false

    override fun getDecks(): Flow<List<DeckItem>> = flowOf(decksList)
    override suspend fun getDeckById(deckId: Long): DeckItem? =
        decksList.find { it.id.toLong() == deckId } ?: mostRecentDeck
    override suspend fun insertDeck(title: String, subject: String): Long = 1L
    override suspend fun getMostRecentDeck(): DeckItem? = mostRecentDeck
    override suspend fun getDeckDueCount(deckId: Int, currentTimeMillis: Long): Int = deckDueCount

    // Aggregated stats
    override fun getDeckStatsForAllDecks(currentTimeMillis: Long): Flow<List<DeckStats>> = flow {
        if (shouldThrowOnDeckStats) throw RuntimeException("DB error")
        emit(deckStatsList)
    }

    override suspend fun getDeckStats(deckId: Int, currentTimeMillis: Long): DeckStats? {
        if (shouldThrowOnDeckStats) throw RuntimeException("DB error")
        return deckStatsMap[deckId]
    }

    // Flashcards
    override fun getDueFlashcards(deckId: Long, currentTimeMillis: Long): Flow<List<FlashcardItem>> = flow {
        if (shouldThrowOnCards) throw RuntimeException("DB error")
        emit(dueCardsMap[deckId] ?: emptyList())
    }

    override fun getAllFlashcardsForDeck(deckId: Long): Flow<List<FlashcardItem>> = flow {
        if (shouldThrowOnCards) throw RuntimeException("DB error")
        emit(allCardsMap[deckId] ?: emptyList())
    }

    override suspend fun processCardReview(cardId: Long, quality: Int): FlashcardItem? {
        if (shouldThrowOnReview) throw RuntimeException("Review error")
        lastReviewedQuality = quality
        return reviewedCardMap[cardId]
    }

    override suspend fun insertFlashcard(deckId: Int, frontContent: String, backContent: String): Long = 1L
    override suspend fun getTotalDueFlashcardsCount(currentTimeMillis: Long): Int = totalDueFlashcards

    override fun getRecentSessions(limit: Int): Flow<List<RecentActivityItem>> = flowOf(emptyList())
    override suspend fun getRecentSessionsList(limit: Int): List<RecentActivityItem> = emptyList()
    override suspend fun recordSession(
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        focusScore: Int,
        appBlockViolations: Int
    ): Long = 1L
    override suspend fun getTodayFocusSeconds(): Long = todaySeconds
    override suspend fun getTotalXp(): Long = 500L
    override suspend fun getWeeklySessionMinutes(startOfWeekMillis: Long, endOfWeekMillis: Long): Map<Int, Int> = weeklyMap

    override fun getActiveSessionState(): Flow<ActiveSessionState> = activeSessionFlow.asStateFlow()
    override fun updateActiveSessionState(isActive: Boolean, remainingSeconds: Int, totalSeconds: Int, subject: String) {
        activeSessionFlow.value = ActiveSessionState(
            isActive = isActive,
            remainingSeconds = remainingSeconds,
            totalSeconds = totalSeconds,
            subject = subject,
            status = if (isActive) com.quovex.domain.model.SessionStatus.RUNNING else com.quovex.domain.model.SessionStatus.IDLE
        )
    }
}
