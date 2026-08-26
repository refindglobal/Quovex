package com.quovex.domain.usecase

import com.quovex.data.local.dao.QuizDao
import com.quovex.data.local.entity.QuizMistakeEntity
import com.quovex.data.local.entity.QuizQuestionEntity
import com.quovex.data.local.entity.QuizResultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeQuizDao : QuizDao {
    var quizCount: Int = 0

    override suspend fun insertQuestions(questions: List<QuizQuestionEntity>): List<Long> = emptyList()
    override suspend fun getQuestionsForMaterial(materialId: Long): List<QuizQuestionEntity> = emptyList()
    override fun getQuestionsForMaterialFlow(materialId: Long): Flow<List<QuizQuestionEntity>> = flowOf(emptyList())
    override suspend fun insertQuizResult(result: QuizResultEntity): Long = 1L
    override suspend fun insertQuizMistakes(mistakes: List<QuizMistakeEntity>): List<Long> = emptyList()
    override fun getResultsForMaterial(materialId: Long): Flow<List<QuizResultEntity>> = flowOf(emptyList())
    override fun getRecentQuizResults(limit: Int): Flow<List<QuizResultEntity>> = flowOf(emptyList())
    override suspend fun getMistakesForResult(resultId: Long): List<QuizMistakeEntity> = emptyList()
    override suspend fun getRecentMistakes(limit: Int): List<QuizMistakeEntity> = emptyList()
    override suspend fun getUnremediedMistakes(): List<QuizMistakeEntity> = emptyList()
    override suspend fun getMistakesForConcept(concept: String): List<QuizMistakeEntity> = emptyList()
    override suspend fun updateRemedialCardId(mistakeId: Long, cardId: Long): Int = 1
    override suspend fun getTotalQuizCount(): Int = quizCount
}
