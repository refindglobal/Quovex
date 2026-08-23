package com.quovex.ui.ai

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.remote.dto.ChatMessageDto
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class UiChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val isUser: Boolean,
    val text: String,
    val attachedImageBitmap: Bitmap? = null,  // shown in user bubble if image was attached
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatUiState(
    /** Subjects loaded from GetConfiguredSubjectsUseCase — ALL streams, not hardcoded */
    val availableSubjects: List<String> = emptyList(),
    val selectedSubject: String = "General",
    val activeTopic: String = "",
    val materialSummary: String? = null,
    val recentMistakes: List<String> = emptyList(),
    val messages: List<UiChatMessage> = listOf(
        UiChatMessage(
            isUser = false,
            text = "Hello! I am your **Quovex AI Study Coach**. Ask me any doubt, derivation, or concept — or attach an image of a problem to analyze it."
        )
    ),
    val isTyping: Boolean = false,
    val inputText: String = "",
    /** Image attached to next message — shown as preview above composer */
    val attachedImage: Bitmap? = null,
    val error: String? = null
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val aiRepository: AIRepository,
    private val getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialSubject: String = savedStateHandle["subject"] ?: ""
    private val initialTopic: String = savedStateHandle["topic"] ?: ""
    private val initialPrompt: String = savedStateHandle["prompt"] ?: ""

    private val _uiState = MutableStateFlow(
        AiChatUiState(
            activeTopic = initialTopic
        )
    )
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
        if (initialPrompt.isNotBlank()) {
            sendMessage(initialPrompt)
        }
    }

    private fun loadSubjects() {
        getConfiguredSubjectsUseCase()
            .onEach { subjects ->
                val resolved = if (initialSubject.isNotBlank() && initialSubject in subjects) {
                    initialSubject
                } else {
                    subjects.firstOrNull() ?: "General"
                }
                _uiState.update {
                    it.copy(
                        availableSubjects = subjects,
                        selectedSubject = if (it.selectedSubject == "General" && resolved != "General") resolved else it.selectedSubject
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun setContext(topic: String, summary: String?, mistakes: List<String> = emptyList()) {
        _uiState.update {
            it.copy(
                activeTopic = topic,
                materialSummary = summary,
                recentMistakes = mistakes
            )
        }
    }

    /** Attach an image to the next message. Replaces any existing attachment. */
    fun onImageAttached(bitmap: Bitmap) {
        _uiState.update { it.copy(attachedImage = bitmap, error = null) }
    }

    /** Remove the currently attached image */
    fun clearAttachedImage() {
        _uiState.update { it.copy(attachedImage = null) }
    }

    fun sendMessage(customText: String? = null) {
        val messageToSend = (customText ?: _uiState.value.inputText).trim()
        val attachedBitmap = _uiState.value.attachedImage

        // Must have text OR image (or both)
        if (messageToSend.isBlank() && attachedBitmap == null) return
        if (_uiState.value.isTyping) return

        val displayText = when {
            messageToSend.isNotBlank() -> messageToSend
            attachedBitmap != null -> "📷 Image attached"
            else -> return
        }

        val userMessage = UiChatMessage(
            isUser = true,
            text = displayText,
            attachedImageBitmap = attachedBitmap
        )

        val currentHistory = _uiState.value.messages
            .dropWhile { !it.isUser }
            .map {
                ChatMessageDto(
                    role = if (it.isUser) "user" else "assistant",
                    content = it.text
                )
            }

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                attachedImage = null,  // Clear attachment after sending
                isTyping = true,
                error = null
            )
        }

        viewModelScope.launch {
            val state = _uiState.value

            val result: Result<String> = if (attachedBitmap != null) {
                // Image + optional text → vision path
                val bytes = compressBitmapToBytes(attachedBitmap, maxKb = 512)
                val domainImage = DomainImageInput(bytes = bytes, mimeType = "image/jpeg")
                aiRepository.sendMessageWithImage(
                    imageInput = domainImage,
                    message = messageToSend.ifBlank { "Please analyze and explain this image." },
                    subject = state.selectedSubject,
                    history = currentHistory
                )
            } else {
                // Text-only → tutor path
                aiRepository.sendTutorMessage(
                    message = messageToSend,
                    subject = state.selectedSubject,
                    topic = state.activeTopic,
                    materialSummary = state.materialSummary,
                    recentMistakes = state.recentMistakes,
                    history = currentHistory
                )
            }

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
                    text = "Quovex AI is temporarily busy. Please try again in a moment."
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

    /** Compress bitmap to JPEG bytes ≤ [maxKb] KB */
    private fun compressBitmapToBytes(bitmap: Bitmap, maxKb: Int = 512): ByteArray {
        var quality = 90
        var stream: ByteArrayOutputStream
        do {
            stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            quality -= 10
        } while (stream.size() > maxKb * 1024 && quality > 20)
        return stream.toByteArray()
    }
}
