'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { BookOpen, Sparkles, ArrowLeft, Download, ExternalLink, Bot, CheckCircle2, ChevronRight } from 'lucide-react';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';

const NCERT_CLASSES = ['Class 12', 'Class 11', 'Class 10', 'Class 9', 'Class 8', 'Class 7', 'Class 6'];
const NCERT_SUBJECTS = ['Physics', 'Chemistry', 'Mathematics', 'Biology'];

const SAMPLE_CHAPTERS: Record<string, string[]> = {
  'Physics': [
    'Electric Charges and Fields',
    'Electrostatic Potential and Capacitance',
    'Current Electricity',
    'Moving Charges and Magnetism',
    'Magnetism and Matter',
    'Electromagnetic Induction',
    'Alternating Current',
    'Ray Optics and Optical Instruments',
    'Wave Optics',
    'Dual Nature of Radiation and Matter',
    'Atoms and Nuclei',
    'Semiconductor Electronics'
  ],
  'Chemistry': [
    'Solutions',
    'Electrochemistry',
    'Chemical Kinetics',
    'The d- and f-Block Elements',
    'Coordination Compounds',
    'Haloalkanes and Haloarenes',
    'Alcohols, Phenols and Ethers',
    'Aldehydes, Ketones and Carboxylic Acids',
    'Amines',
    'Biomolecules'
  ],
  'Mathematics': [
    'Relations and Functions',
    'Inverse Trigonometric Functions',
    'Matrices & Determinants',
    'Continuity and Differentiability',
    'Application of Derivatives',
    'Integrals & Differential Equations',
    'Vector Algebra and 3D Geometry',
    'Linear Programming',
    'Probability'
  ],
  'Biology': [
    'Sexual Reproduction in Flowering Plants',
    'Human Reproduction',
    'Principles of Inheritance and Variation',
    'Molecular Basis of Inheritance',
    'Human Health and Disease',
    'Biotechnology: Principles and Processes',
    'Organisms and Populations',
    'Ecosystem & Biodiversity'
  ]
};

