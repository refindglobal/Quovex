'use client';

import React, { useState, useEffect, use } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { ArrowLeft, RotateCw, CheckCircle2, Sparkles, Award, Plus, Trash2 } from 'lucide-react';
import confetti from 'canvas-confetti';
import { calculateSm2 } from '@/lib/sm2';
import { getCurrentUser } from '@/lib/firebase/auth';
import {
  subscribeToDeckCards,
  saveFlashcard,
  deleteFlashcard,
  Flashcard,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import { ASSETS } from '@/lib/assets';

export default function FlashcardPlayerPage({ params }: { params: Promise<{ deckId: string }> }) {
  const resolvedParams = use(params);
  const deckId = resolvedParams.deckId;

  const [cards, setCards] = useState<Flashcard[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [isFinished, setIsFinished] = useState(false);

  // Add Card Modal
  const [isAddingCard, setIsAddingCard] = useState(false);
  const [newFront, setNewFront] = useState('');
  const [newBack, setNewBack] = useState('');

  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser || !deckId) {
      setLoading(false);
      return;
    }

    const unsub = subscribeToDeckCards(currentUser.uid, deckId, (list) => {
      setCards(list);
      setLoading(false);
    });
    return () => unsub();
  }, [currentUser, deckId]);

  const handleGrade = async (quality: number) => {
    if (!cards[currentIndex] || !currentUser) return;

    const currentCard = cards[currentIndex];
    const sm2Result = calculateSm2(
      quality,
      currentCard.repetitions || 0,
      currentCard.intervalDays || 1,
      currentCard.easeFactor || 2.5
    );

    await saveFlashcard(currentUser.uid, deckId, {
      ...currentCard,
      repetitions: sm2Result.repetitions,
      intervalDays: sm2Result.intervalDays,
      easeFactor: sm2Result.easinessFactor,
      nextReviewDate: sm2Result.nextReviewAtMillis,
    });

    if (currentIndex < cards.length - 1) {
      setIsFlipped(false);
      setCurrentIndex((prev) => prev + 1);
    } else {
      setIsFinished(true);
      confetti({ particleCount: 100, spread: 80, origin: { y: 0.6 } });
    }
  };

  const handleCreateCard = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newFront.trim() || !newBack.trim() || !currentUser) return;

    const card: Flashcard = {
      id: `card_${Date.now()}`,
      deckId,
      frontContent: newFront.trim(),
      backContent: newBack.trim(),
      repetitions: 0,
      intervalDays: 1,
      easeFactor: 2.5,
      nextReviewDate: Date.now(),
    };

    await saveFlashcard(currentUser.uid, deckId, card);
    setNewFront('');
    setNewBack('');
    setIsAddingCard(false);
  };

  const card = cards[currentIndex];

  if (loading) {
    return (
      <div className="max-w-2xl mx-auto p-12 text-center text-xs text-text-secondary">
        Loading flashcards...
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6 pb-20">
      {/* Top Controls */}
      <div className="flex items-center justify-between">
        <Link href="/app/flashcards" className="inline-flex items-center gap-1.5 text-xs font-bold text-text-secondary hover:text-text-primary transition-colors">
          <ArrowLeft className="w-3.5 h-3.5" /> Back to Decks
        </Link>
        <div className="flex items-center gap-3">
          {cards.length > 0 && !isFinished && (
            <span className="text-xs font-mono text-text-secondary font-bold">
              Card {currentIndex + 1} of {cards.length}
            </span>
          )}
          <QuovexButton size="sm" variant="secondary" onClick={() => setIsAddingCard(true)} leftIcon={<Plus className="w-3.5 h-3.5" />}>
            Add Card
          </QuovexButton>
        </div>
      </div>

      {cards.length === 0 ? (
        /* Empty Deck State */
        <QuovexCard className="p-10 text-center space-y-4 shadow-sm">
          <div className="w-16 h-16 relative mx-auto opacity-80">
            <Image
              src={ASSETS.icons3d.flashcards}
              alt="Empty Deck"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <h3 className="text-base font-bold text-text-primary">This Deck is Empty</h3>
            <p className="text-xs text-text-secondary mt-1 max-w-xs mx-auto">
              Add your first active recall question or formula to start spaced repetition training.
            </p>
          </div>
          <QuovexButton variant="primary" size="sm" onClick={() => setIsAddingCard(true)} leftIcon={<Plus className="w-4 h-4" />}>
            Add First Card
          </QuovexButton>
        </QuovexCard>
      ) : !isFinished && card ? (
        <div className="space-y-4">
          {/* Main Flippable Flashcard */}
          <div
            onClick={() => setIsFlipped(!isFlipped)}
            className="min-h-[320px] sm:min-h-[360px] bg-surface-elevated border border-primary/30 rounded-2xl p-6 sm:p-10 flex flex-col justify-between shadow-sm cursor-pointer select-none transition-all duration-200 hover:border-primary/60"
          >
            <div className="flex items-center justify-between">
              <QuovexBadge variant={isFlipped ? 'gold' : 'emerald'} size="sm">
                {isFlipped ? 'ANSWER / DERIVATION' : 'QUESTION / PROMPT'}
              </QuovexBadge>
              <span className="text-xs text-text-secondary flex items-center gap-1">
                <RotateCw className="w-3.5 h-3.5" /> Tap to flip
              </span>
            </div>

            <div className="my-6 text-center">
              <div className="text-base sm:text-lg font-semibold leading-relaxed">
                <LatexRenderer content={isFlipped ? card.backContent : card.frontContent} />
              </div>
            </div>

            <div className="text-center text-xs text-text-secondary">
              {isFlipped ? 'Grade recall difficulty to update SM-2 schedule' : 'Formulate the solution mentally before flipping'}
            </div>
          </div>

          {/* SM-2 Grading Buttons (Only visible when flipped) */}
          {isFlipped ? (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 animate-in fade-in duration-150">
              <button
                onClick={() => handleGrade(0)}
                className="p-3 rounded-xl bg-error-container hover:bg-error-container/80 border border-error/30 text-error font-semibold text-xs transition-all active:scale-95 text-center shadow-xs"
              >
                <span className="block font-bold">Again</span>
                <span className="text-[10px] text-text-secondary">&lt;1 min</span>
              </button>

              <button
                onClick={() => handleGrade(3)}
                className="p-3 rounded-xl bg-warning-container hover:bg-warning-container/80 border border-warning/30 text-warning font-semibold text-xs transition-all active:scale-95 text-center shadow-xs"
              >
                <span className="block font-bold">Hard</span>
                <span className="text-[10px] text-text-secondary">1 day</span>
              </button>

              <button
                onClick={() => handleGrade(4)}
                className="p-3 rounded-xl bg-primary-container hover:bg-primary-container/80 border border-primary/30 text-primary font-semibold text-xs transition-all active:scale-95 text-center shadow-xs"
              >
                <span className="block font-bold">Good</span>
                <span className="text-[10px] text-text-secondary">3 days</span>
              </button>

              <button
                onClick={() => handleGrade(5)}
                className="p-3 rounded-xl bg-[rgba(33,150,243,0.15)] hover:bg-[#2196F3]/20 border border-[#2196F3]/30 text-[#2196F3] font-semibold text-xs transition-all active:scale-95 text-center shadow-xs"
              >
                <span className="block font-bold">Easy</span>
                <span className="text-[10px] text-text-secondary">6 days</span>
              </button>
            </div>
          ) : (
            <QuovexButton
              size="md"
              className="w-full text-xs sm:text-sm py-3 shadow-glow-xs"
              onClick={() => setIsFlipped(true)}
              rightIcon={<RotateCw className="w-4 h-4" />}
            >
              Reveal Answer
            </QuovexButton>
          )}
        </div>
      ) : (
        /* Finished Review Session */
        <QuovexCard className="p-10 text-center space-y-6 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-primary-container text-primary flex items-center justify-center mx-auto shadow-sm">
            <Award className="w-7 h-7" />
          </div>

          <div>
            <h2 className="text-xl font-bold text-text-primary">Daily Review Complete!</h2>
            <p className="text-xs text-text-secondary mt-1.5">
              You reviewed {cards.length} flashcard{cards.length === 1 ? '' : 's'} today. Spaced Repetition dates have been updated in the cloud.
            </p>
          </div>

          <div className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-warning-container/40 border border-warning/30 text-warning text-xs font-bold">
            <Sparkles className="w-4 h-4" />
            <span>+30 Scholar XP Earned</span>
          </div>

          <div className="pt-3 flex justify-center gap-3">
            <QuovexButton
              variant="secondary"
              size="md"
              onClick={() => {
                setCurrentIndex(0);
                setIsFlipped(false);
                setIsFinished(false);
              }}
            >
              Review Again
            </QuovexButton>
            <Link href="/app/flashcards">
              <QuovexButton size="md">Return to Decks</QuovexButton>
            </Link>
          </div>
        </QuovexCard>
      )}

      {/* Add Card Modal */}
      {isAddingCard && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <form onSubmit={handleCreateCard} className="bg-surface border border-border rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl animate-in zoom-in-95">
            <div>
              <h3 className="text-base font-bold text-text-primary">Add Flashcard</h3>
              <p className="text-xs text-text-secondary mt-0.5">LaTeX math syntax $...$ is fully supported</p>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-secondary mb-1">Front (Prompt/Question)</label>
                <textarea
                  rows={3}
                  placeholder="e.g. State Carnot cycle efficiency formula"
                  value={newFront}
                  onChange={(e) => setNewFront(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl p-3 text-xs sm:text-sm text-text-primary focus:outline-none focus:border-primary transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-text-secondary mb-1">Back (Answer/Derivation)</label>
                <textarea
                  rows={3}
                  placeholder="e.g. $$\eta = 1 - \frac{T_C}{T_H}$$"
                  value={newBack}
                  onChange={(e) => setNewBack(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl p-3 text-xs sm:text-sm text-text-primary focus:outline-none focus:border-primary transition-all font-mono"
                  required
                />
              </div>
            </div>

            <div className="flex gap-2 pt-2">
              <QuovexButton variant="secondary" size="md" className="flex-1" onClick={() => setIsAddingCard(false)}>
                Cancel
              </QuovexButton>
              <QuovexButton type="submit" variant="primary" size="md" className="flex-1">
                Save Card
              </QuovexButton>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
