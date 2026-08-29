'use client';

import React, { useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
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
  Send,
  HelpCircle,
  FileText,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import { saveUserNote, saveFlashcardDeck, saveFlashcard } from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import { ASSETS } from '@/lib/assets';

interface StructuredSolution {
  coreConcept: string;
  problemSummary: string;
  steps: string[];
  formulas: string[];
  pitfalls: string[];
  finalAnswer: string;
  similarPractice: string;
}

export default function PhotoDoubtPage() {
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [isSolving, setIsSolving] = useState(false);
  const [solution, setSolution] = useState<StructuredSolution | null>(null);
  const [followUpQuestion, setFollowUpQuestion] = useState('');
  const [savedStatus, setSavedStatus] = useState<string | null>(null);

  const currentUser = getCurrentUser();

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        setSelectedImage(event.target?.result as string);
        setSolution(null);
        setSavedStatus(null);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSolveProblem = async () => {
    if (!selectedImage || isSolving) return;

    setIsSolving(true);
    // Simulate multi-tier vision analysis
    setTimeout(() => {
      setSolution({
        coreConcept: 'Newton\'s Second Law & Rotational Dynamics (Torque Equilibrium)',
        problemSummary: 'A non-uniform rigid rod of mass $M$ and length $L$ pivots about a knife-edge support with an attached point mass $m$ at distance $x$.',
        steps: [
          '**Step 1: Free Body Diagram & Force Equilibrium:**\nFor the system in static equilibrium, the sum of all external vertical forces must vanish:\n$$\\sum F_y = 0 \\implies N - Mg - mg = 0 \\implies N = (M + m)g$$',
          '**Step 2: Torque Balance about the Pivot:**\nTaking counterclockwise moments about pivot $O$:\n$$\\sum \\tau_O = 0 \\implies \\tau_{\\text{rod}} + \\tau_{\\text{mass}} = 0$$\n$$-Mg\\left(\\frac{L}{2} - d\\right) + mg x = 0$$',
          '**Step 3: Solving for Position Invariant $x$:**\nRearranging the torque equation yields:\n$$x = \\frac{M}{m}\\left(\\frac{L}{2} - d\\right)$$'
        ],
        formulas: [
          '\\sum \\vec{\\tau} = I\\vec{\\alpha} = 0',
          '\\tau = r F \\sin(\\theta)',
          'N = (M + m)g'
        ],
        pitfalls: [
          'Do NOT take moments about an arbitrary point without accounting for the normal contact force $N$.',
          'Ensure the center of gravity of the rod is evaluated at $L/2$ only if the mass distribution is uniform.'
        ],
        finalAnswer: '$$x = \\frac{M}{m}\\left(\\frac{L}{2} - d\\right)$$',
        similarPractice: 'A uniform ladder of length $L$ leans against a frictionless wall. If the static friction coefficient at the floor is $\\mu_s$, determine the minimum angle $\\theta$ before slipping occurs.'
      });
      setIsSolving(false);
    }, 1500);
  };

  const handleSaveToMaterials = async () => {
    if (!solution || !currentUser) return;
    const noteId = `note_${Date.now()}`;
    await saveUserNote(currentUser.uid, {
      id: noteId,
      title: `Photo Doubt: ${solution.coreConcept}`,
      subject: 'Physics',
      content: `${solution.problemSummary}\n\n${solution.steps.join('\n\n')}\n\nFinal Answer: ${solution.finalAnswer}`,
      keyPoints: solution.pitfalls,
      formulas: solution.formulas,
      createdAt: Date.now(),
      updatedAt: Date.now(),
    });
    setSavedStatus('Saved to My Materials! 📝');
  };

  const handleCreateFlashcards = async () => {
    if (!solution || !currentUser) return;
    const deckId = `deck_${Date.now()}`;
    await saveFlashcardDeck(currentUser.uid, {
      id: deckId,
      title: `Doubt: ${solution.coreConcept}`,
      subject: 'Physics',
      cardCount: 2,
      masteryPercentage: 0,
      lastStudiedAt: Date.now(),
    });

    await saveFlashcard(currentUser.uid, deckId, {
      id: `card_${Date.now()}_1`,
      deckId,
      frontContent: `📌 Core Formula: ${solution.coreConcept}`,
      backContent: `$$${solution.formulas[0]}$$\n\n${solution.pitfalls[0]}`,
      repetitions: 0,
      intervalDays: 1,
      easeFactor: 2.5,
      nextReviewDate: Date.now(),
      concept: solution.coreConcept,
    });

    setSavedStatus('Created Flashcard Deck! 📇');
  };

  return (
    <div className="max-w-4xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div>
        <h1 className="text-display font-black text-text-primary flex items-center gap-4">
          <BrainCircuit className="w-10 h-10 text-primary" />
          Photo Doubt Solver (6-Tier Proof)
        </h1>
        <p className="text-section text-text-secondary mt-2">
          Upload any handwritten or printed physics, chemistry, or math problem for instant step-by-step reasoning.
        </p>
      </div>

      {/* ── 1. Upload & Viewfinder Card ────────────────────────────────────── */}
      <QuovexCard className="p-8 space-y-6">
        {!selectedImage ? (
          <label className="border-2 border-dashed border-border hover:border-primary/60 rounded-3xl p-8 sm:p-16 flex flex-col items-center justify-center text-center cursor-pointer transition-all bg-surface-variant/40 hover:bg-surface-variant/70">
            <input type="file" accept="image/*" onChange={handleImageSelect} className="hidden" />
            <div className="w-20 h-20 relative mb-6">
              <Image
                src={ASSETS.icons3d.scannerHologram}
                alt="Scanner Viewfinder"
                fill
                className="object-contain"
                unoptimized
              />
            </div>
            <h3 className="text-title font-bold text-text-primary">Click to Upload Problem Image or Screenshot</h3>
            <p className="text-body text-text-secondary mt-2 max-w-md">
              Supports JPEG, PNG, WEBP up to 5MB. Clear lighting produces higher mathematical accuracy.
            </p>
          </label>
        ) : (
          <div className="space-y-6">
            <div className="relative rounded-2xl overflow-hidden border border-border bg-surface-variant max-h-96 flex items-center justify-center p-2">
              <img src={selectedImage} alt="Uploaded Problem" className="max-h-96 object-contain rounded-xl" />
              <button
                onClick={() => setSelectedImage(null)}
                className="absolute top-4 right-4 p-2.5 rounded-full bg-black/60 text-white text-caption font-bold hover:bg-black/80 transition-colors backdrop-blur-md"
              >
                ✕ Remove
              </button>
            </div>

            <div className="flex gap-4">
              <QuovexButton
                variant="primary"
                size="lg"
                className="flex-1 py-4 text-title shadow-glow-lg"
                onClick={handleSolveProblem}
                isLoading={isSolving}
                leftIcon={<Sparkles className="w-5 h-5" />}
              >
                {isSolving ? 'Analyzing Problem & Formulating Derivation...' : 'Solve Problem with Quovex Vision AI'}
              </QuovexButton>
            </div>
          </div>
        )}
      </QuovexCard>

      {/* ── 2. 6-Tier Structured Solution View ─────────────────────────────── */}
      {solution && (
        <div className="space-y-8 animate-in fade-in zoom-in-95">
          {/* Action Bar */}
          <div className="flex flex-wrap items-center justify-between gap-4 p-5 rounded-2xl bg-surface border border-border">
            <div className="flex items-center gap-3">
              <CheckCircle2 className="w-6 h-6 text-primary" />
              <span className="text-body font-bold text-text-primary">Verified Step-by-Step Proof</span>
            </div>
            <div className="flex items-center gap-3">
              <QuovexButton variant="secondary" size="md" onClick={handleSaveToMaterials} leftIcon={<FileText className="w-4 h-4" />}>
                Save to Notes
              </QuovexButton>
              <QuovexButton variant="secondary" size="md" onClick={handleCreateFlashcards} leftIcon={<BookmarkPlus className="w-4 h-4" />}>
                Create Flashcard
              </QuovexButton>
            </div>
          </div>

          {savedStatus && (
            <div className="p-4 rounded-xl bg-success-container text-success border border-success/30 text-body font-bold shadow-sm">
              {savedStatus}
            </div>
          )}

          {/* Tier 1: Governing Concept */}
          <QuovexCard className="space-y-3 border-primary/40 shadow-glow-sm">
            <div className="flex items-center gap-2 text-primary font-bold text-label uppercase tracking-wider">
              <Lightbulb className="w-4 h-4" />
              <span>Tier 1: Core Governing Concept</span>
            </div>
            <LatexRenderer content={`**${solution.coreConcept}**`} className="text-headline font-bold text-text-primary" />
            <LatexRenderer content={solution.problemSummary} className="text-body text-text-secondary mt-2" />
          </QuovexCard>

          {/* Tier 2: Step-by-Step Derivation */}
          <QuovexCard className="space-y-5">
            <h3 className="text-label font-bold text-primary uppercase tracking-wider">
              Tier 2: Step-by-Step Mathematical Derivation
            </h3>
            <div className="space-y-4">
              {solution.steps.map((step, idx) => (
                <div key={idx} className="p-5 rounded-xl bg-surface-variant border border-border">
                  <LatexRenderer content={step} />
                </div>
              ))}
            </div>
          </QuovexCard>

          {/* Tier 3: Formula Sheet */}
          <QuovexCard className="space-y-4">
            <h3 className="text-label font-bold text-primary uppercase tracking-wider">
              Tier 3: Key Formulas & Invariants
            </h3>
            <div className="flex flex-wrap gap-3">
              {solution.formulas.map((f, idx) => (
                <div key={idx} className="px-4 py-2 rounded-xl bg-surface-variant border border-border text-body font-medium">
                  <LatexRenderer content={`$${f}$`} />
                </div>
              ))}
            </div>
          </QuovexCard>

          {/* Tier 4: Common Pitfalls */}
          <QuovexCard className="space-y-4 border-warning/40 bg-warning-container/10">
            <div className="flex items-center gap-2 text-warning font-bold text-label uppercase tracking-wider">
              <AlertTriangle className="w-4 h-4" />
              <span>Tier 4: High-Yield Exam Pitfalls & Traps</span>
            </div>
            <ul className="space-y-3">
              {solution.pitfalls.map((p, idx) => (
                <li key={idx} className="text-body text-text-secondary flex items-start gap-3">
                  <span className="text-warning font-bold shrink-0 mt-0.5">•</span>
                  <span>{p}</span>
                </li>
              ))}
            </ul>
          </QuovexCard>

          {/* Tier 5: Final Verified Answer */}
          <QuovexCard variant="elevated" className="border-primary bg-primary-container/30 text-center space-y-3 p-8 shadow-glow-lg">
            <span className="text-label font-black text-primary uppercase tracking-widest">
              TIER 5: FINAL VERIFIED ANSWER
            </span>
            <LatexRenderer content={solution.finalAnswer} className="text-display font-black text-text-primary" />
          </QuovexCard>

          {/* Tier 6: Similar Practice */}
          <QuovexCard className="space-y-3">
            <h3 className="text-label font-bold text-text-secondary uppercase tracking-wider">
              Tier 6: Recommended Similar Practice Problem
            </h3>
            <LatexRenderer content={solution.similarPractice} className="text-body text-text-primary leading-relaxed" />
          </QuovexCard>
        </div>
      )}
    </div>
  );
}
