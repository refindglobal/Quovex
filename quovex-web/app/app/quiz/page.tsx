'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import {
  HelpCircle,
  CheckCircle2,
  XCircle,
  ArrowRight,
  Sparkles,
  Award,
  RotateCcw,
  BookOpen,
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { getCurrentUser } from '@/lib/firebase/auth';
import {
  saveFlashcardDeck,
  saveFlashcard,
  saveQuizResult,
  subscribeToUserProfile,
  FlashcardDeck,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import { ASSETS } from '@/lib/assets';

interface Question {
  id: string;
  subject: string;
  concept: string;
  question: string;
  options: string[];
  correctIndex: number;
  explanation: string;
}

const SAMPLE_QUIZ: Question[] = [
  {
    id: 'q1',
    subject: 'Physics',
    concept: 'Thermodynamics & Carnot Cycle',
    question: 'A Carnot engine operating between source $T_1$ and sink $T_2$ has an efficiency $\\eta = 0.4$. If the sink temperature $T_2 = 300\\text{ K}$, calculate the source temperature $T_1$ in Kelvin:',
    options: ['450 K', '500 K', '600 K', '750 K'],
    correctIndex: 1,
    explanation: 'Using the Carnot efficiency relation: $$\\eta = 1 - \\frac{T_2}{T_1} \\implies 0.4 = 1 - \\frac{300}{T_1} \\implies \\frac{300}{T_1} = 0.6 \\implies T_1 = 500\\text{ K}$$',
  },
  {
    id: 'q2',
    subject: 'Chemistry',
    concept: 'Organic Carbonyl Reactions',
    question: 'Which of the following organic functional structures produces a positive yellow precipitate in the **Iodoform Reaction** with $\\text{I}_2 / \\text{NaOH}$?',
    options: ['Methanol ($\\text{CH}_3\\text{OH}$)', 'Propan-2-ol ($\\text{CH}_3\\text{CH(OH)CH}_3$)', 'Benzophenone', 'Pentanal'],
    correctIndex: 1,
    explanation: 'The Iodoform reaction specifically targets methyl carbonyl ($\\text{CH}_3\\text{C}=\\text{O}$) or methyl carbinol ($\\text{CH}_3\\text{CH(OH)}-$). Propan-2-ol oxidizes to acetone in situ, yielding positive $\\text{CHI}_3$.',
  },
  {
    id: 'q3',
    subject: 'Mathematics',
    concept: 'Definite Integrals & King\'s Rule',
    question: 'Evaluate the definite integral: $$I = \\int_{0}^{\\pi/2} \\frac{\\sqrt{\\sin x}}{\\sqrt{\\sin x} + \\sqrt{\\cos x}} \\, dx$$',
    options: ['0', '1', '$$\\pi / 4$$', '$$\\pi / 2$$'],
    correctIndex: 2,
    explanation: 'Applying King\'s property $\\int_a^b f(x)dx = \\int_a^b f(a+b-x)dx$, adding the original and transformed integrals gives: $$2I = \\int_0^{\\pi/2} 1 \\, dx = \\frac{\\pi}{2} \\implies I = \\frac{\\pi}{4}$$',
  },
  {
    id: 'q4',
    subject: 'Physics',
    concept: 'Kinetic Theory of Gases',
    question: 'For an ideal gas at temperature $T$, what is the exact analytical ratio of root-mean-square speed ($v_{\\text{rms}}$) to average molecular speed ($v_{\\text{avg}}$)?',
    options: ['$$\\sqrt{\\frac{3\\pi}{8}}$$', '$$\\sqrt{\\frac{8}{3\\pi}}$$', '$$\\sqrt{2}$$', '$$\\frac{3}{2}$$'],
    correctIndex: 0,
    explanation: '$$v_{\\text{rms}} = \\sqrt{\\frac{3RT}{M}}, \\quad v_{\\text{avg}} = \\sqrt{\\frac{8RT}{\\pi M}} \\implies \\frac{v_{\\text{rms}}}{v_{\\text{avg}}} = \\sqrt{\\frac{3\\pi}{8}} \\approx 1.086$$',
  },
  {
    id: 'q5',
    subject: 'Biology',
    concept: 'Mendelian Genetics & Codominance',
    question: 'In human ABO blood group inheritance, which phenotype represents non-recessive **Codominance** where both alleles are simultaneously and fully expressed?',
    options: ['Blood Group O', 'Blood Group A', 'Blood Group B', 'Blood Group AB'],
    correctIndex: 3,
    explanation: 'In Blood Group AB, both the $I^A$ and $I^B$ glycosyltransferase alleles are expressed equally on the erythrocyte membrane.',
  },
];

export default function DiagnosticQuizPage() {
  const [questions] = useState<Question[]>(SAMPLE_QUIZ);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const [isAnswered, setIsAnswered] = useState(false);
  const [score, setScore] = useState(0);
  const [mistakes, setMistakes] = useState<Array<{ q: Question; selected: number }>>([]);
  const [isFinished, setIsFinished] = useState(false);
  const [isGeneratingRemedial, setIsGeneratingRemedial] = useState(false);
  const [remedialGenerated, setRemedialGenerated] = useState(false);

  const currentUser = getCurrentUser();
  const currentQ = questions[currentIndex];

  const handleSelectOption = (index: number) => {
    if (isAnswered) return;
    setSelectedOption(index);
    setIsAnswered(true);

    if (index === currentQ.correctIndex) {
      setScore((s) => s + 1);
    } else {
      setMistakes((prev) => [...prev, { q: currentQ, selected: index }]);
    }
  };

  const handleNext = () => {
    if (currentIndex < questions.length - 1) {
      setCurrentIndex((prev) => prev + 1);
      setSelectedOption(null);
      setIsAnswered(false);
    } else {
      handleCompleteQuiz();
    }
  };

  const handleCompleteQuiz = async () => {
    setIsFinished(true);
    if (score === questions.length) {
      confetti({ particleCount: 120, spread: 90, origin: { y: 0.6 } });
    }

    if (currentUser) {
      // Save quiz history record
      await saveQuizResult(currentUser.uid, {
        id: `quiz_${Date.now()}`,
        subject: 'Multi-Disciplinary Diagnostic',
        targetExam: 'JEE / NEET',
        score,
        totalQuestions: questions.length,
        correctCount: score,
        timestamp: Date.now(),
        mistakes: mistakes.map((m) => ({
          questionText: m.q.question,
          studentAnswer: m.q.options[m.selected] || '',
          correctAnswer: m.q.options[m.q.correctIndex] || '',
          explanation: m.q.explanation,
          concept: m.q.concept,
        })),
      });
    }
  };

  const handleGenerateRemedialDeck = async () => {
    if (!currentUser || mistakes.length === 0 || isGeneratingRemedial) return;

    setIsGeneratingRemedial(true);
    try {
      const remedialDeckId = 'remedial_traps';
      
      const deck: FlashcardDeck = {
        id: remedialDeckId,
        title: '🎯 Remedial Concepts & Traps',
        subject: 'Remedial',
        cardCount: mistakes.length,
        masteryPercentage: 0,
        lastStudiedAt: Date.now(),
      };

      await saveFlashcardDeck(currentUser.uid, deck);

      // Create SM-2 remedial cards with easeFactor 2.1
      for (let i = 0; i < mistakes.length; i++) {
        const m = mistakes[i];
        await saveFlashcard(currentUser.uid, remedialDeckId, {
          id: `card_rem_${Date.now()}_${i}`,
          deckId: remedialDeckId,
          frontContent: `⚠️ Diagnostic Trap: ${m.q.concept}\n\n${m.q.question}`,
          backContent: `**Correct Solution:**\n${m.q.options[m.q.correctIndex]}\n\n**Diagnostic Explanation:**\n${m.q.explanation}`,
          repetitions: 0,
          intervalDays: 1,
          easeFactor: 2.1,
          nextReviewDate: Date.now(),
          isRemedial: true,
          concept: m.q.concept,
        });
      }

      setRemedialGenerated(true);
    } catch (err: any) {
      alert('Failed to synthesize remedial deck: ' + err.message);
    } finally {
      setIsGeneratingRemedial(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div>
        <h1 className="text-display font-black text-text-primary flex items-center gap-4">
          <HelpCircle className="w-10 h-10 text-primary" />
          Daily Diagnostic MCQ Quiz
        </h1>
        <p className="text-section text-text-secondary mt-2">
          5 adaptive multi-disciplinary questions with step-by-step misconception diagnostics.
        </p>
      </div>

      {!isFinished ? (
        <QuovexCard className="p-8 sm:p-12 space-y-8 shadow-sm">
          <div className="flex items-center justify-between border-b border-border pb-5">
            <QuovexBadge variant="emerald" size="md">{currentQ.subject}</QuovexBadge>
            <span className="text-body font-mono text-text-secondary font-bold">
              Question {currentIndex + 1} of {questions.length}
            </span>
          </div>

          <div className="text-headline font-semibold text-text-primary leading-relaxed">
            <LatexRenderer content={currentQ.question} />
          </div>

          {/* Options */}
          <div className="space-y-4 pt-4">
            {currentQ.options.map((opt, idx) => {
              let optionStyle = 'bg-surface-variant border-border text-text-primary hover:bg-surface-elevated';

              if (isAnswered) {
                if (idx === currentQ.correctIndex) {
                  optionStyle = 'bg-success-container border-success text-success font-bold shadow-sm';
                } else if (idx === selectedOption) {
                  optionStyle = 'bg-error-container border-error text-error font-bold shadow-sm';
                }
              }

              return (
                <div
                  key={idx}
                  onClick={() => handleSelectOption(idx)}
                  className={`p-5 rounded-2xl border transition-all cursor-pointer flex items-center justify-between text-body ${optionStyle}`}
                >
                  <div className="flex items-center gap-4">
                    <span className="w-8 h-8 rounded-xl bg-surface flex items-center justify-center text-body font-mono font-bold text-text-secondary shadow-sm">
                      {String.fromCharCode(65 + idx)}
                    </span>
                    <LatexRenderer content={opt} />
                  </div>

                  {isAnswered && idx === currentQ.correctIndex && (
                    <CheckCircle2 className="w-5 h-5 text-success shrink-0" />
                  )}
                  {isAnswered && idx === selectedOption && idx !== currentQ.correctIndex && (
                    <XCircle className="w-5 h-5 text-error shrink-0" />
                  )}
                </div>
              );
            })}
          </div>

          {/* Explanation Banner */}
          {isAnswered && (
            <div className="p-6 rounded-2xl bg-surface-variant border border-border space-y-3 animate-in fade-in shadow-sm mt-4">
              <span className={`text-label font-black uppercase tracking-wider block flex items-center gap-2 ${selectedOption === currentQ.correctIndex ? 'text-success' : 'text-error'}`}>
                {selectedOption === currentQ.correctIndex ? <><CheckCircle2 className="w-4 h-4" /> Correct Derivation</> : <><XCircle className="w-4 h-4" /> Misconception Breakdown</>}
              </span>
              <div className="text-body text-text-secondary leading-relaxed">
                <LatexRenderer content={currentQ.explanation} />
              </div>
            </div>
          )}

          {isAnswered && (
            <div className="pt-4 flex justify-end">
              <QuovexButton size="lg" onClick={handleNext} rightIcon={<ArrowRight className="w-5 h-5" />}>
                {currentIndex < questions.length - 1 ? 'Next Question' : 'View Quiz Summary'}
              </QuovexButton>
            </div>
          )}
        </QuovexCard>
      ) : (
        /* Quiz Complete Screen */
        <QuovexCard className="p-10 sm:p-16 text-center space-y-8 shadow-glow-sm">
          <div className="w-20 h-20 rounded-2xl bg-primary-container text-primary flex items-center justify-center mx-auto shadow-glow">
            <Award className="w-10 h-10" />
          </div>

          <div>
            <h2 className="text-display font-bold text-text-primary">Diagnostic Quiz Complete!</h2>
            <div className="text-[3rem] font-black text-primary my-4 tracking-tight">
              {score} / {questions.length}
            </div>
            <p className="text-body font-bold text-text-secondary">
              Accuracy: {Math.round((score / questions.length) * 100)}% • +40 Scholar XP Awarded
            </p>
          </div>

          {/* Remedial Flashcard Synthesis Block */}
          {mistakes.length > 0 && (
            <div className="p-6 rounded-2xl bg-surface-variant border border-border text-body text-text-primary space-y-4 text-left shadow-sm">
              <div className="font-bold text-warning flex items-center gap-3">
                <Sparkles className="w-5 h-5" />
                <span>Identified {mistakes.length} High-Yield Exam Misconception{mistakes.length === 1 ? '' : 's'}</span>
              </div>
              <p className="text-text-secondary leading-relaxed">
                Synthesize custom SM-2 flashcards targeting these exact questions to permanently close your concept gaps.
              </p>

              {!remedialGenerated ? (
                <QuovexButton
                  size="lg"
                  variant="primary"
                  className="w-full sm:w-auto mt-2"
                  onClick={handleGenerateRemedialDeck}
                  isLoading={isGeneratingRemedial}
                  leftIcon={<Sparkles className="w-4 h-4" />}
                >
                  Synthesize "🎯 Remedial Concepts & Traps" Deck
                </QuovexButton>
              ) : (
                <div className="space-y-3 pt-2">
                  <div className="p-4 rounded-xl bg-success-container text-success font-bold text-body flex items-center gap-3 shadow-sm">
                    <CheckCircle2 className="w-5 h-5 shrink-0" />
                    <span>Deck Synthesized! {mistakes.length} remedial flashcards added to SM-2 queue.</span>
                  </div>
                  <Link href="/app/flashcards/remedial_traps">
                    <QuovexButton size="lg" variant="primary" className="w-full mt-3">
                      Review Remedial Deck Now →
                    </QuovexButton>
                  </Link>
                </div>
              )}
            </div>
          )}

          <div className="pt-4 flex justify-center gap-4">
            <Link href="/app/dashboard">
              <QuovexButton variant="secondary" size="lg">Return to Hub</QuovexButton>
            </Link>
          </div>
        </QuovexCard>
      )}
    </div>
  );
}
