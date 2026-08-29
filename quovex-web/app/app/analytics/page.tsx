'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  BarChart3,
  Clock,
  Award,
  Sparkles,
  TrendingUp,
  PieChart,
  Calendar,
  Download,
  Flame,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import {
  subscribeToUserSessions,
  subscribeToUserProfile,
  subscribeToQuizHistory,
  StudySession,
  QuizResultRecord,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { ASSETS } from '@/lib/assets';

export default function AnalyticsPage() {
  const [sessions, setSessions] = useState<StudySession[]>([]);
  const [quizHistory, setQuizHistory] = useState<QuizResultRecord[]>([]);
  const [profile, setProfile] = useState<any>(null);

  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) return;

    const unsubSessions = subscribeToUserSessions(currentUser.uid, (s) => setSessions(s));
    const unsubProfile = subscribeToUserProfile(currentUser.uid, (p) => setProfile(p));
    const unsubQuiz = subscribeToQuizHistory(currentUser.uid, (q) => setQuizHistory(q));

    return () => {
      unsubSessions();
      unsubProfile();
      unsubQuiz();
    };
  }, [currentUser]);

  // Aggregate stats
  const totalMinutes = sessions.reduce((acc, s) => acc + (s.durationMinutes || 0), 0);
  const totalHours = (totalMinutes / 60).toFixed(1);
  const avgFocusScore = sessions.length > 0
    ? Math.round(sessions.reduce((acc, s) => acc + (s.focusScore || 90), 0) / sessions.length)
    : 95;
  const avgSessionLength = sessions.length > 0
    ? Math.round(totalMinutes / sessions.length)
    : 25;

  // Subject breakdown calculation
  const subjectMap: Record<string, number> = {};
  sessions.forEach((s) => {
    const subj = s.subject || 'General Study';
    subjectMap[subj] = (subjectMap[subj] || 0) + (s.durationMinutes || 0);
  });

  const subjectList = Object.entries(subjectMap).map(([subject, mins]) => ({
    subject,
    mins,
    percentage: totalMinutes > 0 ? Math.round((mins / totalMinutes) * 100) : 0,
  }));

  // Weekly 7-day focus minutes
  const daysOfWeek = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
  const today = new Date();
  const dayBars = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(today.getDate() - (6 - i));
    d.setHours(0, 0, 0, 0);

    const start = d.getTime();
    const end = start + 86400000;
    const daySessions = sessions.filter((s) => s.startTime >= start && s.startTime < end);
    const dayMins = daySessions.reduce((acc, s) => acc + (s.durationMinutes || 0), 0);

    const dayName = daysOfWeek[(d.getDay() + 6) % 7];
    return {
      dayName,
      dateNum: d.getDate(),
      mins: dayMins,
      heightPct: Math.min(100, Math.round((dayMins / 180) * 100)), // scale to 3 hours max
    };
  });

  return (
    <div className="max-w-6xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div>
          <h1 className="text-display font-black text-text-primary flex items-center gap-4">
            <BarChart3 className="w-10 h-10 text-primary" />
            Performance Analytics & Diagnostic Metrics
          </h1>
          <p className="text-section text-text-secondary mt-2">
            Real-time biometric focus metrics, subject distribution, and active recall velocity.
          </p>
        </div>

        <QuovexButton
          variant="secondary"
          size="lg"
          onClick={() => {
            const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify({ sessions, quizHistory, profile }));
            const downloadAnchor = document.createElement('a');
            downloadAnchor.setAttribute('href', dataStr);
            downloadAnchor.setAttribute('download', `quovex_study_export_${Date.now()}.json`);
            document.body.appendChild(downloadAnchor);
            downloadAnchor.click();
            downloadAnchor.remove();
          }}
          leftIcon={<Download className="w-5 h-5" />}
        >
          Export Study Data
        </QuovexButton>
      </div>

      {/* ── 1. Top 4 Metric KPI Cards ──────────────────────────────────────── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <QuovexCard className="p-6 flex items-center gap-5 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-primary-container text-primary flex items-center justify-center shrink-0 shadow-sm">
            <Clock className="w-7 h-7" />
          </div>
          <div>
            <span className="text-label font-bold text-text-secondary uppercase tracking-wider block mb-1">Total Study Time</span>
            <h3 className="text-display font-black text-text-primary">{totalHours} hrs</h3>
          </div>
        </QuovexCard>

        <QuovexCard className="p-6 flex items-center gap-5 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-warning-container text-warning flex items-center justify-center shrink-0 shadow-sm">
            <Award className="w-7 h-7" />
          </div>
          <div>
            <span className="text-label font-bold text-text-secondary uppercase tracking-wider block mb-1">Average Focus Score</span>
            <h3 className="text-display font-black text-text-primary">{avgFocusScore}%</h3>
          </div>
        </QuovexCard>

        <QuovexCard className="p-6 flex items-center gap-5 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-[rgba(255,107,53,0.15)] text-streak-fire flex items-center justify-center shrink-0 shadow-sm">
            <Flame className="w-7 h-7 fill-streak-fire" />
          </div>
          <div>
            <span className="text-label font-bold text-text-secondary uppercase tracking-wider block mb-1">Current Streak</span>
            <h3 className="text-display font-black text-text-primary">{profile?.streakDays || 1} Days</h3>
          </div>
        </QuovexCard>

        <QuovexCard className="p-6 flex items-center gap-5 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-surface-variant text-primary flex items-center justify-center shrink-0 shadow-sm">
            <TrendingUp className="w-7 h-7" />
          </div>
          <div>
            <span className="text-label font-bold text-text-secondary uppercase tracking-wider block mb-1">Avg Session Length</span>
            <h3 className="text-display font-black text-text-primary">{avgSessionLength} mins</h3>
          </div>
        </QuovexCard>
      </div>

      {/* ── 2. 7-Day Focus Time Chart & Subject Breakdown ──────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Weekly Focus Bar Chart */}
        <QuovexCard className="lg:col-span-2 p-8 space-y-8 flex flex-col justify-between shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-bold text-text-primary text-title flex items-center gap-3">
                <Calendar className="w-5 h-5 text-primary" />
                Past 7 Days Focus Velocity
              </h3>
              <p className="text-body text-text-secondary mt-1.5">Daily minutes of recorded uninterrupted focus</p>
            </div>
            <QuovexBadge variant="emerald" size="lg">GOAL: 180M/DAY</QuovexBadge>
          </div>

          {/* Bar Chart Graphics */}
          <div className="grid grid-cols-7 gap-3 sm:gap-6 items-end h-56 pt-6 border-b border-border">
            {dayBars.map((bar, idx) => (
              <div key={idx} className="flex flex-col items-center gap-3 h-full justify-end group">
                <span className="text-label font-mono text-text-secondary font-bold opacity-0 group-hover:opacity-100 transition-opacity">
                  {bar.mins}m
                </span>
                <div className="w-full bg-surface-variant rounded-t-xl h-full flex items-end overflow-hidden p-1 border border-border/50 border-b-0">
                  <div
                    className="w-full bg-gradient-to-t from-primary to-secondary rounded-t-lg transition-all duration-700 shadow-glow"
                    style={{ height: `${Math.max(8, bar.heightPct)}%` }}
                  />
                </div>
                <div className="text-center pb-2">
                  <span className="text-body font-bold text-text-primary block">{bar.dayName}</span>
                  <span className="text-label text-text-secondary block font-bold">{bar.dateNum}</span>
                </div>
              </div>
            ))}
          </div>
        </QuovexCard>

        {/* Subject Distribution Card */}
        <QuovexCard className="p-8 space-y-6 flex flex-col justify-between shadow-sm">
          <div>
            <h3 className="font-bold text-text-primary text-title flex items-center gap-3">
              <PieChart className="w-5 h-5 text-primary" />
              Subject Time Distribution
            </h3>
            <p className="text-body text-text-secondary mt-1.5">Breakdown across your focus streams</p>
          </div>

          <div className="space-y-4">
            {subjectList.length > 0 ? (
              subjectList.map((item, idx) => (
                <div key={idx} className="space-y-2">
                  <div className="flex items-center justify-between text-body font-semibold">
                    <span className="text-text-primary">{item.subject}</span>
                    <span className="text-primary font-mono">{item.mins}m ({item.percentage}%)</span>
                  </div>
                  <div className="h-2.5 bg-surface-variant rounded-full overflow-hidden border border-border">
                    <div
                      className="h-full bg-primary rounded-full transition-all duration-500 shadow-glow"
                      style={{ width: `${item.percentage}%` }}
                    />
                  </div>
                </div>
              ))
            ) : (
              <div className="p-8 text-center text-body text-text-secondary">
                No subject logs recorded yet. Start a focus session to see distribution.
              </div>
            )}
          </div>

          <div className="p-4 rounded-xl bg-surface-variant border border-border text-body text-text-secondary leading-relaxed">
            💡 <strong>Balance Tip:</strong> Allocate at least 30% of study time to high-weightage topics.
          </div>
        </QuovexCard>
      </div>

      {/* ── 3. Diagnostic Quiz History & Accuracy Trend ─────────────────────── */}
      <QuovexCard className="p-8 space-y-6 shadow-sm">
        <h3 className="font-bold text-text-primary text-title flex items-center gap-3">
          <Sparkles className="w-5 h-5 text-primary" />
          Recent Diagnostic Quiz Performance
        </h3>

        {quizHistory.length > 0 ? (
          <div className="divide-y divide-border border-t border-border mt-4">
            {quizHistory.slice(0, 5).map((q) => (
              <div key={q.id} className="py-5 flex items-center justify-between">
                <div>
                  <h4 className="font-bold text-text-primary text-title">{q.subject}</h4>
                  <p className="text-label text-text-secondary mt-1 font-bold">
                    {new Date(q.timestamp).toLocaleDateString(undefined, {
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>

                <div className="flex items-center gap-4">
                  <span className="font-mono font-black text-primary text-title">
                    {q.correctCount} / {q.totalQuestions} ({Math.round((q.correctCount / q.totalQuestions) * 100)}%)
                  </span>
                  <QuovexBadge variant={q.correctCount === q.totalQuestions ? 'gold' : 'muted'} size="md">
                    {q.correctCount === q.totalQuestions ? 'PERFECT' : 'EVALUATED'}
                  </QuovexBadge>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-10 text-center text-body text-text-secondary">
            No diagnostic quiz history yet. Take today's quiz to track accuracy trends.
          </div>
        )}
      </QuovexCard>
    </div>
  );
}
