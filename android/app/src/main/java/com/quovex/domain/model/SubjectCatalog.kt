package com.quovex.domain.model

/**
 * Universal subject taxonomy for Quovex.
 *
 * Quovex is NOT a Science-only application.
 * All features — AI Chat, Scanner, Knowledge Hub, Image Doubt, Flashcards, Quiz,
 * and NCERT Library — must work with every academic stream.
 *
 * This is the SINGLE SOURCE OF TRUTH for subjects in the app.
 * No UI file should contain hardcoded listOf("Physics", "Chemistry", "Maths", "Biology").
 * All subject lists must be sourced from this catalog.
 */

/** Broad academic stream categories */
enum class SubjectCategory(val displayName: String) {
    SCIENCE("Science"),
    COMMERCE("Commerce"),
    HUMANITIES("Humanities"),
    LANGUAGES("Languages"),
    MATHEMATICS("Mathematics"),
    SOCIAL_SCIENCE("Social Science"),
    VOCATIONAL("Vocational"),
    OTHER("Other")
}

/**
 * A configurable subject definition.
 *
 * @param subjectId Unique lowercase identifier (e.g. "physics", "accountancy")
 * @param subjectName Human-readable display name (e.g. "Physics", "Accountancy")
 * @param category Broad stream category
 * @param ncertStream NCERT stream name for catalog grouping (e.g. "Science", "Commerce")
 * @param isNcertAvailable Whether NCERT textbooks exist for this subject
 * @param classesCovered Class levels for which NCERT covers this subject (e.g. 9..12)
 */
data class SubjectDefinition(
    val subjectId: String,
    val subjectName: String,
    val category: SubjectCategory,
    val ncertStream: String,
    val isNcertAvailable: Boolean = true,
    val classesCovered: List<Int> = (9..12).toList()
)

/**
 * The complete Quovex subject catalog.
 *
 * Covers all major NCERT academic streams:
 * Science | Commerce | Humanities | Languages | Mathematics | Social Science
 *
 * Used by:
 * - Knowledge Hub subject filter
 * - AI Chat subject selector
 * - Document Scanner subject selector
 * - Image Doubt subject selector
 * - NCERT Browser subject filter
 * - GetConfiguredSubjectsUseCase
 */
object SubjectCatalog {

