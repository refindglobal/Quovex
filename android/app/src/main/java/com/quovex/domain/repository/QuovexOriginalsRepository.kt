package com.quovex.domain.repository

import com.quovex.domain.model.originals.QuovexOriginalBook
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for querying publicly published Quovex Originals.
 * Pure Kotlin — Zero Android framework dependencies.
 */
interface QuovexOriginalsRepository {
    /**
     * Streams published books filtered by optional subject or curriculum.
     */
    fun getPublishedOriginals(
        subject: String? = null,
        curriculum: String? = null
    ): Flow<List<QuovexOriginalBook>>

    /**
     * Fetches complete details of a single published book.
     */
    fun getOriginalBookDetails(bookId: String): Flow<QuovexOriginalBook?>
}
