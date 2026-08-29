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
  PlusCircle,
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
    <div className="max-w-5xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div>
          <h1 className="text-display font-black text-text-primary flex items-center gap-4">
            <Sparkles className="w-10 h-10 text-warning" />
            SM-2 Spaced Repetition Flashcards
          </h1>
          <p className="text-section text-text-secondary mt-2">
            Optimized active recall cards scheduled according to the scientific forgetting curve.
          </p>
        </div>

        <div className="flex items-center gap-4">
          <QuovexButton size="lg" variant="secondary" onClick={() => setIsCreatingDeck(true)} leftIcon={<Plus className="w-5 h-5" />}>
            New Deck
          </QuovexButton>
          <Link href="/app/knowledge/notes">
            <QuovexButton size="lg" variant="primary" leftIcon={<Sparkles className="w-5 h-5" />}>
              Generate from Notes
            </QuovexButton>
          </Link>
        </div>
      </div>

      {/* Decks Grid or True Empty State */}
      {decks.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {decks.map((deck) => (
            <QuovexCard key={deck.id} hoverEffect className="space-y-4 flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between mb-3">
                  <QuovexBadge variant="emerald" size="md">{deck.subject}</QuovexBadge>
                  <span className="text-label text-text-secondary flex items-center gap-1.5 font-mono">
                    <Layers className="w-4 h-4" /> {deck.cardCount} Cards
                  </span>
                </div>

                <h3 className="text-title font-bold text-text-primary mt-2">{deck.title}</h3>

                {/* Mastery bar */}
                <div className="mt-5 space-y-2">
                  <div className="flex items-center justify-between text-label font-bold text-text-secondary">
                    <span>Mastery Level</span>
                    <span className="font-black text-primary">{deck.masteryPercentage}%</span>
                  </div>
                  <div className="h-2.5 bg-surface-variant rounded-full overflow-hidden border border-border">
                    <div
                      className="h-full bg-gradient-to-r from-primary to-warning rounded-full transition-all duration-500"
                      style={{ width: `${deck.masteryPercentage}%` }}
                    />
                  </div>
                </div>
              </div>

              <div className="pt-4 border-t border-border mt-4">
                <Link href={`/app/flashcards/${deck.id}`}>
                  <QuovexButton size="lg" className="w-full" rightIcon={<Play className="w-4 h-4 fill-current" />}>
                    Start Daily Review
                  </QuovexButton>
                </Link>
              </div>
            </QuovexCard>
          ))}
        </div>
      ) : (
        /* True Empty State (Zero Fake Data) */
        <QuovexCard className="p-16 text-center space-y-6 max-w-lg mx-auto shadow-sm">
          <div className="w-32 h-32 relative mx-auto opacity-90">
            <Image
              src={ASSETS.icons3d.flashcards}
              alt="No Flashcard Decks"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <h3 className="text-headline font-bold text-text-primary">No Flashcard Decks Yet</h3>
            <p className="text-body text-text-secondary mt-2 max-w-sm mx-auto">
              Create a custom spaced repetition deck or generate high-yield cards from your study notes.
            </p>
          </div>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
            <QuovexButton variant="primary" size="lg" onClick={() => setIsCreatingDeck(true)} leftIcon={<Plus className="w-5 h-5" />}>
              Create Deck
            </QuovexButton>
            <Link href="/app/knowledge/notes">
              <QuovexButton variant="secondary" size="lg" leftIcon={<Sparkles className="w-5 h-5" />}>
                Generate from Notes
              </QuovexButton>
            </Link>
          </div>
        </QuovexCard>
      )}

      {/* Create Deck Modal */}
      {isCreatingDeck && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <form onSubmit={handleCreateDeck} className="bg-surface border border-border rounded-3xl max-w-md w-full p-8 space-y-6 shadow-2xl animate-in zoom-in-95">
            <div>
              <h3 className="text-headline font-black text-text-primary">Create Flashcard Deck</h3>
              <p className="text-body text-text-secondary mt-1">Organize active recall cards by subject stream</p>
            </div>

            <div className="space-y-5">
              <div>
                <label className="block text-label font-bold text-text-secondary mb-2">Deck Title</label>
                <input
                  type="text"
                  placeholder="e.g. Organic Chemistry Named Reactions"
                  value={newDeckTitle}
                  onChange={(e) => setNewDeckTitle(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl px-4 py-3 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-label font-bold text-text-secondary mb-2">Subject Stream</label>
                <select
                  value={newDeckSubject}
                  onChange={(e) => setNewDeckSubject(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl px-4 py-3 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
                >
                  {SUBJECTS.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="flex gap-3 pt-4">
              <QuovexButton variant="secondary" size="lg" className="flex-1" onClick={() => setIsCreatingDeck(false)}>
                Cancel
              </QuovexButton>
              <QuovexButton type="submit" size="lg" variant="primary" className="flex-1">
                Create Deck
              </QuovexButton>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
