'use client';

import React from 'react';
import { Flame, Zap, Target, CheckCircle2 } from 'lucide-react';

interface GoalProgressRingProps {
  todayMinutes: number;
  targetHours: number;
}

export const GoalProgressRing: React.FC<GoalProgressRingProps> = ({
  todayMinutes,
  targetHours,
}) => {
  const targetMinutes = Math.max(1, (targetHours || 4) * 60);
  const percentage = Math.min(100, Math.round((todayMinutes / targetMinutes) * 100));
  const radius = 54;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (percentage / 100) * circumference;

  const todayHoursFormatted = (todayMinutes / 60).toFixed(1);
  const remainingMins = Math.max(0, targetMinutes - todayMinutes);

  return (
    <div className="w-full p-6 rounded-2xl bg-surface border border-border flex flex-col sm:flex-row items-center justify-between gap-6 shadow-sm">
      {/* ── Circular Progress Gauge ── */}
      <div className="relative w-36 h-36 flex items-center justify-center shrink-0">
        <svg className="w-full h-full transform -rotate-90" viewBox="0 0 128 128">
          {/* Track */}
          <circle
            cx="64"
            cy="64"
            r={radius}
            className="stroke-surface-variant"
            strokeWidth="10"
            fill="transparent"
          />
          {/* Active Ring */}
          <circle
            cx="64"
            cy="64"
            r={radius}
            className="stroke-primary transition-all duration-1000 ease-out"
            strokeWidth="10"
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
          />
        </svg>

        {/* Center Percentage & Label */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <span className="text-2xl font-black text-text-primary tracking-tight">{percentage}%</span>
          <span className="text-[10px] font-bold text-primary uppercase tracking-wider">Goal</span>
        </div>
      </div>

      {/* ── Detailed Metrics Breakdown ── */}
      <div className="flex-1 space-y-3 text-center sm:text-left">
        <div>
          <div className="flex items-center justify-center sm:justify-start gap-2 text-xs text-text-secondary font-semibold">
            <Target className="w-3.5 h-3.5 text-primary" />
            <span>Today's Focus Target</span>
          </div>
          <div className="flex items-baseline justify-center sm:justify-start gap-2 mt-1">
            <span className="text-3xl font-black text-text-primary">{todayHoursFormatted}</span>
            <span className="text-sm font-semibold text-text-secondary">/ {targetHours} hrs target</span>
          </div>
        </div>

        <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
          {percentage >= 100 ? (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-success-container text-success border border-success/30 text-xs font-bold shadow-xs">
              <CheckCircle2 className="w-3.5 h-3.5" /> Daily Target Conquered!
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-primary-container text-primary border border-primary/30 text-xs font-bold">
              <Zap className="w-3.5 h-3.5" /> {remainingMins} mins to reach 100%
            </span>
          )}
        </div>
      </div>
    </div>
  );
};
