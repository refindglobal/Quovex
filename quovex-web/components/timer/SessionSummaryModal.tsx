'use client';

import React from 'react';
import Image from 'next/image';
import { Award, Sparkles, Clock, CheckCircle2 } from 'lucide-react';
import { QuovexButton } from '../ui/QuovexButton';
import { ASSETS } from '@/lib/assets';

interface SessionSummaryModalProps {
  durationMinutes: number;
  subject: string;
  focusScore: number;
  xpEarned: number;
  onDismiss: () => void;
}

export const SessionSummaryModal: React.FC<SessionSummaryModalProps> = ({
  durationMinutes,
  subject,
  focusScore,
  xpEarned,
  onDismiss,
}) => {
  return (
    <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-md flex items-center justify-center p-4">
      <div className="bg-surface border border-border rounded-3xl max-w-md w-full p-6 sm:p-8 space-y-6 shadow-2xl text-center animate-in zoom-in-95">
        {/* Trophy / Success Emblem */}
        <div className="w-20 h-20 relative mx-auto">
          <Image
            src={ASSETS.icons3d.trophy}
            alt="Session Completed"
            fill
            className="object-contain"
            unoptimized
          />
        </div>

        <div>
          <span className="text-label font-black text-primary uppercase tracking-widest bg-primary-container px-3 py-1.5 rounded-md border border-primary/30">
            SESSION COMPLETE
          </span>
          <h2 className="text-headline text-text-primary mt-4">Focus Goal Crushed! 🎉</h2>
          <p className="text-body text-text-secondary mt-2">
            Phenomenal deep work session logged in {subject || 'Deep Work'}.
          </p>
        </div>

        {/* 3-Stat Result Pill Grid */}
        <div className="grid grid-cols-3 gap-4 p-5 rounded-2xl bg-surface-variant border border-border">
          <div>
            <span className="text-caption text-text-secondary font-semibold block">DURATION</span>
            <span className="text-title font-black text-text-primary mt-1 block">{durationMinutes}m</span>
          </div>
          <div>
            <span className="text-caption text-text-secondary font-semibold block">FOCUS SCORE</span>
            <span className="text-title font-black text-primary mt-1 block">{focusScore}%</span>
          </div>
          <div>
            <span className="text-caption text-text-secondary font-semibold block">XP EARNED</span>
            <span className="text-title font-black text-warning mt-1 block">+{xpEarned} XP</span>
          </div>
        </div>

        {focusScore >= 85 && (
          <div className="p-3 rounded-xl bg-primary-container text-primary text-xs font-semibold flex items-center justify-center gap-2 border border-primary/30">
            <Sparkles className="w-4 h-4 shrink-0" />
            <span>+50 Bonus XP awarded for Laser Focus adherence!</span>
          </div>
        )}

        <QuovexButton variant="primary" size="lg" className="w-full" onClick={onDismiss}>
          Done • Return to Hub
        </QuovexButton>
      </div>
    </div>
  );
};
