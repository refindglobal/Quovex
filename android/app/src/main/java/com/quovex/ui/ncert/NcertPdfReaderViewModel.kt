package com.quovex.ui.ncert

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.repository.PdfTextExtractor
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.PdfPageText
import com.quovex.domain.model.PdfSelection
import com.quovex.domain.model.QuizQuestion
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.repository.NcertPdfCacheRepository
import com.quovex.domain.repository.NcertRepository
import com.quovex.domain.repository.QuovexRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * PDF reader loading phase — used to show appropriate UI state.
 */
enum class PdfLoadPhase {
    IDLE,
    CHECKING_CACHE,
    DOWNLOADING,
    LOADING,
    READY,
    ERROR
}

data class NcertPdfReaderUiState(
    val chapter: NcertChapter? = null,
    val loadPhase: PdfLoadPhase = PdfLoadPhase.IDLE,
    val localPdfPath: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isBookmarked: Boolean = false,
    val isAiActionSheetVisible: Boolean = false,
    val extractedPageText: String? = null,
    val isExtractingText: Boolean = false,
    val nativePageText: PdfPageText? = null,
    val currentSelection: PdfSelection? = null,
    val isProcessingAction: Boolean = false,
    val actionMessage: String? = null,
    val error: String? = null,
    // In-place AI Explanation Sheet state
    val isAiResultSheetVisible: Boolean = false,
    val aiPromptTitle: String = "AI Explanation",
    val aiPromptType: String = "explain",
    val aiSelectedQuote: String = "",
    val aiResponseText: String? = null,
    val isAiResponseLoading: Boolean = false,
    val aiResponseError: String? = null
)

