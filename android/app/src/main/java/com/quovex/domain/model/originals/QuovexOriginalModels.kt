package com.quovex.domain.model.originals

/**
 * Quovex Originals Domain Models
 * Represents curated, human-approved educational books authored via Content Studio.
 *
 * Strict Architectural Separation:
 * - Domain layer has zero Android framework dependencies (pure Kotlin).
 * - Represents ONLY published editorial content.
 */

data class QuovexOriginalBook(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val description: String,
    val subject: String,
    val topic: String,
    val language: String = "en",
    val countryRegion: String = "IN",
    val curriculum: String,
    val gradeClass: String,
    val exam: String? = null,
    val difficulty: String = "Intermediate",
    val targetReadingTimeMinutes: Int = 45,
    val chapterCount: Int,
    val coverImageUrl: String? = null,
    val introduction: String,
    val learningObjectives: List<String> = emptyList(),
    val prerequisites: List<String> = emptyList(),
    val chapters: List<OriginalChapter> = emptyList(),
    val publishedAt: Long = 0L,
    val isStaging: Boolean = false
)

data class OriginalChapter(
    val chapterNumber: Int,
    val title: String,
    val summary: String,
    val learningObjectives: List<String> = emptyList(),
    val sections: List<OriginalSection> = emptyList(),
    val quickRevisionBulletPoints: List<String> = emptyList(),
    val flashcards: List<OriginalFlashcard> = emptyList(),
    val quizQuestions: List<OriginalQuizQuestion> = emptyList()
)

data class OriginalSection(
    val id: String,
    val sectionNumber: String,
    val title: String,
    val conceptualExplanation: String,
    val visualAnalogy: String? = null,
    val workedExamples: List<OriginalWorkedExample> = emptyList(),
    val realWorldExamples: List<OriginalRealWorldExample> = emptyList(),
    val commonMistakes: List<OriginalCommonMistake> = emptyList(),
    val summaryPoints: List<String> = emptyList()
)

data class OriginalWorkedExample(
    val id: String,
    val problemStatement: String,
    val stepByStepSolution: List<SolutionStep> = emptyList(),
    val keyTakeaway: String,
    val difficulty: String = "Intermediate"
)

data class SolutionStep(
    val stepNumber: Int,
    val explanation: String,
    val mathFormula: String? = null
)

data class OriginalRealWorldExample(
    val id: String,
    val domain: String,
    val title: String,
    val narrative: String,
    val physicsOrConceptPrinciple: String
)

data class OriginalCommonMistake(
    val id: String,
    val misconception: String,
    val whyStudentsMakeIt: String,
    val correctUnderstanding: String,
    val quickCheck: String
)

data class OriginalFlashcard(
    val id: String,
    val frontPrompt: String,
    val backAnswer: String,
    val conceptTag: String,
    val difficultyRating: Int = 2
)

data class OriginalQuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val pedagogicalExplanation: String,
    val distractorExplanations: List<String> = emptyList(),
    val formulaReference: String? = null
)
