'use client';

import React from 'react';

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
  const radius = 38;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (percentage / 100) * circumference;

  const todayHoursFormatted = (todayMinutes / 60).toFixed(1);

  return (
    <div className="flex items-center gap-5">
      {/* Circular SVG Ring */}
      <div className="relative w-24 h-24 flex items-center justify-center shrink-0">
        <svg className="w-full h-full transform -rotate-90" viewBox="0 0 96 96">
          {/* Background Track */}
          <circle
            cx="48"
            cy="48"
            r={radius}
            className="stroke-surface-variant"
            strokeWidth="8"
            fill="transparent"
          />
          {/* Foreground Progress Ring */}
          <circle
            cx="48"
            cy="48"
            r={radius}
            className="stroke-primary transition-all duration-700 ease-out"
            strokeWidth="8"
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
          />
        </svg>

        {/* Center Percentage Display */}
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <span className="text-base font-black text-text-primary">{percentage}%</span>
          <span className="text-[9px] font-bold text-text-secondary uppercase tracking-wider">Goal</span>
        </div>
      </div>

      {/* Goal Statistics Detail */}
      <div className="space-y-1">
        <p className="text-xs font-semibold text-text-secondary">Today&apos;s Focus Progress</p>
        <p className="text-xl font-black text-text-primary">
          {todayHoursFormatted} <span className="text-xs font-normal text-text-secondary">/ {targetHours} hrs</span>
        </p>
        <p className="text-[11px] text-primary font-medium">
          {percentage >= 100 ? '🎉 Daily goal achieved!' : `${Math.max(0, (targetMinutes - todayMinutes))} mins remaining`}
        </p>
      </div>
    </div>
  );
};
