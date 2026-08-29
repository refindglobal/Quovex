'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  BrainCircuit,
  Upload,
  Camera,
  Sparkles,
  Lightbulb,
  AlertTriangle,
  CheckCircle2,
  BookmarkPlus,
  ArrowRight,
  FileText,
  HelpCircle,
  RotateCcw,
  Bot,
  Layers,
  ShieldCheck,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import {
  saveUserNote,
  saveFlashcardDeck,
  saveFlashcard,
  saveAiConversation,
  saveAiMessage,
  subscribeToUserProfile,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import { ASSETS } from '@/lib/assets';

interface StructuredSolution {
  isSolvable: boolean;
  unsolvableReason?: string;
  multipleProblemsDetected?: boolean;
  problemCount?: number;
  coreConcept: string;
  problemSummary: string;
  givenInfo?: string[];
  approach?: string;
  steps: string[];
  formulas: string[];
  pitfalls: string[];
  verification?: string;
  finalAnswer: string;
  similarPractice: string;
}

const SUBJECT_OPTIONS = ['Physics', 'Chemistry', 'Mathematics', 'Biology', 'General Science'];

export default function PhotoDoubtPage() {
  const router = useRouter();
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [selectedSubject, setSelectedSubject] = useState('Physics');
  const [questionPrompt, setQuestionPrompt] = useState('');
  const [isSolving, setIsSolving] = useState(false);
  const [solution, setSolution] = useState<StructuredSolution | null>(null);
  const [savedStatus, setSavedStatus] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [profile, setProfile] = useState<any>(null);

  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) return;
    const unsub = subscribeToUserProfile(currentUser.uid, (p) => setProfile(p));
    return () => unsub();
  }, [currentUser]);

  // Current active step: 1 = Upload, 2 = Vision Analysis, 3 = Solution
  const activeStep = solution ? 3 : isSolving ? 2 : selectedImage ? 2 : 1;

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        setSelectedImage(event.target?.result as string);
        setSolution(null);
        setSavedStatus(null);
        setErrorMessage(null);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSolveProblem = async () => {
    if (!selectedImage || isSolving) return;

    setIsSolving(true);
    setErrorMessage(null);
    setSavedStatus(null);

    try {
      const res = await fetch('/api/ai/doubt/image', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          imageBase64: selectedImage,
          subject: selectedSubject,
          targetExam: profile?.targetExam || 'Competitive',
          questionText: questionPrompt,
        }),
      });

      const json = await res.json();
      if (!res.ok || !json.success || !json.data) {
        throw new Error(json.error || 'Failed to solve image problem.');
      }

      setSolution(json.data);
    } catch (err: any) {
      console.error('Vision Solve Error:', err);
      setErrorMessage(err.message || 'Vision AI is temporarily unavailable. Please try again.');
    } finally {
      setIsSolving(false);
    }
  };

  const handleContinueInAiTutor = async () => {
    if (!solution || !currentUser) return;

    const convId = `doubt_conv_${Date.now()}`;
    const userPromptText = questionPrompt.trim()
      ? `Image Doubt: ${solution.problemSummary}\n\nNote: ${questionPrompt}`
      : `Image Doubt: ${solution.problemSummary}`;

    const formattedProof = `### ${solution.coreConcept}\n\n` +
      (solution.approach ? `**Approach:** ${solution.approach}\n\n` : '') +
      `**Step-by-Step Derivation:**\n${solution.steps.join('\n\n')}\n\n` +
      `**Final Answer:**\n${solution.finalAnswer}\n\n` +
      (solution.verification ? `**Verification:** ${solution.verification}\n\n` : '');

    // Save conversation & initial messages to Firestore
    await saveAiConversation(currentUser.uid, {
      id: convId,
      title: `Doubt: ${solution.coreConcept.slice(0, 45)}`,
      subject: selectedSubject,
      sourceType: 'IMAGE_DOUBT',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      lastMessagePreview: solution.finalAnswer.slice(0, 80),
    });

    await saveAiMessage(currentUser.uid, convId, {
      id: `msg_user_${Date.now()}`,
      role: 'user',
      content: userPromptText,
      createdAt: Date.now() - 1000,
    });

    await saveAiMessage(currentUser.uid, convId, {
      id: `msg_ai_${Date.now()}`,
      role: 'assistant',
      content: formattedProof,
      createdAt: Date.now(),
    });

    // Navigate into AI Tutor conversation
    router.push(`/app/ai?chatId=${convId}`);
  };

  const handleSaveToMaterials = async () => {
    if (!solution || !currentUser) return;
    const noteId = `note_${Date.now()}`;
    await saveUserNote(currentUser.uid, {
      id: noteId,
      title: `Photo Doubt: ${solution.coreConcept}`,
      subject: selectedSubject,
      content: `${solution.problemSummary}\n\n${solution.steps.join('\n\n')}\n\nFinal Answer: ${solution.finalAnswer}`,
      keyPoints: solution.pitfalls,
      formulas: solution.formulas,
      createdAt: Date.now(),
      updatedAt: Date.now(),
    });
    setSavedStatus('Saved to Study Notes! 📝');
  };

  const handleCreateFlashcards = async () => {
    if (!solution || !currentUser) return;
    const deckId = `deck_${Date.now()}`;
    await saveFlashcardDeck(currentUser.uid, {
      id: deckId,
      title: `Doubt: ${solution.coreConcept}`,
      subject: selectedSubject,
      cardCount: 1,
      masteryPercentage: 0,
      lastStudiedAt: Date.now(),
    });

    await saveFlashcard(currentUser.uid, deckId, {
      id: `card_${Date.now()}_1`,
      deckId,
      frontContent: `📌 Core Formula: ${solution.coreConcept}`,
      backContent: `${solution.formulas[0] ? `$$${solution.formulas[0]}$$\n\n` : ''}${solution.finalAnswer}\n\n⚠️ ${solution.pitfalls[0] || 'Check SI units.'}`,
      repetitions: 0,
      intervalDays: 1,
      easeFactor: 2.5,
      nextReviewDate: Date.now(),
      concept: solution.coreConcept,
    });

    setSavedStatus('Created Spaced Repetition Flashcard! 📇');
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6 pb-20">
      {/* ── 1. Compact Header ────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
              <BrainCircuit className="w-7 h-7 text-primary" />
              Photo Doubt Solver
            </h1>
            <QuovexBadge variant="gold" size="sm">Live Vision AI</QuovexBadge>
          </div>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Real step-by-step mathematical proofs generated live from textbook or handwritten problems.
          </p>
        </div>

        {solution && (
          <button
            onClick={() => {
              setSelectedImage(null);
              setSolution(null);
              setSavedStatus(null);
              setErrorMessage(null);
            }}
            className="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-surface border border-border text-xs font-bold text-text-secondary hover:text-text-primary transition-colors self-start sm:self-auto"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span>New Doubt</span>
          </button>
        )}
      </div>

      {/* ── 2. 3-Step State Stepper ───────────────────────────────────────── */}
      <div className="grid grid-cols-3 gap-2 p-1.5 rounded-2xl bg-surface border border-border">
        <div
          className={`flex items-center justify-center gap-2 py-2 px-3 rounded-xl text-xs font-bold transition-all ${
            activeStep === 1
              ? 'bg-primary text-primary-foreground shadow-sm'
              : 'text-text-secondary bg-surface-variant/40'
          }`}
        >
          <span className="w-4 h-4 rounded-full bg-black/20 flex items-center justify-center text-[10px]">1</span>
          <span className="hidden sm:inline">Upload Image</span>
          <span className="sm:hidden">Upload</span>
        </div>

        <div
          className={`flex items-center justify-center gap-2 py-2 px-3 rounded-xl text-xs font-bold transition-all ${
            activeStep === 2
              ? 'bg-primary text-primary-foreground shadow-sm'
              : 'text-text-secondary bg-surface-variant/40'
          }`}
        >
          <span className="w-4 h-4 rounded-full bg-black/20 flex items-center justify-center text-[10px]">2</span>
          <span className="hidden sm:inline">Vision Analysis</span>
          <span className="sm:hidden">Analyze</span>
        </div>

        <div
          className={`flex items-center justify-center gap-2 py-2 px-3 rounded-xl text-xs font-bold transition-all ${
            activeStep === 3
              ? 'bg-primary text-primary-foreground shadow-sm'
              : 'text-text-secondary bg-surface-variant/40'
          }`}
        >
          <span className="w-4 h-4 rounded-full bg-black/20 flex items-center justify-center text-[10px]">3</span>
          <span className="hidden sm:inline">Structured Proof</span>
          <span className="sm:hidden">Proof</span>
        </div>
      </div>

      {/* ── 3. Subject Selector & Upload Card ─────────────────────────────── */}
      <QuovexCard className="p-4 sm:p-6 space-y-4 shadow-sm">
        <div className="flex items-center justify-between gap-3">
          <label className="text-xs font-bold text-text-secondary uppercase tracking-wider">
            Subject Stream:
          </label>
          <select
            value={selectedSubject}
            onChange={(e) => setSelectedSubject(e.target.value)}
            disabled={isSolving}
            className="bg-surface-variant border border-border rounded-xl px-3 py-1.5 text-xs text-text-primary font-bold focus:outline-none focus:border-primary cursor-pointer"
          >
            {SUBJECT_OPTIONS.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>

        {!selectedImage ? (
          <label className="border-2 border-dashed border-border hover:border-primary/60 rounded-2xl p-6 sm:p-8 flex flex-col items-center justify-center text-center cursor-pointer transition-all bg-surface-variant/30 hover:bg-surface-variant/60 group">
            <input type="file" accept="image/*" onChange={handleImageSelect} className="hidden" />
            <div className="w-12 h-12 rounded-2xl bg-primary/10 border border-primary/30 flex items-center justify-center mb-3 group-hover:scale-110 transition-transform">
              <Upload className="w-6 h-6 text-primary" />
            </div>
            <h3 className="text-sm sm:text-base font-bold text-text-primary">Click or Drop Problem Image Here</h3>
            <p className="text-xs text-text-secondary mt-1 max-w-sm">
              JPEG, PNG, WEBP up to 5MB • Handwritten equations, diagrams, or printed questions
            </p>
          </label>
        ) : (
          <div className="space-y-4">
            <div className="relative rounded-xl overflow-hidden border border-border bg-surface-variant max-h-72 flex items-center justify-center p-2">
              <img src={selectedImage} alt="Uploaded Problem" className="max-h-64 object-contain rounded-lg" />
              <button
                onClick={() => {
                  setSelectedImage(null);
                  setSolution(null);
                }}
                disabled={isSolving}
                className="absolute top-3 right-3 px-3 py-1 rounded-full bg-black/70 text-white text-xs font-bold hover:bg-black transition-colors backdrop-blur-md"
              >
                ✕ Change Image
              </button>
            </div>

            {!solution && (
              <div className="space-y-3">
                <input
                  type="text"
                  placeholder="Optional: Add any specific question or clarification (e.g. 'Solve part b')..."
                  value={questionPrompt}
                  onChange={(e) => setQuestionPrompt(e.target.value)}
                  disabled={isSolving}
                  className="w-full bg-surface-variant border border-border rounded-xl px-3.5 py-2 text-xs sm:text-sm text-text-primary placeholder:text-text-tertiary focus:outline-none focus:border-primary transition-all"
                />

                <QuovexButton
                  variant="primary"
                  size="lg"
                  className="w-full py-3 text-sm sm:text-base font-bold shadow-glow"
                  onClick={handleSolveProblem}
                  isLoading={isSolving}
                  leftIcon={<Sparkles className="w-4 h-4" />}
                >
                  {isSolving ? 'Solving Live with Vision AI...' : 'Solve with Quovex Vision AI'}
                </QuovexButton>
              </div>
            )}
          </div>
        )}
      </QuovexCard>

      {/* Error Message */}
      {errorMessage && (
        <div className="p-4 rounded-xl bg-error-container text-error border border-error/30 text-xs font-bold flex items-center gap-2">
          <AlertTriangle className="w-4 h-4 shrink-0" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* ── 4. Unsolvable / Ambiguous Image Warning ─────────────────────────── */}
      {solution && !solution.isSolvable && (
        <div className="p-5 rounded-2xl bg-warning-container/20 border border-warning/40 space-y-3">
          <div className="flex items-center gap-2 text-warning font-bold text-sm">
            <AlertTriangle className="w-5 h-5" />
            <span>Image Could Not Be Solved Reliably</span>
          </div>
          <p className="text-xs sm:text-sm text-text-primary leading-relaxed">
            {solution.unsolvableReason || 'The problem text or equations in this image could not be recognized with certainty.'}
          </p>
          <p className="text-xs text-text-secondary">
            💡 <strong>Tip:</strong> Please capture a focused, well-lit photo of the specific question or type the equation directly.
          </p>
        </div>
      )}

      {/* ── 5. Structured 10-Tier Live Solution ─────────────────────────────── */}
      {solution && solution.isSolvable && (
        <div className="space-y-4 animate-in fade-in zoom-in-95">
          {/* Action Toolbar */}
          <div className="flex flex-wrap items-center justify-between gap-3 p-3.5 sm:p-4 rounded-2xl bg-surface border border-border shadow-sm">
            <div className="flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 text-primary shrink-0" />
              <span className="text-xs sm:text-sm font-bold text-text-primary">Live Verified Proof</span>
              {solution.multipleProblemsDetected && (
                <QuovexBadge variant="gold" size="sm">Problem 1 of {solution.problemCount || 2}</QuovexBadge>
              )}
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <QuovexButton
                variant="primary"
                size="sm"
                onClick={handleContinueInAiTutor}
                leftIcon={<Bot className="w-3.5 h-3.5" />}
              >
                Continue in AI Tutor
              </QuovexButton>
              <QuovexButton
                variant="secondary"
                size="sm"
                onClick={handleSaveToMaterials}
                leftIcon={<FileText className="w-3.5 h-3.5" />}
              >
                Save Notes
              </QuovexButton>
              <QuovexButton
                variant="secondary"
                size="sm"
                onClick={handleCreateFlashcards}
                leftIcon={<BookmarkPlus className="w-3.5 h-3.5" />}
              >
                Make Card
              </QuovexButton>
            </div>
          </div>

          {savedStatus && (
            <div className="p-3 rounded-xl bg-success-container text-success border border-success/30 text-xs sm:text-sm font-bold shadow-sm">
              {savedStatus}
            </div>
          )}

          {/* Section 1: Governing Concept & Interpretation */}
          <QuovexCard className="p-4 sm:p-5 space-y-2 border-primary/40 shadow-sm">
            <div className="flex items-center gap-1.5 text-primary font-bold text-xs uppercase tracking-wider">
              <Lightbulb className="w-3.5 h-3.5" />
              <span>1. Core Governing Concept</span>
            </div>
            <h3 className="text-sm sm:text-base font-bold text-text-primary">{solution.coreConcept}</h3>
            <LatexRenderer content={solution.problemSummary} className="text-xs sm:text-sm text-text-secondary mt-1" />
          </QuovexCard>

          {/* Section 2: Given Values & Strategy Approach */}
          {((solution.givenInfo && solution.givenInfo.length > 0) || solution.approach) && (
            <QuovexCard className="p-4 sm:p-5 space-y-3">
              <h3 className="text-xs font-bold text-primary uppercase tracking-wider">
                2. Given Information & Strategy
              </h3>
              {solution.givenInfo && solution.givenInfo.length > 0 && (
                <ul className="space-y-1 text-xs sm:text-sm text-text-secondary list-disc pl-4">
                  {solution.givenInfo.map((g, i) => (
                    <li key={i}>{g}</li>
                  ))}
                </ul>
              )}
              {solution.approach && (
                <p className="text-xs sm:text-sm text-text-primary bg-surface-variant p-3 rounded-xl border border-border">
                  💡 <strong>Strategy:</strong> {solution.approach}
                </p>
              )}
            </QuovexCard>
          )}

          {/* Section 3: Step-by-Step Derivation */}
          <QuovexCard className="p-4 sm:p-5 space-y-3">
            <h3 className="text-xs font-bold text-primary uppercase tracking-wider">
              3. Step-by-Step Mathematical Derivation
            </h3>
            <div className="space-y-3">
              {solution.steps.map((step, idx) => (
                <div key={idx} className="p-3.5 sm:p-4 rounded-xl bg-surface-variant border border-border">
                  <LatexRenderer content={step} />
                </div>
              ))}
            </div>
          </QuovexCard>

          {/* Section 4: Key Formulas */}
          {solution.formulas && solution.formulas.length > 0 && (
            <QuovexCard className="p-4 sm:p-5 space-y-3">
              <h3 className="text-xs font-bold text-primary uppercase tracking-wider">
                4. Key Invariant Formulas
              </h3>
              <div className="flex flex-wrap gap-2">
                {solution.formulas.map((f, idx) => (
                  <div key={idx} className="px-3 py-1.5 rounded-xl bg-surface-variant border border-border text-xs sm:text-sm font-medium">
                    <LatexRenderer content={`$${f}$`} />
                  </div>
                ))}
              </div>
            </QuovexCard>
          )}

          {/* Section 5: Common Traps & Pitfalls */}
          {solution.pitfalls && solution.pitfalls.length > 0 && (
            <QuovexCard className="p-4 sm:p-5 space-y-3 border-warning/40 bg-warning-container/10">
              <div className="flex items-center gap-1.5 text-warning font-bold text-xs uppercase tracking-wider">
                <AlertTriangle className="w-3.5 h-3.5" />
                <span>5. Common Traps & Exam Pitfalls</span>
              </div>
              <ul className="space-y-2">
                {solution.pitfalls.map((p, idx) => (
                  <li key={idx} className="text-xs sm:text-sm text-text-secondary flex items-start gap-2.5">
                    <span className="text-warning font-bold shrink-0 mt-0.5">•</span>
                    <span>{p}</span>
                  </li>
                ))}
              </ul>
            </QuovexCard>
          )}

          {/* Section 6: Final Answer */}
          <QuovexCard variant="elevated" className="border-primary bg-primary-container/20 text-center space-y-2 p-5 sm:p-6 shadow-glow-sm">
            <span className="text-[10px] sm:text-xs font-black text-primary uppercase tracking-widest block">
              FINAL VERIFIED ANSWER
            </span>
            <LatexRenderer content={solution.finalAnswer} className="text-lg sm:text-xl font-black text-text-primary" />
            {solution.verification && (
              <p className="text-xs text-text-secondary pt-1 border-t border-primary/20">
                ✓ <strong>Verification:</strong> {solution.verification}
              </p>
            )}
          </QuovexCard>

          {/* Section 7: Similar Practice */}
          {solution.similarPractice && (
            <QuovexCard className="p-4 sm:p-5 space-y-2">
              <h3 className="text-xs font-bold text-text-secondary uppercase tracking-wider">
                Recommended Practice Problem
              </h3>
              <LatexRenderer content={solution.similarPractice} className="text-xs sm:text-sm text-text-primary leading-relaxed" />
            </QuovexCard>
          )}
        </div>
      )}
    </div>
  );
}
