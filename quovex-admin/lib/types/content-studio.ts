/**
 * Quovex Content Studio & Originals Type Definitions
 * Phase 8 Architecture — Strict Typing & Separation of Concerns
 */

export type ContentType = 'OFFICIAL_RESOURCE' | 'QUOVEX_ORIGINAL' | 'USER_MATERIAL';

export type ApprovalStatus =
  | 'REQUESTED'
  | 'GENERATING'
  | 'DRAFT'
  | 'READY_FOR_REVIEW'
  | 'REVISION_REQUESTED'
  | 'APPROVED'
  | 'PUBLISHED'
  | 'UNPUBLISHED'
  | 'ARCHIVED';

export type JobStatus = 'REQUESTED' | 'GENERATING' | 'READY_FOR_REVIEW' | 'FAILED' | 'FAILED_AI_UNAVAILABLE' | 'CANCELLED';

export type GenerationStage =
  | 'DEMAND_ANALYSIS'
  | 'RESEARCH'
  | 'EVIDENCE_PACK'
  | 'DEBATE'
  | 'SYNTHESIS'
  | 'OUTLINE'
  | 'WRITING'
  | 'EXAMPLES'
  | 'FLASHCARDS'
  | 'QUIZ'
  | 'FACT_VALIDATION'
  | 'MATH_VALIDATION'
  | 'CURRICULUM_VALIDATION'
  | 'PEDAGOGY_VALIDATION'
  | 'CONSISTENCY_VALIDATION'
  | 'FAILED_AI_UNAVAILABLE'
  | 'READY_FOR_REVIEW';

export interface SignalWeights {
  questionWeight: number; // default: 1.5
  mistakeWeight: number; // default: 2.0
  lowAccuracyWeight: number; // default: 2.5
  flashcardFailureWeight: number; // default: 2.0
  imageDoubtWeight: number; // default: 1.8
  affectedStudentsWeight: number; // default: 3.0
}

export interface TopicDemandSignal {
  id: string;
  topicId: string;
  topicName: string;
  subjectId: string;
  subjectName: string;
  subjectCategory: string; // 'SCIENCE' | 'COMMERCE' | 'HUMANITIES' | 'MATHEMATICS' | etc.
  countryRegion: string; // 'IN' | 'US' | 'UK' | 'GLOBAL'
  curriculum: string; // 'CBSE' | 'ICSE' | 'JEE Main/Advanced' | 'NEET' | 'AP' | 'IB' | etc.
  gradeClass: string; // 'Class 11' | 'Class 12' | etc.
  exam?: string;
  language: string;

  // Raw counts (Strictly anonymized, zero PII, zero chat transcripts)
  questionCount: number;
  quizMistakeCount: number;
  lowAccuracyCount: number;
  flashcardFailureCount: number;
  imageDoubtCount: number;
  affectedStudents: number;
  averageAccuracy: number; // 0.0 - 1.0
  failureRate: number; // 0.0 - 1.0

  // Normalized scores (Bounded 0 - 100)
  normalizedQuestions: number;
  normalizedMistakes: number;
  normalizedLowAccuracy: number;
  normalizedFlashcardFailures: number;
  normalizedImageDoubts: number;
  normalizedAffectedStudents: number;

  // Final explainable demand score (0 - 100)
  demandScore: number;
  weights: SignalWeights;
  contributingMetrics: {
    questionScore: number;
    mistakeScore: number;
    accuracyPenaltyScore: number;
    flashcardFailureScore: number;
    imageDoubtScore: number;
    studentBreadthScore: number;
  };

  periodStart: number;
  periodEnd: number;
  updatedAt: number;
}

export interface EvidenceItem {
  evidenceId: string;
  claim: string;
  fact: string;
  sourceUrl: string;
  sourceTitle: string;
  publisher: string;
  retrievedAt: number;
  sourceType: 'OFFICIAL_CURRICULUM' | 'GOVERNMENT_STANDARD' | 'ACADEMIC_REFERENCE' | 'VERIFIED_ENCYCLOPEDIA';
  relevance: number; // 0.0 - 1.0
  confidence: number; // 0.0 - 1.0
}

export interface EvidencePack {
  packId: string;
  topicId: string;
  topicName: string;
  subject: string;
  curriculum: string;
  items: EvidenceItem[];
  verifiedDefinitions: Record<string, string>;
  keyFormulas: Array<{ name: string; formula: string; units: string; context: string }>;
  commonMisconceptions: string[];
  historicalAndRealWorldContext: string[];
  createdAt: number;
}

export interface EditorialBlueprint {
  blueprintId: string;
  architectConceptProgression: string[];
  architectAnalogies: string[];
  challengerIdentifiedRisks: string[];
  challengerEdgeCases: string[];
  synthesisFinalObjectives: string[];
  synthesisChapterPlan: Array<{
    chapterNumber: number;
    title: string;
    targetConcepts: string[];
    difficultyCurve: 'Simple' | 'Intermediate' | 'Advanced';
    realWorldScenario: string;
  }>;
  curriculumAlignmentNotes: string;
  createdAt: number;
}

export interface WorkedExample {
  id: string;
  problemStatement: string;
  stepByStepSolution: Array<{ stepNumber: number; explanation: string; mathFormula?: string }>;
  keyTakeaway: string;
  difficulty: 'Simple' | 'Intermediate' | 'Advanced';
}

export interface RealWorldExample {
  id: string;
  domain: 'AEROSPACE' | 'BIOMEDICAL' | 'TECHNOLOGY' | 'ENERGY' | 'SPORTS' | 'ECONOMICS' | 'DAILY_LIFE';
  title: string;
  narrative: string;
  physicsOrConceptPrinciple: string;
}

