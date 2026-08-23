/**
 * Quovex Content Studio — Asynchronous Pipeline & Worker Orchestrator
 *
 * Implements the 16-stage asynchronous authoring lifecycle:
 * DEMAND_ANALYSIS -> RESEARCH -> EVIDENCE_PACK -> DEBATE -> SYNTHESIS ->
 * OUTLINE -> WRITING -> EXAMPLES -> FLASHCARDS -> QUIZ ->
 * FACT_VALIDATION -> MATH_VALIDATION -> CURRICULUM_VALIDATION ->
 * PEDAGOGY_VALIDATION -> CONSISTENCY_VALIDATION -> READY_FOR_REVIEW
 *
 * Persists intermediate artifacts at every stage for robust recovery.
 */

import {
  ContentGenerationJob,
  BookRequestInput,
  QuovexOriginalBook,
  EvidencePack,
  EditorialBlueprint,
  ContentValidationReport,
  GenerationStage,
  ApprovalStatus,
  ChapterSection
} from '../types/content-studio';
import { ResearchEngine } from './research-engine';
import { DebateEngine } from './debate-engine';
import { WriterEngine } from './writer-engine';
import { ValidatorEngine } from './validator-engine';

// In-Memory & File/Firestore Storage Layer for Phase 8 Content Studio
class ContentStudioStore {
  public jobs: Map<string, ContentGenerationJob> = new Map();
  public books: Map<string, QuovexOriginalBook> = new Map();
  public evidencePacks: Map<string, EvidencePack> = new Map();
  public blueprints: Map<string, EditorialBlueprint> = new Map();
  public validationReports: Map<string, ContentValidationReport> = new Map();
}

export const studioStore = new ContentStudioStore();

export class ContentPipeline {
  private researchEngine = new ResearchEngine();
  private debateEngine = new DebateEngine();
  private writerEngine = new WriterEngine();
  private validatorEngine = new ValidatorEngine();

