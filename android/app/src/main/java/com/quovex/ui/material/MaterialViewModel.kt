package com.quovex.ui.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.SubjectInference
import com.quovex.domain.usecase.ExtractUrlContentUseCase
import com.quovex.domain.usecase.InferNoteMetadataUseCase
import com.quovex.domain.usecase.SynthesizeLearningMaterialUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MaterialUiState {
    data object Idle : MaterialUiState
    data class Processing(val progressMessage: String) : MaterialUiState
    data class Inferred(
        val rawText: String,
        val initialTitle: String,
        val inference: SubjectInference,
        val inputType: NoteInputType,
        val sourceUrl: String? = null
    ) : MaterialUiState
    data class Success(val material: LearningMaterial) : MaterialUiState
    data class Error(val message: String) : MaterialUiState
}

@HiltViewModel
class MaterialViewModel @Inject constructor(
    private val inferNoteMetadataUseCase: InferNoteMetadataUseCase,
    private val synthesizeLearningMaterialUseCase: SynthesizeLearningMaterialUseCase,
    private val extractUrlContentUseCase: ExtractUrlContentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MaterialUiState>(MaterialUiState.Idle)
    val uiState: StateFlow<MaterialUiState> = _uiState.asStateFlow()

    fun importUrlContent(url: String, inputType: NoteInputType) {
        viewModelScope.launch {
            val progressMessage = if (inputType == NoteInputType.YOUTUBE) {
                "Extracting YouTube lecture captions and key metadata..."
            } else {
                "Extracting readable academic text from webpage..."
            }
            _uiState.value = MaterialUiState.Processing(progressMessage)

            val extractionResult = extractUrlContentUseCase(url, inputType)
            extractionResult.onSuccess { extracted ->
                processRawText(
                    title = extracted.title,
                    rawText = extracted.content,
                    inputType = extracted.inputType,
                    sourceUrl = url
                )
            }.onFailure { error ->
                _uiState.value = MaterialUiState.Error(
                    error.message ?: "Failed to extract content from URL. Please verify the link."
                )
            }
        }
    }

    fun processRawText(
        title: String,
        rawText: String,
        inputType: NoteInputType = NoteInputType.TEXT,
        sourceUrl: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = MaterialUiState.Processing("Analyzing study notes with Quovex AI...")

            val inferenceResult = inferNoteMetadataUseCase(
                rawText = rawText,
                fileName = title.ifBlank { null }
            )

            val inference = inferenceResult.getOrDefault(
                SubjectInference(
                    subject = "General",
                    topic = title.ifBlank { "Study Material" },
                    subtopic = null,
                    confidence = 0.85f
                )
            )

            _uiState.value = MaterialUiState.Inferred(
                rawText = rawText,
                initialTitle = title.ifBlank { "${inference.subject} - ${inference.topic}" },
                inference = inference,
                inputType = inputType,
                sourceUrl = sourceUrl
            )
        }
    }

    fun confirmAndSynthesize(
        confirmedSubject: String,
        confirmedTopic: String,
        confirmedTitle: String,
        rawText: String,
        inputType: NoteInputType = NoteInputType.TEXT,
        sourceUrl: String? = null,
        confidence: Float = 0.9f
    ) {
        viewModelScope.launch {
            _uiState.value = MaterialUiState.Processing(
                "Synthesizing structured notes, formulas, flashcards & topic quiz..."
            )

            val result = synthesizeLearningMaterialUseCase(
                title = confirmedTitle,
                subject = confirmedSubject,
                topic = confirmedTopic,
                rawText = rawText,
                inputType = inputType,
                sourceUrl = sourceUrl,
                inferredConfidence = confidence
            )

            result.onSuccess { material ->
                _uiState.value = MaterialUiState.Success(material)
            }.onFailure { error ->
                _uiState.value = MaterialUiState.Error(
                    "AI Synthesis failed: ${error.message ?: "Please try again."}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = MaterialUiState.Idle
    }
}
