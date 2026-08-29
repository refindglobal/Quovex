'use client';

import React from 'react';

interface CircularTimerArcProps {
  progress: number; // 0 to 1
  formattedTime: string;
  subject: string;
  isPlaying: boolean;
}

export const CircularTimerArc: React.FC<CircularTimerArcProps> = ({
  progress,
  formattedTime,
  subject,
  isPlaying,
}) => {
  const radius = 110;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (Math.max(0, Math.min(1, progress)) * circumference);

  return (
    <div className="relative w-64 h-64 sm:w-72 sm:h-72 flex items-center justify-center mx-auto">
      {/* Pulsing Glow Aura */}
      <div
        className={`absolute inset-4 rounded-full bg-primary-glow blur-2xl transition-all duration-1000 ${
          isPlaying ? 'scale-105 opacity-80 animate-pulse' : 'scale-95 opacity-30'
        }`}
      />

      {/* SVG Arc Visualizer */}
      <svg className="w-full h-full transform -rotate-90" viewBox="0 0 260 260">
        {/* Background Track */}
        <circle
          cx="130"
          cy="130"
          r={radius}
          className="stroke-surface-variant"
          strokeWidth="10"
          fill="transparent"
        />
        {/* Active Arc */}
        <circle
          cx="130"
          cy="130"
          r={radius}
          className="stroke-primary transition-all duration-300 ease-linear"
          strokeWidth="10"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          strokeLinecap="round"
          fill="transparent"
        />
      </svg>

      {/* Center Countdown Display */}
      <div className="absolute inset-0 flex flex-col items-center justify-center text-center p-4">
        <span className="text-4xl sm:text-5xl font-extrabold font-mono text-text-primary tracking-tight font-feature-settings: 'tnum'">
          {formattedTime}
        </span>
        <span className="text-[11px] font-extrabold tracking-widest text-primary uppercase mt-1">
          {isPlaying ? 'STAY FOCUSED' : 'PAUSED'}
        </span>
        <span className="text-xs text-text-secondary font-medium mt-1 truncate max-w-[160px]">
          {subject || 'Deep Work'}
        </span>
      </div>
    </div>
  );
};