  /**
   * Initializes and starts a new asynchronous generation job.
   */
  public async createAndStartJob(request: BookRequestInput, adminId: string = 'admin_editor'): Promise<{ jobId: string; bookId: string }> {
    const jobId = `job_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
    const bookId = `book_${Date.now()}_${request.topic.toLowerCase().replace(/[^a-z0-9]+/g, '_')}`;
    const requestId = `req_${Date.now()}`;
    const now = Date.now();

    const job: ContentGenerationJob = {
      jobId,
      bookId,
      requestId,
      status: 'GENERATING',
      stage: 'DEMAND_ANALYSIS',
      progressPercentage: 5,
      stageLogs: [
        {
          stage: 'DEMAND_ANALYSIS',
          timestamp: now,
          message: `Job initiated by admin (${adminId}) for topic: "${request.topic}"`
        }
      ],
      createdBy: adminId,
      createdAt: now,
      startedAt: now,
      updatedAt: now,
      retryCount: 0
    };

    studioStore.jobs.set(jobId, job);

    // Run the pipeline worker asynchronously (does NOT block the caller)
    this.runPipelineWorker(jobId, bookId, request).catch((err) => {
      console.error(`Pipeline worker failed for job ${jobId}:`, err);
      const currentJob = studioStore.jobs.get(jobId);
      if (currentJob) {
        const isAiUnavailable = err?.message?.includes('AI_UNAVAILABLE') || err?.message?.includes('AI');
        currentJob.status = isAiUnavailable ? 'FAILED_AI_UNAVAILABLE' : 'FAILED';
        currentJob.stage = isAiUnavailable ? 'FAILED_AI_UNAVAILABLE' : currentJob.stage;
        currentJob.error = err.message || 'Unknown pipeline failure';
        currentJob.updatedAt = Date.now();
        currentJob.stageLogs.push({
          stage: isAiUnavailable ? 'FAILED_AI_UNAVAILABLE' : currentJob.stage,
          timestamp: Date.now(),
          message: `CRITICAL PIPELINE HALT: ${err.message || 'Unknown error'}. Blocked from reaching review or published state.`
        });
      }
    });

    return { jobId, bookId };
  }

  /**
   * Background worker executing all 16 pipeline stages sequentially with recovery checkpoints.
   */
  public async runPipelineWorker(jobId: string, bookId: string, request: BookRequestInput): Promise<void> {
    const updateStage = (stage: GenerationStage, progress: number, logMsg: string) => {
      const j = studioStore.jobs.get(jobId);
      if (!j) return;
      j.stage = stage;
      j.progressPercentage = progress;
      j.updatedAt = Date.now();
      j.stageLogs.push({
        stage,
        timestamp: Date.now(),
        message: logMsg
      });
    };

    // Stage 1: Demand Analysis
    updateStage('DEMAND_ANALYSIS', 10, 'Analyzing curriculum difficulty vectors and topic friction metrics.');

    // Stage 2 & 3: Controlled Research & Evidence Pack Assembly
    updateStage('RESEARCH', 20, 'Gathering curriculum-aligned educational standards and verified formulas.');
    const evidencePack = await this.researchEngine.generateEvidencePack(request);
    studioStore.evidencePacks.set(evidencePack.packId, evidencePack);

    updateStage('EVIDENCE_PACK', 30, `Evidence Pack (${evidencePack.packId}) assembled with ${evidencePack.items.length} verified citations.`);
    const job = studioStore.jobs.get(jobId);
    if (job) job.evidencePackId = evidencePack.packId;

    // Stage 4 & 5: Multi-Agent Debate & Editorial Blueprint Synthesis
    updateStage('DEBATE', 40, 'Multi-agent debate initiated: Architect (pedagogy) vs Challenger (rigor & misconceptions).');
    const blueprint = await this.debateEngine.conductDebate(request, evidencePack);
    studioStore.blueprints.set(blueprint.blueprintId, blueprint);

    updateStage('SYNTHESIS', 50, `Editorial Blueprint synthesized with ${blueprint.synthesisChapterPlan.length} chapters.`);
    if (job) job.editorialBlueprintId = blueprint.blueprintId;

    // Stage 6 & 7: Chapter Outlining & Original Writing
    updateStage('OUTLINE', 55, 'Structuring chapters, section hierarchies, and difficulty progression curve.');
    updateStage('WRITING', 70, 'Original educational writer drafting pedagogical chapters with LaTeX math formatting.');
    const draftBook = await this.writerEngine.writeDraftBook(request, evidencePack, blueprint, jobId, bookId);

    // Stage 8, 9, 10: Worked Examples, Flashcards & Practice Quizzes
    updateStage('EXAMPLES', 75, 'Authoring step-by-step worked numerical examples and real-world engineering case studies.');
    updateStage('FLASHCARDS', 80, 'Generating integrated Spaced Repetition (SM-2) flashcard decks for all chapters.');
    updateStage('QUIZ', 85, 'Creating concept-reinforcing practice quizzes with pedagogical distractor explanations.');

    // Stage 11, 12, 13, 14, 15: 5-Tier Automated Validation Protocol
    updateStage('FACT_VALIDATION', 88, 'Tier 1 Validation: Verifying claims, laws, and definitions against Evidence Pack.');
    updateStage('MATH_VALIDATION', 90, 'Tier 2 Validation: Verifying mathematical formulas, unit consistency, and exponent formatting.');
    updateStage('CURRICULUM_VALIDATION', 93, 'Tier 3 Validation: Verifying syllabus coverage and grade-level learning objectives.');
    updateStage('PEDAGOGY_VALIDATION', 95, 'Tier 4 Validation: Verifying difficulty curve progression (Simple -> Intermediate -> Advanced).');
    updateStage('CONSISTENCY_VALIDATION', 98, 'Tier 5 Validation: Verifying variable notation and terminology uniformity across chapters.');

    const validationReport = await this.validatorEngine.validateBook(draftBook, evidencePack, blueprint);
    studioStore.validationReports.set(validationReport.reportId, validationReport);
    draftBook.validationReport = validationReport;

    if (job) job.validationReportId = validationReport.reportId;

    // Stage 16: Ready for Human Review
    draftBook.approvalStatus = 'READY_FOR_REVIEW';
    draftBook.updatedAt = Date.now();
    studioStore.books.set(bookId, draftBook);

    updateStage('READY_FOR_REVIEW', 100, `Generation complete! Overall validation score: ${validationReport.overallScore}/100. Staged for human editorial review.`);

    if (job) {
      job.status = 'READY_FOR_REVIEW';
      job.stage = 'READY_FOR_REVIEW';
      job.progressPercentage = 100;
      job.completedAt = Date.now();
    }
  }

  /**
   * Regenerates a single specific section within a chapter (surgical edit).
   */
  public async regenerateSection(
    bookId: string,
    chapterNumber: number,
    sectionNumber: string,
    topic: string,
    subject: string
  ): Promise<ChapterSection | null> {
    const book = studioStore.books.get(bookId);
    if (!book) return null;

    const chapter = book.chapters.find((c) => c.chapterNumber === chapterNumber);
    if (!chapter) return null;

    const updatedSection = await this.writerEngine.regenerateSectionWithAi(chapterNumber, sectionNumber, topic, subject);

    const sectionIndex = chapter.sections.findIndex((s) => s.sectionNumber === sectionNumber);
    if (sectionIndex >= 0) {
      chapter.sections[sectionIndex] = updatedSection;
    } else {
      chapter.sections.push(updatedSection);
    }

    book.version += 1;
    book.updatedAt = Date.now();
    book.versionHistory.push({
      version: book.version,
      generationJobId: book.generationJobId,
      createdAt: Date.now(),
      createdBy: 'admin_editor',
      revisionReason: `Surgical regeneration of Chapter ${chapterNumber}, Section ${sectionNumber}`
    });

    return updatedSection;
  }

  /**
   * Approves a book draft (Mandatory Human Sign-off).
   */
  public approveBook(bookId: string, adminId: string, reviewNotes?: string): QuovexOriginalBook | null {
    const book = studioStore.books.get(bookId);
    if (!book) return null;

    book.approvalStatus = 'APPROVED';
    book.approvedBy = adminId;
    book.approvedAt = Date.now();
    book.reviewNotes = reviewNotes || 'Human editorial review completed and approved.';
    book.updatedAt = Date.now();

    return book;
  }

  /**
   * Requests revision on a book draft.
   */
  public requestRevision(bookId: string, adminId: string, revisionNotes: string): QuovexOriginalBook | null {
    const book = studioStore.books.get(bookId);
    if (!book) return null;

    book.approvalStatus = 'REVISION_REQUESTED';
    book.reviewNotes = revisionNotes;
    book.updatedAt = Date.now();

    return book;
  }

  /**
   * Publishes an APPROVED book (Enforces Server-Side Approval Invariant).
   */
  public publishBook(bookId: string, isStaging: boolean = false): { success: boolean; error?: string; book?: QuovexOriginalBook } {
    const book = studioStore.books.get(bookId);
    if (!book) {
      return { success: false, error: 'Book not found.' };
    }

    // SERVER-SIDE APPROVAL INVARIANT: Rejects publishing if not explicitly approved
    if (book.approvalStatus !== 'APPROVED' || !book.approvedBy || !book.approvedAt) {
      return {
        success: false,
        error: `Server-Side Security Invariant Violation: Cannot publish book with status "${book.approvalStatus}". Book MUST be explicitly APPROVED by an authenticated administrator with valid approvedBy and approvedAt timestamps.`
      };
    }

    book.approvalStatus = 'PUBLISHED';
    book.isStaging = isStaging;
    book.publishedAt = Date.now();
    book.updatedAt = Date.now();

    return { success: true, book };
  }

  /**
   * Unpublishes a previously published book.
   */
  public unpublishBook(bookId: string): { success: boolean; error?: string; book?: QuovexOriginalBook } {
    const book = studioStore.books.get(bookId);
    if (!book) {
      return { success: false, error: 'Book not found.' };
    }

    book.approvalStatus = 'UNPUBLISHED';
    book.updatedAt = Date.now();

    return { success: true, book };
  }

  /**
   * Archives a book.
   */
  public archiveBook(bookId: string): { success: boolean; error?: string; book?: QuovexOriginalBook } {
    const book = studioStore.books.get(bookId);
    if (!book) {
      return { success: false, error: 'Book not found.' };
    }

    book.approvalStatus = 'ARCHIVED';
    book.updatedAt = Date.now();

    return { success: true, book };
  }
}

export const contentPipeline = new ContentPipeline();