export interface CommonMistake {
  id: string;
  misconception: string;
  whyStudentsMakeIt: string;
  correctUnderstanding: string;
  quickCheck: string;
}

export interface IntegratedFlashcard {
  id: string;
  frontPrompt: string;
  backAnswer: string;
  conceptTag: string;
  difficultyRating: number; // 1 - 5
}

export interface IntegratedQuizQuestion {
  id: string;
  question: string;
  options: string[];
  correctIndex: number;
  pedagogicalExplanation: string;
  distractorExplanations: string[];
  formulaReference?: string;
}

export interface ChapterSection {
  id: string;
  sectionNumber: string; // e.g. "1.1", "1.2"
  title: string;
  conceptualExplanation: string;
  visualAnalogy: string;
  workedExamples: WorkedExample[];
  realWorldExamples: RealWorldExample[];
  commonMistakes: CommonMistake[];
  summaryPoints: string[];
}

export interface BookChapter {
  chapterNumber: number;
  title: string;
  summary: string;
  learningObjectives: string[];
  sections: ChapterSection[];
  quickRevisionBulletPoints: string[];
  flashcards: IntegratedFlashcard[];
  quizQuestions: IntegratedQuizQuestion[];
}

export interface ValidationTierResult {
  tierName: 'FACT' | 'MATH' | 'CURRICULUM' | 'PEDAGOGY' | 'CONSISTENCY';
  passed: boolean;
  score: number; // 0 - 100
  inspectedItemsCount: number;
  issues: string[];
  remediationSuggestions: string[];
}

export interface ContentValidationReport {
  reportId: string;
  bookId: string;
  generationJobId: string;
  factValidation: ValidationTierResult;
  mathValidation: ValidationTierResult;
  curriculumValidation: ValidationTierResult;
  pedagogyValidation: ValidationTierResult;
  consistencyValidation: ValidationTierResult;
  overallPassed: boolean;
  overallScore: number; // 0 - 100
  evaluatedAt: number;
}

export interface BookVersionRecord {
  version: number;
  generationJobId: string;
  createdAt: number;
  createdBy: string;
  revisionReason?: string;
  reviewNotes?: string;
  approvedBy?: string;
  approvedAt?: number;
}

export interface QuovexOriginalBook {
  id: string;
  contentType: 'QUOVEX_ORIGINAL';
  title: string;
  subtitle?: string;
  description: string;
  subject: string;
  topic: string;
  language: string; // 'en' | 'hi' | 'es' | 'fr'
  countryRegion: string; // 'IN' | 'US' | 'UK' | 'GLOBAL'
  curriculum: string; // 'CBSE' | 'ICSE' | 'JEE Main/Advanced' | 'NEET' | 'AP' | 'IB' | etc.
  gradeClass: string; // 'Class 11' | 'Class 12' | etc.
  exam?: string;
  difficulty: 'Simple' | 'Intermediate' | 'Advanced';
  targetReadingTimeMinutes: number;
  chapterCount: number;

  generationJobId: string;
  version: number;
  approvalStatus: ApprovalStatus;
  isStaging: boolean; // True for staging test catalog; False for public live production

  coverImageUrl?: string;
  introduction: string;
  learningObjectives: string[];
  prerequisites: string[];
  chapters: BookChapter[];

  validationReport?: ContentValidationReport;
  versionHistory: BookVersionRecord[];

  createdBy: string; // Admin UID or system
  createdAt: number;
  updatedAt: number;

  approvedBy?: string; // Mandatory for PUBLISHED
  approvedAt?: number; // Mandatory for PUBLISHED
  publishedAt?: number;
  reviewNotes?: string;
}

export interface ContentGenerationJob {
  jobId: string;
  bookId: string;
  requestId: string;
  status: JobStatus;
  stage: GenerationStage;
  progressPercentage: number; // 0 - 100
  currentChapter?: number;
  currentSection?: string;
  stageLogs: Array<{
    stage: GenerationStage;
    timestamp: number;
    message: string;
    details?: string;
  }>;

  // Persisted intermediate stage artifacts for recovery & zero re-computation
  demandSignalRef?: string;
  evidencePackId?: string;
  editorialBlueprintId?: string;
  validationReportId?: string;

  createdBy: string;
  createdAt: number;
  startedAt?: number;
  updatedAt: number;
  completedAt?: number;

  error?: string;
  retryCount: number;
}

export interface BookRequestInput {
  title: string;
  subject: string;
  topic: string;
  countryRegion: string;
  curriculum: string;
  gradeClass: string;
  exam?: string;
  language: string;
  difficulty: 'Simple' | 'Intermediate' | 'Advanced';
  targetReadingTimeMinutes: number;
  chapterCount: number;
  learningObjectives: string[];
  prerequisites: string[];
  examRelevance?: string;
  desiredTeachingStyle?: string;
  demandSignalId?: string;
  specialInstructions?: string;
  isStaging?: boolean;
}

export interface PostPublicationAnalytics {
  bookId: string;
  title: string;
  subject: string;
  topic: string;
  viewsCount: number;
  startsCount: number;
  chapterCompletions: Record<number, number>; // chapterNumber -> completedCount
  averageReadingTimeMinutes: number;
  quizAttemptsCount: number;
  averageQuizScore: number;
  flashcardsReviewedCount: number;
  flashcardRetentionRate: number;
  aiTutorFollowupQuestionsCount: number;
  studentHelpfulnessRating: number; // 0.0 - 5.0
  preBookTopicAccuracy: number; // baseline from demand signals
  postBookTopicAccuracy: number; // after studying this book
  updatedAt: number;
}
