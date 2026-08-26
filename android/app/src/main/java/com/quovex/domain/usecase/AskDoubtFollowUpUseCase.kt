package com.quovex.domain.usecase

import com.quovex.data.remote.dto.ChatMessageDto
import com.quovex.domain.model.DoubtFollowUpMessage
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

/**
 * Handles conversational follow-up questions about a solved image problem (Module B3: L-034).
 * Preserves the visual context, problem statement, and previous solution turns.
 */
class AskDoubtFollowUpUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        subject: String,
        problemContext: String,
        solutionContext: String,
        previousMessages: List<DoubtFollowUpMessage>,
        newQuestion: String
    ): Result<String> {
        val cleanQuestion = newQuestion.trim()
        if (cleanQuestion.isBlank()) {
            return Result.failure(IllegalArgumentException("Question cannot be blank"))
        }

        val historyDtos = previousMessages.map { msg ->
            ChatMessageDto(
                role = if (msg.isUser) "user" else "assistant",
                content = msg.text
            )
        }

        val promptWithContext = buildString {
            append("Academic Context: Problem Solving Tutoring\n")
            append("Subject: ").append(subject).append("\n")
            if (problemContext.isNotBlank()) {
                append("Problem Details / Question: ").append(problemContext).append("\n\n")
            }
            append("Original Step-by-Step Solution Provided:\n").append(solutionContext).append("\n\n")
            append("Student's Follow-Up Question: ").append(cleanQuestion)
        }

        return aiRepository.sendTutorMessage(
            message = promptWithContext,
            subject = subject,
            topic = "Image Doubt Follow-Up",
            materialSummary = solutionContext.take(500),
            recentMistakes = emptyList(),
            history = historyDtos
        )
    }
}
