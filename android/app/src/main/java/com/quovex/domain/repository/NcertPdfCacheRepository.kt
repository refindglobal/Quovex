package com.quovex.domain.repository

/**
 * Repository interface for NCERT PDF file download and local caching.
 *
 * RESPONSIBILITY: PDF file lifecycle ONLY.
 *   - Download PDF from official source URL to device cache
 *   - Return cached file path (platform-neutral: String path)
 *   - Cache validation and cleanup
 *
 * SEPARATE from [NcertRepository] which handles catalog metadata.
 *
 * Domain purity:
 * - This interface returns String paths, not java.io.File
 * - The data/presentation layers convert these paths to File/Uri as needed
 * - No Android Context, File, or Uri in this interface
 *
 * Caching policy:
 * - PDFs cached in app's cacheDir (not permanent public storage)
 * - Official source URL is preserved; cache is a performance layer only
 * - User or system may clear cache at any time
 */
interface NcertPdfCacheRepository {

    /**
     * Download an NCERT PDF from its official source URL to the local cache.
     *
     * Returns the absolute file path of the cached PDF, ready for the
     * AndroidPdfViewer to load via File(path).
     *
     * @param chapterId Used as the cache key (filename: {chapterId}.pdf)
     * @param url Official NCERT PDF URL (e.g. ncert.nic.in/...)
     * @return Absolute file path string, or failure if download fails
     */
    suspend fun downloadPdfToCache(chapterId: String, url: String): Result<String>

    /**
     * Returns the absolute file path of a cached PDF if it exists and is valid.
     * Returns null if not cached (caller should then [downloadPdfToCache]).
     *
     * @param chapterId Cache key
     */
    fun getCachedPdfPath(chapterId: String): String?

    /**
     * Clears all cached NCERT PDFs from device storage.
     * Called on low storage or user-initiated cache clear.
     */
    suspend fun clearPdfCache(): Result<Unit>

    /**
     * Checks whether a valid cached PDF exists for the given chapter.
     */
    fun isCached(chapterId: String): Boolean = getCachedPdfPath(chapterId) != null
}
