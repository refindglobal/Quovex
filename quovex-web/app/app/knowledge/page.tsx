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
  Search,
  CheckCircle2,
  Lock,
  Layers,
  ExternalLink,
  Lightbulb,
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
    <div className="max-w-6xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div>
          <h1 className="text-display font-black text-text-primary flex items-center gap-4">
            <Library className="w-10 h-10 text-primary" />
            Knowledge Hub
          </h1>
          <p className="text-section text-text-secondary mt-2">
            Rationalised NCERT textbooks, Quovex Originals with visual analogies, and your personal AI materials.
          </p>
        </div>

        {/* Action Links */}
        <div className="flex items-center gap-4">
          <Link href="/app/knowledge/ncert">
            <QuovexButton variant="secondary" size="lg" leftIcon={<BookOpen className="w-5 h-5" />}>
              NCERT (6–12)
            </QuovexButton>
          </Link>
          <Link href="/app/knowledge/notes">
            <QuovexButton variant="primary" size="lg" leftIcon={<FileText className="w-5 h-5" />}>
              My Materials
            </QuovexButton>
          </Link>
        </div>
      </div>

      {/* 3 Ecosystem Navigation Tabs */}
      <div className="flex items-center gap-4 border-b border-border pb-4">
        <button
          onClick={() => setTab('originals')}
          className={`px-5 py-2.5 rounded-xl text-body font-bold transition-all ${
            tab === 'originals'
              ? 'bg-primary-container text-primary border border-primary/40 shadow-sm'
              : 'text-text-secondary hover:text-text-primary hover:bg-surface-variant/50'
          }`}
        >
          ✨ Quovex Originals
        </button>
        <Link href="/app/knowledge/ncert">
          <button className="px-5 py-2.5 rounded-xl text-body font-semibold text-text-secondary hover:text-text-primary hover:bg-surface-variant/50">
            📚 NCERT Catalog (Class 6-12)
          </button>
        </Link>
        <Link href="/app/knowledge/notes">
          <button className="px-5 py-2.5 rounded-xl text-body font-semibold text-text-secondary hover:text-text-primary hover:bg-surface-variant/50">
            📝 My Materials & PDFs
          </button>
        </Link>
      </div>

      {/* Quovex Originals Gallery */}
      <div className="space-y-8">
        <div className="flex items-center justify-between">
          <h2 className="text-headline font-bold text-text-primary">Published Quovex Originals</h2>
          <QuovexBadge variant="gold" size="lg">PRO VIP ACCESS</QuovexBadge>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Book 1: Rotational Mechanics */}
          <QuovexCard
            hoverEffect
            className="space-y-4 cursor-pointer"
            onClick={() => {
              setReadingBook({
                title: 'Mastering Rotational Mechanics & Angular Invariants',
                subject: 'Physics',
                exam: 'JEE Advanced',
              });
              setActiveChapterIndex(0);
            }}
          >
            <div className="h-48 rounded-2xl bg-gradient-to-br from-primary-container to-surface-variant border border-primary/30 flex flex-col justify-end p-5 relative overflow-hidden">
              <div className="w-20 h-20 absolute top-2 right-2 opacity-30">
                <Image
                  src={ASSETS.icons3d.physicsOrbit}
                  alt="Physics"
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
              <QuovexBadge variant="emerald" size="sm">PHYSICS • JEE ADVANCED</QuovexBadge>
              <h3 className="font-bold text-text-primary text-title mt-3">Mastering Rotational Mechanics</h3>
            </div>
            <p className="text-body text-text-secondary leading-relaxed">
              Step-by-step moment of inertia derivations, torque tensors, and visual analogies for rolling motion.
            </p>
            <div className="pt-2 flex items-center justify-between text-body text-primary font-bold">
              <span>12 Interactive Chapters</span>
              <ArrowRight className="w-5 h-5" />
            </div>
          </QuovexCard>

          {/* Book 2: Organic Chemistry */}
          <QuovexCard
            hoverEffect
            className="space-y-4 cursor-pointer"
            onClick={() => {
              setReadingBook({
                title: 'Organic Reaction Mechanisms & High-Yield Pathways',
                subject: 'Chemistry',
                exam: 'NEET UG',
              });
              setActiveChapterIndex(0);
            }}
          >
            <div className="h-48 rounded-2xl bg-gradient-to-br from-warning-container to-surface-variant border border-warning/30 flex flex-col justify-end p-5 relative overflow-hidden">
              <div className="w-20 h-20 absolute top-2 right-2 opacity-30">
                <Image
                  src={ASSETS.icons3d.chemBenzene}
                  alt="Chemistry"
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
              <QuovexBadge variant="gold" size="sm">CHEMISTRY • NEET UG</QuovexBadge>
              <h3 className="font-bold text-text-primary text-title mt-3">Organic Reaction Mechanisms</h3>
            </div>
            <p className="text-body text-text-secondary leading-relaxed">
              Electrophilic additions, SN1 vs SN2 visual maps, and common student misconception traps decoded.
            </p>
            <div className="pt-2 flex items-center justify-between text-body text-warning font-bold">
              <span>16 Interactive Chapters</span>
              <ArrowRight className="w-5 h-5" />
            </div>
          </QuovexCard>

          {/* Book 3: Calculus */}
          <QuovexCard
            hoverEffect
            className="space-y-4 cursor-pointer"
            onClick={() => {
              setReadingBook({
                title: 'Calculus & Definite Integrals In-Depth',
                subject: 'Mathematics',
                exam: 'JEE Main',
              });
              setActiveChapterIndex(0);
            }}
          >
            <div className="h-48 rounded-2xl bg-gradient-to-br from-surface-variant to-surface-elevated border border-border flex flex-col justify-end p-5 relative overflow-hidden">
              <div className="w-20 h-20 absolute top-2 right-2 opacity-30">
                <Image
                  src={ASSETS.icons3d.mathMobius}
                  alt="Mathematics"
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
              <QuovexBadge variant="muted" size="sm">MATHEMATICS • JEE MAIN</QuovexBadge>
              <h3 className="font-bold text-text-primary text-title mt-3">Calculus & Area Under Curves</h3>
            </div>
            <p className="text-body text-text-secondary leading-relaxed">
              Definite integral tricks, Leibniz rule derivations, and past 10-year question walkthroughs.
            </p>
            <div className="pt-2 flex items-center justify-between text-body text-text-secondary font-bold">
              <span>14 Interactive Chapters</span>
              <ArrowRight className="w-5 h-5" />
            </div>
          </QuovexCard>
        </div>
      </div>

      {/* ── Interactive Chapter Reader Modal ──────────────────────────────── */}
      {readingBook && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-3xl max-w-4xl w-full max-h-[90vh] flex flex-col shadow-2xl overflow-hidden animate-in zoom-in-95">
            {/* Reader Header */}
            <div className="p-8 border-b border-border flex items-center justify-between bg-surface/50">
              <div>
                <QuovexBadge variant="emerald" size="md">{readingBook.subject.toUpperCase()} • {readingBook.exam.toUpperCase()}</QuovexBadge>
                <h3 className="text-display font-black text-text-primary mt-3">{readingBook.title}</h3>
              </div>
              <button
                onClick={() => setReadingBook(null)}
                className="w-10 h-10 rounded-full bg-surface-variant text-text-secondary hover:text-text-primary flex items-center justify-center transition-colors"
              >
                ✕
              </button>
            </div>

            {/* Chapter Navigation Bar */}
            <div className="flex items-center gap-3 px-8 py-4 border-b border-border bg-surface-variant overflow-x-auto no-scrollbar">
              {DEFAULT_ORIGINAL_CHAPTERS.map((ch, idx) => (
                <button
                  key={ch.id}
                  onClick={() => setActiveChapterIndex(idx)}
                  className={`px-4 py-2 rounded-xl text-body font-bold whitespace-nowrap transition-all ${
                    activeChapterIndex === idx
                      ? 'bg-primary text-primary-foreground shadow-glow-sm'
                      : 'text-text-secondary hover:text-text-primary'
                  }`}
                >
                  {ch.title.split(':')[0]}
                </button>
              ))}
            </div>

            {/* Chapter Content Body */}
            <div className="flex-1 overflow-y-auto p-8 space-y-8 text-text-primary leading-relaxed bg-surface">
              <div>
                <h4 className="text-display font-extrabold text-primary">
                  {DEFAULT_ORIGINAL_CHAPTERS[activeChapterIndex].title}
                </h4>
              </div>

              {/* Section 1: Intuitive Analogy */}
              <div className="p-6 rounded-2xl bg-surface-variant border border-border space-y-3 shadow-sm">
                <span className="text-label font-black text-warning uppercase tracking-wider flex items-center gap-2">
                  <Lightbulb className="w-4 h-4" /> Intuitive Visual Analogy
                </span>
                <p className="text-body sm:text-title text-text-secondary leading-relaxed">
                  {DEFAULT_ORIGINAL_CHAPTERS[activeChapterIndex].analogy}
                </p>
              </div>

              {/* Section 2: Mathematical Derivation */}
              <div className="p-6 rounded-2xl bg-surface-elevated border border-primary/30 space-y-4 shadow-sm">
                <span className="text-label font-black text-primary uppercase tracking-wider flex items-center gap-2">
                  <BookOpen className="w-4 h-4" /> Mathematical Proof & Analytical Derivation
                </span>
                <div className="text-body">
                  <LatexRenderer content={DEFAULT_ORIGINAL_CHAPTERS[activeChapterIndex].derivation} />
                </div>
              </div>

              {/* Section 3: High-Yield Exam Takeaway */}
              <div className="p-6 rounded-2xl bg-success-container border border-success/30 text-body text-success font-semibold flex items-start gap-3 shadow-sm">
                <CheckCircle2 className="w-5 h-5 shrink-0 mt-0.5" />
                <span>{DEFAULT_ORIGINAL_CHAPTERS[activeChapterIndex].takeaway}</span>
              </div>
            </div>

            {/* Reader Footer */}
            <div className="p-6 border-t border-border flex items-center justify-between bg-surface/50">
              <QuovexButton
                variant="secondary"
                size="lg"
                disabled={activeChapterIndex === 0}
                onClick={() => setActiveChapterIndex((prev) => Math.max(0, prev - 1))}
              >
                ← Previous Chapter
              </QuovexButton>
              <QuovexButton
                variant="primary"
                size="lg"
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
