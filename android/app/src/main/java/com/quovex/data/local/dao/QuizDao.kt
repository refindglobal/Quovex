package com.quovex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.quovex.data.local.entity.QuizMistakeEntity
import com.quovex.data.local.entity.QuizQuestionEntity
import com.quovex.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>): List<Long>

    @Query("SELECT * FROM quiz_questions WHERE materialId = :materialId")
    suspend fun getQuestionsForMaterial(materialId: Long): List<QuizQuestionEntity>

    @Query("SELECT * FROM quiz_questions WHERE materialId = :materialId")
    fun getQuestionsForMaterialFlow(materialId: Long): Flow<List<QuizQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizMistakes(mistakes: List<QuizMistakeEntity>): List<Long>

    @Query("SELECT * FROM quiz_results WHERE materialId = :materialId ORDER BY takenAt DESC")
    fun getResultsForMaterial(materialId: Long): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_results ORDER BY takenAt DESC LIMIT :limit")
    fun getRecentQuizResults(limit: Int = 10): Flow<List<QuizResultEntity>>

    @Query("SELECT * FROM quiz_mistakes WHERE resultId = :resultId")
    suspend fun getMistakesForResult(resultId: Long): List<QuizMistakeEntity>

    @Query("SELECT * FROM quiz_mistakes ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentMistakes(limit: Int = 20): List<QuizMistakeEntity>

    @Query("SELECT * FROM quiz_mistakes WHERE remedialCardId IS NULL ORDER BY id DESC")
    suspend fun getUnremediedMistakes(): List<QuizMistakeEntity>

    @Query("SELECT * FROM quiz_mistakes WHERE concept = :concept ORDER BY id DESC")
    suspend fun getMistakesForConcept(concept: String): List<QuizMistakeEntity>

    @Query("UPDATE quiz_mistakes SET remedialCardId = :cardId WHERE id = :mistakeId")
    suspend fun updateRemedialCardId(mistakeId: Long, cardId: Long): Int

    @Query("SELECT COUNT(*) FROM quiz_results")
    suspend fun getTotalQuizCount(): Int
}
