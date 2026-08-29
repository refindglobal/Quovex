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
  Upload, 
  CheckCircle2, 
  BookOpen,
  Layers,
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
      
      // Auto-extract lines or paragraphs as card prompts
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

      // Create individual cards
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
    <div className="max-w-5xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div>
          <Link href="/app/knowledge" className="inline-flex items-center gap-2 text-label font-bold text-text-secondary hover:text-text-primary mb-3 transition-colors">
            <ArrowLeft className="w-4 h-4" /> Back to Knowledge Hub
          </Link>
          <h1 className="text-display font-black text-text-primary flex items-center gap-4">
            <FileText className="w-10 h-10 text-primary" />
            Learning Materials Library
          </h1>
          <p className="text-section text-text-secondary mt-2">
            Personal notes, textbook excerpts, and formula cheat sheets synchronized with Android.
          </p>
        </div>

        <QuovexButton
          size="lg"
          onClick={() => setIsCreating(!isCreating)}
          leftIcon={<Plus className="w-5 h-5" />}
        >
          {isCreating ? 'Cancel' : 'New Note / PDF'}
        </QuovexButton>
      </div>

      {successMsg && (
        <div className="p-4 rounded-2xl bg-success-container border border-success/30 text-body text-success font-bold flex items-center gap-3 animate-in fade-in shadow-sm">
          <CheckCircle2 className="w-5 h-5 shrink-0" />
          <span>{successMsg}</span>
        </div>
      )}

      {/* New Note Form */}
      {isCreating && (
        <QuovexCard className="p-8 space-y-6 border-primary/30 animate-in fade-in shadow-glow-sm">
          <h3 className="font-bold text-text-primary text-title">Add New Learning Material</h3>
          <form onSubmit={handleCreateNote} className="space-y-6">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
              <div>
                <label className="block text-label font-bold text-text-secondary mb-2">Title</label>
                <input
                  type="text"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="e.g. Modern Physics Formulas & Traps"
                  className="w-full bg-surface-variant border border-border rounded-xl px-4 py-3 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-label font-bold text-text-secondary mb-2">Subject</label>
                <select
                  value={newSubject}
                  onChange={(e) => setNewSubject(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl px-4 py-3 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
                >
                  {SUBJECTS.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            </div>

            <div>
              <label className="block text-label font-bold text-text-secondary mb-2">Material Content / LaTeX Notes</label>
              <textarea
                rows={8}
                value={newContent}
                onChange={(e) => setNewContent(e.target.value)}
                placeholder="Paste formulas with LaTeX ($...$), summary points, or textbook excerpts..."
                className="w-full bg-surface-variant border border-border rounded-xl p-4 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all font-mono"
                required
              />
            </div>

            <div className="flex justify-end gap-3">
              <QuovexButton variant="ghost" size="lg" type="button" onClick={() => setIsCreating(false)}>
                Cancel
              </QuovexButton>
              <QuovexButton size="lg" type="submit" isLoading={loading}>
                Save Material
              </QuovexButton>
            </div>
          </form>
        </QuovexCard>
      )}

      {/* Notes Grid or True Empty State */}
      {notes.length === 0 ? (
        <QuovexCard className="text-center py-16 px-8 max-w-md mx-auto space-y-6">
          <div className="w-32 h-32 relative mx-auto opacity-90">
            <Image
              src={ASSETS.illustrations.emptyNotes}
              alt="No Learning Materials"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <h3 className="text-title font-bold text-text-primary">No Learning Materials Saved Yet</h3>
            <p className="text-body text-text-secondary mt-2">
              Add your first note or upload study content to automatically generate flashcards and quizzes.
            </p>
          </div>
          <QuovexButton size="lg" className="mt-4 w-full" onClick={() => setIsCreating(true)} leftIcon={<Plus className="w-5 h-5" />}>
            Create First Note
          </QuovexButton>
        </QuovexCard>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {notes.map((note) => (
            <QuovexCard key={note.id} hoverEffect className="space-y-4 flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between gap-3 mb-3">
                  <QuovexBadge variant="emerald" size="md">{note.subject}</QuovexBadge>
                  <button
                    onClick={async () => {
                      if (currentUser && confirm('Delete this learning note?')) {
                        await deleteUserNote(currentUser.uid, note.id);
                      }
                    }}
                    className="text-text-secondary hover:text-error p-1.5 rounded-lg hover:bg-error-container transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>

                <h3 className="text-title font-bold text-text-primary">{note.title}</h3>
                <div className="mt-3 text-body text-text-secondary line-clamp-4 leading-relaxed">
                  <LatexRenderer content={note.content} />
                </div>
              </div>

              <div className="pt-4 border-t border-border flex items-center justify-between gap-3 mt-4">
                <span className="text-caption font-semibold text-text-tertiary">
                  {new Date(note.updatedAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                </span>

                <QuovexButton
                  variant="outline"
                  size="md"
                  onClick={() => handleGenerateFlashcards(note)}
                  leftIcon={<Sparkles className="w-4 h-4" />}
                >
                  Create SM-2 Deck
                </QuovexButton>
              </div>
            </QuovexCard>
          ))}
        </div>
      )}
    </div>
  );
}
