package com.quovex.domain.usecase

import com.quovex.domain.model.NcertBook
import com.quovex.domain.model.NcertCatalog
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.model.NcertContentType
import com.quovex.domain.model.NcertVerificationStatus
import com.quovex.domain.repository.NcertRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// FakeNcertRepository — test double for NcertRepository (catalog-only interface)
// PDF caching is handled by NcertPdfCacheRepository — tested separately

class FakeNcertRepository : NcertRepository {

    val sampleBooks = listOf(
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
    )

    val sampleChapters = listOf(
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

    private val _catalog = MutableStateFlow(
        NcertCatalog(
            version = 1,
            lastUpdated = "2026-08-20",
            books = sampleBooks,
            chapters = sampleChapters
        )
    )

    var shouldFailRefresh: Boolean = false

    override fun getCatalog(): Flow<NcertCatalog> = _catalog.asStateFlow()

    override fun getAvailableClasses(): Flow<List<Int>> {
        return _catalog.map { cat ->
            cat.books.map { it.classLevel }.distinct().sorted()
        }
    }

    override fun getSubjectsForClass(classLevel: Int): Flow<List<String>> {
        return _catalog.map { cat ->
            cat.books.filter { it.classLevel == classLevel }
                .map { it.subject }
                .distinct()
                .sorted()
        }
    }

    override fun getBooks(classLevel: Int?, subject: String?): Flow<List<NcertBook>> {
        return _catalog.map { cat ->
            cat.books.filter { book ->
                (classLevel == null || book.classLevel == classLevel) &&
                (subject.isNullOrBlank() || subject.equals("All", ignoreCase = true) || book.subject.equals(subject, ignoreCase = true))
            }
        }
    }

    override fun getBookById(bookId: String): Flow<NcertBook?> {
        return _catalog.map { cat ->
            cat.books.firstOrNull { it.id == bookId }
        }
    }

    override fun getChaptersForBook(bookId: String): Flow<List<NcertChapter>> {
        return _catalog.map { cat ->
            cat.chapters.filter { it.bookId == bookId }.sortedBy { it.chapterNumber }
        }
    }

    override fun getChapterById(chapterId: String): Flow<NcertChapter?> {
        return _catalog.map { cat ->
            cat.chapters.firstOrNull { it.id == chapterId }
        }
    }

    override suspend fun refreshCatalog(): Result<NcertCatalog> {
        if (shouldFailRefresh) {
            return Result.failure(RuntimeException("Sync failed"))
        }
        return Result.success(_catalog.value)
    }

    fun setCatalog(catalog: NcertCatalog) {
        _catalog.value = catalog
    }
}
