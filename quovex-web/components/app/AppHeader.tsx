'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Flame, Sparkles, Crown, Moon, Sun, Monitor, Target } from 'lucide-react';
import { UserProfile } from '@/lib/firebase/auth';
import { QuovexButton } from '../ui/QuovexButton';
import { useTheme } from '../providers/ThemeProvider';

import { ASSETS } from '@/lib/assets';

interface AppHeaderProps {
  profile: UserProfile | null;
}

export const AppHeader: React.FC<AppHeaderProps> = ({ profile }) => {
  const isPro = profile?.subscriptionTier && profile.subscriptionTier !== 'FREE';
  const { themeMode, setThemeMode } = useTheme();

  const avatarUrl = profile?.avatarId
    ? ASSETS.avatars(profile.avatarId)
    : null;

  return (
    <header className="h-14 bg-surface/90 backdrop-blur-md border-b border-border px-4 sm:px-6 flex items-center justify-between sticky top-0 z-30 transition-colors duration-200">
      {/* ── LEFT: Target Goal & Mobile Logo ─────────────────────────────── */}
      <div className="flex items-center gap-3">
        <Link href="/app/dashboard" className="flex items-center gap-2 lg:hidden">
          <div className="w-7 h-7 rounded-lg overflow-hidden bg-primary/10 border border-primary/30 flex items-center justify-center shrink-0">
            <img
              src={ASSETS.brand.emblem}
              alt="Quovex Logo"
              className="w-5 h-5 object-contain"
            />
          </div>
          <span className="font-black text-text-primary text-sm tracking-tight">QUOVEX</span>
        </Link>

        <div className="hidden sm:flex items-center gap-2 px-3 py-1 rounded-xl bg-surface-variant/80 border border-border text-xs">
          <Target className="w-3.5 h-3.5 text-primary" />
          <span className="text-text-secondary font-medium">Target:</span>
          <span className="font-bold text-text-primary">
            {profile?.targetExam || 'JEE Advanced'}
          </span>
        </div>
      </div>

      {/* ── RIGHT: Theme → Streak → XP → Pro → Avatar ────────────────── */}
      <div className="flex items-center gap-2 sm:gap-3">
        {/* Compact Theme Selector */}
        <div className="flex items-center bg-surface-variant border border-border rounded-lg p-0.5 gap-0.5">
          <button
            onClick={() => setThemeMode('DARK')}
            title="Dark Mode"
            className={`p-1 rounded-md text-xs transition-all ${
              themeMode === 'DARK'
                ? 'bg-primary text-primary-foreground font-bold shadow-xs'
                : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            <Moon className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => setThemeMode('LIGHT')}
            title="Light Mode"
            className={`p-1 rounded-md text-xs transition-all ${
              themeMode === 'LIGHT'
                ? 'bg-primary text-primary-foreground font-bold shadow-xs'
                : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            <Sun className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => setThemeMode('SYSTEM')}
            title="System Theme"
            className={`p-1 rounded-md text-xs transition-all ${
              themeMode === 'SYSTEM'
                ? 'bg-primary text-primary-foreground font-bold shadow-xs'
                : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            <Monitor className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Streak Indicator */}
        <Link href="/app/streaks">
          <div className="flex items-center gap-1 px-2.5 py-1 rounded-lg bg-[rgba(255,107,53,0.12)] border border-streak-fire/30 text-streak-fire text-xs font-bold transition-all hover:scale-105">
            <Flame className="w-3.5 h-3.5 fill-streak-fire text-streak-fire" />
            <span>{profile?.streakDays || 1}d</span>
          </div>
        </Link>

        {/* XP Counter */}
        <div className="hidden md:flex items-center gap-1 px-2.5 py-1 rounded-lg bg-warning-container/30 border border-warning/30 text-warning text-xs font-bold">
          <Sparkles className="w-3.5 h-3.5" />
          <span>{profile?.xp || 100} XP</span>
        </div>

        {/* Pro Badge or Upgrade CTA */}
        {isPro ? (
          <div className="flex items-center gap-1 px-2 py-1 rounded-lg bg-primary-container border border-primary/40 text-primary text-xs font-bold">
            <Crown className="w-3.5 h-3.5" />
            <span>PRO</span>
          </div>
        ) : (
          <Link href="/app/profile">
            <QuovexButton size="sm" variant="primary" className="text-xs py-1 px-2.5 h-7">
              Upgrade
            </QuovexButton>
          </Link>
        )}

        {/* User Avatar */}
        <Link href="/app/profile" className="flex items-center rounded-full hover:ring-2 hover:ring-primary/40 transition-all ml-0.5">
          {avatarUrl ? (
            <div className="w-7 h-7 rounded-full overflow-hidden bg-primary-container border border-primary/50 relative">
              <Image
                src={avatarUrl}
                alt="Scholar Avatar"
                width={28}
                height={28}
                className="object-cover"
                unoptimized
              />
            </div>
          ) : (
            <div className="w-7 h-7 rounded-full bg-primary-container border border-primary/50 flex items-center justify-center text-primary text-xs font-bold">
              {profile?.name ? profile.name.charAt(0).toUpperCase() : 'S'}
            </div>
          )}
        </Link>
      </div>
    </header>
  );
};
