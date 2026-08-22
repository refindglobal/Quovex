package com.quovex.domain.usecase

import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.NoteItem
import com.quovex.domain.model.NoteProcessingStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for OCR state machine behavior and DeleteNoteUseCase with Storage cleanup.
 *
 * OCR flow (Document Scanner):
 *   Camera/gallery → ML Kit OCR → editable text → SaveNoteUseCase
 *
 * Image Doubt flow (separate):
 *   Camera/gallery → DomainImageInput → SolveImageDoubtUseCase → solution
 *
 * These are separate business flows that share image compression infrastructure.
 */
class OcrStateTest {

    private lateinit var repository: FakeQuovexRepository
    private lateinit var aiRepository: FakeAIRepository
    private lateinit var storageHelper: FakeFirebaseStorageHelper

    @Before
    fun setup() {
        repository = FakeQuovexRepository()
        aiRepository = FakeAIRepository()
        storageHelper = FakeFirebaseStorageHelper()
    }

    // ── OCR Text Assembly ──

    @Test
    fun `empty OCR output falls back to user question text only`() {
        // Simulate what the ViewModel does before calling SolveImageDoubtUseCase
        val ocrText = ""
        val userQuestion = "What is the acceleration of the block?"

        val combinedQuery = buildCombinedQuery(ocrText, userQuestion)

        // If OCR is empty, the query should be user question alone
        assertEquals(userQuestion, combinedQuery)
    }

    @Test
    fun `OCR text and user question are combined into a single query`() {
        val ocrText = "F = 10 N, m = 2 kg, find acceleration"
        val userQuestion = "Apply Newton's second law"

        val combinedQuery = buildCombinedQuery(ocrText, userQuestion)

        assertTrue(combinedQuery.contains(ocrText))
        assertTrue(combinedQuery.contains(userQuestion))
    }

    @Test
    fun `user question alone is used when OCR blank and user has entered text`() {
        val ocrText = "   " // whitespace only
        val userQuestion = "Explain photosynthesis"

        val combinedQuery = buildCombinedQuery(ocrText, userQuestion)
        assertEquals(userQuestion, combinedQuery)
    }

    @Test
    fun `OCR text alone is used when user question is empty`() {
        val ocrText = "H2O + CO2 → glucose + O2"
        val userQuestion = ""

        val combinedQuery = buildCombinedQuery(ocrText, userQuestion)
        assertEquals(ocrText, combinedQuery)
    }

    // ── Image Doubt Flow ──

    @Test
    fun `SolveImageDoubtUseCase succeeds with valid image bytes and combined query`() = runTest {
        val imageBytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val imageInput = DomainImageInput(bytes = imageBytes, mimeType = "image/jpeg")
        val combinedQuery = "OCR: F=ma solve for a\nUser: Find acceleration given m=2kg, F=4N"

        val useCase = SolveImageDoubtUseCase(aiRepository)
        val result = useCase(imageInput, subject = "Physics", questionText = combinedQuery)

        assertTrue(result.isSuccess)
        val solution = result.getOrNull()
        assertTrue(solution!!.solution.isNotBlank())
    }

    @Test
    fun `SolveImageDoubtUseCase rejects empty image bytes regardless of query`() = runTest {
        val emptyImage = DomainImageInput(bytes = ByteArray(0))
        val useCase = SolveImageDoubtUseCase(aiRepository)
        val result = useCase(emptyImage, subject = "Chemistry", questionText = "Balance equation")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `SolveImageDoubtUseCase propagates network failure from AI gateway`() = runTest {
        aiRepository.shouldFailDoubt = true
        val imageInput = DomainImageInput(bytes = byteArrayOf(1, 2, 3))
        val useCase = SolveImageDoubtUseCase(aiRepository)
        val result = useCase(imageInput, subject = "Physics")

        assertTrue(result.isFailure)
        assertEquals("Vision AI Provider unavailable", result.exceptionOrNull()?.message)
    }

    // ── Delete Note with Storage Cleanup ──

    @Test
    fun `DeleteNoteUseCase cleans up Storage file when storageRef is present`() = runTest {
        val storageRef = "notes/user123/42/scan.jpg"
        val note = NoteItem(
            id = 42L,
            title = "Scanned Physics Notes",
            subject = "Physics",
            content = "OCR extracted text",
            status = NoteProcessingStatus.READY,
            storageRef = storageRef
        )
        repository.insertNote(note)

        val deleteUseCase = DeleteNoteUseCase(repository, storageHelper)
        val result = deleteUseCase(42L, storageRef)

        assertEquals(1, result)
        assertNull(repository.getNoteById(42L))
        // Storage delete must have been called
        assertTrue(storageHelper.deletedPaths.contains(storageRef))
    }

    @Test
    fun `DeleteNoteUseCase succeeds even when Storage delete path is null`() = runTest {
        val note = NoteItem(id = 55L, title = "Text note", subject = "History", content = "Some content")
        repository.insertNote(note)

        val deleteUseCase = DeleteNoteUseCase(repository, storageHelper)
        val result = deleteUseCase(55L, null)

        assertEquals(1, result)
        assertNull(repository.getNoteById(55L))
        assertTrue(storageHelper.deletedPaths.isEmpty()) // No storage delete should occur
    }

    // ── Helper ──

    /**
     * Mirrors the ViewModel-level logic that combines OCR extracted text with user's typed question.
     */
    private fun buildCombinedQuery(ocrText: String, userQuestion: String): String {
        val trimmedOcr = ocrText.trim()
        val trimmedQ = userQuestion.trim()
        return when {
            trimmedOcr.isNotEmpty() && trimmedQ.isNotEmpty() -> "$trimmedOcr\n$trimmedQ"
            trimmedOcr.isNotEmpty() -> trimmedOcr
            else -> trimmedQ
        }
    }
}
