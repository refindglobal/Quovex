package com.quovex.domain.usecase

import com.quovex.domain.model.QuizResult
import com.quovex.domain.repository.QuovexRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuizResultsUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    operator fun invoke(materialId: Long): Flow<List<QuizResult>> {
        return repository.getQuizResultsForMaterial(materialId)
    }
}
