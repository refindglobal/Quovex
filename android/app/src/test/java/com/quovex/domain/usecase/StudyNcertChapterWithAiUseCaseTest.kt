package com.quovex.domain.usecase

import com.quovex.domain.model.LearningMaterial
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.model.NcertContentType
import com.quovex.domain.model.NcertVerificationStatus
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.model.NoteProcessingStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StudyNcertChapterWithAiUseCaseTest {

    private lateinit var fakeQuovexRepository: FakeQuovexRepository
    private lateinit var fakeAiRepository: FakeAIRepository
    private lateinit var saveNoteUseCase: SaveNoteUseCase
    private lateinit var summarizeNoteUseCase: SummarizeNoteUseCase
    private lateinit var useCase: StudyNcertChapterWithAiUseCase

    private val sampleChapter = NcertChapter(
        id = "ncert-12-phy-1-ch6",
        bookId = "ncert-12-phy-1",
        bookCode = "leph1",
        bookTitle = "Physics Part I",
        chapterNumber = 6,
        chapterTitle = "Electromagnetic Induction",
        classLevel = 12,
        subject = "Physics",
        officialSourceUrl = "https://ncert.nic.in/textbook.php?leph1=6-8",
        curriculum = "CBSE / NCERT",
        publisher = "NCERT",
        contentType = NcertContentType.OFFICIAL_RESOURCE,
        verificationStatus = NcertVerificationStatus.VERIFIED
    )

    @Before
    fun setup() {
        fakeQuovexRepository = FakeQuovexRepository()
        fakeAiRepository = FakeAIRepository()
        saveNoteUseCase = SaveNoteUseCase(fakeQuovexRepository)
        summarizeNoteUseCase = SummarizeNoteUseCase(fakeAiRepository)

        useCase = StudyNcertChapterWithAiUseCase(
            quovexRepository = fakeQuovexRepository,
            saveNoteUseCase = saveNoteUseCase,
            summarizeNoteUseCase = summarizeNoteUseCase
        )
    }

    @Test
    fun testStudyNcertChapterWithAiCreatesMaterial() = runTest {
        val result = useCase(sampleChapter)
        assertTrue(result.isSuccess)
        val materialId = result.getOrNull()
        assertNotNull(materialId)
        assertTrue(materialId!! > 0)

        val savedNote = fakeQuovexRepository.notesMap[materialId]
        assertNotNull("Note must be saved in notesMap", savedNote)
        assertEquals("Physics", savedNote?.subject)
        assertEquals("https://ncert.nic.in/textbook.php?leph1=6-8", savedNote?.sourceUrl)
        assertEquals(NoteInputType.URL, savedNote?.inputType)
        assertEquals(NoteProcessingStatus.READY, savedNote?.status)
    }

    @Test
    fun testStudyNcertChapterReusesExistingMaterial() = runTest {
        // Pre-populate with existing material
        fakeQuovexRepository.materialsMap[42L] = LearningMaterial(
            id = 42L,
            title = "Physics Part I: Ch 6 - Electromagnetic Induction",
            subject = "Physics",
            topic = "Electromagnetic Induction",
            sourceUrl = "https://ncert.nic.in/textbook.php?leph1=6-8",
            status = NoteProcessingStatus.READY
        )

        val result = useCase(sampleChapter)
        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
    }
}
