'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { signInWithGoogle, subscribeToAuthChanges } from '@/lib/firebase/auth';
import { doc, getDoc, setDoc } from 'firebase/firestore';
import { db } from '@/lib/firebase/config';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { QuovexInput } from '@/components/ui/QuovexInput';
import { ShieldCheck, CheckCircle2, ArrowRight } from 'lucide-react';

const EXAM_OPTIONS = [
  'JEE Advanced',
  'JEE Main',
  'NEET UG',
  'CBSE Class 12',
  'CBSE Class 10',
  'UPSC CSE',
  'SAT / ACT',
  'MCAT',
  'College / University',
  'Other Exam',
];

export default function AuthPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Onboarding wizard state
  const [step, setStep] = useState<'auth' | 'onboarding'>('auth');
  const [userId, setUserId] = useState<string | null>(null);
  const [userName, setUserName] = useState('');
  const [selectedExam, setSelectedExam] = useState('JEE Advanced');
  const [dailyHours, setDailyHours] = useState(4);

  useEffect(() => {
    const unsubscribe = subscribeToAuthChanges(async (user) => {
      if (user) {
        setUserId(user.uid);
        setUserName(user.displayName || 'Scholar');

        // Check if user is already onboarded in Firestore
        const userDoc = await getDoc(doc(db, 'users', user.uid));
        if (userDoc.exists() && userDoc.data().isOnboarded) {
          router.push('/app/dashboard');
        } else if (userDoc.exists()) {
          setStep('onboarding');
        }
      }
    });

    return () => unsubscribe();
  }, [router]);

  const handleGoogleSignIn = async () => {
    setLoading(true);
    setError(null);
    try {
      const user = await signInWithGoogle();
      setUserId(user.uid);
      setUserName(user.displayName || 'Scholar');

      const userDoc = await getDoc(doc(db, 'users', user.uid));
      if (userDoc.exists() && userDoc.data().isOnboarded) {
        router.push('/app/dashboard');
      } else {
        setStep('onboarding');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to sign in with Google');
    } finally {
      setLoading(false);
    }
  };

  const handleCompleteOnboarding = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      await setDoc(
        doc(db, 'users', userId),
        {
          name: userName,
          targetExam: selectedExam,
          dailyGoalHours: dailyHours,
          isOnboarded: true,
          updatedAt: Date.now(),
        },
        { merge: true }
      );
      router.push('/app/dashboard');
    } catch (err: any) {
      setError(err.message || 'Failed to save profile');
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center p-6 relative overflow-hidden transition-colors duration-200">
      {/* Background glow */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[500px] h-[500px] bg-primary-glow rounded-full blur-[120px] pointer-events-none" />

      <div className="w-full max-w-md relative z-10">
        {step === 'auth' ? (
          <QuovexCard className="p-8 text-center space-y-6">
            <div className="w-14 h-14 rounded-2xl overflow-hidden bg-primary/10 border border-primary/30 flex items-center justify-center p-1 mx-auto shadow-glow">
              <img
                src="/assets/brand/emblem.png"
                alt="Quovex Logo"
                className="w-10 h-10 object-contain"
              />
            </div>

            <div>
              <h1 className="text-headline text-text-primary">
                Welcome to Quovex
              </h1>
              <p className="text-body text-text-secondary mt-1.5">
                Sign in to sync your focus sessions, flashcards, and notes.
              </p>
            </div>

            {error && (
              <div className="p-3 rounded-xl bg-error-container border border-error/30 text-caption text-error text-left">
                {error}
              </div>
            )}

            <div className="pt-2">
              <button
                onClick={handleGoogleSignIn}
                disabled={loading}
                className="w-full py-3.5 px-4 rounded-xl bg-white hover:bg-gray-100 text-gray-900 font-semibold text-body flex items-center justify-center gap-3 transition-all duration-200 shadow-md active:scale-[0.98] disabled:opacity-50"
              >
                <svg className="w-5 h-5" viewBox="0 0 24 24">
                  <path
                    fill="#4285F4"
                    d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                  />
                  <path
                    fill="#34A853"
                    d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                  />
                  <path
                    fill="#FBBC05"
                    d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
                  />
                  <path
                    fill="#EA4335"
                    d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
                  />
                </svg>
                <span>Continue with Google</span>
              </button>
            </div>

            <div className="pt-4 border-t border-border/50 space-y-2 text-left">
              <div className="flex items-center gap-2 text-caption text-text-secondary">
                <ShieldCheck className="w-4 h-4 text-primary shrink-0" />
                <span>Zero Guest Mode • Unified Single Firebase Identity</span>
              </div>
              <div className="flex items-center gap-2 text-caption text-text-secondary">
                <CheckCircle2 className="w-4 h-4 text-primary shrink-0" />
                <span>Same account works seamlessly on Android and Web</span>
              </div>
            </div>
          </QuovexCard>
        ) : (
          <QuovexCard className="p-8 space-y-6">
            <div className="text-center">
              <QuovexBadge variant="emerald">STEP 1 OF 1</QuovexBadge>
              <h2 className="text-title text-text-primary mt-3">Set Up Your Study Profile</h2>
              <p className="text-body text-text-secondary mt-1">
                Personalize your AI study plan and diagnostic quizzes.
              </p>
            </div>

            <div className="space-y-4">
              <QuovexInput
                label="Your Full Name"
                type="text"
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
              />

              <div className="w-full">
                <label className="block text-label text-text-primary mb-1.5">
                  Target Exam / Goal
                </label>
                <select
                  value={selectedExam}
                  onChange={(e) => setSelectedExam(e.target.value)}
                  className="w-full bg-surface-elevated border border-border rounded-xl px-4 py-3 text-body text-text-primary focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/50 transition-all duration-200"
                >
                  {EXAM_OPTIONS.map((exam) => (
                    <option key={exam} value={exam}>
                      {exam}
                    </option>
                  ))}
                </select>
              </div>

              <div className="w-full">
                <label className="block text-label text-text-primary mb-1.5">
                  Daily Study Goal: <span className="text-primary font-bold">{dailyHours} Hours</span>
                </label>
                <input
                  type="range"
                  min="1"
                  max="12"
                  step="0.5"
                  value={dailyHours}
                  onChange={(e) => setDailyHours(parseFloat(e.target.value))}
                  className="w-full accent-primary"
                />
              </div>
            </div>

            <QuovexButton
              size="lg"
              className="w-full"
              isLoading={loading}
              onClick={handleCompleteOnboarding}
              rightIcon={<ArrowRight className="w-5 h-5" />}
            >
              Enter Quovex Command Center
            </QuovexButton>
          </QuovexCard>
        )}
      </div>
    </div>
  );
}
