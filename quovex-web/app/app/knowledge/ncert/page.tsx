'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  BookOpen,
  Sparkles,
  ArrowLeft,
  ExternalLink,
  Bot,
  CheckCircle2,
  ChevronRight,
  ShieldCheck,
  AlertCircle,
  FileText,
  Bookmark,
} from 'lucide-react';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import {
  getAvailableClasses,
  getSubjectsForClass,
  getChaptersForClassAndSubject,
  getChapterById,
  NcertChapter,
} from '@/lib/ncertCatalog';

export default function NcertExplorerPage() {
  const router = useRouter();
  const availableClasses = getAvailableClasses();

  // Class / Subject State
  const [selectedClass, setSelectedClass] = useState<string>(availableClasses[0] || 'Class 12');
  const availableSubjects = getSubjectsForClass(selectedClass);
  const [selectedSubject, setSelectedSubject] = useState<string>(availableSubjects[0] || 'Physics');
  
  // Chapter State
  const [selectedChapterId, setSelectedChapterId] = useState<string | null>(null);
  const [activeChapter, setActiveChapter] = useState<NcertChapter | null>(null);
  const [summary, setSummary] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [metadataError, setMetadataError] = useState<string | null>(null);

  // When class changes, reset subject, chapter, and summary
  const handleClassChange = (newClass: string) => {
    setSelectedClass(newClass);
    const newSubjects = getSubjectsForClass(newClass);
    const firstSubject = newSubjects[0] || '';
    setSelectedSubject(firstSubject);
    setSelectedChapterId(null);
    setActiveChapter(null);
    setSummary(null);
    setMetadataError(null);
  };

  // When subject changes, reset chapter and summary
  const handleSubjectChange = (newSubject: string) => {
    setSelectedSubject(newSubject);
    setSelectedChapterId(null);
    setActiveChapter(null);
    setSummary(null);
    setMetadataError(null);
  };

  // Fetch chapters strictly for the currently selected class and subject
  const currentChapters = getChaptersForClassAndSubject(selectedClass, selectedSubject);

  const handleSelectChapter = (chapterId: string) => {
    const chapter = getChapterById(chapterId);
    if (!chapter) {
      setMetadataError('Chapter resource not found in catalog.');
      return;
    }

    // Strict Assertion Check (Section 6 of requirements)
    if (
      chapter.gradeClass !== selectedClass ||
      chapter.subject !== selectedSubject ||
      chapter.contentType !== 'OFFICIAL_RESOURCE'
    ) {
      setMetadataError('Content metadata mismatch — please retry.');
      setActiveChapter(null);
      setSummary(null);
      return;
    }

    setMetadataError(null);
    setSelectedChapterId(chapterId);
    setActiveChapter(chapter);
    setLoading(true);
    setSummary(null);

    // Formulate AI Chapter Breakdown grounded in actual chapter key concepts and formulas
    (async () => {
      try {
        const res = await fetch('/api/ai/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            message: `Provide an elite high-yield NCERT exam syllabus breakdown for ${chapter.gradeClass} ${chapter.subject} Chapter ${chapter.chapterNumber}: "${chapter.chapterTitle}". Include syllabus competencies, governing formulas in LaTeX, common student traps, and high-yield exam tips.`,
            subject: chapter.subject,
            topic: chapter.chapterTitle,
            targetExam: 'CBSE / Competitive',
            materialSummary: `Chapter: ${chapter.gradeClass} • ${chapter.subject} • ${chapter.chapterTitle}\nKey Concepts: ${chapter.keyConcepts.join(', ')}`,
          }),
        });

        const json = await res.json();
        if (json.success && json.response) {
          setSummary(json.response);
        } else {
          // Robust conceptual overview based on canonical catalog metadata
          const conceptsList = chapter.keyConcepts.map((k) => `* **${k}:** Core curriculum mastery requirement.`).join('\n');
          const formulasSection = chapter.sampleFormulas && chapter.sampleFormulas.length > 0
            ? `\n\n2. **Governing Mathematical Relations & Invariants:**\n${chapter.sampleFormulas.map(f => `$$${f}$$`).join('\n')}`
            : '';

          setSummary(`### ${chapter.gradeClass} • ${chapter.subject}: ${chapter.chapterTitle}\n\n` +
            `**Official Curriculum Context:** NCERT Chapter ${chapter.chapterNumber}\n\n` +
            `1. **Core Syllabus Competencies:**\n${conceptsList}${formulasSection}\n\n` +
            `3. **High-Yield Examination Traps & Notes ⚠️:**\n` +
            `* Always ensure standard SI units are maintained across all derivation steps.\n` +
            `* Pay close attention to boundary conditions and textbook definitions during theoretical assessment.`);
        }
      } catch (_) {
        const conceptsList = chapter.keyConcepts.map((k) => `* **${k}:** Core curriculum mastery requirement.`).join('\n');
        setSummary(`### ${chapter.gradeClass} • ${chapter.subject}: ${chapter.chapterTitle}\n\n1. **Core Syllabus Competencies:**\n${conceptsList}`);
      } finally {
        setLoading(false);
      }
    })();
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6 pb-16">
      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Link
            href="/app/knowledge"
            className="inline-flex items-center gap-1.5 text-xs font-semibold text-text-secondary hover:text-text-primary mb-2 transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" /> Back to Knowledge Hub
          </Link>
          <div className="flex items-center gap-2.5">
            <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
              <BookOpen className="w-7 h-7 text-primary" />
              Rationalised NCERT Dynamic Catalog
            </h1>
            <QuovexBadge variant="emerald" size="sm">OFFICIAL RESOURCE</QuovexBadge>
          </div>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Data-driven CBSE & state board curriculum with verified chapter taxonomy.
          </p>
        </div>
      </div>

      {/* ── Data-Driven Class & Subject Selectors ─────────────────────────── */}
      <div className="space-y-3">
        {/* Class Selector Bar */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
          <span className="text-xs font-bold text-text-secondary uppercase tracking-wider shrink-0 mr-1">
            Grade:
          </span>
          {availableClasses.map((c) => (
            <button
              key={c}
              onClick={() => handleClassChange(c)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all ${
                selectedClass === c
                  ? 'bg-primary text-primary-foreground shadow-sm'
                  : 'bg-surface-variant text-text-secondary hover:text-text-primary border border-border'
              }`}
            >
              {c}
            </button>
          ))}
        </div>

        {/* Dynamic Subject Selector Bar (Strictly Derived from Selected Class) */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
          <span className="text-xs font-bold text-text-secondary uppercase tracking-wider shrink-0 mr-1">
            Subject:
          </span>
          {availableSubjects.map((s) => (
            <button
              key={s}
              onClick={() => handleSubjectChange(s)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all ${
                selectedSubject === s
                  ? 'bg-primary-container text-primary border border-primary/50 shadow-xs'
                  : 'bg-surface text-text-secondary hover:text-text-primary border border-border'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* ── Metadata Error Banner (if any) ─────────────────────────────────── */}
      {metadataError && (
        <div className="p-4 rounded-xl bg-error-container text-error border border-error/30 text-xs font-bold flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{metadataError}</span>
        </div>
      )}

      {/* ── Main Catalog & Reader Split View ───────────────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Chapters List */}
        <div className="lg:col-span-1 space-y-2.5">
          <div className="flex items-center justify-between">
            <h2 className="text-xs sm:text-sm font-bold text-text-primary">
              {selectedClass} • {selectedSubject} ({currentChapters.length} Chapters)
            </h2>
          </div>

          {currentChapters.length > 0 ? (
            <div className="space-y-2">
              {currentChapters.map((ch) => (
                <div
                  key={ch.id}
                  onClick={() => handleSelectChapter(ch.id)}
                  className={`p-3.5 rounded-xl border transition-all cursor-pointer flex items-center justify-between text-xs ${
                    selectedChapterId === ch.id
                      ? 'bg-primary-container border-primary text-primary shadow-xs font-bold'
                      : 'bg-surface border-border text-text-primary hover:bg-surface-variant hover:border-primary/30'
                  }`}
                >
                  <div className="flex items-center gap-2.5 pr-2">
                    <span className="w-6 h-6 rounded-md bg-surface-variant text-text-secondary text-[10px] flex items-center justify-center font-mono font-bold shrink-0">
                      {ch.chapterNumber}
                    </span>
                    <span className="leading-snug">{ch.chapterTitle}</span>
                  </div>
                  <ChevronRight className="w-4 h-4 text-text-secondary shrink-0" />
                </div>
              ))}
            </div>
          ) : (
            <div className="p-6 rounded-xl bg-surface border border-border text-center text-xs text-text-secondary">
              No official NCERT chapters registered for {selectedClass} • {selectedSubject}.
            </div>
          )}
        </div>

        {/* Right Column: AI Chapter Breakdown Reader */}
        <div className="lg:col-span-2">
          <QuovexCard className="p-5 sm:p-6 space-y-4 min-h-[440px] flex flex-col justify-between shadow-sm">
            {/* Reader Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-border pb-4">
              <div>
                <h3 className="font-bold text-text-primary text-sm sm:text-base">
                  {activeChapter ? activeChapter.chapterTitle : 'Select a Chapter to Begin'}
                </h3>
                <span className="text-xs text-text-secondary mt-0.5 block">
                  {selectedClass} • {selectedSubject}
                  {activeChapter ? ` • ${activeChapter.bookTitle}` : ''}
                </span>
              </div>

              {activeChapter && (
                <div className="flex items-center gap-2 self-start sm:self-auto">
                  <a
                    href={activeChapter.officialSourceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-surface-variant border border-border text-[11px] font-bold text-text-secondary hover:text-text-primary transition-colors"
                  >
                    <ExternalLink className="w-3.5 h-3.5" />
                    <span>Official NCERT Portal</span>
                  </a>

                  <QuovexButton
                    variant="primary"
                    size="sm"
                    onClick={() => {
                      router.push(
                        `/app/ai?context=ncert&chapterId=${activeChapter.id}&subject=${encodeURIComponent(
                          activeChapter.subject
                        )}&title=${encodeURIComponent(activeChapter.chapterTitle)}&class=${encodeURIComponent(
                          activeChapter.gradeClass
                        )}`
                      );
                    }}
                    leftIcon={<Bot className="w-3.5 h-3.5" />}
                  >
                    Study with Quovex AI
                  </QuovexButton>
                </div>
              )}
            </div>

            {/* Reader Body */}
            <div className="flex-1 bg-surface-variant/40 rounded-xl p-4 sm:p-5 border border-border text-xs sm:text-sm text-text-primary leading-relaxed overflow-y-auto max-h-[500px]">
              {loading ? (
                <div className="h-full flex flex-col items-center justify-center text-center text-text-secondary py-16">
                  <Bot className="w-8 h-8 text-primary animate-spin mb-3" />
                  <span className="font-semibold text-text-primary">
                    Formulating canonical NCERT study breakdown...
                  </span>
                  <span className="text-[11px] text-text-secondary mt-1">
                    Grounded in official curriculum guidelines and examination patterns
                  </span>
                </div>
              ) : summary ? (
                <LatexRenderer content={summary} />
              ) : (
                <div className="h-full flex flex-col items-center justify-center text-center text-text-secondary py-16 space-y-2">
                  <BookOpen className="w-8 h-8 text-primary/40 mx-auto" />
                  <p className="font-bold text-text-primary">Official NCERT Textbook Reader</p>
                  <p className="text-xs max-w-sm">
                    Select any chapter on the left to inspect its official syllabus competencies, formulas, and examination traps.
                  </p>
                </div>
              )}
            </div>

            {/* Invariant Footer */}
            {activeChapter && (
              <div className="flex flex-wrap items-center justify-between text-[11px] text-text-tertiary pt-2 border-t border-border gap-2">
                <span className="flex items-center gap-1 text-primary font-semibold">
                  <ShieldCheck className="w-3.5 h-3.5" /> Verified Official NCERT Curriculum
                </span>
                <span>Publisher: NCERT • Edition: Rationalised 2026</span>
              </div>
            )}
          </QuovexCard>
        </div>
      </div>
    </div>
  );
}
