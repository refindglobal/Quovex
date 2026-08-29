'use client';

import React, { useEffect, useState } from 'react';
import Image from 'next/image';
import { ShieldAlert, Eye, AlertTriangle, ArrowRight, ShieldCheck } from 'lucide-react';
import { QuovexCard } from '../ui/QuovexCard';
import { QuovexBadge } from '../ui/QuovexBadge';
import { ASSETS } from '@/lib/assets';

interface WebFocusShieldProps {
  isActive: boolean;
  onDistractionDetected: (count: number) => void;
}

export const WebFocusShield: React.FC<WebFocusShieldProps> = ({
  isActive,
  onDistractionDetected,
}) => {
  const [distractionCount, setDistractionCount] = useState(0);
  const [showWarning, setShowWarning] = useState(false);

  useEffect(() => {
    if (!isActive) {
      setDistractionCount(0);
      setShowWarning(false);
      return;
    }

    const handleVisibilityChange = () => {
      if (document.hidden) {
        setDistractionCount((prev) => {
          const next = prev + 1;
          onDistractionDetected(next);
          return next;
        });
        setShowWarning(true);
        playWarningChime();
      }
    };

    const handleWindowBlur = () => {
      if (isActive) {
        setDistractionCount((prev) => {
          const next = prev + 1;
          onDistractionDetected(next);
          return next;
        });
        setShowWarning(true);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);
    window.addEventListener('blur', handleWindowBlur);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      window.removeEventListener('blur', handleWindowBlur);
    };
  }, [isActive, onDistractionDetected]);

  const playWarningChime = () => {
    try {
      const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(440, audioCtx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(880, audioCtx.currentTime + 0.2);
      gain.gain.setValueAtTime(0.15, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.3);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + 0.3);
    } catch (_) {}
  };

  return (
    <div className="space-y-4">
      <QuovexCard variant="elevated" className="p-4 sm:p-5">
        <div className="flex items-center justify-between gap-4">
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 relative shrink-0">
              <Image
                src={ASSETS.icons3d.shieldChains}
                alt="Web Focus Shield"
                fill
                className="object-contain"
                unoptimized
              />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h4 className="text-xs font-bold text-text-primary">Web Focus Tab Shield</h4>
                {isActive ? (
                  <QuovexBadge variant="emerald" size="sm">ACTIVE SHIELD</QuovexBadge>
                ) : (
                  <QuovexBadge variant="muted" size="sm">STANDBY</QuovexBadge>
                )}
              </div>
              <p className="text-[11px] text-text-secondary mt-0.5">
                Monitors browser tab switches & window blurs to compute focus score.
              </p>
            </div>
          </div>

          <div className="text-right shrink-0">
            <span className="text-xs font-bold text-text-secondary block">Distractions</span>
            <span className={`text-base font-black font-mono ${distractionCount > 0 ? 'text-warning' : 'text-primary'}`}>
              {distractionCount}
            </span>
          </div>
        </div>

        {showWarning && isActive && (
          <div className="mt-3 p-3 rounded-xl bg-warning-container text-warning text-xs font-semibold flex items-center justify-between border border-warning/30 animate-in fade-in">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>Tab switch detected! Return your attention to the study session.</span>
            </div>
            <button
              onClick={() => setShowWarning(false)}
              className="text-[10px] uppercase font-bold underline ml-2 shrink-0 hover:opacity-80"
            >
              Dismiss
            </button>
          </div>
        )}
      </QuovexCard>
    </div>
  );
};
