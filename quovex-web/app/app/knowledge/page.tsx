'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import {
  BookOpen,
  Sparkles,
  FileText,
  ArrowRight,
  Library,
  Lightbulb,
  CheckCircle2,
} from 'lucide-react';
import { subscribeToQuovexOriginals, QuovexOriginalBook } from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import { ASSETS } from '@/lib/assets';

interface InteractiveChapter {
  id: number;
  title: string;
  analogy: string;
  derivation: string;
  takeaway: string;
}

const DEFAULT_ORIGINAL_CHAPTERS: InteractiveChapter[] = [
  {
    id: 1,
    title: 'Chapter 1: Moment of Inertia & The Rotating Flywheel',
    analogy: 'Imagine spinning a heavy stone attached to a string versus spinning a marble. Mass located far from the axis resists rotational acceleration with the square of the distance ($r^2$).',
    derivation: '$$I = \\int r^2 \\, dm = \\sum m_i r_i^2$$\nFor a uniform thin rod of length $L$ and mass $M$ about its center:\n$$I_{\\text{cm}} = \\int_{-L/2}^{L/2} x^2 \\left(\\frac{M}{L}\\right) dx = \\frac{1}{12} M L^2$$',
    takeaway: 'Applying the Parallel Axis Theorem: $I = I_{\\text{cm}} + M d^2$. Moment of inertia is minimum about the center of mass axis.',
  },
  {
    id: 2,
    title: 'Chapter 2: Pure Rolling Motion Without Slipping',
    analogy: 'When a car wheel rolls smoothly on asphalt, the exact instantaneous point of contact with the ground is completely stationary relative to the road ($v_{\\text{contact}} = 0$).',
    derivation: '$$v_{\\text{cm}} = R \\omega, \\quad a_{\\text{cm}} = R \\alpha$$\nTotal kinetic energy of a rolling body:\n$$K_{\\text{total}} = \\frac{1}{2} M v_{\\text{cm}}^2 + \\frac{1}{2} I_{\\text{cm}} \\omega^2 = \\frac{1}{2} M v_{\\text{cm}}^2 \\left(1 + \\frac{k^2}{R^2}\\right)$$',
    takeaway: 'Friction in pure rolling is static friction and does ZERO net mechanical work.',
  },
];

