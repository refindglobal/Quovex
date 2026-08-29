'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
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
  FlashcardDeck,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';

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
    <div className="max-w-3xl mx-auto space-y-6 pb-20">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
              <HelpCircle className="w-7 h-7 text-primary" />
              Daily Diagnostic MCQ Quiz
            </h1>
            <QuovexBadge variant="fire" size="sm">Adaptive</QuovexBadge>
          </div>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            5 multi-disciplinary questions targeting exam traps & misconception remedies.
          </p>
        </div>
      </div>

      {!isFinished ? (
        <QuovexCard className="p-5 sm:p-8 space-y-6 shadow-sm">
          {/* Progress Header */}
          <div className="flex items-center justify-between border-b border-border pb-3">
            <QuovexBadge variant="emerald" size="md">{currentQ.subject}</QuovexBadge>
            <span className="text-xs sm:text-sm font-mono text-text-secondary font-bold">
              Question {currentIndex + 1} of {questions.length}
            </span>
          </div>

          {/* Question Text with Math */}
          <div className="text-sm sm:text-base font-semibold text-text-primary leading-relaxed">
            <LatexRenderer content={currentQ.question} />
          </div>

          {/* Options */}
          <div className="space-y-3 pt-2">
            {currentQ.options.map((opt, idx) => {
              let optionStyle = 'bg-surface-variant/80 border-border text-text-primary hover:bg-surface-elevated';

              if (isAnswered) {
                if (idx === currentQ.correctIndex) {
                  optionStyle = 'bg-success-container text-success border-success font-bold shadow-xs';
                } else if (idx === selectedOption) {
                  optionStyle = 'bg-error-container text-error border-error font-bold shadow-xs';
                }
              }

              return (
                <div
                  key={idx}
                  onClick={() => handleSelectOption(idx)}
                  className={`p-3.5 sm:p-4 rounded-xl border transition-all cursor-pointer flex items-center justify-between gap-3 text-xs sm:text-sm ${optionStyle}`}
                >
                  <div className="flex items-center gap-3 flex-1">
                    <span className="w-6 h-6 rounded-lg bg-surface flex items-center justify-center text-xs font-mono font-bold text-text-secondary shadow-xs shrink-0">
                      {String.fromCharCode(65 + idx)}
                    </span>
                    <LatexRenderer content={opt} className="flex-1" />
                  </div>

                  {isAnswered && idx === currentQ.correctIndex && (
                    <CheckCircle2 className="w-4 h-4 text-success shrink-0" />
                  )}
                  {isAnswered && idx === selectedOption && idx !== currentQ.correctIndex && (
                    <XCircle className="w-4 h-4 text-error shrink-0" />
                  )}
                </div>
              );
            })}
          </div>

          {/* Explanation Banner */}
          {isAnswered && (
            <div className="p-4 rounded-xl bg-surface-variant border border-border space-y-2 animate-in fade-in shadow-xs">
              <span className={`text-xs font-bold uppercase tracking-wider flex items-center gap-1.5 ${selectedOption === currentQ.correctIndex ? 'text-success' : 'text-error'}`}>
                {selectedOption === currentQ.correctIndex ? <><CheckCircle2 className="w-3.5 h-3.5" /> Correct Derivation</> : <><XCircle className="w-3.5 h-3.5" /> Misconception Analysis</>}
              </span>
              <div className="text-xs sm:text-sm text-text-secondary leading-relaxed">
                <LatexRenderer content={currentQ.explanation} />
              </div>
            </div>
          )}

          {isAnswered && (
            <div className="pt-2 flex justify-end">
              <QuovexButton size="md" onClick={handleNext} rightIcon={<ArrowRight className="w-4 h-4" />}>
                {currentIndex < questions.length - 1 ? 'Next Question' : 'View Summary'}
              </QuovexButton>
            </div>
          )}
        </QuovexCard>
      ) : (
        /* Quiz Complete Screen */
        <QuovexCard className="p-8 sm:p-12 text-center space-y-6 shadow-sm">
          <div className="w-16 h-16 rounded-2xl bg-primary-container text-primary flex items-center justify-center mx-auto shadow-sm">
            <Award className="w-8 h-8" />
          </div>

          <div>
            <h2 className="text-xl sm:text-2xl font-black text-text-primary">Diagnostic Quiz Complete!</h2>
            <div className="text-4xl font-black text-primary my-3 tracking-tight">
              {score} / {questions.length}
            </div>
            <p className="text-xs sm:text-sm text-text-secondary font-medium">
              Accuracy: {Math.round((score / questions.length) * 100)}% • +40 Scholar XP Awarded
            </p>
          </div>

          {/* Remedial Flashcard Synthesis Block */}
          {mistakes.length > 0 && (
            <div className="p-5 rounded-2xl bg-surface-variant border border-border text-xs sm:text-sm text-text-primary space-y-3 text-left shadow-xs">
              <div className="font-bold text-warning flex items-center gap-2">
                <Sparkles className="w-4 h-4" />
                <span>Identified {mistakes.length} High-Yield Exam Misconception{mistakes.length === 1 ? '' : 's'}</span>
              </div>
              <p className="text-text-secondary leading-relaxed">
                Synthesize custom SM-2 flashcards targeting these questions to close your concept gaps.
              </p>

              {!remedialGenerated ? (
                <QuovexButton
                  size="md"
                  variant="primary"
                  className="w-full sm:w-auto mt-1"
                  onClick={handleGenerateRemedialDeck}
                  isLoading={isGeneratingRemedial}
                  leftIcon={<Sparkles className="w-4 h-4" />}
                >
                  Synthesize Remedial Flashcard Deck
                </QuovexButton>
              ) : (
                <div className="space-y-3 pt-1">
                  <div className="p-3 rounded-xl bg-success-container text-success font-bold text-xs flex items-center gap-2 shadow-xs">
                    <CheckCircle2 className="w-4 h-4 shrink-0" />
                    <span>Deck Synthesized! {mistakes.length} cards added to SM-2 spaced repetition queue.</span>
                  </div>
                  <Link href="/app/flashcards/remedial_traps">
                    <QuovexButton size="md" variant="primary" className="w-full">
                      Review Remedial Deck Now →
                    </QuovexButton>
                  </Link>
                </div>
              )}
            </div>
          )}

          <div className="pt-2 flex justify-center gap-3">
            <Link href="/app/dashboard">
              <QuovexButton variant="secondary" size="md">Return to Hub</QuovexButton>
            </Link>
          </div>
        </QuovexCard>
      )}
    </div>
  );
}
