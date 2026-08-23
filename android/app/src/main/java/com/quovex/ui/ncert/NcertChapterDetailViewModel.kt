package com.quovex.ui.ncert

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.repository.NcertRepository
import com.quovex.domain.usecase.StudyNcertChapterWithAiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NcertChapterDetailUiState(
    val chapter: NcertChapter? = null,
    val isLoading: Boolean = false,
    val isProcessingAiStudy: Boolean = false,
    val aiStudyProgressMessage: String = "",
    val createdMaterialId: Long? = null,
    val error: String? = null
)

@HiltViewModel
class NcertChapterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ncertRepository: NcertRepository,
    private val studyNcertChapterWithAiUseCase: StudyNcertChapterWithAiUseCase
) : ViewModel() {

    private val chapterId: String = checkNotNull(savedStateHandle["chapterId"])

    private val _isProcessingAiStudy = MutableStateFlow(false)
    private val _aiStudyProgressMessage = MutableStateFlow("")
    private val _createdMaterialId = MutableStateFlow<Long?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<NcertChapterDetailUiState> = combine(
        ncertRepository.getChapterById(chapterId),
        _isProcessingAiStudy,
        _aiStudyProgressMessage,
        _createdMaterialId,
        _error
    ) { chapter, isProcessing, progressMsg, materialId, err ->
        if (chapter == null) {
            NcertChapterDetailUiState(error = "Chapter not found in NCERT catalog.")
        } else {
            NcertChapterDetailUiState(
                chapter = chapter,
                isLoading = false,
                isProcessingAiStudy = isProcessing,
                aiStudyProgressMessage = progressMsg,
                createdMaterialId = materialId,
                error = err
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NcertChapterDetailUiState(isLoading = true)
    )

    fun studyWithQuovexAi(onSuccess: (materialId: Long) -> Unit) {
        val currentChapter = uiState.value.chapter ?: return
        viewModelScope.launch {
            _isProcessingAiStudy.value = true
            _aiStudyProgressMessage.value = "Quovex AI is analyzing Chapter ${currentChapter.chapterNumber} & generating study aids..."
            _error.value = null

            val result = studyNcertChapterWithAiUseCase(currentChapter)
            _isProcessingAiStudy.value = false

            result.fold(
                onSuccess = { matId ->
                    _createdMaterialId.value = matId
                    onSuccess(matId)
                },
                onFailure = { ex ->
                    _error.value = ex.message ?: "Failed to generate Quovex AI study material."
                }
            )
        }
    }
}
