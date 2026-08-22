package com.quovex.domain.usecase

import com.quovex.data.remote.dto.ChatMessageDto
import com.quovex.domain.repository.AIRepository
import javax.inject.Inject

class AskAiDoubtUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        userQuestion: String,
        subject: String = "Physics",
        history: List<Pair<String, String>> = emptyList()
    ): Result<String> {
        val historyDtos = history.map { (role, content) ->
            ChatMessageDto(role = role, content = content)
        }
        return aiRepository.sendChatMessage(
            message = userQuestion,
            subject = subject,
            history = historyDtos
        )
    }
}
