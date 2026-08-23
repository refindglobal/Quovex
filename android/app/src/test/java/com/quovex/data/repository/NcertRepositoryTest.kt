package com.quovex.data.repository

import com.quovex.domain.model.NcertBook
import com.quovex.domain.model.NcertCatalog
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.model.NcertContentType
import com.quovex.domain.model.NcertVerificationStatus
import com.quovex.domain.usecase.FakeNcertRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NcertRepositoryTest {

    private lateinit var repository: FakeNcertRepository

    private val sampleCatalog = NcertCatalog(
        version = 1,
        lastUpdated = "2026-08-20",
        books = listOf(
            NcertBook(
                id = "ncert-12-phy-1",
                bookCode = "leph1",
                title = "Physics Part I",
                classLevel = 12,
                subject = "Physics",
                totalChapters = 8
            ),
            NcertBook(
                id = "ncert-12-chem-1",
                bookCode = "lech1",
                title = "Chemistry Part I",
                classLevel = 12,
                subject = "Chemistry",
                totalChapters = 5
            ),
            NcertBook(
                id = "ncert-10-sci",
                bookCode = "jesc1",
                title = "Science",
                classLevel = 10,
                subject = "Science",
                totalChapters = 13
            )
        ),
        chapters = listOf(
            NcertChapter(
                id = "ncert-12-phy-1-ch1",
                bookId = "ncert-12-phy-1",
                bookCode = "leph1",
                bookTitle = "Physics Part I",
                chapterNumber = 1,
                chapterTitle = "Electric Charges and Fields",
                classLevel = 12,
                subject = "Physics",
                officialSourceUrl = "https://ncert.nic.in/textbook.php?leph1=1-8",
                verificationStatus = NcertVerificationStatus.VERIFIED
            ),
            NcertChapter(
                id = "ncert-12-phy-1-ch6",
                bookId = "ncert-12-phy-1",
                bookCode = "leph1",
                bookTitle = "Physics Part I",
                chapterNumber = 6,
                chapterTitle = "Electromagnetic Induction",
                classLevel = 12,
                subject = "Physics",
                officialSourceUrl = "https://ncert.nic.in/textbook.php?leph1=6-8",
                verificationStatus = NcertVerificationStatus.VERIFIED
            )
        )
    )

    @Before
    fun setup() {
        repository = FakeNcertRepository()
        repository.setCatalog(sampleCatalog)
    }

    @Test
    fun testGetAvailableClasses() = runTest {
        val classes = repository.getAvailableClasses().first()
        assertEquals(listOf(10, 12), classes)
    }

    @Test
    fun testGetSubjectsForClass() = runTest {
        val subjects12 = repository.getSubjectsForClass(12).first()
        assertEquals(listOf("Chemistry", "Physics"), subjects12)

        val subjects10 = repository.getSubjectsForClass(10).first()
        assertEquals(listOf("Science"), subjects10)
    }

    @Test
    fun testGetBooksFiltering() = runTest {
        val physicsBooks = repository.getBooks(12, "Physics").first()
        assertEquals(1, physicsBooks.size)
        assertEquals("Physics Part I", physicsBooks[0].title)

        val allClass12 = repository.getBooks(12, "All").first()
        assertEquals(2, allClass12.size)

        val class10 = repository.getBooks(10, null).first()
        assertEquals(1, class10.size)
        assertEquals("Science", class10[0].title)
    }

    @Test
    fun testGetBookAndChaptersById() = runTest {
        val book = repository.getBookById("ncert-12-phy-1").first()
        assertNotNull(book)
        assertEquals("Physics Part I", book?.title)

        val chapters = repository.getChaptersForBook("ncert-12-phy-1").first()
        assertEquals(2, chapters.size)
        assertEquals(1, chapters[0].chapterNumber)
        assertEquals(6, chapters[1].chapterNumber)

        val chapter6 = repository.getChapterById("ncert-12-phy-1-ch6").first()
        assertNotNull(chapter6)
        assertEquals("Electromagnetic Induction", chapter6?.chapterTitle)
        assertEquals(NcertContentType.OFFICIAL_RESOURCE, chapter6?.contentType)
        assertEquals("NCERT", chapter6?.publisher)
    }

    @Test
    fun testRemoteCatalogSyncFallbackOnFailure() = runTest {
        repository.shouldFailRefresh = true
        val result = repository.refreshCatalog()
        assertTrue(result.isFailure)

        // Existing catalog remains intact
        val catalog = repository.getCatalog().first()
        assertEquals(3, catalog.books.size)
    }
}
