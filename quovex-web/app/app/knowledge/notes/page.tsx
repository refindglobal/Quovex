'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { 
  FileText, 
  Plus, 
  Trash2, 
  Sparkles, 
  ArrowLeft, 
  CheckCircle2, 
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import { 
  subscribeToUserNotes, 
  saveUserNote, 
  deleteUserNote, 
  saveFlashcardDeck,
  saveFlashcard,
  NoteItem,
  FlashcardDeck 
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import { ASSETS } from '@/lib/assets';

const SUBJECTS = ['Physics', 'Chemistry', 'Mathematics', 'Biology', 'General Study', 'Revision'];

export default function NotesLibraryPage() {
  const [notes, setNotes] = useState<NoteItem[]>([]);
  const [isCreating, setIsCreating] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newSubject, setNewSubject] = useState('Physics');
  const [newContent, setNewContent] = useState('');
  const [loading, setLoading] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) return;
    const unsub = subscribeToUserNotes(currentUser.uid, (list) => setNotes(list));
    return () => unsub();
  }, [currentUser]);

  const handleCreateNote = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim() || !newContent.trim() || !currentUser) return;

    setLoading(true);
    try {
      const note: NoteItem = {
        id: `note_${Date.now()}`,
        title: newTitle.trim(),
        subject: newSubject,
        content: newContent.trim(),
        createdAt: Date.now(),
        updatedAt: Date.now(),
      };

      await saveUserNote(currentUser.uid, note);
      setNewTitle('');
      setNewContent('');
      setIsCreating(false);
      setSuccessMsg('Material added to your cloud library.');
      setTimeout(() => setSuccessMsg(null), 4000);
    } catch (err: any) {
      alert(err.message || 'Failed to save note');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateFlashcards = async (note: NoteItem) => {
    if (!currentUser) return;

    setSuccessMsg(`Synthesizing SM-2 flashcards for "${note.title}"...`);

    try {
      const deckId = `deck_${Date.now()}`;
      const lines = note.content.split('\n').filter(l => l.trim().length > 10);
      const cardCount = Math.max(1, Math.min(5, lines.length));

      const newDeck: FlashcardDeck = {
        id: deckId,
        title: `${note.title} (AI Deck)`,
        subject: note.subject,
        cardCount,
        masteryPercentage: 0,
        lastStudiedAt: Date.now(),
      };

      await saveFlashcardDeck(currentUser.uid, newDeck);

      for (let i = 0; i < cardCount; i++) {
        const line = lines[i] || note.content;
        await saveFlashcard(currentUser.uid, deckId, {
          id: `card_${Date.now()}_${i}`,
          deckId,
          frontContent: `Key Concept #${i + 1} from ${note.title}`,
          backContent: line,
          repetitions: 0,
          intervalDays: 1,
          easeFactor: 2.5,
          nextReviewDate: Date.now(),
        });
      }

      setSuccessMsg(`Deck created! ${cardCount} flashcards added to your Spaced Repetition queue.`);
      setTimeout(() => setSuccessMsg(null), 5000);
    } catch (err: any) {
      alert(err.message || 'Failed to create flashcard deck');
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-20">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Link href="/app/knowledge" className="inline-flex items-center gap-1.5 text-xs font-bold text-text-secondary hover:text-text-primary mb-1 transition-colors">
            <ArrowLeft className="w-3.5 h-3.5" /> Back to Knowledge Hub
          </Link>
          <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
            <FileText className="w-7 h-7 text-primary" />
            Learning Materials Library
          </h1>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Personal notes, textbook excerpts, and formula cheat sheets.
          </p>
        </div>

        <QuovexButton
          size="sm"
          onClick={() => setIsCreating(!isCreating)}
          leftIcon={<Plus className="w-4 h-4" />}
        >
          {isCreating ? 'Cancel' : 'New Note'}
        </QuovexButton>
      </div>

      {successMsg && (
        <div className="p-3.5 rounded-xl bg-success-container border border-success/30 text-xs sm:text-sm text-success font-bold flex items-center gap-2.5 animate-in fade-in shadow-xs">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* New Note Form */}
      {isCreating && (
        <QuovexCard className="p-5 sm:p-6 space-y-4 border-primary/30 animate-in fade-in shadow-sm">
          <h3 className="font-bold text-text-primary text-sm sm:text-base">Add New Learning Material</h3>
          <form onSubmit={handleCreateNote} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-text-secondary mb-1">Title</label>
                <input
                  type="text"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="e.g. Modern Physics Formulas & Traps"
                  className="w-full bg-surface-variant border border-border rounded-xl px-3 py-2 text-xs sm:text-sm text-text-primary focus:outline-none focus:border-primary transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-text-secondary mb-1">Subject</label>
                <select
                  value={newSubject}
                  onChange={(e) => setNewSubject(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl px-3 py-2 text-xs sm:text-sm text-text-primary focus:outline-none focus:border-primary transition-all"
                >
                  {SUBJECTS.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-text-secondary mb-1">Material Content / LaTeX Notes</label>
              <textarea
                rows={6}
                value={newContent}
                onChange={(e) => setNewContent(e.target.value)}
                placeholder="Paste formulas with LaTeX ($...$), summary points, or textbook excerpts..."
                className="w-full bg-surface-variant border border-border rounded-xl p-3 text-xs sm:text-sm text-text-primary focus:outline-none focus:border-primary transition-all font-mono"
                required
              />
            </div>

            <div className="flex justify-end gap-2">
              <QuovexButton variant="ghost" size="sm" type="button" onClick={() => setIsCreating(false)}>
                Cancel
              </QuovexButton>
              <QuovexButton size="sm" type="submit" isLoading={loading}>
                Save Material
              </QuovexButton>
            </div>
          </form>
        </QuovexCard>
      )}

      {/* Notes Grid or Empty State */}
      {notes.length === 0 ? (
        <QuovexCard className="text-center py-12 px-6 max-w-sm mx-auto space-y-4 shadow-sm">
          <div className="w-20 h-20 relative mx-auto opacity-80">
            <Image
              src={ASSETS.illustrations.emptyNotes}
              alt="No Learning Materials"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <h3 className="text-sm sm:text-base font-bold text-text-primary">No Notes Saved Yet</h3>
            <p className="text-xs text-text-secondary mt-1">
              Add your first note to automatically synthesize SM-2 flashcard decks.
            </p>
          </div>
          <QuovexButton size="sm" className="mt-2 w-full" onClick={() => setIsCreating(true)} leftIcon={<Plus className="w-4 h-4" />}>
            Create First Note
          </QuovexButton>
        </QuovexCard>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {notes.map((note) => (
            <QuovexCard key={note.id} hoverEffect className="p-4 space-y-3 flex flex-col justify-between border-border bg-surface">
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <QuovexBadge variant="emerald" size="sm">{note.subject}</QuovexBadge>
                  <button
                    onClick={async () => {
                      if (currentUser && confirm('Delete this learning note?')) {
                        await deleteUserNote(currentUser.uid, note.id);
                      }
                    }}
                    className="text-text-secondary hover:text-error p-1 rounded-lg hover:bg-error-container/20 transition-colors"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>

                <h3 className="text-sm font-bold text-text-primary">{note.title}</h3>
                <div className="mt-2 text-xs text-text-secondary line-clamp-3 leading-relaxed">
                  <LatexRenderer content={note.content} />
                </div>
              </div>

              <div className="pt-3 border-t border-border flex items-center justify-between gap-2 mt-3">
                <span className="text-[10px] font-semibold text-text-secondary">
                  {new Date(note.updatedAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                </span>

                <QuovexButton
                  variant="outline"
                  size="sm"
                  onClick={() => handleGenerateFlashcards(note)}
                  leftIcon={<Sparkles className="w-3.5 h-3.5" />}
                  className="text-xs"
                >
                  Make SM-2 Deck
                </QuovexButton>
              </div>
            </QuovexCard>
          ))}
        </div>
      )}
    </div>
  );
}
