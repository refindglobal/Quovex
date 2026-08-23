package com.quovex.domain.model

/**
 * Pure Kotlin domain models for NCERT Official Resources.
 * Contains metadata-only representation of official NCERT textbooks & chapters.
 * No textbook text is stored or embedded.
 */

enum class NcertContentType {
    OFFICIAL_RESOURCE
}

enum class NcertVerificationStatus {
    VERIFIED,
    PENDING,
    FAILED
}

data class NcertBook(
    val id: String,
    val bookCode: String,
    val title: String,
    val classLevel: Int,
    val subject: String,
    val language: String = "English",
    val curriculum: String = "CBSE / NCERT",
    val publisher: String = "NCERT",
    val contentType: NcertContentType = NcertContentType.OFFICIAL_RESOURCE,
    val edition: String = "Rationalised Current Edition",
    val totalChapters: Int = 0,
    val coverUrl: String? = null
)

data class NcertChapter(
    val id: String,
    val bookId: String,
    val bookCode: String,
    val bookTitle: String,
    val chapterNumber: Int,
    val chapterTitle: String,
    val classLevel: Int,
    val subject: String,
    val officialSourceUrl: String,
    val language: String = "English",
    val curriculum: String = "CBSE / NCERT",
    val publisher: String = "NCERT",
    val contentType: NcertContentType = NcertContentType.OFFICIAL_RESOURCE,
    val lastVerifiedAt: String = "2026-08-20",
    val verificationStatus: NcertVerificationStatus = NcertVerificationStatus.VERIFIED
)

data class NcertCatalog(
    val version: Int = 1,
    val lastUpdated: String = "2026-08-20",
    val books: List<NcertBook> = emptyList(),
    val chapters: List<NcertChapter> = emptyList()
)
