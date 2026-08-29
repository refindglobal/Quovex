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
  Calendar,
  Layers,
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

  // Active study tasks for today from real study plan
  const todayTasks = studyPlan?.tasks?.filter(t => !t.isCompleted).slice(0, 3) || [];

  return (
    <div className="max-w-6xl mx-auto space-y-8 pb-20">
      {/* ── 1. Greeting + Streak + Primary CTA ────────────────────────────── */}
      <section className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 rounded-2xl bg-surface border border-border shadow-sm">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-black text-text-primary">
              {greeting}, <span className="text-primary">{profile?.name ? profile.name.split(' ')[0] : 'Scholar'}</span>! 👋
            </h1>
            <div className="flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-[rgba(255,107,53,0.15)] text-streak-fire text-xs font-bold border border-streak-fire/30">
              <Flame className="w-3.5 h-3.5 fill-current" />
              <span>{profile?.streakDays || 1}d Streak</span>
            </div>
          </div>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Targeting <strong className="text-text-primary">{profile?.targetExam || 'JEE Advanced'}</strong> • Ready for your focus block?
          </p>
        </div>

        <Link href="/app/timer">
          <QuovexButton variant="primary" size="md" leftIcon={<Timer className="w-4 h-4" />} className="shadow-glow sm:self-auto w-full sm:w-auto">
            Enter Focus Engine
          </QuovexButton>
        </Link>
      </section>

      {/* ── 2 & 3: Goal Progress Centerpiece + Exam & Tasks ────────────────── */}
      <section className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Goal Ring Centerpiece (Col 1-6) */}
        <div className="lg:col-span-6 flex">
          <GoalProgressRing
            todayMinutes={todayFocusMinutes}
            targetHours={profile?.dailyGoalHours || 4}
          />
        </div>

        {/* Countdown & Active Roadmap Tasks (Col 7-12) */}
        <div className="lg:col-span-6 space-y-4 flex flex-col justify-between">
          <ExamCountdownCard targetExam={profile?.targetExam || 'JEE Advanced'} />

          <div className="p-5 rounded-2xl bg-surface border border-border flex-1 flex flex-col justify-between shadow-sm">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-xs sm:text-sm font-bold text-text-primary flex items-center gap-2">
                <Zap className="w-4 h-4 text-primary" />
                Active Roadmap Tasks
              </h3>
              <Link href="/app/planner" className="text-xs text-primary font-semibold hover:underline">
                View Planner →
              </Link>
            </div>

            {todayTasks.length > 0 ? (
              <div className="space-y-2">
                {todayTasks.map((task) => (
                  <div
                    key={task.id}
                    className="p-3 rounded-xl bg-surface-variant border border-border/80 flex items-center justify-between gap-3 text-xs"
                  >
                    <div className="flex items-center gap-2.5">
                      <div className="w-6 h-6 rounded-full bg-surface flex items-center justify-center text-text-secondary">
                        <CheckCircle2 className="w-3.5 h-3.5" />
                      </div>
                      <div>
                        <p className="font-bold text-text-primary">{task.title}</p>
                        <p className="text-[11px] text-text-secondary">{task.subject} • {task.durationMinutes}m focus</p>
                      </div>
                    </div>
                    <QuovexBadge variant="muted" size="sm">{task.priority}</QuovexBadge>
                  </div>
                ))}
              </div>
            ) : (
              <div className="py-4 text-center space-y-2">
                <p className="text-xs text-text-secondary">No active roadmap tasks scheduled for today.</p>
                <Link href="/app/planner">
                  <QuovexButton variant="secondary" size="sm">Generate AI Study Plan</QuovexButton>
                </Link>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* ── 4: Quick Actions / Study Command Tools ────────────────────────── */}
      <section className="space-y-3">
        <h2 className="text-sm sm:text-base font-bold text-text-primary">Study Command Tools</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          <Link href="/app/timer" className="group">
            <QuovexCard hoverEffect className="p-5 h-full flex flex-col justify-between border-border bg-surface">
              <div className="flex items-center justify-between mb-4">
                <div className="w-11 h-11 rounded-xl bg-primary-container text-primary flex items-center justify-center group-hover:scale-105 transition-transform">
                  <Timer className="w-5 h-5" />
                </div>
                <QuovexBadge variant="emerald" size="sm">FOCUS</QuovexBadge>
              </div>
              <div>
                <h3 className="text-sm font-bold text-text-primary group-hover:text-primary transition-colors">Focus Timer</h3>
                <p className="text-xs text-text-secondary mt-1">Pomodoro & Deep Work with ambient soundscapes.</p>
              </div>
            </QuovexCard>
          </Link>

          <Link href="/app/ai/doubt" className="group">
            <QuovexCard hoverEffect className="p-5 h-full flex flex-col justify-between border-border bg-surface">
              <div className="flex items-center justify-between mb-4">
                <div className="w-11 h-11 rounded-xl bg-warning-container/30 text-warning flex items-center justify-center group-hover:scale-105 transition-transform">
                  <BrainCircuit className="w-5 h-5" />
                </div>
                <QuovexBadge variant="gold" size="sm">6-TIER AI</QuovexBadge>
              </div>
              <div>
                <h3 className="text-sm font-bold text-text-primary group-hover:text-primary transition-colors">Photo Doubt Solver</h3>
                <p className="text-xs text-text-secondary mt-1">Upload problem images for verified proofs.</p>
              </div>
            </QuovexCard>
          </Link>

          <Link href="/app/quiz" className="group">
            <QuovexCard hoverEffect className="p-5 h-full flex flex-col justify-between border-border bg-surface">
              <div className="flex items-center justify-between mb-4">
                <div className="w-11 h-11 rounded-xl bg-[rgba(255,107,53,0.15)] text-streak-fire flex items-center justify-center group-hover:scale-105 transition-transform">
                  <HelpCircle className="w-5 h-5" />
                </div>
                <QuovexBadge variant="fire" size="sm">DAILY</QuovexBadge>
              </div>
              <div>
                <h3 className="text-sm font-bold text-text-primary group-hover:text-primary transition-colors">Diagnostic Quiz</h3>
                <p className="text-xs text-text-secondary mt-1">5 adaptive questions with trap diagnostics.</p>
              </div>
            </QuovexCard>
          </Link>

          <Link href="/app/ai" className="group">
            <QuovexCard hoverEffect className="p-5 h-full flex flex-col justify-between border-border bg-surface">
              <div className="flex items-center justify-between mb-4">
                <div className="w-11 h-11 rounded-xl bg-primary-container text-primary flex items-center justify-center group-hover:scale-105 transition-transform">
                  <Bot className="w-5 h-5" />
                </div>
                <QuovexBadge variant="emerald" size="sm">TUTOR</QuovexBadge>
              </div>
              <div>
                <h3 className="text-sm font-bold text-text-primary group-hover:text-primary transition-colors">AI Study Coach</h3>
                <p className="text-xs text-text-secondary mt-1">Interactive STEM tutor with LaTeX formulas.</p>
              </div>
            </QuovexCard>
          </Link>
        </div>
      </section>

      {/* ── 5 & 6: Weekly Consistency + Recent Activity ──────────────────── */}
      <section className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Weekly Consistency */}
        <div className="p-5 rounded-2xl bg-surface border border-border shadow-sm flex flex-col justify-between">
          <WeeklyConsistencyStrip sessions={sessions} />
        </div>

        {/* Jump Back In Flashcards / Recent Focus Log */}
        <div className="p-5 rounded-2xl bg-surface border border-border shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between mb-3">
            <h3 className="text-xs sm:text-sm font-bold text-text-primary flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-primary" />
              Active Recall Flashcards
            </h3>
            <Link href="/app/flashcards" className="text-xs text-primary font-semibold hover:underline">
              All Decks →
            </Link>
          </div>

          {lastDeck ? (
            <div className="p-4 rounded-xl bg-surface-variant border border-border flex items-center justify-between gap-4">
              <div>
                <QuovexBadge variant="emerald" size="sm" className="mb-1">{lastDeck.subject}</QuovexBadge>
                <h4 className="text-sm font-bold text-text-primary">{lastDeck.title}</h4>
                <p className="text-[11px] text-text-secondary">{lastDeck.cardCount} cards scheduled</p>
              </div>
              <Link href={`/app/flashcards/${lastDeck.id}`}>
                <QuovexButton size="sm" variant="primary">Review</QuovexButton>
              </Link>
            </div>
          ) : (
            <div className="py-4 text-center space-y-2">
              <p className="text-xs text-text-secondary">No flashcard decks created yet.</p>
              <Link href="/app/flashcards">
                <QuovexButton variant="secondary" size="sm">Create Spaced Repetition Deck</QuovexButton>
              </Link>
            </div>
          )}
        </div>
      </section>

      {/* ── 7: Recent Focus Sessions ─────────────────────────────────────── */}
      {sessions.length > 0 && (
        <section className="space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-xs sm:text-sm font-bold text-text-primary flex items-center gap-2">
              <Clock className="w-4 h-4 text-primary" />
              Recent Focus Log
            </h3>
            <Link href="/app/analytics" className="text-xs text-primary font-semibold hover:underline">
              View Analytics →
            </Link>
          </div>

          <div className="p-4 rounded-2xl bg-surface border border-border shadow-sm divide-y divide-border">
            {sessions.slice(0, 3).map((session) => (
              <div key={session.id} className="py-3 flex items-center justify-between first:pt-0 last:pb-0">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-surface-variant flex items-center justify-center font-mono font-bold text-xs text-text-primary">
                    {session.durationMinutes}m
                  </div>
                  <div>
                    <p className="text-xs sm:text-sm font-bold text-text-primary">{session.subject || 'Deep Focus Session'}</p>
                    <p className="text-[11px] text-text-secondary">
                      {new Date(session.startTime).toLocaleDateString(undefined, { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-black text-warning">+{session.durationMinutes * 2} XP</span>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
