package com.quovex.domain.repository

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Fake implementation of NcertPdfCacheRepository for testing.
 */
class FakeNcertPdfCacheRepository : NcertPdfCacheRepository {
    val cachedPdfs = mutableMapOf<String, String>()
    var shouldFailDownload: Boolean = false

    override suspend fun downloadPdfToCache(chapterId: String, url: String): Result<String> {
        if (shouldFailDownload) {
            return Result.failure(Exception("NCERT content is temporarily unavailable."))
        }
        val path = "/fake/cache/ncert_pdfs/$chapterId.pdf"
        cachedPdfs[chapterId] = path
        return Result.success(path)
    }

    override fun getCachedPdfPath(chapterId: String): String? {
        return cachedPdfs[chapterId]
    }

    override suspend fun clearPdfCache(): Result<Unit> {
        cachedPdfs.clear()
        return Result.success(Unit)
    }
}

class NcertPdfCacheRepositoryTest {

    private lateinit var repository: FakeNcertPdfCacheRepository

    @Before
    fun setUp() {
        repository = FakeNcertPdfCacheRepository()
    }

    @Test
    fun `isCached returns false when not downloaded`() {
        assertFalse(repository.isCached("chapter-1"))
        assertNull(repository.getCachedPdfPath("chapter-1"))
    }

    @Test
    fun `downloadPdfToCache caches path and isCached returns true`() = runBlocking {
        val result = repository.downloadPdfToCache("chapter-1", "https://ncert.nic.in/textbook.php?leph1=1-8")
        assertTrue(result.isSuccess)
        assertEquals("/fake/cache/ncert_pdfs/chapter-1.pdf", result.getOrNull())

        assertTrue(repository.isCached("chapter-1"))
        assertEquals("/fake/cache/ncert_pdfs/chapter-1.pdf", repository.getCachedPdfPath("chapter-1"))
    }

    @Test
    fun `clearPdfCache removes all cached paths`() = runBlocking {
        repository.downloadPdfToCache("chapter-1", "https://ncert.nic.in/textbook.php?leph1=1-8")
        repository.downloadPdfToCache("chapter-2", "https://ncert.nic.in/textbook.php?leph1=2-8")
        assertTrue(repository.isCached("chapter-1"))
        assertTrue(repository.isCached("chapter-2"))

        repository.clearPdfCache()
        assertFalse(repository.isCached("chapter-1"))
        assertFalse(repository.isCached("chapter-2"))
    }

    @Test
    fun `download failure returns descriptive error`() = runBlocking {
        repository.shouldFailDownload = true
        val result = repository.downloadPdfToCache("chapter-1", "https://ncert.nic.in/textbook.php?leph1=1-8")
        assertTrue(result.isFailure)
        assertEquals("NCERT content is temporarily unavailable.", result.exceptionOrNull()?.message)
    }
}
