'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import {
  Sparkles,
  Plus,
  Play,
  Layers,
  BookOpen,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import { subscribeToFlashcardDecks, saveFlashcardDeck, FlashcardDeck } from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { ASSETS } from '@/lib/assets';

const SUBJECTS = ['Physics', 'Chemistry', 'Mathematics', 'Biology', 'General Study', 'Remedial'];

export default function FlashcardsDirectoryPage() {
  const [decks, setDecks] = useState<FlashcardDeck[]>([]);
  const [loading, setLoading] = useState(true);
  const [isCreatingDeck, setIsCreatingDeck] = useState(false);
  const [newDeckTitle, setNewDeckTitle] = useState('');
  const [newDeckSubject, setNewDeckSubject] = useState('Physics');

  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) {
      setLoading(false);
      return;
    }

    const unsub = subscribeToFlashcardDecks(currentUser.uid, (list) => {
      setDecks(list);
      setLoading(false);
    });
    return () => unsub();
  }, [currentUser]);

  const handleCreateDeck = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newDeckTitle.trim() || !currentUser) return;

    const newDeck: FlashcardDeck = {
      id: `deck_${Date.now()}`,
      title: newDeckTitle.trim(),
      subject: newDeckSubject,
      cardCount: 0,
      masteryPercentage: 0,
      lastStudiedAt: Date.now(),
    };

    await saveFlashcardDeck(currentUser.uid, newDeck);
    setNewDeckTitle('');
    setIsCreatingDeck(false);
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-20">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
              <Sparkles className="w-7 h-7 text-warning" />
              SM-2 Spaced Repetition Flashcards
            </h1>
            <QuovexBadge variant="emerald" size="sm">Active Recall</QuovexBadge>
          </div>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Algorithmically scheduled reviews targeting the scientific forgetting curve.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <QuovexButton size="sm" variant="secondary" onClick={() => setIsCreatingDeck(true)} leftIcon={<Plus className="w-4 h-4" />}>
            New Deck
          </QuovexButton>
          <Link href="/app/knowledge/notes">
            <QuovexButton size="sm" variant="primary" leftIcon={<Sparkles className="w-4 h-4" />}>
              From Notes
            </QuovexButton>
          </Link>
        </div>
      </div>

      {/* Decks Grid or True Empty State */}
      {decks.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {decks.map((deck) => (
            <QuovexCard key={deck.id} hoverEffect className="p-5 space-y-3 flex flex-col justify-between border-border bg-surface">
              <div>
                <div className="flex items-center justify-between mb-2">
                  <QuovexBadge variant="emerald" size="sm">{deck.subject}</QuovexBadge>
                  <span className="text-[11px] text-text-secondary flex items-center gap-1 font-mono">
                    <Layers className="w-3.5 h-3.5" /> {deck.cardCount} Cards
                  </span>
                </div>

                <h3 className="text-sm font-bold text-text-primary mt-1">{deck.title}</h3>

                {/* Mastery bar */}
                <div className="mt-4 space-y-1.5">
                  <div className="flex items-center justify-between text-[11px] font-bold text-text-secondary">
                    <span>Mastery</span>
                    <span className="text-primary">{deck.masteryPercentage}%</span>
                  </div>
                  <div className="h-2 bg-surface-variant rounded-full overflow-hidden border border-border">
                    <div
                      className="h-full bg-gradient-to-r from-primary to-warning rounded-full transition-all duration-500"
                      style={{ width: `${deck.masteryPercentage}%` }}
                    />
                  </div>
                </div>
              </div>

              <div className="pt-3 border-t border-border mt-3">
                <Link href={`/app/flashcards/${deck.id}`}>
                  <QuovexButton size="sm" className="w-full" rightIcon={<Play className="w-3.5 h-3.5 fill-current" />}>
                    Review Deck
                  </QuovexButton>
                </Link>
              </div>
            </QuovexCard>
          ))}
        </div>
      ) : (
        /* True Empty State */
        <QuovexCard className="p-10 sm:p-14 text-center space-y-4 max-w-md mx-auto shadow-sm">
          <div className="w-20 h-20 relative mx-auto opacity-80">
            <Image
              src={ASSETS.icons3d.flashcards}
              alt="No Flashcard Decks"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <h3 className="text-base sm:text-lg font-bold text-text-primary">No Flashcard Decks Yet</h3>
            <p className="text-xs text-text-secondary mt-1 max-w-xs mx-auto">
              Create a custom deck or synthesize high-yield formula cards from study notes.
            </p>
          </div>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
            <QuovexButton variant="primary" size="sm" onClick={() => setIsCreatingDeck(true)} leftIcon={<Plus className="w-4 h-4" />}>
              Create Deck
            </QuovexButton>
            <Link href="/app/knowledge/notes">
              <QuovexButton variant="secondary" size="sm" leftIcon={<Sparkles className="w-4 h-4" />}>
                From Notes
              </QuovexButton>
            </Link>
          </div>
        </QuovexCard>
      )}

      {/* Create Deck Modal */}
      {isCreatingDeck && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <form onSubmit={handleCreateDeck} className="bg-surface border border-border rounded-2xl max-w-md w-full p-6 space-y-4 shadow-2xl animate-in zoom-in-95">
            <div>
              <h3 className="text-base font-bold text-text-primary">Create Flashcard Deck</h3>
              <p className="text-xs text-text-secondary mt-0.5">Organize active recall cards by subject stream</p>
            </div>

            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-text-secondary mb-1">Deck Title</label>
                <input
                  type="text"
                  placeholder="e.g. Organic Chemistry Named Reactions"
                  value={newDeckTitle}
                  onChange={(e) => setNewDeckTitle(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl px-3 py-2 text-xs sm:text-sm text-text-primary focus:outline-none focus:border-primary transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-text-secondary mb-1">Subject Stream</label>
                <select
                  value={newDeckSubject}
                  onChange={(e) => setNewDeckSubject(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl px-3 py-2 text-xs sm:text-sm text-text-primary focus:outline-none focus:border-primary transition-all"
                >
                  {SUBJECTS.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex gap-2 pt-2">
              <QuovexButton variant="secondary" size="md" className="flex-1" onClick={() => setIsCreatingDeck(false)}>
                Cancel
              </QuovexButton>
              <QuovexButton type="submit" size="md" variant="primary" className="flex-1">
                Create Deck
              </QuovexButton>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
