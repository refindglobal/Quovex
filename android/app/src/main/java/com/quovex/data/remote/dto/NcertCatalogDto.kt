package com.quovex.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.quovex.domain.model.NcertBook
import com.quovex.domain.model.NcertCatalog
import com.quovex.domain.model.NcertChapter
import com.quovex.domain.model.NcertContentType
import com.quovex.domain.model.NcertVerificationStatus

data class NcertCatalogResponseDto(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("lastUpdated") val lastUpdated: String = "",
    @SerializedName("curriculum") val curriculum: String = "CBSE / NCERT",
    @SerializedName("publisher") val publisher: String = "NCERT",
    @SerializedName("books") val books: List<NcertBookDto> = emptyList(),
    @SerializedName("chapters") val chapters: List<NcertChapterDto> = emptyList()
)

data class NcertBookDto(
    @SerializedName("id") val id: String,
    @SerializedName("bookCode") val bookCode: String,
    @SerializedName("title") val title: String,
    @SerializedName("classLevel") val classLevel: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("language") val language: String = "English",
    @SerializedName("curriculum") val curriculum: String = "CBSE / NCERT",
    @SerializedName("publisher") val publisher: String = "NCERT",
    @SerializedName("contentType") val contentType: String = "OFFICIAL_RESOURCE",
    @SerializedName("edition") val edition: String = "Rationalised Current Edition",
    @SerializedName("totalChapters") val totalChapters: Int = 0,
    @SerializedName("coverUrl") val coverUrl: String? = null
)

data class NcertChapterDto(
    @SerializedName("id") val id: String,
    @SerializedName("bookId") val bookId: String,
    @SerializedName("bookCode") val bookCode: String,
    @SerializedName("bookTitle") val bookTitle: String,
    @SerializedName("chapterNumber") val chapterNumber: Int,
    @SerializedName("chapterTitle") val chapterTitle: String,
    @SerializedName("classLevel") val classLevel: Int,
    @SerializedName("subject") val subject: String,
    @SerializedName("officialSourceUrl") val officialSourceUrl: String,
    @SerializedName("language") val language: String = "English",
    @SerializedName("curriculum") val curriculum: String = "CBSE / NCERT",
    @SerializedName("publisher") val publisher: String = "NCERT",
    @SerializedName("contentType") val contentType: String = "OFFICIAL_RESOURCE",
    @SerializedName("lastVerifiedAt") val lastVerifiedAt: String = "2026-08-20",
    @SerializedName("verificationStatus") val verificationStatus: String = "VERIFIED"
)

fun NcertCatalogResponseDto.toDomain(): NcertCatalog {
    return NcertCatalog(
        version = version,
        lastUpdated = lastUpdated,
        books = books.map { it.toDomain() },
        chapters = chapters.map { it.toDomain() }
    )
}

fun NcertBookDto.toDomain(): NcertBook {
    return NcertBook(
        id = id,
        bookCode = bookCode,
        title = title,
        classLevel = classLevel,
        subject = subject,
        language = language,
        curriculum = curriculum,
        publisher = publisher,
        contentType = NcertContentType.OFFICIAL_RESOURCE,
        edition = edition,
        totalChapters = totalChapters,
        coverUrl = coverUrl
    )
}

fun NcertChapterDto.toDomain(): NcertChapter {
    val status = try {
        NcertVerificationStatus.valueOf(verificationStatus.uppercase())
    } catch (e: Exception) {
        NcertVerificationStatus.VERIFIED
    }

    return NcertChapter(
        id = id,
        bookId = bookId,
        bookCode = bookCode,
        bookTitle = bookTitle,
        chapterNumber = chapterNumber,
        chapterTitle = chapterTitle,
        classLevel = classLevel,
        subject = subject,
        officialSourceUrl = officialSourceUrl,
        language = language,
        curriculum = curriculum,
        publisher = publisher,
        contentType = NcertContentType.OFFICIAL_RESOURCE,
        lastVerifiedAt = lastVerifiedAt,
        verificationStatus = status
    )
}
