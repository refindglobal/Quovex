'use client';

import React from 'react';
import Image from 'next/image';
import { Calendar, Flame } from 'lucide-react';
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
    'CUET (UG)': 72,
    'CLAT (UG)': 114,
    'UPSC CSE': 118,
    'CBSE Class 12': 38,
    'CBSE Class 11': 65,
    'CBSE Class 10': 35,
  };

  const daysRemaining = examDaysMap[targetExam] || 60;

  return (
    <div className="flex items-center justify-between gap-4 p-5 rounded-2xl bg-surface border border-border shadow-sm">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-primary/10 border border-primary/30 flex items-center justify-center shrink-0">
          <Calendar className="w-5 h-5 text-primary" />
        </div>
        <div>
          <span className="text-[10px] font-bold text-primary uppercase tracking-wider block">Competitive Target</span>
          <h4 className="text-sm sm:text-base font-extrabold text-text-primary">{targetExam}</h4>
          <p className="text-[11px] text-text-secondary">Official Exam Milestone</p>
        </div>
      </div>

      <div className="text-right shrink-0 px-3 py-1.5 rounded-xl bg-surface-variant border border-border">
        <span className="text-xl sm:text-2xl font-black text-primary font-mono block leading-none">{daysRemaining}</span>
        <span className="text-[10px] text-text-secondary font-bold uppercase tracking-wider">Days Left</span>
      </div>
    </div>
  );
};
