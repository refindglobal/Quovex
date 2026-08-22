package com.quovex.data.repository

import android.util.Log
import com.quovex.data.local.SessionStateManager
import com.quovex.data.local.dao.QuovexDao
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.DeckStatsProjection
import com.quovex.data.local.entity.FlashcardEntity
import com.quovex.data.local.entity.SessionEntity
import com.quovex.data.local.mapper.toDomain
import com.quovex.data.local.mapper.toEntity
import com.quovex.data.remote.FirestoreNoteDataSource
import com.quovex.domain.model.ActiveSessionState
import com.quovex.domain.model.DeckItem
import com.quovex.domain.model.DeckStats
import com.quovex.domain.model.FlashcardItem
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.RecentActivityItem
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.Sm2Calculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuovexRepositoryImpl @Inject constructor(
    private val dao: QuovexDao,
    private val sessionStateManager: SessionStateManager,
    private val firestoreNotes: FirestoreNoteDataSource
) : QuovexRepository {

    /**
     * Background scope for best-effort Firestore sync.
     * Uses SupervisorJob so one failed sync never cancels others.
     */
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Notes ─────────────────────────────────────────────────────────────

    override fun getNotes(): Flow<List<NoteItem>> {
        return dao.getAllNotes().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getNotesBySubject(subject: String): Flow<List<NoteItem>> {
        return dao.getNotesBySubject(subject).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getNoteById(id: Long): NoteItem? {
        return dao.getNoteById(id)?.toDomain()
    }

    override suspend fun insertNote(note: NoteItem): Long {
        val localId = dao.insertNote(note.toEntity())
        // Best-effort Firestore sync — never blocks the local operation
        syncScope.launch {
            val syncResult = firestoreNotes.saveNote(note.copy(id = localId))
            if (syncResult.isFailure) {
                Log.w("QuovexRepo", "Firestore note insert sync failed (will retry on next save)", syncResult.exceptionOrNull())
            }
        }
        return localId
    }

    override suspend fun updateNote(note: NoteItem): Int {
        val rows = dao.updateNote(note.toEntity())
        // Best-effort Firestore sync
        syncScope.launch {
            val syncResult = firestoreNotes.saveNote(note)
            if (syncResult.isFailure) {
                Log.w("QuovexRepo", "Firestore note update sync failed", syncResult.exceptionOrNull())
            }
        }
        return rows
    }

    override suspend fun deleteNote(id: Long): Int {
        val rows = dao.deleteNoteById(id)
        // Best-effort Firestore sync — delete the cloud document
        syncScope.launch {
            val syncResult = firestoreNotes.deleteNote(id)
            if (syncResult.isFailure) {
                Log.w("QuovexRepo", "Firestore note delete sync failed", syncResult.exceptionOrNull())
            }
        }
        return rows
    }

    // ── Decks ─────────────────────────────────────────────────────────────

    override fun getDecks(): Flow<List<DeckItem>> {
        return dao.getAllDecks().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDeckById(deckId: Long): DeckItem? {
        return dao.getDeckById(deckId.toInt())?.toDomain()
    }

    override suspend fun insertDeck(title: String, subject: String): Long {
        return dao.insertDeck(DeckEntity(title = title, subject = subject))
    }

    override suspend fun getMostRecentDeck(): DeckItem? {
        return dao.getMostRecentDeck()?.toDomain()
    }

    override suspend fun getDeckDueCount(deckId: Int, currentTimeMillis: Long): Int {
        return dao.getDeckDueCount(deckId, currentTimeMillis)
    }

    // ── Deck Stats (aggregated — no N+1) ──────────────────────────────────

    override fun getDeckStatsForAllDecks(currentTimeMillis: Long): Flow<List<DeckStats>> {
        return dao.getDeckStatsProjections(currentTimeMillis).map { projections ->
            projections.map { it.toDomain() }
        }
    }

    override suspend fun getDeckStats(deckId: Int, currentTimeMillis: Long): DeckStats? {
        return dao.getDeckStatsProjection(deckId, currentTimeMillis)?.toDomain()
    }

    // ── Flashcards ────────────────────────────────────────────────────────

    override fun getDueFlashcards(deckId: Long, currentTimeMillis: Long): Flow<List<FlashcardItem>> {
        return dao.getDueFlashcardsFlow(deckId.toInt(), currentTimeMillis)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllFlashcardsForDeck(deckId: Long): Flow<List<FlashcardItem>> {
        return dao.getFlashcardsForDeck(deckId.toInt())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun processCardReview(cardId: Long, quality: Int): FlashcardItem? {
        val card = dao.getFlashcardById(cardId.toInt()) ?: return null

        val sm2Result = Sm2Calculator.calculate(
            quality = quality,
            currentRepetitions = card.repetitions,
            currentIntervalDays = card.intervalDays,
            currentEf = card.easeFactor
        )

        val updatedCard = card.copy(
            repetitions = sm2Result.repetitions,
            intervalDays = sm2Result.intervalDays,
            easeFactor = sm2Result.easinessFactor,
            nextReviewDate = sm2Result.nextReviewAtMillis
        )

        dao.updateFlashcard(updatedCard)
        return updatedCard.toDomain()
    }

    override suspend fun insertFlashcard(deckId: Int, frontContent: String, backContent: String): Long {
        val entity = FlashcardEntity(deckId = deckId, frontContent = frontContent, backContent = backContent)
        val id = dao.insertFlashcard(entity)
        dao.incrementDeckCardCount(deckId)
        return id
    }

    override suspend fun getTotalDueFlashcardsCount(currentTimeMillis: Long): Int {
        return dao.getTotalDueFlashcardsCount(currentTimeMillis)
    }

    // ── Sessions ──────────────────────────────────────────────────────────

    override fun getRecentSessions(limit: Int): Flow<List<RecentActivityItem>> {
        return dao.getAllSessions().map { list ->
            list.take(limit).map { it.toDomain() }
        }
    }

    override suspend fun getRecentSessionsList(limit: Int): List<RecentActivityItem> {
        return dao.getRecentSessionsList(limit).map { it.toDomain() }
    }

    override suspend fun recordSession(
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        focusScore: Int,
        appBlockViolations: Int
    ): Long {
        val session = SessionEntity(
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            focusScore = focusScore,
            appBlockViolations = appBlockViolations
        )
        return dao.insertSession(session)
    }

    override suspend fun getTodayFocusSeconds(): Long {
        val zone = ZoneId.systemDefault()
        val startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val minutes = dao.getTotalStudyMinutesSince(startOfDay) ?: 0
        return minutes * 60L
    }

    override suspend fun getTotalXp(): Long {
        val count = dao.getTotalSessionsCount()
        return (count * 150L)
    }

    override suspend fun getWeeklySessionMinutes(
        startOfWeekMillis: Long,
        endOfWeekMillis: Long
    ): Map<Int, Int> {
        val zone = ZoneId.systemDefault()
        val sessions = dao.getSessionsBetween(startOfWeekMillis, endOfWeekMillis)
        val minutesMap = mutableMapOf<Int, Int>()
        for (day in 1..7) minutesMap[day] = 0
        sessions.forEach { session ->
            val localDate = Instant.ofEpochMilli(session.startTime).atZone(zone).toLocalDate()
            val dayOfWeek = localDate.dayOfWeek.value
            minutesMap[dayOfWeek] = (minutesMap[dayOfWeek] ?: 0) + session.durationMinutes
        }
        return minutesMap
    }

    override fun getActiveSessionState(): Flow<ActiveSessionState> {
        return sessionStateManager.activeSession
    }

    override fun updateActiveSessionState(
        isActive: Boolean,
        remainingSeconds: Int,
        totalSeconds: Int,
        subject: String
    ) {
        sessionStateManager.updateActiveSession(isActive, remainingSeconds, totalSeconds, subject)
    }

    // ── Private Mappers ────────────────────────────────────────────────────
    // All Room entities are mapped here in the data layer.
    // Nothing above this boundary knows about Room entities.

    private fun DeckEntity.toDomain() = DeckItem(
        id = id,
        title = title,
        subject = subject,
        totalCards = totalCards,
        createdAt = createdAt,
        xpValue = xpValue
    )

    private fun FlashcardEntity.toDomain() = FlashcardItem(
        id = id,
        deckId = deckId,
        frontContent = frontContent,
        backContent = backContent,
        easeFactor = easeFactor,
        intervalDays = intervalDays,
        repetitions = repetitions,
        nextReviewDate = nextReviewDate
    )

    private fun DeckStatsProjection.toDomain(): DeckStats {
        val learning = (totalCards - masteredCards - dueCards).coerceAtLeast(0)
        return DeckStats(
            deckId = deckId,
            title = title,
            subject = subject,
            totalCards = totalCards,
            dueCards = dueCards,
            masteredCards = masteredCards,
            learningCards = learning,
            xpValue = xpValue
        )
    }

    private fun SessionEntity.toDomain() = RecentActivityItem(
        id = id,
        title = "Focus Session",
        subject = "Deep Work",
        durationMinutes = durationMinutes,
        focusScore = focusScore,
        timestamp = startTime
    )
}
