package com.quovex.domain.usecase

import com.quovex.domain.model.SubjectInference
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

class ClassifyMaterialUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(textSample: String, filename: String? = null): Result<SubjectInference> {
        return aiRepository.classifyMaterial(textSample, filename)
    }
}