    val ALL: List<SubjectDefinition> = listOf(
        // ── MATHEMATICS ───────────────────────────────────────────────────────
        SubjectDefinition(
            subjectId = "mathematics",
            subjectName = "Mathematics",
            category = SubjectCategory.MATHEMATICS,
            ncertStream = "Mathematics",
            classesCovered = (6..12).toList()
        ),

        // ── SCIENCE ───────────────────────────────────────────────────────────
        SubjectDefinition(
            subjectId = "science",
            subjectName = "Science",
            category = SubjectCategory.SCIENCE,
            ncertStream = "Science",
            classesCovered = (6..10).toList()
        ),
        SubjectDefinition(
            subjectId = "physics",
            subjectName = "Physics",
            category = SubjectCategory.SCIENCE,
            ncertStream = "Science",
            classesCovered = listOf(11, 12)
        ),
        SubjectDefinition(
            subjectId = "chemistry",
            subjectName = "Chemistry",
            category = SubjectCategory.SCIENCE,
            ncertStream = "Science",
            classesCovered = listOf(11, 12)
        ),
        SubjectDefinition(
            subjectId = "biology",
            subjectName = "Biology",
            category = SubjectCategory.SCIENCE,
            ncertStream = "Science",
            classesCovered = listOf(11, 12)
        ),

        // ── COMMERCE ──────────────────────────────────────────────────────────
        SubjectDefinition(
            subjectId = "accountancy",
            subjectName = "Accountancy",
            category = SubjectCategory.COMMERCE,
            ncertStream = "Commerce",
            classesCovered = listOf(11, 12)
        ),
        SubjectDefinition(
            subjectId = "business_studies",
            subjectName = "Business Studies",
            category = SubjectCategory.COMMERCE,
            ncertStream = "Commerce",
            classesCovered = listOf(11, 12)
        ),
        SubjectDefinition(
            subjectId = "economics",
            subjectName = "Economics",
            category = SubjectCategory.COMMERCE,
            ncertStream = "Commerce",
            classesCovered = listOf(11, 12)
        ),

        // ── HUMANITIES / SOCIAL SCIENCE ───────────────────────────────────────
        SubjectDefinition(
            subjectId = "social_science",
            subjectName = "Social Science",
            category = SubjectCategory.SOCIAL_SCIENCE,
            ncertStream = "Social Science",
            classesCovered = (6..10).toList()
        ),
        SubjectDefinition(
            subjectId = "history",
            subjectName = "History",
            category = SubjectCategory.HUMANITIES,
            ncertStream = "Humanities",
            classesCovered = listOf(11, 12)
        ),
        SubjectDefinition(
            subjectId = "political_science",
            subjectName = "Political Science",
            category = SubjectCategory.HUMANITIES,
            ncertStream = "Humanities",
            classesCovered = listOf(11, 12)
        ),
        SubjectDefinition(
            subjectId = "geography",
            subjectName = "Geography",
            category = SubjectCategory.HUMANITIES,
            ncertStream = "Humanities",
            classesCovered = listOf(11, 12)
        ),
        SubjectDefinition(
            subjectId = "sociology",
            subjectName = "Sociology",
            category = SubjectCategory.HUMANITIES,
            ncertStream = "Humanities",
            classesCovered = listOf(11, 12)
        ),
        SubjectDefinition(
            subjectId = "psychology",
            subjectName = "Psychology",
            category = SubjectCategory.HUMANITIES,
            ncertStream = "Humanities",
            classesCovered = listOf(11, 12)
        ),

        // ── COMPUTER SCIENCE ──────────────────────────────────────────────────
        SubjectDefinition(
            subjectId = "computer_science",
            subjectName = "Computer Science",
            category = SubjectCategory.SCIENCE,
            ncertStream = "Computer Science",
            classesCovered = listOf(11, 12)
        ),

        // ── LANGUAGES ─────────────────────────────────────────────────────────
        SubjectDefinition(
            subjectId = "english",
            subjectName = "English",
            category = SubjectCategory.LANGUAGES,
            ncertStream = "Languages",
            classesCovered = (6..12).toList()
        ),
        SubjectDefinition(
            subjectId = "hindi",
            subjectName = "Hindi",
            category = SubjectCategory.LANGUAGES,
            ncertStream = "Languages",
            classesCovered = (6..12).toList()
        ),
        SubjectDefinition(
            subjectId = "sanskrit",
            subjectName = "Sanskrit",
            category = SubjectCategory.LANGUAGES,
            ncertStream = "Languages",
            classesCovered = (6..12).toList()
        )
    )

    /** All subject names (display) in default order */
    val allNames: List<String> get() = ALL.map { it.subjectName }

    /** Subjects belonging to a specific category */
    fun byCategory(category: SubjectCategory): List<SubjectDefinition> =
        ALL.filter { it.category == category }

    /** Subjects available for a given class level */
    fun forClass(classLevel: Int): List<SubjectDefinition> =
        ALL.filter { classLevel in it.classesCovered }

    /** Subject names for a given class level */
    fun namesForClass(classLevel: Int): List<String> =
        forClass(classLevel).map { it.subjectName }

    /** Get definition by subjectId (e.g. "physics") */
    fun findById(subjectId: String): SubjectDefinition? =
        ALL.find { it.subjectId == subjectId }

    /** Get definition by display name (e.g. "Physics") */
    fun findByName(name: String): SubjectDefinition? =
        ALL.find { it.subjectName.equals(name, ignoreCase = true) }

    /**
     * Names for AI Chat and scanner subject selectors.
     * Returns top-level subjects grouped conveniently for human selection.
     * Excludes duplicates (e.g. "Science" (Class 6-10) and "Physics"/"Chemistry"/"Biology"
     * (Class 11-12) are both available; the selector shows the higher-level groups first).
     */
    val chatSelectorNames: List<String> = listOf(
        "Mathematics",
        "Physics",
        "Chemistry",
        "Biology",
        "Accountancy",
        "Business Studies",
        "Economics",
        "History",
        "Political Science",
        "Geography",
        "Sociology",
        "Psychology",
        "Computer Science",
        "English",
        "Hindi",
        "General"
    )
}
