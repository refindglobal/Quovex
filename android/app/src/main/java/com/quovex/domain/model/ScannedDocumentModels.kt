package com.quovex.domain.model

/**
 * Domain models for the Document Scanner feature.
 *
 * DOMAIN PURITY RULES:
 * - NO android.graphics.Bitmap in this file
 * - NO android.net.Uri in this file
 * - NO java.io.File in this file
 * - Pages are represented by platform-neutral file path strings
 * - Android-layer converts these strings to Bitmap/Uri for rendering
 *
 * The scanner is image-based ONLY — no OCR dependency.
 * AI vision handles document understanding after scanning.
 * Architecturally separate from ImageDoubt (single image problem solving).
 */

// ── SCANNER STATE MACHINE ─────────────────────────────────────────────────────

/** Phases of the multi-page document scanning workflow */
enum class ScanPhase {
    /** Initial state — ready to launch scanner */
    IDLE,

    /** ML Kit Document Scanner is active (BETA: 16.0.0-beta1) */
    SCANNING,

    /**
     * Pages captured — user reviewing page list.
     * Can: Add page, Retake, Delete, Reorder, Finish Scan.
     */
    REVIEWING_PAGES,

    /**
     * Quovex AI is analyzing document images.
     * 5-page batches sent to ai/document/analyze endpoint.
     */
    ANALYZING,

    /**
     * AI has proposed a chapter/subtopic organization.
     * User can rename, merge, split, delete before confirming.
     */
    CONFIRMING_ORGANIZATION,

    /** User confirmed — notes are being saved to Room */
    SAVING,

    /** Final state — document successfully saved as structured notes */
    SAVED,

    /** Error occurred at any phase */
    ERROR
}

// ── PAGE MODEL ────────────────────────────────────────────────────────────────

/**
 * A single captured page from the document scanner.
 *
 * Uses platform-neutral file path references — NOT Bitmap or android.net.Uri.
 * The presentation layer (ViewModel / Composable) is responsible for
 * loading the image file from [imageFilePath] into a Bitmap for rendering.
 *
 * @param pageIndex Zero-based index of this page in the document
 * @param imageFilePath Absolute path to the full-resolution page image file
 *                      (e.g. /data/user/0/com.quovex/cache/scanner/page_0.jpg)
 * @param thumbnailFilePath Absolute path to the thumbnail image file
 *                          (smaller version for page-strip UI display)
 * @param widthPx Image width in pixels (for aspect ratio display without loading Bitmap)
 * @param heightPx Image height in pixels
 */
data class ScannedPage(
    val pageIndex: Int,
    val imageFilePath: String,
    val thumbnailFilePath: String,
    val widthPx: Int = 0,
    val heightPx: Int = 0
)

// ── AI ORGANIZATION OUTPUT ────────────────────────────────────────────────────

/**
 * AI-proposed note section within a subtopic.
 *
 * @param sectionTitle Section heading (e.g. "Definition", "Key Formula", "Important Event")
 * @param content Main note content (plain text — subject-agnostic)
 * @param keyPoints Bullet-point key facts for quick review
 * @param sourcePageRange Pages this section was derived from
 */
data class ScannedNoteSection(
    val sectionTitle: String = "",
    val content: String = "",
    val keyPoints: List<String> = emptyList(),
    val sourcePageRange: IntRange = 0..0
)

/**
 * AI-proposed subtopic within a chapter.
 *
 * Subject-agnostic design:
 * - Science: may include formulas, worked examples
 * - Commerce: may include journal entries, accounting tables
 * - Humanities: may include dates, definitions, causes, effects
 * - Languages: may include vocabulary, grammar rules, literary analysis
 *
 * @param title Subtopic name (derived from document content — NOT hardcoded)
 * @param noteSections Structured note sections within this subtopic
 * @param pageRange Pages in the original scan that belong to this subtopic
 */
data class ScannedSubtopic(
    val title: String,
    val noteSections: List<ScannedNoteSection> = emptyList(),
    val pageRange: IntRange = 0..0
)

/**
 * AI-proposed chapter within a scanned document.
 *
 * @param title Chapter name (derived from document visual content — NOT hardcoded)
 * @param subtopics Sub-sections within this chapter
 * @param pageRange Pages in the original scan that belong to this chapter
 */
data class ScannedChapter(
    val title: String,
    val subtopics: List<ScannedSubtopic> = emptyList(),
    val pageRange: IntRange = 0..0
)

/**
 * Complete AI-proposed organization for a scanned multi-page document.
 *
 * Returned by [AnalyzeDocumentImagesUseCase] after all page batches are processed.
 * Shown to user for confirmation/editing before saving as structured notes.
 *
 * Expected fields per API contract (ai/document/analyze):
 * - detectedSubject: inferred subject (e.g. "Physics", "Accountancy", "History")
 * - documentTitle: AI-inferred document title
 * - chapters: proposed chapter breakdown with subtopics
 * - confidence: AI confidence in the organization (0.0–1.0)
 *
 * @param detectedSubject AI-inferred subject — works for ALL streams (not Science-only)
 * @param detectedStream AI-inferred stream (e.g. "Science", "Commerce", "Humanities")
 * @param documentTitle AI-inferred title for the overall document
 * @param chapters Proposed chapter breakdown
 * @param confidence AI confidence score (0.0–1.0)
 */
data class ScannedDocumentOrganization(
    val detectedSubject: String = "",
    val detectedStream: String = "",
    val documentTitle: String = "",
    val chapters: List<ScannedChapter> = emptyList(),
    val confidence: Float = 0.0f
)

// ── EDITABLE CONFIRMATION MODELS ──────────────────────────────────────────────

/**
 * Editable version of a chapter during user confirmation step.
 * User may rename, reorder, merge, split, or delete before saving.
 */
data class EditableScannedChapter(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val subtopics: List<EditableScannedSubtopic> = emptyList(),
    val pageRange: IntRange = 0..0,
    val isDeleted: Boolean = false
)

data class EditableScannedSubtopic(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String = "",
    val keyPoints: List<String> = emptyList(),
    val pageRange: IntRange = 0..0,
    val isDeleted: Boolean = false
)

/** Convert AI output to editable form for user confirmation screen */
fun ScannedDocumentOrganization.toEditable(): List<EditableScannedChapter> =
    chapters.map { chapter ->
        EditableScannedChapter(
            title = chapter.title,
            pageRange = chapter.pageRange,
            subtopics = chapter.subtopics.map { sub ->
                EditableScannedSubtopic(
                    title = sub.title,
                    pageRange = sub.pageRange,
                    content = sub.noteSections.firstOrNull()?.content ?: "",
                    keyPoints = sub.noteSections.flatMap { it.keyPoints }
                )
            }
        )
    }
