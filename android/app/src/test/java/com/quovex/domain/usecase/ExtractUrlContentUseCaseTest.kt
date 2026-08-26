package com.quovex.domain.usecase

import com.quovex.domain.model.ExtractedContent
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.repository.ContentExtractionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExtractUrlContentUseCaseTest {

    private val repository = mockk<ContentExtractionRepository>()
    private lateinit var useCase: ExtractUrlContentUseCase

    @Before
    fun setup() {
        useCase = ExtractUrlContentUseCase(repository)
    }

    @Test
    fun invoke_withInvalidUrl_returnsFailureWithoutCallingRepository() = runTest {
        val result = useCase("invalid-url", NoteInputType.URL)
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { repository.extractWebArticle(any()) }
        coVerify(exactly = 0) { repository.extractYouTubeVideo(any()) }
    }

    @Test
    fun invoke_withWebUrl_delegatesToExtractWebArticle() = runTest {
        val expected = ExtractedContent(
            title = "Quantum Physics",
            content = "Extracted article body",
            inputType = NoteInputType.URL,
            sourceUrl = "https://example.com/quantum"
        )
        coEvery { repository.extractWebArticle("https://example.com/quantum") } returns Result.success(expected)

        val result = useCase("https://example.com/quantum", NoteInputType.URL)
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { repository.extractWebArticle("https://example.com/quantum") }
    }

    @Test
    fun invoke_withYouTubeUrl_delegatesToExtractYouTubeVideo() = runTest {
        val expected = ExtractedContent(
            title = "Calculus 101",
            content = "Extracted transcript",
            inputType = NoteInputType.YOUTUBE,
            sourceUrl = "https://youtube.com/watch?v=12345678901",
            authorOrChannel = "MIT OpenCourseWare"
        )
        coEvery { repository.extractYouTubeVideo("https://youtube.com/watch?v=12345678901") } returns Result.success(expected)

        val result = useCase("https://youtube.com/watch?v=12345678901", NoteInputType.YOUTUBE)
        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
        coVerify(exactly = 1) { repository.extractYouTubeVideo("https://youtube.com/watch?v=12345678901") }
    }
}
