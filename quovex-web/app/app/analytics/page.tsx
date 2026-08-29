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
      heightPct: Math.min(100, Math.round((dayMins / 180) * 100)),
    };
  });

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-20">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
            <BarChart3 className="w-7 h-7 text-primary" />
            Performance Analytics
          </h1>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Focus metrics, subject distribution, and diagnostic accuracy trends.
          </p>
        </div>

        <QuovexButton
          variant="secondary"
          size="sm"
          onClick={() => {
            const dataStr = 'data:text/json;charset=utf-8,' + encodeURIComponent(JSON.stringify({ sessions, quizHistory, profile }));
            const downloadAnchor = document.createElement('a');
            downloadAnchor.setAttribute('href', dataStr);
            downloadAnchor.setAttribute('download', `quovex_study_export_${Date.now()}.json`);
            document.body.appendChild(downloadAnchor);
            downloadAnchor.click();
            downloadAnchor.remove();
          }}
          leftIcon={<Download className="w-4 h-4" />}
        >
          Export Data
        </QuovexButton>
      </div>

      {/* ── 1. Top 4 Metric KPI Cards ──────────────────────────────────────── */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
        <QuovexCard className="p-4 flex items-center gap-3 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-primary-container text-primary flex items-center justify-center shrink-0">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[10px] font-bold text-text-secondary uppercase tracking-wider block">Total Focus</span>
            <h3 className="text-lg sm:text-xl font-black text-text-primary">{totalHours} hrs</h3>
          </div>
        </QuovexCard>

        <QuovexCard className="p-4 flex items-center gap-3 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-warning-container/30 text-warning flex items-center justify-center shrink-0">
            <Award className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[10px] font-bold text-text-secondary uppercase tracking-wider block">Avg Focus</span>
            <h3 className="text-lg sm:text-xl font-black text-text-primary">{avgFocusScore}%</h3>
          </div>
        </QuovexCard>

        <QuovexCard className="p-4 flex items-center gap-3 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-[rgba(255,107,53,0.15)] text-streak-fire flex items-center justify-center shrink-0">
            <Flame className="w-5 h-5 fill-streak-fire" />
          </div>
          <div>
            <span className="text-[10px] font-bold text-text-secondary uppercase tracking-wider block">Streak</span>
            <h3 className="text-lg sm:text-xl font-black text-text-primary">{profile?.streakDays || 1} Days</h3>
          </div>
        </QuovexCard>

        <QuovexCard className="p-4 flex items-center gap-3 shadow-sm">
          <div className="w-10 h-10 rounded-xl bg-surface-variant text-primary flex items-center justify-center shrink-0">
            <TrendingUp className="w-5 h-5" />
          </div>
          <div>
            <span className="text-[10px] font-bold text-text-secondary uppercase tracking-wider block">Avg Session</span>
            <h3 className="text-lg sm:text-xl font-black text-text-primary">{avgSessionLength}m</h3>
          </div>
        </QuovexCard>
      </div>

      {/* ── 2. 7-Day Focus Chart & Subject Breakdown ───────────────────────── */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* Weekly Focus Bar Chart */}
        <QuovexCard className="lg:col-span-2 p-5 space-y-4 flex flex-col justify-between shadow-sm">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-bold text-text-primary text-sm sm:text-base flex items-center gap-2">
                <Calendar className="w-4 h-4 text-primary" />
                7-Day Focus Velocity
              </h3>
              <p className="text-xs text-text-secondary mt-0.5">Daily recorded focus minutes</p>
            </div>
            <QuovexBadge variant="emerald" size="sm">GOAL: 180M/D</QuovexBadge>
          </div>

          {/* Bar Chart Graphics */}
          <div className="grid grid-cols-7 gap-2 sm:gap-4 items-end h-44 pt-4 border-b border-border">
            {dayBars.map((bar, idx) => (
              <div key={idx} className="flex flex-col items-center gap-2 h-full justify-end group">
                <span className="text-[10px] font-mono text-text-secondary font-bold opacity-0 group-hover:opacity-100 transition-opacity">
                  {bar.mins}m
                </span>
                <div className="w-full bg-surface-variant rounded-t-lg h-full flex items-end overflow-hidden p-0.5 border border-border/40 border-b-0">
                  <div
                    className="w-full bg-primary rounded-t-sm transition-all duration-500"
                    style={{ height: `${Math.max(6, bar.heightPct)}%` }}
                  />
                </div>
                <div className="text-center pb-1">
                  <span className="text-xs font-bold text-text-primary block">{bar.dayName}</span>
                  <span className="text-[10px] text-text-secondary block">{bar.dateNum}</span>
                </div>
              </div>
            ))}
          </div>
        </QuovexCard>

        {/* Subject Distribution Card */}
        <QuovexCard className="p-5 space-y-4 flex flex-col justify-between shadow-sm">
          <div>
            <h3 className="font-bold text-text-primary text-sm sm:text-base flex items-center gap-2">
              <PieChart className="w-4 h-4 text-primary" />
              Subject Distribution
            </h3>
            <p className="text-xs text-text-secondary mt-0.5">Time across study streams</p>
          </div>

          <div className="space-y-3">
            {subjectList.length > 0 ? (
              subjectList.map((item, idx) => (
                <div key={idx} className="space-y-1">
                  <div className="flex items-center justify-between text-xs font-semibold">
                    <span className="text-text-primary">{item.subject}</span>
                    <span className="text-primary font-mono">{item.mins}m ({item.percentage}%)</span>
                  </div>
                  <div className="h-2 bg-surface-variant rounded-full overflow-hidden border border-border">
                    <div
                      className="h-full bg-primary rounded-full transition-all duration-500"
                      style={{ width: `${item.percentage}%` }}
                    />
                  </div>
                </div>
              ))
            ) : (
              <div className="p-4 text-center text-xs text-text-secondary">
                No subject logs recorded yet.
              </div>
            )}
          </div>

          <div className="p-3 rounded-xl bg-surface-variant border border-border text-xs text-text-secondary leading-relaxed">
            💡 <strong>Tip:</strong> Allocate at least 30% of study time to high-weightage topics.
          </div>
        </QuovexCard>
      </div>

      {/* ── 3. Diagnostic Quiz History ─────────────────────────────────────── */}
      <QuovexCard className="p-5 space-y-4 shadow-sm">
        <h3 className="font-bold text-text-primary text-sm sm:text-base flex items-center gap-2">
          <Sparkles className="w-4 h-4 text-primary" />
          Recent Diagnostic Quiz Results
        </h3>

        {quizHistory.length > 0 ? (
          <div className="divide-y divide-border border-t border-border mt-2">
            {quizHistory.slice(0, 4).map((q) => (
              <div key={q.id} className="py-3 flex items-center justify-between">
                <div>
                  <h4 className="font-bold text-text-primary text-xs sm:text-sm">{q.subject}</h4>
                  <p className="text-[10px] text-text-secondary mt-0.5">
                    {new Date(q.timestamp).toLocaleDateString(undefined, {
                      month: 'short',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </p>
                </div>

                <div className="flex items-center gap-3">
                  <span className="font-mono font-bold text-primary text-xs sm:text-sm">
                    {q.correctCount} / {q.totalQuestions} ({Math.round((q.correctCount / q.totalQuestions) * 100)}%)
                  </span>
                  <QuovexBadge variant={q.correctCount === q.totalQuestions ? 'gold' : 'muted'} size="sm">
                    {q.correctCount === q.totalQuestions ? 'PERFECT' : 'EVALUATED'}
                  </QuovexBadge>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-6 text-center text-xs text-text-secondary">
            No diagnostic quiz history yet.
          </div>
        )}
      </QuovexCard>
    </div>
  );
}
