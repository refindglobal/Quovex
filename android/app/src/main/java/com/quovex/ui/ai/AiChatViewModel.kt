package com.quovex.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.usecase.AskAiDoubtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UiChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatUiState(
    val selectedSubject: String = "Physics",
    val messages: List<UiChatMessage> = listOf(
        UiChatMessage(
            isUser = false,
            text = "Hello Arjun! I am your **Quovex AI Study Coach**. Ask me any doubt, formula derivation, or complex concept to master today."
        )
    ),
    val isTyping: Boolean = false,
    val inputText: String = "",
    val error: String? = null
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val askAiDoubtUseCase: AskAiDoubtUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun sendMessage(customText: String? = null) {
        val messageToSend = (customText ?: _uiState.value.inputText).trim()
        if (messageToSend.isBlank() || _uiState.value.isTyping) return

        val userMessage = UiChatMessage(isUser = true, text = messageToSend)
        val currentHistory = _uiState.value.messages.map { (if (it.isUser) "user" else "assistant") to it.text }

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isTyping = true,
                error = null
            )
        }

        viewModelScope.launch {
            val result = askAiDoubtUseCase(
                userQuestion = messageToSend,
                subject = _uiState.value.selectedSubject,
                history = currentHistory
            )

            result.onSuccess { responseText ->
                val aiMessage = UiChatMessage(isUser = false, text = responseText)
                _uiState.update {
                    it.copy(
                        messages = it.messages + aiMessage,
                        isTyping = false
                    )
                }
            }.onFailure { error ->
                val errorMessage = UiChatMessage(
                    isUser = false,
                    text = "⚠️ Could not reach AI tutor: ${error.message ?: "Please check your connection."}"
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isTyping = false,
                        error = error.message
                    )
                }
            }
        }
    }
}
