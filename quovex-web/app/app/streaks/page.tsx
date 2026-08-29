'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  Flame,
  CheckCircle2,
  Sparkles,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import {
  subscribeToUserProfile,
  subscribeToUserSessions,
  useRescueToken,
  StudySession,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { ASSETS } from '@/lib/assets';

export default function StreaksPage() {
  const [profile, setProfile] = useState<any>(null);
  const [sessions, setSessions] = useState<StudySession[]>([]);
  const [isUsingToken, setIsUsingToken] = useState(false);
  const [tokenMessage, setTokenMessage] = useState<string | null>(null);

  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) return;

    const unsubProfile = subscribeToUserProfile(currentUser.uid, (p) => setProfile(p));
    const unsubSessions = subscribeToUserSessions(currentUser.uid, (s) => setSessions(s));

    return () => {
      unsubProfile();
      unsubSessions();
    };
  }, [currentUser]);

  const streakDays = profile?.streakDays || 1;
  const rescueTokens = profile?.rescueTokens ?? 1;
  const totalHours = (sessions.reduce((acc, s) => acc + (s.durationMinutes || 0), 0) / 60).toFixed(1);

  // Compute real 84-day (12-week) study heatmap from actual sessions
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  const heatmapDays = Array.from({ length: 84 }, (_, i) => {
    const d = new Date(today);
    d.setDate(today.getDate() - (83 - i));
    const dayStart = d.getTime();
    const dayEnd = dayStart + 86400000;

    const daySessions = sessions.filter(
      (s) => s.startTime >= dayStart && s.startTime < dayEnd && (s.durationMinutes || 0) > 0
    );
    const dayMinutes = daySessions.reduce((acc, s) => acc + (s.durationMinutes || 0), 0);

    return {
      dateStr: d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
      minutes: dayMinutes,
      level: dayMinutes >= 60 ? 3 : dayMinutes >= 30 ? 2 : dayMinutes >= 15 ? 1 : 0,
    };
  });

  const handleUseToken = async () => {
    if (!currentUser || isUsingToken || rescueTokens <= 0) return;

    setIsUsingToken(true);
    try {
      const ok = await useRescueToken(currentUser.uid);
      if (ok) {
        setTokenMessage('🛡️ Rescue Token activated! Your streak is protected for the next 24 hours.');
      } else {
        setTokenMessage('No available rescue tokens in vault.');
      }
    } catch (_) {
      setTokenMessage('Failed to activate token. Try again.');
    } finally {
      setIsUsingToken(false);
    }
  };

  const BADGES = [
    {
      id: 'b1',
      name: 'Unbroken Flame',
      desc: '7 consecutive study days',
      icon: ASSETS.icons3d.flameBurning,
      unlocked: streakDays >= 7,
    },
    {
      id: 'b2',
      name: 'Deep Work Pioneer',
      desc: '25+ hours of focus',
      icon: ASSETS.icons3d.stopwatch,
      unlocked: Number(totalHours) >= 25,
    },
    {
      id: 'b3',
      name: 'Vault Guardian',
      desc: 'Protect with Rescue Token',
      icon: ASSETS.icons3d.vaultChest,
      unlocked: Boolean(profile?.streakProtected),
    },
    {
      id: 'b4',
      name: 'Grandmaster',
      desc: 'Reach 3500+ XP',
      icon: ASSETS.icons3d.trophy,
      unlocked: (profile?.xp || 0) >= 3500,
    },
  ];

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-20">
      {/* Header */}
      <div>
        <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
          <Flame className="w-7 h-7 text-streak-fire fill-streak-fire" />
          Streaks & Scholar Progression
        </h1>
        <p className="text-xs sm:text-sm text-text-secondary mt-1">
          Daily habit consistency, Titanium Rescue Token protection, and mastery badges.
        </p>
      </div>

      {/* Main Streak & Rescue Banner */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Streak Card */}
        <QuovexCard className="md:col-span-2 p-6 sm:p-8 bg-gradient-to-br from-[rgba(255,107,53,0.12)] via-surface to-surface-elevated border-streak-fire/30 shadow-sm flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <QuovexBadge variant="fire" size="sm">UNBROKEN FLAME</QuovexBadge>
              <div className="w-12 h-12 relative">
                <Image
                  src={ASSETS.icons3d.flameBurning}
                  alt="Streak Fire"
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
            </div>

            <div className="flex items-baseline gap-3 mt-4">
              <span className="text-4xl sm:text-5xl font-black text-text-primary leading-none">{streakDays}</span>
              <span className="text-base sm:text-lg font-bold text-streak-fire">Days Consecutive</span>
            </div>
            <p className="text-xs sm:text-sm text-text-secondary mt-2 max-w-md">
              Every day of focused recall cements neural connections toward your target exam.
            </p>
          </div>

          <div className="mt-6 pt-3 border-t border-border flex flex-col sm:flex-row sm:items-center justify-between gap-1 text-xs text-text-primary">
            <span>Status: <strong className="text-primary font-bold">Active Streak</strong></span>
            <span>Next Milestone: <strong>{streakDays >= 7 ? '14 Days' : '7 Days'}</strong></span>
          </div>
        </QuovexCard>

        {/* Rescue Token Vault */}
        <QuovexCard className="p-6 space-y-4 flex flex-col justify-between border-primary/20 shadow-sm">
          <div>
            <div className="w-10 h-10 relative mb-2">
              <Image
                src={ASSETS.icons3d.vaultChest}
                alt="Vault Chest"
                fill
                className="object-contain"
                unoptimized
              />
            </div>
            <h3 className="text-sm sm:text-base font-bold text-text-primary">Rescue Vault</h3>
            <p className="text-xs text-text-secondary mt-1">
              Redeem a token to freeze and preserve your streak during an emergency.
            </p>
          </div>

          <div className="space-y-3">
            <div className="p-3 rounded-xl bg-surface-variant border border-border text-xs flex items-center justify-between">
              <span className="text-text-secondary font-bold">Balance:</span>
              <span className="font-bold text-primary font-mono">{rescueTokens} Token{rescueTokens === 1 ? '' : 's'}</span>
            </div>

            <QuovexButton
              variant="secondary"
              size="sm"
              className="w-full text-xs"
              onClick={handleUseToken}
              isLoading={isUsingToken}
              disabled={rescueTokens <= 0}
            >
              Redeem Token
            </QuovexButton>
          </div>
        </QuovexCard>
      </div>

      {tokenMessage && (
        <div className="p-3.5 rounded-xl bg-primary-container text-primary border border-primary/30 text-xs font-bold flex items-center gap-2 shadow-xs">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{tokenMessage}</span>
        </div>
      )}

      {/* ── Real 12-Week Study Heatmap ────────────────────────────────────── */}
      <QuovexCard className="p-5 sm:p-6 space-y-4 shadow-sm">
        <div className="flex items-center justify-between">
          <h3 className="text-xs sm:text-sm font-bold text-text-primary flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-primary" />
            12-Week Study Heatmap
          </h3>
          <span className="text-xs text-text-secondary font-mono font-bold">{totalHours} hrs logged</span>
        </div>

        {/* Heatmap Grid */}
        <div className="grid grid-flow-col grid-rows-7 gap-1.5 overflow-x-auto py-2 scrollbar-hide">
          {heatmapDays.map((d, idx) => {
            const bgClass =
              d.level === 3
                ? 'bg-primary'
                : d.level === 2
                ? 'bg-primary/70'
                : d.level === 1
                ? 'bg-primary/35'
                : 'bg-surface-variant border border-border/40';

            return (
              <div
                key={idx}
                className={`w-3.5 h-3.5 sm:w-4 sm:h-4 rounded-xs transition-all ${bgClass}`}
                title={`${d.dateStr}: ${d.minutes} mins`}
              />
            );
          })}
        </div>

        <div className="flex items-center justify-between text-[11px] text-text-secondary pt-3 border-t border-border">
          <span>0m</span>
          <div className="flex items-center gap-1.5">
            <div className="w-3 h-3 rounded-xs bg-surface-variant border border-border" />
            <div className="w-3 h-3 rounded-xs bg-primary/35" />
            <div className="w-3 h-3 rounded-xs bg-primary/70" />
            <div className="w-3 h-3 rounded-xs bg-primary" />
          </div>
          <span>60m+</span>
        </div>
      </QuovexCard>

      {/* ── 3D Mastery Badges Gallery ─────────────────────────────────────── */}
      <div className="space-y-3">
        <h2 className="text-xs sm:text-sm font-bold text-text-primary">Scholar Badges</h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          {BADGES.map((b) => (
            <QuovexCard
              key={b.id}
              className={`p-4 space-y-2 text-center transition-all ${
                b.unlocked
                  ? 'border-warning/30 bg-surface-elevated shadow-xs'
                  : 'border-border opacity-50 bg-surface grayscale hover:grayscale-0'
              }`}
            >
              <div className="w-12 h-12 relative mx-auto">
                <Image
                  src={b.icon}
                  alt={b.name}
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
              <h4 className="text-xs font-bold text-text-primary">{b.name}</h4>
              <p className="text-[11px] text-text-secondary">{b.desc}</p>
              <div className="pt-1">
                {b.unlocked ? (
                  <QuovexBadge variant="gold" size="sm">UNLOCKED</QuovexBadge>
                ) : (
                  <QuovexBadge variant="muted" size="sm">LOCKED</QuovexBadge>
                )}
              </div>
            </QuovexCard>
          ))}
        </div>
      </div>
    </div>
  );
}
