package com.quovex.ui.scanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.EditableScannedChapter
import com.quovex.domain.model.EditableScannedSubtopic
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.model.ScanPhase
import com.quovex.domain.model.ScannedDocumentOrganization
import com.quovex.domain.model.ScannedPage
import com.quovex.domain.model.SubjectCatalog
import com.quovex.domain.model.SubjectCategory
import com.quovex.domain.model.toEditable
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.AnalyzeDocumentImagesUseCase
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.SaveNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

data class DocumentScannerUiState(
    val phase: ScanPhase = ScanPhase.IDLE,
    val scannedPages: List<ScannedPage> = emptyList(),
    val selectedSubject: String = "General",
    val availableSubjects: List<String> = emptyList(),
    val documentTitle: String = "",
    val detectedSubject: String = "",
    val detectedStream: String = "",
    val editableChapters: List<EditableScannedChapter> = emptyList(),
    val confidence: Float = 0f,
    val isAnalyzing: Boolean = false,
    val analysisProgressMessage: String = "Analyzing document structure with Quovex AI...",
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedNoteId: Long? = null
)

@HiltViewModel
class DocumentScannerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyzeDocumentImagesUseCase: AnalyzeDocumentImagesUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val quovexRepository: QuovexRepository,
    private val getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentScannerUiState())
    val uiState: StateFlow<DocumentScannerUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        getConfiguredSubjectsUseCase()
            .onEach { subjects ->
                _uiState.update {
                    it.copy(
                        availableSubjects = if (subjects.isNotEmpty()) subjects else SubjectCatalog.chatSelectorNames,
                        selectedSubject = if (it.selectedSubject == "General" && subjects.isNotEmpty()) subjects.first() else it.selectedSubject
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun scannerCacheDir(): File {
        val dir = File(context.cacheDir, "scanner_pages")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Add captured page image URIs from ML Kit Document Scanner or Gallery.
     * Copies image files to app cacheDir to maintain domain-pure file references.
     */
    fun onPagesCaptured(uris: List<Uri>) {
        if (uris.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(phase = ScanPhase.SCANNING, error = null) }
            val currentPages = _uiState.value.scannedPages.toMutableList()

            for (uri in uris) {
                try {
                    val pageIndex = currentPages.size
                    val fileName = "page_${pageIndex}_${UUID.randomUUID().toString().take(8)}.jpg"
                    val thumbName = "thumb_${pageIndex}_${UUID.randomUUID().toString().take(8)}.jpg"
                    val pageFile = File(scannerCacheDir(), fileName)
                    val thumbFile = File(scannerCacheDir(), thumbName)

                    // Read bitmap from Uri
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (bitmap != null) {
                        // Save full-res JPEG
                        FileOutputStream(pageFile).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                        }

                        // Save thumbnail
                        val thumbBitmap = Bitmap.createScaledBitmap(
                            bitmap,
                            180,
                            (180 * (bitmap.height.toFloat() / bitmap.width.toFloat())).toInt().coerceAtLeast(180),
                            true
                        )
                        FileOutputStream(thumbFile).use { out ->
                            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
                        }

                        currentPages.add(
                            ScannedPage(
                                pageIndex = pageIndex,
                                imageFilePath = pageFile.absolutePath,
                                thumbnailFilePath = thumbFile.absolutePath,
                                widthPx = bitmap.width,
                                heightPx = bitmap.height
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Log and continue with next page
                }
            }

            _uiState.update {
                it.copy(
                    phase = if (currentPages.isNotEmpty()) ScanPhase.REVIEWING_PAGES else ScanPhase.IDLE,
                    scannedPages = currentPages,
                    documentTitle = if (it.documentTitle.isBlank()) "Scanned Document (${currentPages.size} pages)" else it.documentTitle
                )
            }
        }
    }

    /** Move page up in the scan sequence */
    fun movePageUp(index: Int) {
        if (index <= 0) return
        val pages = _uiState.value.scannedPages.toMutableList()
        val temp = pages[index]
        pages[index] = pages[index - 1].copy(pageIndex = index)
        pages[index - 1] = temp.copy(pageIndex = index - 1)
        _uiState.update { it.copy(scannedPages = pages) }
    }

    /** Move page down in the scan sequence */
    fun movePageDown(index: Int) {
        val pages = _uiState.value.scannedPages.toMutableList()
        if (index >= pages.size - 1) return
        val temp = pages[index]
        pages[index] = pages[index + 1].copy(pageIndex = index)
        pages[index + 1] = temp.copy(pageIndex = index + 1)
        _uiState.update { it.copy(scannedPages = pages) }
    }

    /** Remove a page from review */
    fun deletePage(index: Int) {
        val pages = _uiState.value.scannedPages.toMutableList()
        if (index in pages.indices) {
            val removed = pages.removeAt(index)
            try {
                File(removed.imageFilePath).delete()
                File(removed.thumbnailFilePath).delete()
            } catch (_: Exception) {}
            // Re-index remaining pages
            val reindexed = pages.mapIndexed { idx, page -> page.copy(pageIndex = idx) }
            _uiState.update {
                it.copy(
                    scannedPages = reindexed,
                    phase = if (reindexed.isEmpty()) ScanPhase.IDLE else ScanPhase.REVIEWING_PAGES
                )
            }
        }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun onDocumentTitleChanged(title: String) {
        _uiState.update { it.copy(documentTitle = title) }
    }

    /**
     * Launch Quovex AI Document Analysis.
     * Batches page images (5 per batch via DocumentAnalysisBatcher / AIRepository)
     * and receives ScannedDocumentOrganization.
     */
    fun analyzeDocumentWithAi() {
        val pages = _uiState.value.scannedPages
        if (pages.isEmpty()) {
            _uiState.update { it.copy(error = "Please scan or import at least one page.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    phase = ScanPhase.ANALYZING,
                    isAnalyzing = true,
                    error = null
                )
            }

            // Convert page image files to DomainImageInput (compressed JPEG bytes)
            val domainInputs = withContext(Dispatchers.IO) {
                pages.mapNotNull { page ->
                    try {
                        val file = File(page.imageFilePath)
                        if (file.exists() && file.length() > 0) {
                            DomainImageInput(bytes = file.readBytes(), mimeType = "image/jpeg")
                        } else null
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (domainInputs.isEmpty()) {
                _uiState.update {
                    it.copy(
                        phase = ScanPhase.REVIEWING_PAGES,
                        isAnalyzing = false,
                        error = "Could not load scanned pages. Please try again."
                    )
                }
                return@launch
            }

            val subjectHint = _uiState.value.selectedSubject.ifBlank { "General" }
            val result = analyzeDocumentImagesUseCase(domainInputs, subjectHint)

            result.onSuccess { organization ->
                val detectedSubj = organization.detectedSubject.ifBlank { subjectHint }
                val detectedStrm = organization.detectedStream.ifBlank { "General" }
                val docTitle = organization.documentTitle.ifBlank {
                    "$detectedSubj - ${organization.chapters.firstOrNull()?.title ?: "Scanned Notes"}"
                }

                _uiState.update {
                    it.copy(
                        phase = ScanPhase.CONFIRMING_ORGANIZATION,
                        isAnalyzing = false,
                        detectedSubject = detectedSubj,
                        detectedStream = detectedStrm,
                        documentTitle = docTitle,
                        editableChapters = organization.toEditable(),
                        confidence = organization.confidence
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        phase = ScanPhase.REVIEWING_PAGES,
                        isAnalyzing = false,
                        error = error.message ?: "Document AI analysis is temporarily busy. You can retry or save directly."
                    )
                }
            }
        }
    }

    // ── EDITING CHAPTERS / SUBTOPICS ─────────────────────────────────────────

    fun onChapterTitleChanged(chapterId: String, newTitle: String) {
        _uiState.update { state ->
            state.copy(
                editableChapters = state.editableChapters.map { ch ->
                    if (ch.id == chapterId) ch.copy(title = newTitle) else ch
                }
            )
        }
    }

    fun deleteChapter(chapterId: String) {
        _uiState.update { state ->
            state.copy(
                editableChapters = state.editableChapters.filterNot { it.id == chapterId }
            )
        }
    }

    fun onSubtopicContentChanged(chapterId: String, subtopicId: String, newContent: String) {
        _uiState.update { state ->
            state.copy(
                editableChapters = state.editableChapters.map { ch ->
                    if (ch.id == chapterId) {
                        ch.copy(
                            subtopics = ch.subtopics.map { sub ->
                                if (sub.id == subtopicId) sub.copy(content = newContent) else sub
                            }
                        )
                    } else ch
                }
            )
        }
    }

    /**
     * Save the confirmed structured notes into Room.
     */
    fun saveStructuredNotes(onSuccess: (noteId: Long) -> Unit) {
        val state = _uiState.value
        val title = state.documentTitle.ifBlank { "Scanned Notes - ${state.detectedSubject}" }
        val subject = state.detectedSubject.ifBlank { state.selectedSubject }

        // Build composite markdown / structured text from confirmed chapters
        val contentBuilder = StringBuilder()
        val allKeyPoints = mutableListOf<String>()

        state.editableChapters.forEach { chapter ->
            contentBuilder.append("## ${chapter.title}\n\n")
            chapter.subtopics.forEach { sub ->
                contentBuilder.append("### ${sub.title}\n")
                if (sub.content.isNotBlank()) {
                    contentBuilder.append("${sub.content}\n\n")
                }
                if (sub.keyPoints.isNotEmpty()) {
                    allKeyPoints.addAll(sub.keyPoints)
                    contentBuilder.append("**Key Points:**\n")
                    sub.keyPoints.forEach { pt ->
                        contentBuilder.append("- $pt\n")
                    }
                    contentBuilder.append("\n")
                }
            }
        }

        val fullContent = contentBuilder.toString().ifBlank {
            "Scanned document containing ${state.scannedPages.size} pages."
        }

        viewModelScope.launch {
            _uiState.update { it.copy(phase = ScanPhase.SAVING, isSaving = true, error = null) }
            try {
                // 1. Save as NoteItem
                val note = NoteItem(
                    title = title,
                    subject = subject,
                    content = fullContent,
                    keyPoints = allKeyPoints.distinct().take(10),
                    status = NoteProcessingStatus.READY,
                    inputType = NoteInputType.SCAN_MULTI_PAGE
                )
                val noteId = saveNoteUseCase(note)

                // 2. Also insert as LearningMaterial for Knowledge Hub transformation
                val material = LearningMaterial(
                    title = title,
                    subject = subject,
                    subjectCategory = SubjectCatalog.findByName(subject)?.category ?: SubjectCategory.OTHER,
                    topic = state.editableChapters.firstOrNull()?.title ?: "",
                    summary = fullContent.take(500),
                    keyPoints = allKeyPoints.distinct().take(8),
                    inputType = NoteInputType.SCAN_MULTI_PAGE,
                    status = NoteProcessingStatus.READY
                )
                quovexRepository.insertMaterial(material)

                _uiState.update {
                    it.copy(
                        phase = ScanPhase.SAVED,
                        isSaving = false,
                        savedNoteId = noteId
                    )
                }

                // Cleanup temporary scan files
                withContext(Dispatchers.IO) {
                    try {
                        scannerCacheDir().listFiles()?.forEach { it.delete() }
                    } catch (_: Exception) {}
                }

                onSuccess(noteId)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        phase = ScanPhase.CONFIRMING_ORGANIZATION,
                        isSaving = false,
                        error = e.message ?: "Failed to save notes. Please try again."
                    )
                }
            }
        }
    }

    /** Reset scanner state */
    fun reset() {
        _uiState.update {
            DocumentScannerUiState(
                availableSubjects = it.availableSubjects,
                selectedSubject = it.selectedSubject
            )
        }
    }
}
