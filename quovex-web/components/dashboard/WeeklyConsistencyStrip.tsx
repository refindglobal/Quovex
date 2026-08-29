'use client';

import React from 'react';
import { StudySession } from '@/lib/firebase/firestore';

interface WeeklyConsistencyStripProps {
  sessions: StudySession[];
}

export const WeeklyConsistencyStrip: React.FC<WeeklyConsistencyStripProps> = ({ sessions }) => {
  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const today = new Date();
  
  // Build the last 7 calendar days
  const last7Days = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(today.getDate() - (6 - i));
    d.setHours(0, 0, 0, 0);

    const isToday = i === 6;
    const dayLabel = daysOfWeek[d.getDay()];
    const dateNum = d.getDate();

    // Check if any session occurred on this day
    const dayStart = d.getTime();
    const dayEnd = dayStart + 86400000;
    const daySessions = sessions.filter(
      (s) => s.startTime >= dayStart && s.startTime < dayEnd && (s.durationMinutes || 0) > 0
    );
    const dayMinutes = daySessions.reduce((acc, s) => acc + (s.durationMinutes || 0), 0);
    const hasStudied = dayMinutes >= 15;

    return {
      dayLabel,
      dateNum,
      isToday,
      dayMinutes,
      hasStudied,
    };
  });

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between text-xs">
        <span className="font-bold text-text-primary">7-Day Study Consistency</span>
        <span className="text-text-secondary text-[11px]">Goal: 15m+ daily</span>
      </div>

      <div className="grid grid-cols-7 gap-2">
        {last7Days.map((day, idx) => (
          <div
            key={idx}
            className={`flex flex-col items-center justify-center p-2 rounded-xl border text-center transition-all ${
              day.hasStudied
                ? 'bg-primary-container border-primary/50 text-primary shadow-sm'
                : day.isToday
                ? 'bg-surface-variant border-border/80 text-text-primary ring-1 ring-primary/40'
                : 'bg-surface border-border/40 text-text-tertiary'
            }`}
          >
            <span className="text-[10px] font-semibold">{day.dayLabel}</span>
            <span className="text-xs font-bold my-0.5">{day.dateNum}</span>
            <div
              className={`w-2 h-2 rounded-full mt-1 ${
                day.hasStudied ? 'bg-primary shadow-glow' : 'bg-surface-variant'
              }`}
            />
          </div>
        ))}
      </div>
    </div>
  );
};
