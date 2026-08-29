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

    // Save updated SM-2 schedule to Firestore
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
      <div className="max-w-3xl mx-auto p-12 text-center text-xs text-text-secondary">
        Loading deck cards...
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div className="flex items-center justify-between">
        <Link href="/app/flashcards" className="inline-flex items-center gap-2 text-label font-bold text-text-secondary hover:text-text-primary transition-colors">
          <ArrowLeft className="w-4 h-4" /> Back to Decks
        </Link>
        <div className="flex items-center gap-4">
          {cards.length > 0 && !isFinished && (
            <span className="text-body font-mono text-text-secondary font-bold">
              Card {currentIndex + 1} of {cards.length}
            </span>
          )}
          <QuovexButton size="lg" variant="secondary" onClick={() => setIsAddingCard(true)} leftIcon={<Plus className="w-5 h-5" />}>
            Add Card
          </QuovexButton>
        </div>
      </div>

      {cards.length === 0 ? (
        /* Empty Deck State */
        <QuovexCard className="p-16 text-center space-y-6 shadow-sm">
          <div className="w-24 h-24 relative mx-auto opacity-80">
            <Image
              src={ASSETS.icons3d.flashcards}
              alt="Empty Deck"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <h3 className="text-headline font-bold text-text-primary">This Deck is Empty</h3>
            <p className="text-body text-text-secondary mt-2 max-w-sm mx-auto">
              Add your first active recall question or formula to start spaced repetition training.
            </p>
          </div>
          <QuovexButton variant="primary" size="lg" onClick={() => setIsAddingCard(true)} leftIcon={<Plus className="w-5 h-5" />}>
            Add First Card
          </QuovexButton>
        </QuovexCard>
      ) : !isFinished && card ? (
        <div className="space-y-8">
          {/* Main Flippable Flashcard */}
          <div
            onClick={() => setIsFlipped(!isFlipped)}
            className="min-h-[400px] bg-surface-elevated border border-primary/30 rounded-3xl p-10 sm:p-16 flex flex-col justify-between shadow-glow-lg cursor-pointer select-none transition-all duration-300 hover:border-primary/60"
          >
            <div className="flex items-center justify-between">
              <QuovexBadge variant={isFlipped ? 'gold' : 'emerald'} size="lg">
                {isFlipped ? 'ANSWER / DERIVATION' : 'QUESTION / PROMPT'}
              </QuovexBadge>
              <span className="text-label text-text-secondary flex items-center gap-1.5">
                <RotateCw className="w-4 h-4" /> Tap card to flip
              </span>
            </div>

            <div className="my-12 text-center">
              <div className="text-headline sm:text-display font-semibold leading-relaxed">
                <LatexRenderer content={isFlipped ? card.backContent : card.frontContent} />
              </div>
            </div>

            <div className="text-center text-body text-text-secondary">
              {isFlipped ? 'Grade your recall below to update SM-2 schedule' : 'Formulate the answer in your mind before flipping'}
            </div>
          </div>

          {/* SM-2 Grading Buttons (Only visible when flipped) */}
          {isFlipped ? (
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 animate-in fade-in slide-in-from-bottom-2 duration-200">
              <button
                onClick={() => handleGrade(0)}
                className="p-4 rounded-2xl bg-error-container hover:bg-error-container/80 border border-error/30 text-error font-semibold text-body transition-all active:scale-95 text-center shadow-sm"
              >
                <span className="block font-bold">Again</span>
                <span className="text-label text-text-secondary font-normal">&lt;1 min</span>
              </button>

              <button
                onClick={() => handleGrade(3)}
                className="p-4 rounded-2xl bg-warning-container hover:bg-warning-container/80 border border-warning/30 text-warning font-semibold text-body transition-all active:scale-95 text-center shadow-sm"
              >
                <span className="block font-bold">Hard</span>
                <span className="text-label text-text-secondary font-normal">1 day</span>
              </button>

              <button
                onClick={() => handleGrade(4)}
                className="p-4 rounded-2xl bg-primary-container hover:bg-primary-container/80 border border-primary/30 text-primary font-semibold text-body transition-all active:scale-95 text-center shadow-sm"
              >
                <span className="block font-bold">Good</span>
                <span className="text-label text-text-secondary font-normal">3 days</span>
              </button>

              <button
                onClick={() => handleGrade(5)}
                className="p-4 rounded-2xl bg-[rgba(33,150,243,0.15)] hover:bg-[#2196F3]/20 border border-[#2196F3]/30 text-[#2196F3] font-semibold text-body transition-all active:scale-95 text-center shadow-sm"
              >
                <span className="block font-bold">Easy</span>
                <span className="text-label text-text-secondary font-normal">6 days</span>
              </button>
            </div>
          ) : (
            <QuovexButton
              size="lg"
              className="w-full text-title py-4 shadow-glow-sm"
              onClick={() => setIsFlipped(true)}
              rightIcon={<RotateCw className="w-5 h-5" />}
            >
              Reveal Answer
            </QuovexButton>
          )}
        </div>
      ) : (
        /* Finished Review Session */
        <QuovexCard className="p-16 text-center space-y-8 shadow-glow-sm">
          <div className="w-20 h-20 rounded-2xl bg-primary-container text-primary flex items-center justify-center mx-auto shadow-glow">
            <Award className="w-10 h-10" />
          </div>

          <div>
            <h2 className="text-display font-bold text-text-primary">Daily Review Complete!</h2>
            <p className="text-body text-text-secondary mt-3">
              You reviewed {cards.length} flashcard{cards.length === 1 ? '' : 's'} today. Spaced Repetition dates have been updated in the cloud.
            </p>
          </div>

          <div className="inline-flex items-center gap-3 px-5 py-3 rounded-xl bg-warning-container border border-warning/30 text-warning text-body font-bold">
            <Sparkles className="w-5 h-5" />
            <span>+30 Scholar XP Earned</span>
          </div>

          <div className="pt-6 flex flex-col sm:flex-row justify-center gap-4">
            <QuovexButton
              variant="secondary"
              size="lg"
              onClick={() => {
                setCurrentIndex(0);
                setIsFlipped(false);
                setIsFinished(false);
              }}
            >
              Review Again
            </QuovexButton>
            <Link href="/app/flashcards">
              <QuovexButton size="lg">Return to Decks</QuovexButton>
            </Link>
          </div>
        </QuovexCard>
      )}

      {/* Add Card Modal */}
      {isAddingCard && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <form onSubmit={handleCreateCard} className="bg-surface border border-border rounded-3xl max-w-lg w-full p-8 space-y-6 shadow-2xl animate-in zoom-in-95">
            <div>
              <h3 className="text-headline font-black text-text-primary">Add Flashcard</h3>
              <p className="text-body text-text-secondary mt-1">LaTeX math syntax $...$ is fully supported</p>
            </div>

            <div className="space-y-5">
              <div>
                <label className="block text-label font-bold text-text-secondary mb-2">Front (Prompt/Question)</label>
                <textarea
                  rows={4}
                  placeholder="e.g. State Carnot cycle efficiency formula"
                  value={newFront}
                  onChange={(e) => setNewFront(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl p-4 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-label font-bold text-text-secondary mb-2">Back (Answer/Derivation)</label>
                <textarea
                  rows={4}
                  placeholder="e.g. $$\eta = 1 - \frac{T_C}{T_H}$$"
                  value={newBack}
                  onChange={(e) => setNewBack(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl p-4 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all font-mono"
                  required
                />
              </div>
            </div>

            <div className="flex gap-3 pt-4">
              <QuovexButton variant="secondary" size="lg" className="flex-1" onClick={() => setIsAddingCard(false)}>
                Cancel
              </QuovexButton>
              <QuovexButton type="submit" variant="primary" size="lg" className="flex-1">
                Save Card
              </QuovexButton>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