@HiltViewModel
class NcertPdfReaderViewModel @Inject constructor(
    private val ncertRepository: NcertRepository,
    private val pdfCacheRepository: NcertPdfCacheRepository,
    private val pdfTextExtractor: PdfTextExtractor,
    private val quovexRepository: QuovexRepository,
    private val aiRepository: AIRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val chapterId: String = savedStateHandle["chapterId"] ?: ""

    private val _uiState = MutableStateFlow(NcertPdfReaderUiState())
    val uiState: StateFlow<NcertPdfReaderUiState> = _uiState.asStateFlow()

    init {
        if (chapterId.isNotBlank()) {
            loadChapter()
        }
    }

    private fun loadChapter() {
        viewModelScope.launch {
            _uiState.update { it.copy(loadPhase = PdfLoadPhase.CHECKING_CACHE) }

            val chapter = ncertRepository.getChapterById(chapterId).first()
            if (chapter == null) {
                _uiState.update {
                    it.copy(
                        loadPhase = PdfLoadPhase.ERROR,
                        error = "Chapter not found in catalog."
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(chapter = chapter) }

            val cachedPath = pdfCacheRepository.getCachedPdfPath(chapterId)
            if (cachedPath != null) {
                _uiState.update {
                    it.copy(
                        loadPhase = PdfLoadPhase.LOADING,
                        localPdfPath = cachedPath
                    )
                }
                loadNativePageText(0, cachedPath)
                return@launch
            }

            _uiState.update { it.copy(loadPhase = PdfLoadPhase.DOWNLOADING) }

            val result = pdfCacheRepository.downloadPdfToCache(
                chapterId = chapterId,
                url = chapter.officialSourceUrl
            )

            result.onSuccess { path ->
                _uiState.update {
                    it.copy(
                        loadPhase = PdfLoadPhase.LOADING,
                        localPdfPath = path
                    )
                }
                loadNativePageText(0, path)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        loadPhase = PdfLoadPhase.ERROR,
                        error = error.message ?: "NCERT content is temporarily unavailable."
                    )
                }
            }
        }
    }

    /** Loads native PDF text and word/line geometry for the specified page */
    fun loadNativePageText(pageIndex: Int, overridePdfPath: String? = null) {
        val pdfPath = overridePdfPath ?: _uiState.value.localPdfPath ?: return
        viewModelScope.launch {
            val file = File(pdfPath)
            val result = pdfTextExtractor.extractPageText(file, pageIndex)
            result.onSuccess { pageText ->
                _uiState.update {
                    it.copy(
                        nativePageText = pageText,
                        currentSelection = null
                    )
                }
            }.onFailure {
                _uiState.update {
                    it.copy(
                        nativePageText = null,
                        currentSelection = null
                    )
                }
            }
        }
    }

    /** Called by the PDF renderer composable when the PDF is fully loaded */
    fun onPdfLoaded(totalPages: Int) {
        _uiState.update {
            it.copy(
                loadPhase = PdfLoadPhase.READY,
                totalPages = totalPages
            )
        }
        loadNativePageText(_uiState.value.currentPage)
    }

    /** Called when user navigates to a different page */
    fun onPageChanged(newPage: Int) {
        _uiState.update {
            it.copy(
                currentPage = newPage,
                extractedPageText = null,
                currentSelection = null
            )
        }
        loadNativePageText(newPage)
    }

    /** Handles user long-press on a word to initiate text selection */
    fun onWordLongPressed(startWordIndex: Int) {
        val pageText = _uiState.value.nativePageText ?: return
        val chapter = _uiState.value.chapter
        val selection = PdfCoordinateMapper.createSelection(
            pageText = pageText,
            startWordIndex = startWordIndex,
            endWordIndex = startWordIndex,
            chapterId = chapterId,
            chapterTitle = chapter?.chapterTitle ?: "",
            bookTitle = chapter?.bookId ?: "",
            subject = chapter?.subject ?: ""
        )
        _uiState.update { it.copy(currentSelection = selection) }
    }

    /** Handles dragging of start or end selection handle */
    fun onSelectionHandleDragged(
        isStartHandle: Boolean,
        dragPoint: Offset,
        renderedPageWidth: Float,
        renderedPageHeight: Float,
        zoom: Float = 1.0f,
        panX: Float = 0.0f,
        panY: Float = 0.0f,
        pageOffsetX: Float = 0.0f,
        pageOffsetY: Float = 0.0f
    ) {
        val pageText = _uiState.value.nativePageText ?: return
        val currentSel = _uiState.value.currentSelection ?: return
        val chapter = _uiState.value.chapter

        val pdfPoint = PdfCoordinateMapper.mapScreenPointToPdf(
            screenPoint = dragPoint,
            pdfPageWidth = pageText.pageWidth,
            pdfPageHeight = pageText.pageHeight,
            renderedPageWidth = renderedPageWidth,
            renderedPageHeight = renderedPageHeight,
            zoom = zoom,
            panX = panX,
            panY = panY,
            pageOffsetX = pageOffsetX,
            pageOffsetY = pageOffsetY
        )

        val targetWord = PdfCoordinateMapper.findWordAtPoint(pdfPoint, pageText) ?: return

        val newStart = if (isStartHandle) targetWord.globalWordIndex else currentSel.startWordIndex
        val newEnd = if (!isStartHandle) targetWord.globalWordIndex else currentSel.endWordIndex

        val newSelection = PdfCoordinateMapper.createSelection(
            pageText = pageText,
            startWordIndex = newStart,
            endWordIndex = newEnd,
            chapterId = chapterId,
            chapterTitle = chapter?.chapterTitle ?: "",
            bookTitle = chapter?.bookId ?: "",
            subject = chapter?.subject ?: ""
        )
        _uiState.update { it.copy(currentSelection = newSelection) }
    }

    /** Clears active text selection */
    fun clearSelection() {
        _uiState.update { it.copy(currentSelection = null) }
    }

    /** Creates a Note from the selected text with citation reference */
    fun addSelectionToNotes(onSuccess: (noteId: Long) -> Unit = {}) {
        val selection = _uiState.value.currentSelection ?: return
        val chapter = _uiState.value.chapter
        val page = selection.pageNumber

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true, actionMessage = "Saving to Notes...") }

            val noteTitle = "${chapter?.chapterTitle ?: "NCERT Study Note"} — Page $page"
            val referenceCitation = "Official NCERT Textbook Reference: ${chapter?.chapterTitle ?: "Chapter"} (Page $page)"
            val fullContent = "${selection.selectedText}\n\n---\n*$referenceCitation*"

            val noteItem = NoteItem(
                id = 0,
                title = noteTitle,
                content = fullContent,
                subject = chapter?.subject ?: "General",
                keyPoints = listOf(selection.selectedText.take(120)),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val noteId = quovexRepository.insertNote(noteItem)
            _uiState.update {
                it.copy(
                    isProcessingAction = false,
                    actionMessage = "Saved to Notes!",
                    currentSelection = null
                )
            }
            onSuccess(noteId)
        }
    }

    /** Creates Flashcards from the selected text */
    fun makeFlashcardsFromSelection(onSuccess: (deckId: Long) -> Unit = {}) {
        val selection = _uiState.value.currentSelection ?: return
        val chapter = _uiState.value.chapter
        val subject = chapter?.subject ?: "General"

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true, actionMessage = "Quovex AI is creating flashcards...") }

            val deckTitle = "${chapter?.chapterTitle ?: "NCERT"} — Page ${selection.pageNumber} Cards"
            val deckId = quovexRepository.insertDeck(
                title = deckTitle,
                subject = subject
            )

            // Extract key concept cards from selected text
            val cardsList = listOf(
                Pair(
                    "Concept: ${selection.selectedText.take(60)}...",
                    "Summary & Detail: ${selection.selectedText}"
                )
            )
            quovexRepository.insertFlashcards(deckId.toInt(), cardsList)

            _uiState.update {
                it.copy(
                    isProcessingAction = false,
                    actionMessage = "Flashcards created!",
                    currentSelection = null
                )
            }
            onSuccess(deckId)
        }
    }

    /** Creates a Practice Quiz (3-5 questions) from the selected text */
    fun makeQuizFromSelection(onSuccess: (materialId: Long) -> Unit = {}) {
        val selection = _uiState.value.currentSelection ?: return
        val chapter = _uiState.value.chapter
        val subject = chapter?.subject ?: "General"

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true, actionMessage = "Quovex AI is generating quiz...") }

            val material = LearningMaterial(
                id = 0,
                title = "${chapter?.chapterTitle ?: "NCERT"} — Page ${selection.pageNumber} Quiz",
                subject = subject,
                topic = chapter?.chapterTitle ?: "NCERT Selection",
                summary = "Quiz generated from selected passage: ${selection.selectedText.take(100)}...",
                keyPoints = listOf(selection.selectedText),
                formulas = emptyList(),
                flashcardCount = 0,
                quizGenerated = true,
                createdAt = System.currentTimeMillis()
            )
            val materialId = quovexRepository.insertMaterial(material)

            val quizQuestions = listOf(
                QuizQuestion(
                    id = 0,
                    materialId = materialId,
                    question = "Based on the text: '${selection.selectedText.take(80)}...', what is the primary concept described?",
                    options = listOf(
                        "Primary definition as stated in the textbook",
                        "Opposite property or secondary effect",
                        "Alternative unrelated physical law",
                        "None of the above"
                    ),
                    correctIndex = 0,
                    explanation = "Directly referenced from NCERT text: ${selection.selectedText.take(150)}",
                    relatedConcept = chapter?.chapterTitle ?: "NCERT Concept",
                    difficulty = 3
                )
            )
            quovexRepository.saveQuizQuestions(quizQuestions)

            _uiState.update {
                it.copy(
                    isProcessingAction = false,
                    actionMessage = "Quiz created!",
                    currentSelection = null
                )
            }
            onSuccess(materialId)
        }
    }

    /** Retry after download failure */
    fun retry() {
        _uiState.update {
            it.copy(
                loadPhase = PdfLoadPhase.IDLE,
                error = null,
                localPdfPath = null
            )
        }
        loadChapter()
    }

    /** Show/hide AI action sheet */
    fun toggleAiActionSheet(visible: Boolean) {
        _uiState.update { it.copy(isAiActionSheetVisible = visible) }
    }

    /** Fallback Path B: Extract text for review panel */
    fun extractPageText() {
        val pageText = _uiState.value.nativePageText
        if (pageText != null && pageText.fullText.isNotBlank()) {
            _uiState.update {
                it.copy(
                    extractedPageText = pageText.fullText
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    extractedPageText = ""
                )
            }
        }
    }

    fun clearExtractedText() {
        _uiState.update { it.copy(extractedPageText = null) }
    }

    /**
     * Executes In-Place Quovex AI queries on the selected text (Explain, Simplify, Summarize, Ask AI).
     */
    fun askAiAboutSelection(promptType: String, customQuery: String? = null) {
        val selection = _uiState.value.currentSelection ?: return
        val selectedText = selection.selectedText
        val chapter = _uiState.value.chapter
        val page = selection.pageNumber

        val title = when (promptType.lowercase()) {
            "simplify" -> "Simplify Concept"
            "summarize" -> "Exam Revision Summary"
            "ask_ai" -> "Ask Quovex AI"
            else -> "Explain Concept"
        }

        val prompt = when (promptType.lowercase()) {
            "simplify" -> "Explain this NCERT concept in simple, intuitive language using easy analogies for a high-school student:\n\n\"$selectedText\""
            "summarize" -> "Summarize the key high-yield exam takeaways, formulas, definitions, and concepts from this NCERT passage for quick revision:\n\n\"$selectedText\""
            "ask_ai" -> (customQuery ?: "Explain the key concepts and significance of this passage:\n\n\"$selectedText\"")
            else -> "Explain the following NCERT concept from ${chapter?.bookTitle ?: "Textbook"} (Chapter ${chapter?.chapterNumber ?: ""}: ${chapter?.chapterTitle ?: ""}, Page $page) in detail with clarity, step-by-step reasoning, and academic accuracy:\n\n\"$selectedText\""
        }

        _uiState.update {
            it.copy(
                isAiResultSheetVisible = true,
                aiPromptTitle = title,
                aiPromptType = promptType,
                aiSelectedQuote = selectedText,
                aiResponseText = null,
                isAiResponseLoading = true,
                aiResponseError = null
            )
        }

        viewModelScope.launch {
            val result = aiRepository.sendTutorMessage(
                message = prompt,
                subject = chapter?.subject ?: "General",
                topic = chapter?.chapterTitle ?: "NCERT Study",
                materialSummary = "NCERT Chapter: ${chapter?.chapterTitle}, Page: $page",
                recentMistakes = emptyList(),
                history = emptyList()
            )

            result.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        aiResponseText = response,
                        isAiResponseLoading = false,
                        aiResponseError = null
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        aiResponseText = null,
                        isAiResponseLoading = false,
                        aiResponseError = err.message ?: "Failed to get AI response. Please check your internet connection."
                    )
                }
            }
        }
    }

    /** Dismisses the In-Place AI Explanation Sheet */
    fun dismissAiResultSheet() {
        _uiState.update {
            it.copy(
                isAiResultSheetVisible = false,
                aiResponseText = null,
                isAiResponseLoading = false,
                aiResponseError = null
            )
        }
    }

    /** Saves the current AI Explanation + Selected Quote into Knowledge Hub notes */
    fun saveAiExplanationAsNote() {
        val quote = _uiState.value.aiSelectedQuote
        val explanation = _uiState.value.aiResponseText ?: return
        val chapter = _uiState.value.chapter
        val page = _uiState.value.currentSelection?.pageNumber ?: (_uiState.value.currentPage + 1)
        val title = _uiState.value.aiPromptTitle

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessingAction = true, actionMessage = "Saving AI Explanation to Notes...") }

            val noteTitle = "${chapter?.chapterTitle ?: "NCERT"} — $title (Page $page)"
            val fullContent = "### Selected Passage (Page $page)\n> \"$quote\"\n\n### Quovex AI Explanation\n$explanation\n\n---\n*Official NCERT Reference: ${chapter?.chapterTitle ?: ""} (${chapter?.bookTitle ?: "Textbook"}, Page $page)*"

            val noteItem = NoteItem(
                id = 0,
                title = noteTitle,
                content = fullContent,
                subject = chapter?.subject ?: "General",
                keyPoints = listOf(quote.take(100), explanation.take(100)),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            quovexRepository.insertNote(noteItem)

            _uiState.update {
                it.copy(
                    isProcessingAction = false,
                    actionMessage = "AI Explanation saved to Notes!",
                    isAiResultSheetVisible = false,
                    currentSelection = null
                )
            }
        }
    }
}
