'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import {
  Timer,
  Bot,
  BrainCircuit,
  Sparkles,
  Flame,
  ArrowRight,
  Clock,
  BookOpen,
  HelpCircle,
  Zap,
  CheckCircle2,
  AlertCircle,
  Compass,
} from 'lucide-react';
import {
  subscribeToUserProfile,
  subscribeToUserSessions,
  subscribeToFlashcardDecks,
  subscribeToStudyPlan,
  StudySession,
  FlashcardDeck,
  StudyPlan,
} from '@/lib/firebase/firestore';
import { getCurrentUser, UserProfile } from '@/lib/firebase/auth';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { QuovexEmptyState } from '@/components/ui/QuovexEmptyState';
import { GoalProgressRing } from '@/components/dashboard/GoalProgressRing';
import { ExamCountdownCard } from '@/components/dashboard/ExamCountdownCard';
import { WeeklyConsistencyStrip } from '@/components/dashboard/WeeklyConsistencyStrip';
import { ASSETS } from '@/lib/assets';

export default function DashboardPage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [sessions, setSessions] = useState<StudySession[]>([]);
  const [decks, setDecks] = useState<FlashcardDeck[]>([]);
  const [studyPlan, setStudyPlan] = useState<StudyPlan | null>(null);

  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) return;

    const unsubProfile = subscribeToUserProfile(currentUser.uid, (p) => setProfile(p));
    const unsubSessions = subscribeToUserSessions(currentUser.uid, (s) => setSessions(s));
    const unsubDecks = subscribeToFlashcardDecks(currentUser.uid, (d) => setDecks(d));
    const unsubPlan = subscribeToStudyPlan(currentUser.uid, (plan) => setStudyPlan(plan));

    return () => {
      unsubProfile();
      unsubSessions();
      unsubDecks();
      unsubPlan();
    };
  }, [currentUser]);

  // Calculate today's focus minutes
  const todayStart = new Date();
  todayStart.setHours(0, 0, 0, 0);
  const todaySessions = sessions.filter(
    (s) => s.startTime >= todayStart.getTime() && (s.durationMinutes || 0) > 0
  );
  const todayFocusMinutes = todaySessions.reduce((acc, s) => acc + (s.durationMinutes || 0), 0);

  // Time of day greeting
  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening';

  // Last deck studied for Jump Back In
  const lastDeck = decks.length > 0 ? decks[0] : null;
  const deckSubjectMap: Record<string, string> = {
    Physics: ASSETS.decks.physics,
    Chemistry: ASSETS.decks.chemistry,
    Biology: ASSETS.decks.biology,
    Mathematics: ASSETS.decks.maths,
    History: ASSETS.decks.history,
  };
  const lastDeckBg = lastDeck?.subject ? deckSubjectMap[lastDeck.subject] || ASSETS.decks.physics : ASSETS.decks.physics;

  // Active study tasks for today from real study plan
  const todayTasks = studyPlan?.tasks?.filter(t => !t.isCompleted).slice(0, 3) || [];

  return (
    <div className="max-w-6xl mx-auto space-y-12 pb-24">
      {/* ── Layer 1: Hero Greeting & Identity ─────────────────────────── */}
      <section className="flex flex-col md:flex-row md:items-end justify-between gap-6 pt-4">
        <div>
          <h1 className="text-display text-text-primary">
            {greeting}, <span className="font-bold text-primary">{profile?.name ? profile.name.split(' ')[0] : 'Scholar'}</span>! 👋
          </h1>
          <p className="text-section text-text-secondary mt-2">
            Targeting <span className="text-text-primary font-bold">{profile?.targetExam || 'JEE Advanced'}</span> • Ready for deep work?
          </p>
        </div>

        <Link href="/app/timer">
          <QuovexButton variant="primary" size="lg" leftIcon={<Timer className="w-5 h-5" />} className="w-full md:w-auto shadow-glow-lg">
            Enter Focus Engine
          </QuovexButton>
        </Link>
      </section>

      {/* ── Layer 2 & 3: Goal Progress & Today's Tasks ──────────────────── */}
      <section className="grid grid-cols-1 lg:grid-cols-12 gap-8">
        {/* Main Goal Ring (Col 1-7) */}
        <div className="lg:col-span-7 flex flex-col justify-center items-center py-8">
          <GoalProgressRing
            todayMinutes={todayFocusMinutes}
            targetHours={profile?.dailyGoalHours || 4}
          />
        </div>

        {/* Exam Countdown & Active Tasks (Col 8-12) */}
        <div className="lg:col-span-5 space-y-6">
          <ExamCountdownCard targetExam={profile?.targetExam || 'JEE Advanced'} />

          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-title text-text-primary flex items-center gap-2">
                <Zap className="w-5 h-5 text-primary" />
                Active Roadmap
              </h3>
              <Link href="/app/planner" className="text-label text-primary hover:underline">
                View Planner →
              </Link>
            </div>

            {todayTasks.length > 0 ? (
              <div className="space-y-3">
                {todayTasks.map((task) => (
                  <div
                    key={task.id}
                    className="p-4 rounded-xl bg-surface hover:bg-surface-elevated border border-border flex items-center justify-between gap-4 transition-colors"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-full bg-surface-variant flex items-center justify-center">
                        <CheckCircle2 className="w-4 h-4 text-text-secondary" />
                      </div>
                      <div>
                        <p className="text-body font-bold text-text-primary">{task.title}</p>
                        <p className="text-caption text-text-secondary">{task.subject} • {task.durationMinutes}m focus</p>
                      </div>
                    </div>
                    <QuovexBadge variant="muted">{task.priority}</QuovexBadge>
                  </div>
                ))}
              </div>
            ) : (
              <QuovexEmptyState 
                icon={<BrainCircuit className="w-8 h-8" />}
                title="Your Day is Clear"
                description="Generate an AI roadmap to get personalized daily tasks."
                action={
                  <Link href="/app/planner">
                    <QuovexButton variant="secondary" size="sm">Generate Roadmap</QuovexButton>
                  </Link>
                }
              />
            )}
          </div>
        </div>
      </section>

      {/* ── Layer 4: Quick Actions / Study Command Tools ───────────────── */}
      <section>
        <h2 className="text-headline text-text-primary mb-6">Study Command Tools</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          <Link href="/app/timer" className="group">
            <QuovexCard hoverEffect className="p-6 h-full flex flex-col justify-between border-transparent bg-surface-elevated">
              <div className="flex items-center justify-between mb-8">
                <div className="w-16 h-16 rounded-2xl bg-primary-container text-primary flex items-center justify-center shadow-glow group-hover:scale-110 transition-transform">
                  <Timer className="w-8 h-8" />
                </div>
                <QuovexBadge variant="emerald">CORE</QuovexBadge>
              </div>
              <div>
                <h3 className="text-title text-text-primary group-hover:text-primary transition-colors">Focus Timer</h3>
                <p className="text-body text-text-secondary mt-2">Pomodoro & Deep Work with ambient soundscapes.</p>
              </div>
            </QuovexCard>
          </Link>

          <Link href="/app/ai/doubt" className="group">
            <QuovexCard hoverEffect className="p-6 h-full flex flex-col justify-between border-transparent bg-surface-elevated">
              <div className="flex items-center justify-between mb-8">
                <div className="w-16 h-16 rounded-2xl bg-surface-variant text-text-primary flex items-center justify-center group-hover:scale-110 transition-transform">
                  <Flame className="w-8 h-8" />
                </div>
                <QuovexBadge variant="gold">6-TIER AI</QuovexBadge>
              </div>
              <div>
                <h3 className="text-title text-text-primary group-hover:text-primary transition-colors">Doubt Solver</h3>
                <p className="text-body text-text-secondary mt-2">Upload visual problems for step-by-step proofs.</p>
              </div>
            </QuovexCard>
          </Link>

          <Link href="/app/quiz" className="group">
            <QuovexCard hoverEffect className="p-6 h-full flex flex-col justify-between border-transparent bg-surface-elevated">
              <div className="flex items-center justify-between mb-8">
                <div className="w-16 h-16 rounded-2xl bg-surface-variant text-text-primary flex items-center justify-center group-hover:scale-110 transition-transform">
                  <HelpCircle className="w-8 h-8" />
                </div>
                <QuovexBadge variant="fire">DAILY</QuovexBadge>
              </div>
              <div>
                <h3 className="text-title text-text-primary group-hover:text-primary transition-colors">Diagnostic Quiz</h3>
                <p className="text-body text-text-secondary mt-2">Auto-synthesizes remedial flashcards.</p>
              </div>
            </QuovexCard>
          </Link>

          <Link href="/app/ai" className="group">
            <QuovexCard hoverEffect className="p-6 h-full flex flex-col justify-between border-transparent bg-surface-elevated">
              <div className="flex items-center justify-between mb-8">
                <div className="w-16 h-16 rounded-2xl bg-surface-variant text-text-primary flex items-center justify-center group-hover:scale-110 transition-transform">
                  <Bot className="w-8 h-8" />
                </div>
                <QuovexBadge variant="emerald">STREAMING</QuovexBadge>
              </div>
              <div>
                <h3 className="text-title text-text-primary group-hover:text-primary transition-colors">Quovex AI Tutor</h3>
                <p className="text-body text-text-secondary mt-2">Contextual math & science tutor with LaTeX.</p>
              </div>
            </QuovexCard>
          </Link>
        </div>
      </section>

      {/* ── Layer 5 & 6: Progress & Recent Activity ────────────────────── */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-12 pt-8 border-t border-border/50">
        <div>
          <h2 className="text-title text-text-primary mb-6 flex items-center gap-2">
            <Sparkles className="w-6 h-6 text-primary" />
            Jump Back In
          </h2>
          
          {lastDeck ? (
            <Link href={`/app/flashcards/${lastDeck.id}`} className="block group">
              <div className="relative rounded-3xl overflow-hidden border border-border group-hover:border-primary shadow-elevated transition-all">
                <div className="h-48 relative bg-surface-variant">
                  <Image
                    src={lastDeckBg}
                    alt={lastDeck.title}
                    fill
                    className="object-cover opacity-80 group-hover:scale-105 transition-transform duration-500"
                    unoptimized
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-surface via-surface/40 to-transparent" />
                  <div className="absolute bottom-6 left-6 right-6 flex items-end justify-between">
                    <div>
                      <QuovexBadge variant="emerald" className="mb-2">{lastDeck.subject}</QuovexBadge>
                      <h4 className="text-headline text-text-primary leading-tight">{lastDeck.title}</h4>
                    </div>
                    <span className="text-label text-primary bg-primary-container/80 backdrop-blur-md px-3 py-1.5 rounded-full border border-primary/30">
                      {lastDeck.cardCount} Cards Due
                    </span>
                  </div>
                </div>
              </div>
            </Link>
          ) : (
            <QuovexEmptyState 
              icon={<BookOpen className="w-8 h-8" />}
              title="No Flashcard Decks"
              description="Create a deck manually or generate one from notes."
              action={
                <Link href="/app/flashcards">
                  <QuovexButton variant="secondary">Go to Flashcards</QuovexButton>
                </Link>
              }
            />
          )}
        </div>

        <div>
          <h2 className="text-title text-text-primary mb-6 flex items-center gap-2">
            <Clock className="w-6 h-6 text-primary" />
            Recent Focus Log
          </h2>

          <div className="bg-surface rounded-3xl p-6 border border-border">
            {sessions.length > 0 ? (
              <div className="divide-y divide-border/50">
                {sessions.slice(0, 4).map((session) => (
                  <div key={session.id} className="py-4 flex items-center justify-between">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-xl bg-surface-variant flex items-center justify-center text-section font-bold text-text-primary">
                        {session.durationMinutes}m
                      </div>
                      <div>
                        <p className="text-section text-text-primary">{session.subject || 'Deep Work Session'}</p>
                        <p className="text-caption text-text-secondary mt-1">
                          {new Date(session.startTime).toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                        </p>
                      </div>
                    </div>
                    <div className="text-right">
                      <span className="text-section font-bold text-xp-gold">+{session.durationMinutes * 2} XP</span>
                      {session.focusScore && (
                        <span className="block text-caption text-primary mt-1">{session.focusScore}% Focus</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <QuovexEmptyState 
                icon={<Timer className="w-8 h-8" />}
                title="No Sessions Yet"
                description="Your study logs will appear here after you finish a timer."
              />
            )}
          </div>
        </div>
      </section>
      
      <section className="pt-8">
        <WeeklyConsistencyStrip sessions={sessions} />
      </section>
    </div>
  );
}
