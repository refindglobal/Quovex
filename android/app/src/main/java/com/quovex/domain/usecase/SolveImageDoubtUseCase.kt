package com.quovex.domain.usecase

import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ImageDoubtSolution
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

class SolveImageDoubtUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        imageInput: DomainImageInput,
        subject: String = "General",
        questionText: String = ""
    ): Result<ImageDoubtSolution> {
        if (imageInput.bytes.isEmpty()) {
            return Result.failure(IllegalArgumentException("Image data cannot be empty"))
        }

        return aiRepository.solveImageDoubt(
            imageInput = imageInput,
            subject = subject,
            questionText = questionText
        )
    }
}