export default function NcertExplorerPage() {
  const [selectedClass, setSelectedClass] = useState('Class 12');
  const [selectedSubject, setSelectedSubject] = useState('Physics');
  const [selectedChapter, setSelectedChapter] = useState<string | null>(null);
  const [summary, setSummary] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const chapters = SAMPLE_CHAPTERS[selectedSubject] || SAMPLE_CHAPTERS['Physics'];

  const handleGenerateSummary = (chapterTitle: string) => {
    setSelectedChapter(chapterTitle);
    setLoading(true);
    setSummary(null);

    setTimeout(() => {
      setSummary(`### High-Yield NCERT Exam Summary: ${chapterTitle}\n\n1. **Core Postulates & Definitions:**\n   This chapter formulates the foundational laws in ${selectedSubject}. All state variables must obey boundary constraints.\n\n2. **Governing Mathematical Equation:**\n   $$\\oint \\vec{E} \\cdot d\\vec{A} = \\frac{q_{\\text{enclosed}}}{\\varepsilon_0}$$\n   * Key invariant: Gauss's law applies strictly to closed surfaces (Gaussian surfaces).\n\n3. **Examination Trap Warning ⚠️:**\n   * Remember that electric field inside a charged conductor in static equilibrium is strictly zero ($\\vec{E} = 0$).\n   * Electric potential is continuous across the surface: $V_{\\text{inside}} = V_{\\text{surface}} = \\frac{1}{4\\pi\\varepsilon_0} \\frac{Q}{R}$.\n\n4. **Recommended Next Step:**\n   * Solve NCERT Exemplar problems #4, #7, and #12 for board and competitive mastery.`);
      setLoading(false);
    }, 600);
  };

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-12">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <Link href="/app/knowledge" className="inline-flex items-center gap-1.5 text-xs font-semibold text-text-secondary hover:text-text-primary mb-2 transition-colors">
            <ArrowLeft className="w-3.5 h-3.5" /> Back to Knowledge Hub
          </Link>
          <h1 className="text-2xl sm:text-3xl font-black text-text-primary flex items-center gap-2.5">
            <BookOpen className="w-7 h-7 text-primary" />
            Rationalised NCERT Dynamic Catalog
          </h1>
          <p className="text-xs text-text-secondary mt-1">
            Standard CBSE & state board curriculum with AI chapter breakdown and formula sheets.
          </p>
        </div>
      </div>

      {/* Class & Subject Selector */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex items-center gap-2 overflow-x-auto pb-2 sm:pb-0">
          {NCERT_CLASSES.map((c) => (
            <button
              key={c}
              onClick={() => setSelectedClass(c)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all ${
                selectedClass === c
                  ? 'bg-primary text-primary-foreground shadow-glow'
                  : 'bg-surface-variant text-text-secondary hover:text-text-primary border border-border'
              }`}
            >
              {c}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2 overflow-x-auto pb-2 sm:pb-0 sm:ml-auto">
          {NCERT_SUBJECTS.map((s) => (
            <button
              key={s}
              onClick={() => setSelectedSubject(s)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-bold shrink-0 transition-all ${
                selectedSubject === s
                  ? 'bg-primary-container text-primary border border-primary/40 shadow-sm'
                  : 'bg-surface text-text-secondary hover:text-text-primary border border-border'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Chapters List */}
        <div className="lg:col-span-1 space-y-2.5">
          <h2 className="text-sm font-bold text-text-primary mb-3">
            {selectedClass} • {selectedSubject} Chapters
          </h2>
          {chapters.map((ch, idx) => (
            <div
              key={ch}
              onClick={() => handleGenerateSummary(ch)}
              className={`p-3.5 rounded-2xl border transition-all cursor-pointer flex items-center justify-between text-xs ${
                selectedChapter === ch
                  ? 'bg-primary-container border-primary text-primary shadow-sm font-bold'
                  : 'bg-surface border-border text-text-primary hover:bg-surface-variant hover:border-primary/30'
              }`}
            >
              <div className="flex items-center gap-2.5">
                <span className="w-5 h-5 rounded-md bg-surface-variant text-text-secondary text-[10px] flex items-center justify-center font-mono font-bold">
                  {idx + 1}
                </span>
                <span>{ch}</span>
              </div>
              <ChevronRight className="w-4 h-4 text-text-secondary shrink-0" />
            </div>
          ))}
        </div>

        {/* AI Chapter Breakdown Reader */}
        <div className="lg:col-span-2">
          <QuovexCard className="p-6 space-y-4 min-h-[420px] flex flex-col justify-between">
            <div className="flex items-center justify-between border-b border-border pb-4">
              <div>
                <h3 className="font-bold text-text-primary text-base">
                  {selectedChapter || 'Select a Chapter to Begin'}
                </h3>
                <span className="text-xs text-text-secondary">
                  {selectedClass} • {selectedSubject}
                </span>
              </div>
              {selectedChapter && (
                <QuovexBadge variant="emerald">NCERT RATIONALISED</QuovexBadge>
              )}
            </div>

            <div className="flex-1 bg-surface-variant rounded-2xl p-5 border border-border text-xs sm:text-sm text-text-primary leading-relaxed overflow-y-auto">
              {loading ? (
                <div className="h-full flex flex-col items-center justify-center text-center text-text-secondary py-16">
                  <Bot className="w-8 h-8 text-primary animate-spin mb-3" />
                  <span>Synthesizing NCERT formulas, key definitions, and exam models...</span>
                </div>
              ) : summary ? (
                <LatexRenderer content={summary} />
              ) : (
                <div className="h-full flex flex-col items-center justify-center text-center text-text-secondary py-16">
                  <BookOpen className="w-8 h-8 text-primary/30 mb-2" />
                  <span>Click on any chapter on the left to generate its instant AI exam summary.</span>
                </div>
              )}
            </div>
          </QuovexCard>
        </div>
      </div>
    </div>
  );
}
