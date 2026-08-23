package com.quovex.domain.repository

import com.quovex.domain.model.NcertBook
import com.quovex.domain.model.NcertCatalog
import com.quovex.domain.model.NcertChapter
import kotlinx.coroutines.flow.Flow

/**
 * Pure Kotlin repository interface for the NCERT Official Resource Library.
 *
 * RESPONSIBILITY: Catalog metadata ONLY.
 *   - Classes, subjects, books, chapters
 *   - Official source URLs
 *   - Verification status
 *   - Remote/local catalog sync
 *
 * NOT responsible for:
 *   - PDF file download (see NcertPdfCacheRepository)
 *   - Local file storage (see NcertPdfCacheRepository)
 *   - Cache management (see NcertPdfCacheRepository)
 *
 * Domain purity: No Android File, Uri, or Bitmap imports in this interface.
 */
interface NcertRepository {
    fun getCatalog(): Flow<NcertCatalog>
    fun getAvailableClasses(): Flow<List<Int>>
    fun getSubjectsForClass(classLevel: Int): Flow<List<String>>
    fun getBooks(classLevel: Int?, subject: String?): Flow<List<NcertBook>>
    fun getBookById(bookId: String): Flow<NcertBook?>
    fun getChaptersForBook(bookId: String): Flow<List<NcertChapter>>
    fun getChapterById(chapterId: String): Flow<NcertChapter?>
    suspend fun refreshCatalog(): Result<NcertCatalog>
}
