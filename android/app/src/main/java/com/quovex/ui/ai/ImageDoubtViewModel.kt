package com.quovex.ui.ai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quovex.data.ocr.MlKitOcrHelper
import com.quovex.domain.model.DoubtFollowUpMessage
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteProcessingStatus
import com.quovex.domain.model.StructuredDoubtSolution
import com.quovex.domain.model.SubjectCatalog
import com.quovex.domain.model.SubjectCategory
import com.quovex.domain.repository.AIRepository
import com.quovex.domain.repository.QuovexRepository
import com.quovex.domain.usecase.AskDoubtFollowUpUseCase
import com.quovex.domain.usecase.GetConfiguredSubjectsUseCase
import com.quovex.domain.usecase.SolveImageDoubtUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class ImageDoubtUiState(
    val capturedBitmap: Bitmap? = null,
    val selectedSubject: String = "Physics",
    val availableSubjects: List<String> = listOf("Physics", "Chemistry", "Mathematics", "Biology", "General"),
    val questionText: String = "",
    val isSolving: Boolean = false,
    val isSavingAsMaterial: Boolean = false,
    val isCreatingFlashcards: Boolean = false,
    val solutionText: String? = null,
    val structuredSolution: StructuredDoubtSolution? = null,
    val solutionProvider: String? = null,
    val savedMaterialId: Long? = null,
    val flashcardsCreatedMessage: String? = null,
    val followUpMessages: List<DoubtFollowUpMessage> = emptyList(),
    val followUpInputText: String = "",
    val isSendingFollowUp: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ImageDoubtViewModel @Inject constructor(
    private val solveImageDoubtUseCase: SolveImageDoubtUseCase,
    private val askDoubtFollowUpUseCase: AskDoubtFollowUpUseCase,
    private val repository: QuovexRepository,
    private val aiRepository: AIRepository,
    private val getConfiguredSubjectsUseCase: GetConfiguredSubjectsUseCase,
    private val mlKitOcrHelper: MlKitOcrHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImageDoubtUiState())
    val uiState: StateFlow<ImageDoubtUiState> = _uiState.asStateFlow()

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            getConfiguredSubjectsUseCase().collect { subjects ->
                if (subjects.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            availableSubjects = subjects + "General",
                            selectedSubject = subjects.first()
                        )
                    }
                }
            }
        }
    }

    fun onImageCaptured(bitmap: Bitmap) {
        _uiState.update {
            it.copy(
                capturedBitmap = bitmap,
                solutionText = null,
                structuredSolution = null,
                followUpMessages = emptyList(),
                followUpInputText = "",
                error = null,
                savedMaterialId = null,
                flashcardsCreatedMessage = null
            )
        }
    }

    fun onQuestionTextChanged(text: String) {
        _uiState.update { it.copy(questionText = text) }
    }

    fun onFollowUpInputChanged(text: String) {
        _uiState.update { it.copy(followUpInputText = text) }
    }

    fun selectSubject(subject: String) {
        _uiState.update { it.copy(selectedSubject = subject) }
    }

    fun clearImage() {
        _uiState.update {
            it.copy(
                capturedBitmap = null,
                solutionText = null,
                structuredSolution = null,
                followUpMessages = emptyList(),
                followUpInputText = "",
                error = null,
                savedMaterialId = null
            )
        }
    }

    fun solveDoubt() {
        val bitmap = _uiState.value.capturedBitmap ?: run {
            _uiState.update { it.copy(error = "Please capture or choose a problem photo first.") }
            return
        }

        if (_uiState.value.isSolving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSolving = true, error = null) }

            // Extract text from image on-device via ML Kit
            val ocrText = mlKitOcrHelper.extractTextFromBitmap(bitmap).getOrDefault("").trim()
            android.util.Log.i("QuovexOCR", "ML Kit on-device OCR extracted (${ocrText.length} chars): $ocrText")

            val userText = _uiState.value.questionText.trim()
            val queryText = buildString {
                if (userText.isNotBlank()) {
                    append(userText)
                }
                if (ocrText.isNotBlank()) {
                    if (isNotEmpty()) append("\n\n")
                    append("Extracted text from problem image:\n$ocrText")
                }
            }.ifBlank { "Please identify the problem, state the governing concept, and provide step-by-step reasoning with formulas and common pitfalls." }

            // Compress image to JPEG <= 512KB
            val bytes = compressBitmapToBytes(bitmap, maxKb = 512)
            val domainImage = DomainImageInput(bytes = bytes, mimeType = "image/jpeg")

            val result = solveImageDoubtUseCase(
                imageInput = domainImage,
                subject = _uiState.value.selectedSubject,
                questionText = queryText
            )

            result.onSuccess { solution ->
                val structured = solution.toStructured(_uiState.value.selectedSubject)
                _uiState.update {
                    it.copy(
                        isSolving = false,
                        solutionText = solution.solution,
                        structuredSolution = structured,
                        solutionProvider = solution.provider
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSolving = false,
                        error = error.message ?: "Failed to solve problem image. Please try again."
                    )
                }
            }
        }
    }

    fun sendFollowUpMessage(customPrompt: String? = null) {
        val promptToSend = (customPrompt ?: _uiState.value.followUpInputText).trim()
        if (promptToSend.isBlank()) return
        if (_uiState.value.isSendingFollowUp) return

        val solutionContext = _uiState.value.solutionText ?: return
        val problemContext = _uiState.value.questionText

        val userMessage = DoubtFollowUpMessage(isUser = true, text = promptToSend)
        val currentMessages = _uiState.value.followUpMessages + userMessage

        _uiState.update {
            it.copy(
                followUpMessages = currentMessages,
                followUpInputText = "",
                isSendingFollowUp = true,
                error = null
            )
        }

        viewModelScope.launch {
            val result = askDoubtFollowUpUseCase(
                subject = _uiState.value.selectedSubject,
                problemContext = problemContext,
                solutionContext = solutionContext,
                previousMessages = currentMessages,
                newQuestion = promptToSend
            )

            result.onSuccess { aiResponse ->
                val assistantMessage = DoubtFollowUpMessage(isUser = false, text = aiResponse)
                _uiState.update {
                    it.copy(
                        followUpMessages = it.followUpMessages + assistantMessage,
                        isSendingFollowUp = false
                    )
                }
            }.onFailure { error ->
                val assistantMessage = DoubtFollowUpMessage(
                    isUser = false,
                    text = "Quovex AI is temporarily busy. Please try asking your follow-up again."
                )
                _uiState.update {
                    it.copy(
                        followUpMessages = it.followUpMessages + assistantMessage,
                        isSendingFollowUp = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun saveSolutionAsMaterial(onSuccess: (materialId: Long) -> Unit) {
        val solution = _uiState.value.solutionText ?: return
        val structured = _uiState.value.structuredSolution
        val subject = _uiState.value.selectedSubject
        val question = _uiState.value.questionText.ifBlank { structured?.coreConcept ?: "Visual Problem Solution" }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingAsMaterial = true, error = null) }
            try {
                val subjectCategory = SubjectCatalog.findByName(subject)?.category ?: SubjectCategory.OTHER
                val material = LearningMaterial(
                    title = "Doubt Solved: ${question.take(60)}",
                    subject = subject,
                    subjectCategory = subjectCategory,
                    topic = structured?.coreConcept?.take(50) ?: "Problem Solving",
                    summary = structured?.problemSummary ?: solution.take(400),
                    keyPoints = (structured?.steps ?: emptyList()).take(6),
                    formulas = structured?.formulas ?: emptyList(),
                    inputType = NoteInputType.SCAN,
                    status = NoteProcessingStatus.READY
                )
                val materialId = repository.insertMaterial(material)
                _uiState.update { it.copy(isSavingAsMaterial = false, savedMaterialId = materialId) }
                onSuccess(materialId)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingAsMaterial = false, error = e.message ?: "Failed to save to Knowledge Hub.") }
            }
        }
    }

    fun createFlashcardDeck() {
        val solution = _uiState.value.solutionText ?: return
        val structured = _uiState.value.structuredSolution
        val subject = _uiState.value.selectedSubject
        val question = _uiState.value.questionText.ifBlank { structured?.coreConcept ?: "Academic Doubt" }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingFlashcards = true, error = null) }
            try {
                val deckId = repository.insertDeck(
                    title = "Flashcards: $subject Doubt",
                    subject = subject
                )

                // Generate structured atomic flashcards using Groq gpt-oss-20b JSON schema pipeline
                val prompt = "Extract 1 to 3 atomic study flashcards from this academic problem and step-by-step solution.\n\nProblem:\n$question\n\nSolution:\n$solution"
                val summarizeResult = aiRepository.summarizeNote(
                    rawText = prompt,
                    subject = subject
                )

                var cardsCreated = 0
                summarizeResult.onSuccess { summaryResult ->
                    if (summaryResult.flashcards.isNotEmpty()) {
                        summaryResult.flashcards.forEach { cardDto ->
                            val formulaSuffix = if (!cardDto.formula.isNullOrBlank()) {
                                "\n\nFormula: ${cardDto.formula}"
                            } else ""
                            repository.insertFlashcard(
                                deckId = deckId.toInt(),
                                frontContent = cardDto.question,
                                backContent = "${cardDto.answer}$formulaSuffix"
                            )
                            cardsCreated++
                        }
                    }
                }

                // Fallback if AI returned no cards
                if (cardsCreated == 0) {
                    val formulaText = structured?.formulas?.firstOrNull()?.latex
                    val formulaSuffix = if (!formulaText.isNullOrBlank()) "\n\nFormula: $formulaText" else ""
                    repository.insertFlashcard(
                        deckId = deckId.toInt(),
                        frontContent = "Key Formula & Concept ($subject): ${question.take(50)}",
                        backContent = "${structured?.coreConcept ?: solution.take(300)}$formulaSuffix"
                    )
                    cardsCreated = 1
                }

                _uiState.update {
                    it.copy(
                        isCreatingFlashcards = false,
                        flashcardsCreatedMessage = "Created $cardsCreated atomic card${if (cardsCreated > 1) "s" else ""} in Flashcards Library!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingFlashcards = false,
                        error = e.message ?: "Failed to create flashcard deck."
                    )
                }
            }
        }
    }

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
