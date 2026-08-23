'use client';

import { useState, useEffect, use } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import { QuovexOriginalBook, ChapterSection, ContentValidationReport } from '@/lib/types/content-studio';
import {
  ArrowLeft,
  Sparkles,
  BookOpen,
  CheckCircle2,
  AlertTriangle,
  RefreshCw,
  Layers,
  HelpCircle,
  BrainCircuit,
  Eye,
  ShieldCheck,
} from 'lucide-react';

export default function BookDraftEditorPage({
  params,
}: {
  params: Promise<{ bookId: string }>;
}) {
  const { bookId } = use(params);
  const [book, setBook] = useState<QuovexOriginalBook | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedChapterIdx, setSelectedChapterIdx] = useState(0);
  const [selectedSectionIdx, setSelectedSectionIdx] = useState(0);
  const [activeTab, setActiveTab] = useState<'CONTENT' | 'FLASHCARDS' | 'QUIZ' | 'VALIDATION'>('CONTENT');
  const [regenerating, setRegenerating] = useState(false);

  const fetchBook = async () => {
    try {
      const res = await fetch(`/api/content-studio/books/${bookId}`);
      const data = await res.json();
      if (data.success) {
        setBook(data.book);
      }
    } catch (err) {
      console.error('Failed to load book draft:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBook();
  }, [bookId]);

  const handleRegenerateSection = async () => {
    if (!book) return;
    const currentChapter = book.chapters[selectedChapterIdx];
    if (!currentChapter) return;
    const currentSection = currentChapter.sections[selectedSectionIdx];
    if (!currentSection) return;

    setRegenerating(true);
    try {
      const res = await fetch(`/api/content-studio/books/${bookId}/regenerate-section`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          chapterNumber: currentChapter.chapterNumber,
          sectionNumber: currentSection.sectionNumber,
          topic: currentSection.title,
          subject: book.subject,
        }),
      });

      const data = await res.json();
      if (data.success) {
        await fetchBook();
      }
    } catch (err) {
      console.error('Failed to regenerate section:', err);
    } finally {
      setRegenerating(false);
    }
  };

  if (loading) {
    return <div className="p-12 text-xs text-muted-foreground">Loading draft book...</div>;
  }

  if (!book) {
    return <div className="p-12 text-xs text-destructive">Book draft not found.</div>;
  }

  const currentChapter = book.chapters?.[selectedChapterIdx];
  const currentSection = currentChapter?.sections?.[selectedSectionIdx];
  const valReport = book.validationReport;

  return (
    <div className="flex-1 flex flex-col min-h-0 bg-background">
      <Header
        title={book.title}
        description={`Version v${book.version} • ${book.subject} • ${book.curriculum} — ${book.gradeClass}`}
        action={
          <div className="flex items-center gap-2">
            <Link
              href="/content-studio/drafts"
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-border text-xs text-muted-foreground hover:text-white transition-colors"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>All Drafts</span>
            </Link>

            <Link
              href="/content-studio/review"
              className="flex items-center gap-1.5 px-4 py-1.5 rounded-lg bg-primary text-primary-foreground font-semibold text-xs hover:bg-primary/90 transition-colors"
            >
              <CheckCircle2 className="w-3.5 h-3.5" />
              <span>Review & Sign-off</span>
            </Link>
          </div>
        }
      />

      <div className="flex-1 flex min-h-0 overflow-hidden">
        {/* Left Navigation Tree: Chapters & Sections */}
        <div className="w-72 border-r border-border bg-[#0C120F] flex flex-col shrink-0">
          <div className="p-4 border-b border-border">
            <div className="text-[10px] uppercase font-semibold text-muted-foreground/80 mb-1">
              Table of Contents
            </div>
            <div className="text-xs font-semibold text-white">
              {book.chapters?.length || 0} Chapters • {book.difficulty}
            </div>
          </div>

          <div className="overflow-y-auto flex-1 p-3 space-y-3">
            {book.chapters?.map((ch, chIdx) => (
              <div key={ch.chapterNumber} className="space-y-1">
                <div
                  onClick={() => {
                    setSelectedChapterIdx(chIdx);
                    setSelectedSectionIdx(0);
                  }}
                  className={`p-2 rounded-lg text-xs font-semibold cursor-pointer transition-colors flex items-center justify-between ${
                    selectedChapterIdx === chIdx
                      ? 'bg-primary/15 text-primary border border-primary/20'
                      : 'text-white hover:bg-[#151D19]'
                  }`}
                >
                  <span className="truncate">
                    {ch.chapterNumber}. {ch.title}
                  </span>
                  <span className="text-[10px] text-muted-foreground shrink-0 ml-1">
                    ({ch.sections?.length || 0})
                  </span>
                </div>

                {selectedChapterIdx === chIdx && (
                  <div className="pl-3 space-y-1 border-l border-primary/30 ml-3 mt-1">
                    {ch.sections?.map((sec, sIdx) => (
                      <div
                        key={sec.id}
                        onClick={() => setSelectedSectionIdx(sIdx)}
                        className={`px-2.5 py-1.5 rounded text-[11px] cursor-pointer transition-colors truncate ${
                          selectedSectionIdx === sIdx
                            ? 'bg-primary/20 text-primary font-medium'
                            : 'text-muted-foreground hover:text-white hover:bg-[#151D19]'
                        }`}
                      >
                        {sec.sectionNumber} {sec.title}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div>

          {/* Validation Score Pill */}
          {valReport && (
            <div className="p-3 border-t border-border bg-[#121815]">
              <div className="flex items-center justify-between text-xs">
                <span className="text-muted-foreground font-medium">5-Tier Validation:</span>
                <span className="text-primary font-bold">{valReport.overallScore}/100</span>
              </div>
            </div>
          )}
        </div>

        {/* Main Content Workspace */}
        <div className="flex-1 flex flex-col min-w-0 overflow-y-auto p-8 space-y-6">
          {/* Section Toolbar & Tabs */}
          <div className="flex items-center justify-between pb-4 border-b border-border">
            <div className="flex items-center gap-2">
              <button
                onClick={() => setActiveTab('CONTENT')}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  activeTab === 'CONTENT'
                    ? 'bg-primary text-primary-foreground font-semibold'
                    : 'text-muted-foreground hover:text-white'
                }`}
              >
                Section Content
              </button>
              <button
                onClick={() => setActiveTab('FLASHCARDS')}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  activeTab === 'FLASHCARDS'
                    ? 'bg-primary text-primary-foreground font-semibold'
                    : 'text-muted-foreground hover:text-white'
                }`}
              >
                Flashcards ({currentChapter?.flashcards?.length || 0})
              </button>
              <button
                onClick={() => setActiveTab('QUIZ')}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  activeTab === 'QUIZ'
                    ? 'bg-primary text-primary-foreground font-semibold'
                    : 'text-muted-foreground hover:text-white'
                }`}
              >
                Quiz ({currentChapter?.quizQuestions?.length || 0})
              </button>
              <button
                onClick={() => setActiveTab('VALIDATION')}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                  activeTab === 'VALIDATION'
                    ? 'bg-primary text-primary-foreground font-semibold'
                    : 'text-muted-foreground hover:text-white'
                }`}
              >
                Validation Report
              </button>
            </div>

            {activeTab === 'CONTENT' && currentSection && (
              <button
                onClick={handleRegenerateSection}
                disabled={regenerating}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-primary/30 bg-primary/10 hover:bg-primary/20 text-primary text-xs font-medium transition-colors disabled:opacity-50"
              >
                <RefreshCw className={`w-3.5 h-3.5 ${regenerating ? 'animate-spin' : ''}`} />
                <span>{regenerating ? 'Regenerating Section...' : 'Surgically Regenerate'}</span>
              </button>
            )}
          </div>

          {/* TAB 1: SECTION CONTENT */}
          {activeTab === 'CONTENT' && currentSection && (
            <div className="space-y-6 max-w-4xl">
              <div>
                <div className="text-[11px] text-primary font-semibold">
                  Section {currentSection.sectionNumber}
                </div>
                <h2 className="text-xl font-bold text-white mt-0.5">{currentSection.title}</h2>
              </div>

              {/* Conceptual Prose */}
              <div className="p-6 rounded-xl bg-card border border-border space-y-3">
                <h3 className="text-xs font-semibold text-primary uppercase tracking-wider">
                  Conceptual Exposition
                </h3>
                <p className="text-xs text-foreground/90 leading-relaxed whitespace-pre-line">
                  {currentSection.conceptualExplanation}
                </p>
              </div>

              {/* Visual Analogy */}
              {currentSection.visualAnalogy && (
                <div className="p-5 rounded-xl bg-[#0D1411] border border-primary/30 space-y-2">
                  <div className="flex items-center gap-2 text-primary text-xs font-semibold">
                    <BrainCircuit className="w-4 h-4" />
                    <span>Visual Physical Mental Model</span>
                  </div>
                  <p className="text-xs text-muted-foreground leading-relaxed italic">
                    "{currentSection.visualAnalogy}"
                  </p>
                </div>
              )}

              {/* Worked Examples */}
              {currentSection.workedExamples?.length > 0 && (
                <div className="space-y-4">
                  <h3 className="text-xs font-semibold text-white uppercase tracking-wider">
                    Step-by-Step Worked Numericals ({currentSection.workedExamples.length})
                  </h3>
                  {currentSection.workedExamples.map((ex, idx) => (
                    <div key={ex.id || idx} className="p-5 rounded-xl bg-card border border-border space-y-3">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold text-white">Problem {idx + 1}</span>
                        <span className="px-2 py-0.5 rounded bg-border text-[10px] font-semibold text-primary">
                          {ex.difficulty}
                        </span>
                      </div>
                      <p className="text-xs text-foreground font-medium">{ex.problemStatement}</p>

                      <div className="p-4 rounded-lg bg-[#0C120F] border border-border/80 space-y-2">
                        <div className="text-[11px] font-semibold text-muted-foreground">Solution Steps:</div>
                        {ex.stepByStepSolution?.map((step) => (
                          <div key={step.stepNumber} className="text-xs text-muted-foreground flex items-start gap-2">
                            <span className="text-primary font-bold">{step.stepNumber}.</span>
                            <div>
                              <span>{step.explanation}</span>
                              {step.mathFormula && (
                                <div className="mt-1 px-2.5 py-1 rounded bg-[#151D19] border border-primary/20 text-primary font-mono text-xs">
                                  {step.mathFormula}
                                </div>
                              )}
                            </div>
                          </div>
                        ))}
                      </div>

                      <div className="text-[11px] text-muted-foreground">
                        <span className="font-semibold text-foreground">Key Takeaway:</span> {ex.keyTakeaway}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Real World Applications */}
              {currentSection.realWorldExamples?.length > 0 && (
                <div className="space-y-4">
                  <h3 className="text-xs font-semibold text-white uppercase tracking-wider">
                    Real-World Engineering & Scientific Applications
                  </h3>
                  {currentSection.realWorldExamples.map((rw, idx) => (
                    <div key={rw.id || idx} className="p-5 rounded-xl bg-card border border-border space-y-2">
                      <div className="flex items-center gap-2">
                        <span className="px-1.5 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20 text-[10px] font-semibold">
                          {rw.domain}
                        </span>
                        <span className="text-xs font-semibold text-white">{rw.title}</span>
                      </div>
                      <p className="text-xs text-muted-foreground leading-relaxed">{rw.narrative}</p>
                      <div className="text-[11px] text-primary">
                        <span className="font-semibold">Governing Principle:</span> {rw.physicsOrConceptPrinciple}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Common Mistakes & Student Traps */}
              {currentSection.commonMistakes?.length > 0 && (
                <div className="space-y-4">
                  <h3 className="text-xs font-semibold text-white uppercase tracking-wider">
                    Common Misconceptions & Exam Traps
                  </h3>
                  {currentSection.commonMistakes.map((cm, idx) => (
                    <div key={cm.id || idx} className="p-5 rounded-xl bg-destructive/5 border border-destructive/20 space-y-2">
                      <div className="flex items-center gap-2 text-destructive text-xs font-semibold">
                        <AlertTriangle className="w-4 h-4" />
                        <span>Trap {idx + 1}: {cm.misconception}</span>
                      </div>
                      <div className="text-xs text-muted-foreground">
                        <span className="font-semibold text-foreground">Why Students Get Confused:</span> {cm.whyStudentsMakeIt}
                      </div>
                      <div className="text-xs text-emerald-400">
                        <span className="font-semibold">Correct Understanding:</span> {cm.correctUnderstanding}
                      </div>
                      <div className="text-[11px] text-muted-foreground/80 italic">
                        Quick Check: {cm.quickCheck}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 2: FLASHCARDS */}
          {activeTab === 'FLASHCARDS' && (
            <div className="space-y-4 max-w-3xl">
              <h3 className="text-xs font-semibold text-white uppercase tracking-wider">
                Integrated SM-2 Active Recall Flashcards ({currentChapter?.flashcards?.length || 0})
              </h3>
              {currentChapter?.flashcards?.map((fc, idx) => (
                <div key={fc.id || idx} className="p-5 rounded-xl bg-card border border-border space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="px-2 py-0.5 rounded bg-border text-[10px] font-semibold text-primary">
                      {fc.conceptTag}
                    </span>
                    <span className="text-[10px] text-muted-foreground">
                      Difficulty Level {fc.difficultyRating}/5
                    </span>
                  </div>
                  <div>
                    <div className="text-[11px] font-semibold text-muted-foreground mb-1">Front (Prompt):</div>
                    <p className="text-xs font-medium text-white">{fc.frontPrompt}</p>
                  </div>
                  <div className="pt-2 border-t border-border/60">
                    <div className="text-[11px] font-semibold text-muted-foreground mb-1">Back (Answer):</div>
                    <p className="text-xs text-primary">{fc.backAnswer}</p>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* TAB 3: QUIZ */}
          {activeTab === 'QUIZ' && (
            <div className="space-y-4 max-w-3xl">
              <h3 className="text-xs font-semibold text-white uppercase tracking-wider">
                Integrated Concept Quiz Questions ({currentChapter?.quizQuestions?.length || 0})
              </h3>
              {currentChapter?.quizQuestions?.map((q, idx) => (
                <div key={q.id || idx} className="p-5 rounded-xl bg-card border border-border space-y-4">
                  <div className="text-xs font-semibold text-white">
                    Q{idx + 1}. {q.question}
                  </div>

                  <div className="space-y-2">
                    {q.options?.map((opt, oIdx) => (
                      <div
                        key={oIdx}
                        className={`p-3 rounded-lg text-xs border ${
                          oIdx === q.correctIndex
                            ? 'bg-primary/10 border-primary text-primary font-semibold'
                            : 'bg-[#0C120F] border-border text-muted-foreground'
                        }`}
                      >
                        {String.fromCharCode(65 + oIdx)}. {opt}
                      </div>
                    ))}
                  </div>

                  <div className="p-4 rounded-lg bg-[#0C120F] border border-border text-xs space-y-1">
                    <div className="font-semibold text-primary">Pedagogical Explanation:</div>
                    <p className="text-muted-foreground leading-relaxed">{q.pedagogicalExplanation}</p>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* TAB 4: 5-TIER VALIDATION REPORT */}
          {activeTab === 'VALIDATION' && valReport && (
            <div className="space-y-6 max-w-4xl">
              <div className="p-6 rounded-xl bg-card border border-border flex items-center justify-between">
                <div>
                  <div className="text-[10px] uppercase font-semibold text-primary mb-1">
                    Automated Verification Report
                  </div>
                  <h3 className="text-base font-bold text-white">5-Tier Quality Inspection</h3>
                  <p className="text-xs text-muted-foreground mt-0.5">
                    Evaluated at {new Date(valReport.evaluatedAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="text-right">
                  <div className="text-3xl font-black text-primary">{valReport.overallScore} / 100</div>
                  <div className="text-xs text-emerald-400 font-semibold mt-1">
                    {valReport.overallPassed ? '✓ PASSED ALL TIERS' : 'REVISIONS RECOMMENDED'}
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="p-4 rounded-xl bg-card border border-border space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-white">Tier 1: Fact Validation</span>
                    <span className="font-bold text-primary">{valReport.factValidation.score}/100</span>
                  </div>
                  <p className="text-[11px] text-muted-foreground">
                    Verifies claims, laws, and definitions against the Evidence Pack.
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-card border border-border space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-white">Tier 2: Math & Formula Validation</span>
                    <span className="font-bold text-primary">{valReport.mathValidation.score}/100</span>
                  </div>
                  <p className="text-[11px] text-muted-foreground">
                    Verifies algebraic balance, exponents, units, and formatting.
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-card border border-border space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-white">Tier 3: Curriculum Validation</span>
                    <span className="font-bold text-primary">{valReport.curriculumValidation.score}/100</span>
                  </div>
                  <p className="text-[11px] text-muted-foreground">
                    Verifies coverage of all stated learning objectives and syllabus bounds.
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-card border border-border space-y-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-white">Tier 4: Pedagogy Validation</span>
                    <span className="font-bold text-primary">{valReport.pedagogyValidation.score}/100</span>
                  </div>
                  <p className="text-[11px] text-muted-foreground">
                    Verifies progressive difficulty curve (Simple → Intermediate → Advanced).
                  </p>
                </div>

                <div className="p-4 rounded-xl bg-card border border-border space-y-2 md:col-span-2">
                  <div className="flex items-center justify-between text-xs">
                    <span className="font-semibold text-white">Tier 5: Consistency Validation</span>
                    <span className="font-bold text-primary">{valReport.consistencyValidation.score}/100</span>
                  </div>
                  <p className="text-[11px] text-muted-foreground">
                    Verifies uniform variable notation and terminology across all chapters.
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
