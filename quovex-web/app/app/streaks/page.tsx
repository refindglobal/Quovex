'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  Flame,
  Shield,
  Award,
  Sparkles,
  CheckCircle2,
  Zap,
  Trophy,
  Star,
  Lock,
  AlertTriangle,
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

  // Compute REAL 84-day (12-week) study heatmap from actual sessions
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
      desc: 'Maintain 7 consecutive study days',
      icon: ASSETS.icons3d.flameBurning,
      unlocked: streakDays >= 7,
    },
    {
      id: 'b2',
      name: 'Deep Work Pioneer',
      desc: 'Log 25+ total hours of deep study',
      icon: ASSETS.icons3d.stopwatch,
      unlocked: Number(totalHours) >= 25,
    },
    {
      id: 'b3',
      name: 'Titanium Vault Guardian',
      desc: 'Protect streak with Rescue Token',
      icon: ASSETS.icons3d.vaultChest,
      unlocked: Boolean(profile?.streakProtected),
    },
    {
      id: 'b4',
      name: 'Grandmaster Trophy',
      desc: 'Reach Grandmaster Scholar Rank (3500+ XP)',
      icon: ASSETS.icons3d.trophy,
      unlocked: (profile?.xp || 0) >= 3500,
    },
  ];

  return (
    <div className="max-w-5xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div>
        <h1 className="text-display font-black text-text-primary flex items-center gap-4">
          <Flame className="w-10 h-10 text-streak-fire fill-streak-fire" />
          Streaks & Scholar Progression
        </h1>
        <p className="text-section text-text-secondary mt-2">
          Daily habit consistency, Titanium Rescue Token protection, and 3D mastery badges.
        </p>
      </div>

      {/* Main Streak & Rescue Banner */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Streak Card */}
        <QuovexCard className="md:col-span-2 p-8 sm:p-12 bg-gradient-to-br from-[rgba(255,107,53,0.15)] via-surface to-surface-elevated border-streak-fire/40 shadow-glow flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <QuovexBadge variant="fire" size="lg">UNBROKEN FLAME</QuovexBadge>
              <div className="w-16 h-16 relative">
                <Image
                  src={ASSETS.icons3d.flameBurning}
                  alt="Streak Fire"
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
            </div>

            <div className="flex items-baseline gap-4 mt-6">
              <span className="text-[5rem] font-black text-text-primary leading-none">{streakDays}</span>
              <span className="text-headline font-bold text-streak-fire">Days Consecutive</span>
            </div>
            <p className="text-body text-text-secondary mt-4 max-w-md">
              Every day of focused recall cements neural connections and compounds toward your competitive exam target.
            </p>
          </div>

          <div className="mt-10 pt-5 border-t border-border flex flex-col sm:flex-row sm:items-center justify-between gap-2 text-body text-text-primary">
            <span>Today's Status: <strong className="text-primary font-bold">Active Streak</strong></span>
            <span>Next Milestone: <strong>{streakDays >= 7 ? '14 Days' : '7 Days'}</strong></span>
          </div>
        </QuovexCard>

        {/* Titanium Rescue Token Vault */}
        <QuovexCard className="p-8 space-y-6 flex flex-col justify-between border-primary/30 shadow-sm">
          <div>
            <div className="w-16 h-16 relative mb-4">
              <Image
                src={ASSETS.icons3d.vaultChest}
                alt="Vault Chest"
                fill
                className="object-contain"
                unoptimized
              />
            </div>
            <h3 className="text-title font-bold text-text-primary">Rescue Token Vault</h3>
            <p className="text-body text-text-secondary mt-2 leading-relaxed">
              If an emergency breaks your streak, redeem a token to freeze and preserve your progress.
            </p>
          </div>

          <div className="space-y-4">
            <div className="p-4 rounded-xl bg-surface-variant border border-border text-body flex items-center justify-between shadow-sm">
              <span className="text-text-secondary font-bold">Vault Balance:</span>
              <span className="font-bold text-primary font-mono">{rescueTokens} Token{rescueTokens === 1 ? '' : 's'}</span>
            </div>

            <QuovexButton
              variant="secondary"
              size="lg"
              className="w-full text-body"
              onClick={handleUseToken}
              isLoading={isUsingToken}
              disabled={rescueTokens <= 0}
            >
              Redeem Rescue Token
            </QuovexButton>
          </div>
        </QuovexCard>
      </div>

      {tokenMessage && (
        <div className="p-5 rounded-2xl bg-primary-container text-primary border border-primary/30 text-body font-bold flex items-center gap-3 shadow-sm">
          <CheckCircle2 className="w-5 h-5 shrink-0" />
          <span>{tokenMessage}</span>
        </div>
      )}

      {/* ── Real 12-Week Study Heatmap ────────────────────────────────────── */}
      <QuovexCard className="p-8 space-y-6 shadow-sm">
        <div className="flex items-center justify-between">
          <h3 className="text-title font-bold text-text-primary flex items-center gap-3">
            <Sparkles className="w-5 h-5 text-primary" />
            12-Week Real Session Heatmap
          </h3>
          <span className="text-label text-text-secondary font-mono font-bold">{totalHours} total hours logged</span>
        </div>

        {/* Heatmap Grid (7 rows x 12 columns = 84 days) */}
        <div className="grid grid-flow-col grid-rows-7 gap-2 overflow-x-auto py-4 scrollbar-hide">
          {heatmapDays.map((d, idx) => {
            const bgClass =
              d.level === 3
                ? 'bg-primary shadow-sm'
                : d.level === 2
                ? 'bg-primary/70'
                : d.level === 1
                ? 'bg-primary/35'
                : 'bg-surface-variant border border-border/50';

            return (
              <div
                key={idx}
                className={`w-4 h-4 sm:w-5 sm:h-5 rounded-sm transition-all ${bgClass}`}
                title={`${d.dateStr}: ${d.minutes} focus minutes`}
              />
            );
          })}
        </div>

        <div className="flex items-center justify-between text-label text-text-secondary pt-4 border-t border-border font-bold">
          <span>Less Active (0m)</span>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded-sm bg-surface-variant border border-border" />
            <div className="w-4 h-4 rounded-sm bg-primary/35" />
            <div className="w-4 h-4 rounded-sm bg-primary/70" />
            <div className="w-4 h-4 rounded-sm bg-primary shadow-sm" />
          </div>
          <span>Goal Met (60m+)</span>
        </div>
      </QuovexCard>

      {/* ── 3D Mastery Badges Gallery ─────────────────────────────────────── */}
      <div className="space-y-6">
        <h2 className="text-title font-bold text-text-primary">Scholar Mastery Badges</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
          {BADGES.map((b) => (
            <QuovexCard
              key={b.id}
              className={`p-6 space-y-4 text-center transition-all ${
                b.unlocked
                  ? 'border-warning/30 bg-surface-elevated shadow-glow-sm'
                  : 'border-border opacity-50 bg-surface grayscale hover:grayscale-0'
              }`}
            >
              <div className="w-20 h-20 relative mx-auto">
                <Image
                  src={b.icon}
                  alt={b.name}
                  fill
                  className="object-contain"
                  unoptimized
                />
              </div>
              <h4 className="text-body font-bold text-text-primary">{b.name}</h4>
              <p className="text-label text-text-secondary leading-relaxed">{b.desc}</p>
              <div className="pt-2">
                {b.unlocked ? (
                  <QuovexBadge variant="gold" size="md">UNLOCKED</QuovexBadge>
                ) : (
                  <QuovexBadge variant="muted" size="md">LOCKED</QuovexBadge>
                )}
              </div>
            </QuovexCard>
          ))}
        </div>
      </div>
    </div>
  );
}
