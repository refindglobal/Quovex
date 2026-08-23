package com.quovex.domain.usecase

import com.quovex.data.batch.DocumentAnalysisBatcher
import com.quovex.domain.model.DomainImageInput
import com.quovex.domain.model.ScannedChapter
import com.quovex.domain.model.ScannedDocumentOrganization
import com.quovex.domain.model.ScannedNoteSection
import com.quovex.domain.model.ScannedSubtopic
import com.quovex.domain.repository.AIRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyzeDocumentImagesUseCaseTest {

    private lateinit var fakeAiRepository: FakeAIRepository
    private lateinit var useCase: AnalyzeDocumentImagesUseCase

    @Before
    fun setUp() {
        fakeAiRepository = FakeAIRepository()
        useCase = AnalyzeDocumentImagesUseCase(fakeAiRepository)
    }

    @Test
    fun `invoke with empty pages returns failure`() = runBlocking {
        val result = useCase(emptyList())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `invoke with valid pages delegates to repository and returns organization`() = runBlocking {
        val pages = listOf(
            DomainImageInput(bytes = byteArrayOf(1, 2, 3), mimeType = "image/jpeg"),
            DomainImageInput(bytes = byteArrayOf(4, 5, 6), mimeType = "image/jpeg")
        )

        fakeAiRepository.documentOrganizationResult = Result.success(
            ScannedDocumentOrganization(
                detectedSubject = "Accountancy",
                detectedStream = "Commerce",
                documentTitle = "Journal Entries & Ledger",
                chapters = listOf(
                    ScannedChapter(
                        title = "Chapter 1: Rules of Debit and Credit",
                        subtopics = listOf(
                            ScannedSubtopic(
                                title = "Types of Accounts",
                                noteSections = listOf(
                                    ScannedNoteSection(
                                        sectionTitle = "Personal, Real, Nominal",
                                        content = "Golden rules of accounting",
                                        keyPoints = listOf("Debit what comes in, credit what goes out")
                                    )
                                )
                            )
                        )
                    )
                ),
                confidence = 0.95f
            )
        )

        val result = useCase(pages, "Accountancy")
        assertTrue(result.isSuccess)

        val org = result.getOrNull()!!
        assertEquals("Accountancy", org.detectedSubject)
        assertEquals("Commerce", org.detectedStream)
        assertEquals(1, org.chapters.size)
        assertEquals("Chapter 1: Rules of Debit and Credit", org.chapters[0].title)
    }

    @Test
    fun `batcher default size is 5 and configurable`() {
        val batcher = DocumentAnalysisBatcher(fakeAiRepository)
        assertEquals(5, batcher.maxPagesPerRequest)

        batcher.maxPagesPerRequest = 10
        assertEquals(10, batcher.maxPagesPerRequest)
    }
}
