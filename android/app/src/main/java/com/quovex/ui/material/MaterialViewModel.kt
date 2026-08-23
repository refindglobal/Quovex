package com.quovex.ui.material

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.model.SubjectInference
import com.quovex.domain.usecase.ClassifyMaterialUseCase
import com.quovex.domain.usecase.ConfirmMaterialSubjectUseCase
import com.quovex.domain.usecase.GenerateQuizUseCase
import com.quovex.domain.usecase.GetNoteByIdUseCase
import com.quovex.domain.usecase.ProcessScanAndSummarizeUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import com.quovex.domain.usecase.SummarizeNoteUseCase
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
        val materialId: Long,
        val rawText: String,
        val inference: SubjectInference,
        val inputType: NoteInputType
    ) : MaterialUiState
    data class Success(val materialId: Long) : MaterialUiState
    data class Error(val message: String) : MaterialUiState
}

@HiltViewModel
class MaterialViewModel @Inject constructor(
    private val saveNoteUseCase: SaveNoteUseCase,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val summarizeNoteUseCase: SummarizeNoteUseCase,
    private val classifyMaterialUseCase: ClassifyMaterialUseCase,
    private val processScanAndSummarizeUseCase: ProcessScanAndSummarizeUseCase,
    private val confirmMaterialSubjectUseCase: ConfirmMaterialSubjectUseCase,
    private val generateQuizUseCase: GenerateQuizUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MaterialUiState>(MaterialUiState.Idle)
    val uiState: StateFlow<MaterialUiState> = _uiState.asStateFlow()

    fun processRawText(title: String, rawText: String, inputType: NoteInputType = NoteInputType.TEXT) {
        viewModelScope.launch {
            _uiState.value = MaterialUiState.Processing("Quovex AI is analyzing and classifying your material...")

            val classificationResult = classifyMaterialUseCase(rawText.take(2500))
            val inference = classificationResult.getOrDefault(
                SubjectInference(
                    subject = "General",
                    topic = title.ifBlank { "Study Topic" },
                    confidence = 0.6f
                )
            )

            val materialId = saveNoteUseCase(
                NoteItem(
                    title = title.ifBlank { inference.topic.ifBlank { "Study Material" } },
                    subject = inference.subject,
                    content = rawText,
                    inputType = inputType,
                    status = NoteProcessingStatus.PROCESSING
                )
            )

            _uiState.value = MaterialUiState.Inferred(
                materialId = materialId,
                rawText = rawText,
                inference = inference,
                inputType = inputType
            )
        }
    }

    fun confirmAndTransform(
        materialId: Long,
        confirmedSubject: String,
        confirmedTopic: String,
        confirmedTitle: String,
        rawText: String
    ) {
        viewModelScope.launch {
            _uiState.value = MaterialUiState.Processing("Quovex AI is generating summaries, key concepts & flashcards...")

            val summaryResult = summarizeNoteUseCase(rawText, confirmedSubject)

            summaryResult.onSuccess { summaryData ->
                val flashcardsCount = summaryData.flashcards?.size ?: 0

                val updatedItem = NoteItem(
                    id = materialId,
                    title = confirmedTitle,
                    subject = confirmedSubject,
                    content = rawText,
                    status = NoteProcessingStatus.READY,
                    keyPoints = summaryData.keyPoints ?: emptyList(),
                    flashcardCount = flashcardsCount
                )
                saveNoteUseCase(updatedItem)

                _uiState.value = MaterialUiState.Success(materialId)
            }.onFailure { error ->
                _uiState.value = MaterialUiState.Error(
                    "Quovex AI couldn't complete transformation: ${error.message ?: "Please try again."}"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = MaterialUiState.Idle
    }
}
