package com.quovex.data.repository

import android.util.Log
import com.quovex.data.local.SessionStateManager
import com.quovex.data.local.dao.FlashcardDao
import com.quovex.data.local.dao.MaterialDao
import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.dao.QuovexDao
import com.quovex.data.local.dao.SessionDao
import com.quovex.data.local.entity.DeckEntity
import com.quovex.data.local.entity.DeckStatsProjection
import com.quovex.data.local.entity.FlashcardEntity
import com.quovex.data.local.entity.SessionEntity
import com.quovex.data.local.mapper.toDomain
import com.quovex.data.local.mapper.toEntity
import com.quovex.data.local.mapper.toLearningMaterial
import com.quovex.data.remote.FirestoreNoteDataSource
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
    private val materialDao: MaterialDao,
    private val flashcardDao: FlashcardDao,
    private val sessionDao: SessionDao,
    private val quizDao: QuizDao,
    private val sessionStateManager: SessionStateManager,
    private val firestoreNotes: FirestoreNoteDataSource
) : QuovexRepository {

    /**
     * Background scope for best-effort Firestore sync.
     * Uses SupervisorJob so one failed sync never cancels others.
     */
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Learning Materials ──────────────────────────────────────────────────

    override fun getMaterials(): Flow<List<LearningMaterial>> {
        return materialDao.getAllMaterials().map { entities -> entities.map { it.toLearningMaterial() } }
    }

    override fun getMaterialsBySubject(subject: String): Flow<List<LearningMaterial>> {
        return materialDao.getMaterialsBySubject(subject).map { entities -> entities.map { it.toLearningMaterial() } }
    }

    override suspend fun getMaterialById(id: Long): LearningMaterial? {
        return materialDao.getMaterialById(id)?.toLearningMaterial()
    }

    override suspend fun insertMaterial(material: LearningMaterial): Long {
        val localId = materialDao.insertMaterial(material.toEntity())
        syncScope.launch {
            val syncResult = firestoreNotes.saveNote(
                NoteItem(
                    id = localId,
                    cloudId = material.cloudId,
                    title = material.title,
                    subject = material.subject,
                    content = material.summary.ifBlank { material.title },
                    status = material.status,
                    inputType = material.inputType,
                    sourceUrl = material.sourceUrl,
                    storageRef = material.storageRef,
                    keyPoints = material.keyPoints,
                    flashcardCount = material.flashcardCount,
                    createdAt = material.createdAt,
                    updatedAt = material.updatedAt
                )
            )
            if (syncResult.isFailure) {
                Log.w("QuovexRepo", "Firestore material insert sync failed", syncResult.exceptionOrNull())
            }
        }
        return localId
    }

    override suspend fun updateMaterial(material: LearningMaterial): Int {
        val rows = materialDao.updateMaterial(material.toEntity())
        syncScope.launch {
            val syncResult = firestoreNotes.saveNote(
                NoteItem(
                    id = material.id,
                    cloudId = material.cloudId,
                    title = material.title,
                    subject = material.subject,
                    content = material.summary.ifBlank { material.title },
                    status = material.status,
                    inputType = material.inputType,
                    sourceUrl = material.sourceUrl,
                    storageRef = material.storageRef,
                    keyPoints = material.keyPoints,
                    flashcardCount = material.flashcardCount,
                    createdAt = material.createdAt,
                    updatedAt = material.updatedAt
                )
            )
            if (syncResult.isFailure) {
                Log.w("QuovexRepo", "Firestore material update sync failed", syncResult.exceptionOrNull())
            }
        }
        return rows
    }

    override suspend fun deleteMaterial(id: Long): Int {
        val rows = materialDao.deleteMaterialById(id)
        syncScope.launch {
            val syncResult = firestoreNotes.deleteNote(id)
            if (syncResult.isFailure) {
                Log.w("QuovexRepo", "Firestore material delete sync failed", syncResult.exceptionOrNull())
            }
        }
        return rows
    }

    override fun getDistinctMaterialSubjects(): Flow<List<String>> {
        return materialDao.getDistinctSubjects()
    }

    // ── Legacy Notes ────────────────────────────────────────────────────────

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
        syncScope.launch {
            val syncResult = firestoreNotes.saveNote(note.copy(id = localId))
            if (syncResult.isFailure) {
                Log.w("QuovexRepo", "Firestore note insert sync failed", syncResult.exceptionOrNull())
            }
        }
        return localId
    }

    override suspend fun updateNote(note: NoteItem): Int {
        val rows = dao.updateNote(note.toEntity())
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
        return flashcardDao.getAllDecks().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getDeckById(deckId: Long): DeckItem? {
        return flashcardDao.getDeckById(deckId.toInt())?.toDomain()
    }

    override suspend fun getDeckByMaterialId(materialId: Long): DeckItem? {
        return flashcardDao.getDeckByMaterialId(materialId)?.toDomain()
    }

    override suspend fun insertDeck(title: String, subject: String, sourceMaterialId: Long?): Long {
        return flashcardDao.insertDeck(
            DeckEntity(
                title = title,
                subject = subject,
                sourceMaterialId = sourceMaterialId
            )
        )
    }

    override suspend fun getMostRecentDeck(): DeckItem? {
        return flashcardDao.getMostRecentDeck()?.toDomain()
    }

    override suspend fun getDeckDueCount(deckId: Int, currentTimeMillis: Long): Int {
        return flashcardDao.getDeckDueCount(deckId, currentTimeMillis)
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
        return flashcardDao.getDueFlashcardsFlow(deckId.toInt(), currentTimeMillis)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllFlashcardsForDeck(deckId: Long): Flow<List<FlashcardItem>> {
        return flashcardDao.getFlashcardsForDeck(deckId.toInt())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun processCardReview(cardId: Long, quality: Int): FlashcardItem? {
        val card = flashcardDao.getFlashcardById(cardId.toInt()) ?: return null

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

        flashcardDao.updateFlashcard(updatedCard)
        return updatedCard.toDomain()
    }

    override suspend fun insertFlashcard(deckId: Int, frontContent: String, backContent: String): Long {
        val entity = FlashcardEntity(deckId = deckId, frontContent = frontContent, backContent = backContent)
        val id = flashcardDao.insertFlashcard(entity)
        flashcardDao.incrementDeckCardCount(deckId)
        return id
    }

    override suspend fun insertFlashcards(deckId: Int, cards: List<Pair<String, String>>): List<Long> {
        val entities = cards.map { (front, back) ->
            FlashcardEntity(deckId = deckId, frontContent = front, backContent = back)
        }
        val ids = flashcardDao.insertFlashcards(entities)
        repeat(cards.size) {
            flashcardDao.incrementDeckCardCount(deckId)
        }
        return ids
    }

    override suspend fun getTotalDueFlashcardsCount(currentTimeMillis: Long): Int {
        return flashcardDao.getTotalDueFlashcardsCount(currentTimeMillis)
    }

    override suspend fun createRemedialFlashcard(mistake: QuizMistake, deckId: Int): Long {
        val entity = FlashcardEntity(
            deckId = deckId,
            frontContent = mistake.questionText,
            backContent = "Answer: ${mistake.correctAnswer}\n\nExplanation: ${mistake.explanation}",
            isRemedial = true,
            tags = "remedial,${mistake.concept}",
            intervalDays = 0,
            repetitions = 0,
            easeFactor = 2.0f
        )
        val id = flashcardDao.insertFlashcard(entity)
        flashcardDao.incrementDeckCardCount(deckId)
        return id
    }

    // ── Quiz Engine ────────────────────────────────────────────────────────

    override suspend fun saveQuizQuestions(questions: List<QuizQuestion>): List<Long> {
        return quizDao.insertQuestions(questions.map { it.toEntity() })
    }

    override fun getQuizQuestionsForMaterial(materialId: Long): Flow<List<QuizQuestion>> {
        return quizDao.getQuestionsForMaterialFlow(materialId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getQuizQuestionsList(materialId: Long): List<QuizQuestion> {
        return quizDao.getQuestionsForMaterial(materialId).map { it.toDomain() }
    }

    override suspend fun recordQuizResult(result: QuizResult): Long {
        val resultId = quizDao.insertQuizResult(result.toEntity())
        if (result.mistakes.isNotEmpty()) {
            quizDao.insertQuizMistakes(result.mistakes.map { it.toEntity(resultId) })
        }
        return resultId
    }

    override fun getQuizResultsForMaterial(materialId: Long): Flow<List<QuizResult>> {
        return quizDao.getResultsForMaterial(materialId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getRecentMistakes(limit: Int): List<QuizMistake> {
        return quizDao.getRecentMistakes(limit).map { it.toDomain() }
    }

    // ── Sessions ──────────────────────────────────────────────────────────

    override fun getRecentSessions(limit: Int): Flow<List<RecentActivityItem>> {
        return sessionDao.getAllSessions().map { list ->
            list.take(limit).map { it.toDomain() }
        }
    }

    override suspend fun getRecentSessionsList(limit: Int): List<RecentActivityItem> {
        return sessionDao.getRecentSessionsList(limit).map { it.toDomain() }
    }

    override suspend fun recordSession(
        startTime: Long,
        endTime: Long,
        durationMinutes: Int,
        focusScore: Int,
        appBlockViolations: Int,
        subject: String
    ): Long {
        val session = SessionEntity(
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            focusScore = focusScore,
            appBlockViolations = appBlockViolations,
            subject = subject
        )
        return sessionDao.insertSession(session)
    }

    override suspend fun getTodayFocusSeconds(): Long {
        val zone = ZoneId.systemDefault()
        val startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val minutes = sessionDao.getTotalStudyMinutesSince(startOfDay) ?: 0
        return minutes * 60L
    }

    override suspend fun getTotalXp(): Long {
        val count = sessionDao.getTotalSessionsCount()
        return (count * 150L)
    }

    override suspend fun getWeeklySessionMinutes(
        startOfWeekMillis: Long,
        endOfWeekMillis: Long
    ): Map<Int, Int> {
        val zone = ZoneId.systemDefault()
        val sessions = sessionDao.getSessionsBetween(startOfWeekMillis, endOfWeekMillis)
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
        title = if (subject.isNotBlank()) "Focus Session: $subject" else "Focus Session",
        subject = subject.ifBlank { "Deep Work" },
        durationMinutes = durationMinutes,
        focusScore = focusScore,
        timestamp = startTime
    )
}
