'use client';

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Flame, Sparkles, User, Crown, Moon, Sun, Monitor } from 'lucide-react';
import { UserProfile } from '@/lib/firebase/auth';
import { QuovexButton } from '../ui/QuovexButton';
import { useTheme } from '../providers/ThemeProvider';

interface AppHeaderProps {
  profile: UserProfile | null;
}

export const AppHeader: React.FC<AppHeaderProps> = ({ profile }) => {
  const isPro = profile?.subscriptionTier && profile.subscriptionTier !== 'FREE';
  const { themeMode, resolvedTheme, setThemeMode } = useTheme();

  const avatarUrl = profile?.avatarId
    ? `/assets/avatars/avatar_${profile.avatarId}.png`
    : null;

  return (
    <header className="h-16 bg-surface/85 backdrop-blur-md border-b border-border px-4 sm:px-6 flex items-center justify-between sticky top-0 z-30 transition-colors duration-200">
      {/* Mobile Brand / Left Goal Target */}
      <div className="flex items-center gap-3">
        <Link href="/app/dashboard" className="flex items-center gap-2 lg:hidden">
          <div className="w-8 h-8 rounded-lg bg-primary-container border border-primary/40 flex items-center justify-center text-primary font-bold text-sm">
            Q
          </div>
          <span className="font-bold text-text-primary text-base">QUOVEX</span>
        </Link>

        <div className="hidden sm:flex items-center gap-2">
          <span className="text-xs text-text-secondary">Target Goal:</span>
          <span className="text-xs font-semibold text-primary bg-primary-container px-2 py-0.5 rounded-md border border-primary/30">
            {profile?.targetExam || 'JEE Advanced'}
          </span>
        </div>
      </div>

      {/* Right Stats, Quick Theme Toggle & Profile */}
      <div className="flex items-center gap-2.5 sm:gap-4">
        {/* Quick Theme Switcher */}
        <div className="flex items-center bg-surface-variant border border-border rounded-xl p-1 gap-0.5">
          <button
            onClick={() => setThemeMode('DARK')}
            title="Dark Mode"
            className={`p-1.5 rounded-lg text-xs transition-all ${
              themeMode === 'DARK'
                ? 'bg-primary text-primary-foreground font-bold shadow-sm'
                : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            <Moon className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => setThemeMode('LIGHT')}
            title="Light Mode"
            className={`p-1.5 rounded-lg text-xs transition-all ${
              themeMode === 'LIGHT'
                ? 'bg-primary text-primary-foreground font-bold shadow-sm'
                : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            <Sun className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => setThemeMode('SYSTEM')}
            title="System Theme"
            className={`p-1.5 rounded-lg text-xs transition-all ${
              themeMode === 'SYSTEM'
                ? 'bg-primary text-primary-foreground font-bold shadow-sm'
                : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            <Monitor className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Streak Badge */}
        <Link href="/app/streaks">
          <div className="flex items-center gap-1.5 px-2.5 sm:px-3 py-1.5 rounded-xl bg-[rgba(255,107,53,0.15)] border border-streak-fire/40 text-streak-fire text-xs font-bold transition-all hover:scale-105">
            <Flame className="w-4 h-4 fill-streak-fire text-streak-fire" />
            <span>{profile?.streakDays || 1}d</span>
          </div>
        </Link>

        {/* XP Badge */}
        <div className="hidden md:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-warning-container border border-warning/40 text-warning text-xs font-bold">
          <Sparkles className="w-3.5 h-3.5" />
          <span>{profile?.xp || 100} XP</span>
        </div>

        {/* Upgrade / Pro Badge */}
        {isPro ? (
          <div className="flex items-center gap-1 px-2.5 sm:px-3 py-1.5 rounded-xl bg-primary-container border border-primary/40 text-primary text-xs font-bold">
            <Crown className="w-3.5 h-3.5" />
            <span>PRO</span>
          </div>
        ) : (
          <Link href="/app/profile">
            <QuovexButton size="sm" variant="primary" className="text-xs py-1.5 px-3">
              Upgrade
            </QuovexButton>
          </Link>
        )}

        {/* Profile Avatar */}
        <Link href="/app/profile" className="flex items-center gap-2 p-0.5 rounded-full hover:ring-2 hover:ring-primary/50 transition-all">
          {avatarUrl ? (
            <div className="w-8 h-8 rounded-full overflow-hidden bg-primary-container border-2 border-primary relative">
              <Image
                src={avatarUrl}
                alt="Scholar Avatar"
                width={32}
                height={32}
                className="object-cover"
                unoptimized
              />
            </div>
          ) : (
            <div className="w-8 h-8 rounded-full bg-primary-container border-2 border-primary flex items-center justify-center text-primary text-xs font-bold">
              {profile?.name ? profile.name.charAt(0).toUpperCase() : 'S'}
            </div>
          )}
        </Link>
      </div>
    </header>
  );
};
