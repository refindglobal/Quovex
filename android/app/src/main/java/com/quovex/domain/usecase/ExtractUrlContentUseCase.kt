package com.quovex.domain.usecase

import com.quovex.domain.model.ExtractedContent
import com.quovex.domain.model.NoteInputType
import com.quovex.domain.repository.ContentExtractionRepository
import javax.inject.Inject

class ExtractUrlContentUseCase @Inject constructor(
    private val contentExtractionRepository: ContentExtractionRepository
) {
    suspend operator fun invoke(
        url: String,
        inputType: NoteInputType
    ): Result<ExtractedContent> {
        val trimmed = url.trim()
        if (trimmed.isBlank() || (!trimmed.startsWith("http://", ignoreCase = true) && !trimmed.startsWith("https://", ignoreCase = true))) {
            return Result.failure(IllegalArgumentException("Please enter a valid URL starting with http:// or https://"))
        }

        return when (inputType) {
            NoteInputType.YOUTUBE -> contentExtractionRepository.extractYouTubeVideo(trimmed)
            else -> contentExtractionRepository.extractWebArticle(trimmed)
        }
    }
}
