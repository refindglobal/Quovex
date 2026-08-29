'use client';

import React from 'react';
import Image from 'next/image';
import { ASSETS } from '@/lib/assets';

interface ExamCountdownCardProps {
  targetExam: string;
}

export const ExamCountdownCard: React.FC<ExamCountdownCardProps> = ({ targetExam }) => {
  // Estimated target days for primary exams
  const examDaysMap: Record<string, number> = {
    'JEE Advanced': 82,
    'JEE Mains': 46,
    'NEET (UG)': 94,
    'UPSC CSE': 118,
    'CBSE Class 12': 38,
    'CBSE Class 11': 65,
    'CBSE Class 10': 35,
  };

  const daysRemaining = examDaysMap[targetExam] || 60;

  return (
    <div className="flex items-center justify-between gap-4 p-4 rounded-2xl bg-surface-elevated border border-border">
      <div className="flex items-center gap-3.5">
        <div className="w-12 h-12 relative shrink-0">
          <Image
            src={ASSETS.icons3d.graduationCap}
            alt="Exam Target"
            fill
            className="object-contain"
            unoptimized
          />
        </div>
        <div>
          <span className="text-[10px] font-bold text-primary uppercase tracking-wider">Exam Countdown</span>
          <h4 className="text-sm font-extrabold text-text-primary">{targetExam}</h4>
          <p className="text-[11px] text-text-secondary">Stay consistent with daily recall sessions</p>
        </div>
      </div>

      <div className="text-right shrink-0">
        <span className="text-2xl font-black text-primary font-mono">{daysRemaining}</span>
        <span className="text-xs text-text-secondary block font-medium">Days Left</span>
      </div>
    </div>
  );
};