export default function KnowledgeHubPage() {
  const [tab, setTab] = useState<'originals' | 'ncert' | 'notes'>('originals');
  const [originals, setOriginals] = useState<QuovexOriginalBook[]>([]);
  const [readingBook, setReadingBook] = useState<{ title: string; subject: string; exam: string } | null>(null);
  const [activeChapterIndex, setActiveChapterIndex] = useState(0);

  useEffect(() => {
    const unsub = subscribeToQuovexOriginals((books) => setOriginals(books));
    return () => unsub();
  }, []);

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-20">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
              <Library className="w-7 h-7 text-primary" />
              Knowledge Hub
            </h1>
            <QuovexBadge variant="emerald" size="sm">Curated Library</QuovexBadge>
          </div>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Rationalised NCERT textbooks, Quovex Originals with visual analogies, and student notes.
          </p>
        </div>

        {/* Action Links */}
        <div className="flex items-center gap-3">
          <Link href="/app/knowledge/ncert">
            <QuovexButton variant="secondary" size="sm" leftIcon={<BookOpen className="w-4 h-4" />}>
              NCERT (6–12)
            </QuovexButton>
          </Link>
          <Link href="/app/knowledge/notes">
            <QuovexButton variant="primary" size="sm" leftIcon={<FileText className="w-4 h-4" />}>
              My Materials
            </QuovexButton>
          </Link>
        </div>
      </div>

      {/* 3 Ecosystem Navigation Tabs */}
      <div className="flex items-center gap-2 border-b border-border pb-3 overflow-x-auto no-scrollbar">
        <button
          onClick={() => setTab('originals')}
          className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all ${
            tab === 'originals'
              ? 'bg-primary-container text-primary border border-primary/40 shadow-xs'
              : 'text-text-secondary hover:text-text-primary hover:bg-surface-variant/50'
          }`}
        >
          ✨ Quovex Originals
        </button>
        <Link href="/app/knowledge/ncert">
          <button className="px-3.5 py-1.5 rounded-xl text-xs font-semibold text-text-secondary hover:text-text-primary hover:bg-surface-variant/50 whitespace-nowrap">
            📚 NCERT Catalog (Class 6-12)
          </button>
        </Link>
        <Link href="/app/knowledge/notes">
          <button className="px-3.5 py-1.5 rounded-xl text-xs font-semibold text-text-secondary hover:text-text-primary hover:bg-surface-variant/50 whitespace-nowrap">
            📝 My Materials & PDFs
          </button>
        </Link>
      </div>

      {/* Quovex Originals Gallery */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-sm sm:text-base font-bold text-text-primary">Published Quovex Originals</h2>
          <QuovexBadge variant="gold" size="sm">PRO ACCESS</QuovexBadge>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Book 1: Rotational Mechanics */}
          <QuovexCard
            hoverEffect
            className="p-5 space-y-3 cursor-pointer border-border bg-surface"
            onClick={() => {
              setReadingBook({
                title: 'Mastering Rotational Mechanics & Angular Invariants',
                subject: 'Physics',
                exam: 'JEE Advanced',
              });
              setActiveChapterIndex(0);
            }}
          >
            <div className="h-36 rounded-xl bg-gradient-to-br from-primary-container/80 to-surface-variant border border-primary/30 flex flex-col justify-end p-4 relative overflow-hidden">
              <div className="w-16 h-16 absolute top-2 right-2 opacity-30">
                <Image
                  src={ASSETS.icons3d.physicsOrbit}
                  alt="Physics"
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
              <QuovexBadge variant="emerald" size="sm">PHYSICS • JEE ADVANCED</QuovexBadge>
              <h3 className="font-bold text-text-primary text-sm mt-2">Mastering Rotational Mechanics</h3>
            </div>
            <p className="text-xs text-text-secondary leading-relaxed">
              Step-by-step moment of inertia derivations, torque tensors, and visual analogies for rolling motion.
            </p>
            <div className="pt-1 flex items-center justify-between text-xs text-primary font-bold">
              <span>12 Chapters</span>
              <ArrowRight className="w-4 h-4" />
            </div>
          </QuovexCard>

          {/* Book 2: Organic Chemistry */}
          <QuovexCard
            hoverEffect
            className="p-5 space-y-3 cursor-pointer border-border bg-surface"
            onClick={() => {
              setReadingBook({
                title: 'Organic Reaction Mechanisms & High-Yield Pathways',
                subject: 'Chemistry',
                exam: 'NEET UG',
              });
              setActiveChapterIndex(0);
            }}
          >
            <div className="h-36 rounded-xl bg-gradient-to-br from-warning-container/80 to-surface-variant border border-warning/30 flex flex-col justify-end p-4 relative overflow-hidden">
              <div className="w-16 h-16 absolute top-2 right-2 opacity-30">
                <Image
                  src={ASSETS.icons3d.chemBenzene}
                  alt="Chemistry"
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
              <QuovexBadge variant="gold" size="sm">CHEMISTRY • NEET UG</QuovexBadge>
              <h3 className="font-bold text-text-primary text-sm mt-2">Organic Reaction Mechanisms</h3>
            </div>
            <p className="text-xs text-text-secondary leading-relaxed">
              Electrophilic additions, SN1 vs SN2 visual maps, and common student misconception traps decoded.
            </p>
            <div className="pt-1 flex items-center justify-between text-xs text-warning font-bold">
              <span>16 Chapters</span>
              <ArrowRight className="w-4 h-4" />
            </div>
          </QuovexCard>

          {/* Book 3: Calculus */}
          <QuovexCard
            hoverEffect
            className="p-5 space-y-3 cursor-pointer border-border bg-surface"
            onClick={() => {
              setReadingBook({
                title: 'Calculus & Definite Integrals In-Depth',
                subject: 'Mathematics',
                exam: 'JEE Main',
              });
              setActiveChapterIndex(0);
            }}
          >
            <div className="h-36 rounded-xl bg-gradient-to-br from-surface-variant to-surface-elevated border border-border flex flex-col justify-end p-4 relative overflow-hidden">
              <div className="w-16 h-16 absolute top-2 right-2 opacity-30">
                <Image
                  src={ASSETS.icons3d.mathMobius}
                  alt="Mathematics"
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
              <QuovexBadge variant="muted" size="sm">MATHEMATICS • JEE MAIN</QuovexBadge>
              <h3 className="font-bold text-text-primary text-sm mt-2">Calculus & Area Under Curves</h3>
            </div>
            <p className="text-xs text-text-secondary leading-relaxed">
              Definite integral tricks, Leibniz rule derivations, and past 10-year question walkthroughs.
            </p>
            <div className="pt-1 flex items-center justify-between text-xs text-text-secondary font-bold">
              <span>14 Chapters</span>
              <ArrowRight className="w-4 h-4" />
            </div>
          </QuovexCard>
        </div>
      </div>

      {/* ── Interactive Chapter Reader Modal ──────────────────────────────── */}
      {readingBook && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-2xl max-w-3xl w-full max-h-[85vh] flex flex-col shadow-2xl overflow-hidden animate-in zoom-in-95">
            {/* Reader Header */}
            <div className="p-5 border-b border-border flex items-center justify-between bg-surface/50">
              <div>
                <QuovexBadge variant="emerald" size="sm">{readingBook.subject.toUpperCase()} • {readingBook.exam.toUpperCase()}</QuovexBadge>
                <h3 className="text-base font-bold text-text-primary mt-1">{readingBook.title}</h3>
              </div>
              <button
                onClick={() => setReadingBook(null)}
                className="w-8 h-8 rounded-full bg-surface-variant text-text-secondary hover:text-text-primary flex items-center justify-center transition-colors text-xs"
              >
                ✕
              </button>
            </div>

            {/* Chapter Navigation Bar */}
            <div className="flex items-center gap-2 px-5 py-3 border-b border-border bg-surface-variant overflow-x-auto no-scrollbar">
              {DEFAULT_ORIGINAL_CHAPTERS.map((ch, idx) => (
                <button
                  key={ch.id}
                  onClick={() => setActiveChapterIndex(idx)}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold whitespace-nowrap transition-all ${
                    activeChapterIndex === idx
                      ? 'bg-primary text-primary-foreground shadow-xs'
                      : 'text-text-secondary hover:text-text-primary'
                  }`}
                >
                  {ch.title.split(':')[0]}
                </button>
              ))}
            </div>

            {/* Chapter Content Body */}
            <div className="flex-1 overflow-y-auto p-5 sm:p-6 space-y-4 text-text-primary leading-relaxed bg-surface">
              <h4 className="text-base font-bold text-primary">
                {DEFAULT_ORIGINAL_CHAPTERS[activeChapterIndex].title}
              </h4>

              {/* Section 1: Intuitive Analogy */}
              <div className="p-4 rounded-xl bg-surface-variant border border-border space-y-1.5 shadow-xs">
                <span className="text-[11px] font-bold text-warning uppercase tracking-wider flex items-center gap-1.5">
                  <Lightbulb className="w-3.5 h-3.5" /> Intuitive Visual Analogy
                </span>
                <p className="text-xs sm:text-sm text-text-secondary leading-relaxed">
                  {DEFAULT_ORIGINAL_CHAPTERS[activeChapterIndex].analogy}
                </p>
              </div>

              {/* Section 2: Mathematical Derivation */}
              <div className="p-4 rounded-xl bg-surface-elevated border border-primary/30 space-y-2 shadow-xs">
                <span className="text-[11px] font-bold text-primary uppercase tracking-wider flex items-center gap-1.5">
                  <BookOpen className="w-3.5 h-3.5" /> Mathematical Proof
                </span>
                <div className="text-xs sm:text-sm">
                  <LatexRenderer content={DEFAULT_ORIGINAL_CHAPTERS[activeChapterIndex].derivation} />
                </div>
              </div>

              {/* Section 3: High-Yield Exam Takeaway */}
              <div className="p-4 rounded-xl bg-success-container border border-success/30 text-xs sm:text-sm text-success font-semibold flex items-start gap-2.5 shadow-xs">
                <CheckCircle2 className="w-4 h-4 shrink-0 mt-0.5" />
                <span>{DEFAULT_ORIGINAL_CHAPTERS[activeChapterIndex].takeaway}</span>
              </div>
            </div>

            {/* Reader Footer */}
            <div className="p-4 border-t border-border flex items-center justify-between bg-surface/50">
              <QuovexButton
                variant="secondary"
                size="sm"
                disabled={activeChapterIndex === 0}
                onClick={() => setActiveChapterIndex((prev) => Math.max(0, prev - 1))}
              >
                ← Previous
              </QuovexButton>
              <QuovexButton
                variant="primary"
                size="sm"
                disabled={activeChapterIndex === DEFAULT_ORIGINAL_CHAPTERS.length - 1}
                onClick={() => setActiveChapterIndex((prev) => Math.min(DEFAULT_ORIGINAL_CHAPTERS.length - 1, prev + 1))}
              >
                Next Chapter →
              </QuovexButton>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
