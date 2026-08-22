package com.quovex.domain.usecase

import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.repository.QuovexRepository
import javax.inject.Inject

class ConfirmMaterialSubjectUseCase @Inject constructor(
    private val repository: QuovexRepository
) {
    suspend operator fun invoke(
        materialId: Long,
        confirmedSubject: String,
        confirmedTopic: String,
        title: String? = null
    ): Result<LearningMaterial> {
        val existing = repository.getMaterialById(materialId)
            ?: return Result.failure(IllegalArgumentException("Material not found: $materialId"))

        val updated = existing.copy(
            subject = confirmedSubject,
            topic = confirmedTopic,
            title = title ?: existing.title,
            inferredSubject = existing.inferredSubject ?: confirmedSubject,
            inferredTopic = existing.inferredTopic ?: confirmedTopic,
            updatedAt = System.currentTimeMillis()
        )

        repository.updateMaterial(updated)
        return Result.success(updated)
    }
}
