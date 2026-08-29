'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  User,
  Shield,
  Crown,
  Sparkles,
  Flame,
  CheckCircle2,
  Lock,
  LogOut,
  Moon,
  Sun,
  Monitor,
  Tag,
  ArrowRight,
  Clock,
  Award,
  BookOpen,
  Share2,
} from 'lucide-react';
import {
  subscribeToUserProfile,
  subscribeToUserSessions,
  updateUserProfile,
  StudySession,
} from '@/lib/firebase/firestore';
import { getCurrentUser, signOut, UserProfile } from '@/lib/firebase/auth';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { useTheme } from '@/components/providers/ThemeProvider';
import { ASSETS } from '@/lib/assets';

const EXAMS = [
  'JEE Advanced',
  'JEE Mains',
  'NEET (UG)',
  'UPSC CSE',
  'CBSE Class 12',
  'CBSE Class 11',
  'CBSE Class 10',
  'General Competitive',
];

export default function ProfilePage() {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [sessions, setSessions] = useState<StudySession[]>([]);
  const [isEditingAvatar, setIsEditingAvatar] = useState(false);
  const [couponCode, setCouponCode] = useState('');
  const [couponStatus, setCouponStatus] = useState<{ message: string; success: boolean } | null>(null);
  const [isValidatingCoupon, setIsValidatingCoupon] = useState(false);
  const [targetExam, setTargetExam] = useState('JEE Advanced');
  const [dailyGoalHours, setDailyGoalHours] = useState(4);
  const [isSavingSettings, setIsSavingSettings] = useState(false);
  const [showUpgradeModal, setShowUpgradeModal] = useState(false);

  const { themeMode, resolvedTheme, setThemeMode } = useTheme();
  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) return;

    const unsubProfile = subscribeToUserProfile(currentUser.uid, (p) => {
      if (p) {
        setProfile(p);
        setTargetExam(p.targetExam || 'JEE Advanced');
        setDailyGoalHours(p.dailyGoalHours || 4);
      }
    });

    const unsubSessions = subscribeToUserSessions(currentUser.uid, (s) => {
      setSessions(s);
    });

    return () => {
      unsubProfile();
      unsubSessions();
    };
  }, [currentUser]);

  // Compute real user statistics
  const totalStudyMinutes = sessions.reduce((acc, s) => acc + (s.durationMinutes || 0), 0);
  const totalStudyHours = (totalStudyMinutes / 60).toFixed(1);
  const avgFocusScore = sessions.length > 0
    ? Math.round(sessions.reduce((acc, s) => acc + (s.focusScore || 90), 0) / sessions.length)
    : 95;

  const currentXp = profile?.xp || 100;
  
  // 4 Scholar Ranks matching Android
  const rankInfo = currentXp >= 3500
    ? { name: 'Grandmaster', level: 4, icon: ASSETS.icons3d.rankGrandmaster, nextXp: 5000, progress: 100 }
    : currentXp >= 1500
    ? { name: 'Strategist', level: 3, icon: ASSETS.icons3d.rankStrategist, nextXp: 3500, progress: Math.min(100, Math.round(((currentXp - 1500) / 2000) * 100)) }
    : currentXp >= 500
    ? { name: 'Apprentice', level: 2, icon: ASSETS.icons3d.rankApprentice, nextXp: 1500, progress: Math.min(100, Math.round(((currentXp - 500) / 1000) * 100)) }
    : { name: 'Novice Scholar', level: 1, icon: ASSETS.icons3d.rankNovice, nextXp: 500, progress: Math.min(100, Math.round((currentXp / 500) * 100)) };

  const isPro = profile?.subscriptionTier && profile.subscriptionTier !== 'FREE';

  const handleSelectAvatar = async (avatarId: number) => {
    if (!currentUser) return;
    await updateUserProfile(currentUser.uid, { avatarId });
    setIsEditingAvatar(false);
  };

  const handleSavePreferences = async () => {
    if (!currentUser) return;
    setIsSavingSettings(true);
    try {
      await updateUserProfile(currentUser.uid, {
        targetExam,
        dailyGoalHours: Number(dailyGoalHours),
      });
    } finally {
      setIsSavingSettings(false);
    }
  };

  const handleApplyCoupon = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!couponCode.trim() || !currentUser) return;

    setIsValidatingCoupon(true);
    setCouponStatus(null);

    try {
      const code = couponCode.trim().toUpperCase();
      if (code === 'FOUNDER50' || code === 'STUDENT100' || code === 'QUOVEXVIP') {
        await updateUserProfile(currentUser.uid, {
          subscriptionTier: 'PRO_ANNUAL',
        });
        setCouponStatus({ message: '🎉 Coupon verified! 100% Pro Annual pass activated.', success: true });
        setCouponCode('');
      } else {
        setCouponStatus({ message: 'Invalid or expired promotion coupon code.', success: false });
      }
    } catch (_) {
      setCouponStatus({ message: 'Could not validate coupon. Try again later.', success: false });
    } finally {
      setIsValidatingCoupon(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-12 pb-24">
      {/* ── 1. Hero Identity & Scholar Rank Card ─────────────────────────────── */}
      <QuovexCard variant="glass" className="relative overflow-hidden p-8 sm:p-12 shadow-sm">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-8">
          <div className="flex items-center gap-6">
            {/* Avatar with Click-to-Change */}
            <div className="relative group cursor-pointer" onClick={() => setIsEditingAvatar(true)}>
              <div className="w-24 h-24 sm:w-28 sm:h-28 rounded-2xl overflow-hidden bg-primary-container border-2 border-primary shadow-glow flex items-center justify-center relative">
                {profile?.avatarId ? (
                  <Image
                    src={ASSETS.avatars(profile.avatarId)}
                    alt="Scholar Avatar"
                    width={112}
                    height={112}
                    className="object-cover transition-transform group-hover:scale-105"
                    unoptimized
                  />
                ) : (
                  <span className="text-display font-black text-primary">
                    {profile?.name ? profile.name.charAt(0).toUpperCase() : 'S'}
                  </span>
                )}
              </div>
              <div className="absolute inset-0 rounded-2xl bg-black/60 opacity-0 group-hover:opacity-100 flex items-center justify-center text-white text-label font-bold transition-opacity">
                Change Avatar
              </div>
            </div>

            <div>
              <div className="flex flex-col sm:flex-row sm:items-center gap-3">
                <h1 className="text-display font-black text-text-primary">
                  {profile?.name || 'Scholar'}
                </h1>
                <div className="self-start sm:self-auto">
                  {isPro ? (
                    <QuovexBadge variant="gold" size="lg">PRO VIP</QuovexBadge>
                  ) : (
                    <QuovexBadge variant="emerald" size="lg">FREE SCHOLAR</QuovexBadge>
                  )}
                </div>
              </div>
              <p className="text-body text-text-secondary mt-2">{profile?.email}</p>

              {/* Scholar Rank Chip */}
              <div className="flex items-center gap-3 mt-4">
                <div className="w-8 h-8 relative shrink-0 drop-shadow-sm">
                  <Image
                    src={rankInfo.icon}
                    alt={rankInfo.name}
                    fill
                    className="object-contain"
                    unoptimized
                  />
                </div>
                <span className="text-body font-extrabold text-primary">
                  Level {rankInfo.level} — {rankInfo.name}
                </span>
              </div>
            </div>
          </div>

          {/* Quick Actions */}
          <div className="flex flex-col sm:items-end gap-3 w-full sm:w-auto">
            {!isPro && (
              <QuovexButton
                variant="primary"
                size="lg"
                onClick={() => setShowUpgradeModal(true)}
                leftIcon={<Crown className="w-5 h-5" />}
              >
                Upgrade to Pro
              </QuovexButton>
            )}
            <span className="text-label text-text-tertiary font-mono font-bold">
              UID: {currentUser?.uid.slice(0, 12)}...
            </span>
          </div>
        </div>

        {/* Scholar XP Progression Bar */}
        <div className="mt-8 pt-6 border-t border-border space-y-3">
          <div className="flex items-center justify-between text-body">
            <span className="font-semibold text-text-secondary">Scholar XP Progress</span>
            <span className="font-mono font-bold text-text-primary">{currentXp} / {rankInfo.nextXp} XP</span>
          </div>
          <div className="w-full h-4 rounded-full bg-surface-variant overflow-hidden border border-border">
            <div
              className="h-full bg-gradient-to-r from-primary to-secondary transition-all duration-500 rounded-full shadow-glow"
              style={{ width: `${rankInfo.progress}%` }}
            />
          </div>
        </div>
      </QuovexCard>

      {/* ── 2. Real Stats 3-Grid ────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
        <QuovexCard className="flex items-center gap-5 p-6 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-primary-container border border-primary/40 flex items-center justify-center shrink-0 shadow-sm">
            <Clock className="w-7 h-7 text-primary" />
          </div>
          <div>
            <p className="text-label text-text-secondary font-bold uppercase tracking-wider mb-1">Focus Hours Logged</p>
            <p className="text-display font-black text-text-primary">{totalStudyHours} hrs</p>
          </div>
        </QuovexCard>

        <QuovexCard className="flex items-center gap-5 p-6 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-[rgba(255,107,53,0.15)] border border-streak-fire/40 flex items-center justify-center shrink-0 shadow-sm">
            <Flame className="w-7 h-7 text-streak-fire fill-streak-fire" />
          </div>
          <div>
            <p className="text-label text-text-secondary font-bold uppercase tracking-wider mb-1">Active Streak</p>
            <p className="text-display font-black text-text-primary">{profile?.streakDays || 1} Days</p>
          </div>
        </QuovexCard>

        <QuovexCard className="flex items-center gap-5 p-6 shadow-sm">
          <div className="w-14 h-14 rounded-2xl bg-warning-container border border-warning/40 flex items-center justify-center shrink-0 shadow-sm">
            <Award className="w-7 h-7 text-warning" />
          </div>
          <div>
            <p className="text-label text-text-secondary font-bold uppercase tracking-wider mb-1">Average Focus Score</p>
            <p className="text-display font-black text-text-primary">{avgFocusScore}%</p>
          </div>
        </QuovexCard>
      </div>

      {/* ── 3. Canonical 3-Mode Theme Selector (Dark / Light / System) ─────── */}
      <QuovexCard className="space-y-6 p-8 shadow-sm">
        <div>
          <h2 className="text-title font-bold text-text-primary flex items-center gap-3">
            <Moon className="w-6 h-6 text-primary" />
            Appearance & Theme Engine
          </h2>
          <p className="text-body text-text-secondary mt-1.5">
            Choose your preferred interface theme. Synchronized seamlessly across all screens and components.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2">
          <button
            onClick={() => setThemeMode('DARK')}
            className={`p-5 rounded-2xl border flex flex-col items-start gap-3 transition-all text-left ${
              themeMode === 'DARK'
                ? 'bg-primary-container border-primary shadow-glow-sm text-primary font-bold'
                : 'bg-surface-variant border-border text-text-secondary hover:text-text-primary hover:border-border/80'
            }`}
          >
            <Moon className="w-6 h-6 shrink-0" />
            <div>
              <p className="text-body font-bold text-text-primary">🌙 Cyber Dark</p>
              <p className="text-label opacity-80 mt-1">Default Emerald palette</p>
            </div>
          </button>

          <button
            onClick={() => setThemeMode('LIGHT')}
            className={`p-5 rounded-2xl border flex flex-col items-start gap-3 transition-all text-left ${
              themeMode === 'LIGHT'
                ? 'bg-primary-container border-primary shadow-glow-sm text-primary font-bold'
                : 'bg-surface-variant border-border text-text-secondary hover:text-text-primary hover:border-border/80'
            }`}
          >
            <Sun className="w-6 h-6 shrink-0" />
            <div>
              <p className="text-body font-bold text-text-primary">☀️ Clean Light</p>
              <p className="text-label opacity-80 mt-1">Crisp high-contrast day mode</p>
            </div>
          </button>

          <button
            onClick={() => setThemeMode('SYSTEM')}
            className={`p-5 rounded-2xl border flex flex-col items-start gap-3 transition-all text-left ${
              themeMode === 'SYSTEM'
                ? 'bg-primary-container border-primary shadow-glow-sm text-primary font-bold'
                : 'bg-surface-variant border-border text-text-secondary hover:text-text-primary hover:border-border/80'
            }`}
          >
            <Monitor className="w-6 h-6 shrink-0" />
            <div>
              <p className="text-body font-bold text-text-primary">🔄 System Default</p>
              <p className="text-label opacity-80 mt-1">Follows OS theme automatically</p>
            </div>
          </button>
        </div>
      </QuovexCard>

      {/* ── 4. Study Target & Preferences ─────────────────────────────────── */}
      <QuovexCard className="space-y-6 p-8 shadow-sm">
        <div>
          <h2 className="text-title font-bold text-text-primary flex items-center gap-3">
            <BookOpen className="w-6 h-6 text-primary" />
            Academic Target & Goals
          </h2>
          <p className="text-body text-text-secondary mt-1.5">
            Personalize your AI study coach and diagnostic quizzes to your specific competitive exam.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 pt-2">
          <div>
            <label className="block text-label font-bold text-text-secondary uppercase tracking-wider mb-2">
              Target Competitive Exam
            </label>
            <select
              value={targetExam}
              onChange={(e) => setTargetExam(e.target.value)}
              className="w-full bg-surface-variant border border-border rounded-xl px-5 py-3.5 text-body text-text-primary font-bold focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all cursor-pointer"
            >
              {EXAMS.map((ex) => (
                <option key={ex} value={ex} className="bg-surface text-text-primary">
                  {ex}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-label font-bold text-text-secondary uppercase tracking-wider mb-2">
              Daily Focus Target (Hours)
            </label>
            <input
              type="number"
              min={1}
              max={16}
              value={dailyGoalHours}
              onChange={(e) => setDailyGoalHours(Number(e.target.value))}
              className="w-full bg-surface-variant border border-border rounded-xl px-5 py-3.5 text-body text-text-primary font-bold focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
            />
          </div>
        </div>

        <div className="flex justify-end pt-2">
          <QuovexButton
            variant="primary"
            size="lg"
            onClick={handleSavePreferences}
            isLoading={isSavingSettings}
          >
            Save Target Settings
          </QuovexButton>
        </div>
      </QuovexCard>

      {/* ── 5. Promotion Coupon & Entitlement ───────────────────────────────── */}
      <QuovexCard className="space-y-6 p-8 shadow-sm">
        <div>
          <h2 className="text-title font-bold text-text-primary flex items-center gap-3">
            <Tag className="w-6 h-6 text-primary" />
            Marketing & Institutional Coupons
          </h2>
          <p className="text-body text-text-secondary mt-1.5">
            Redeem official Quovex promo codes, school partner licenses, or founder coupons.
          </p>
        </div>

        <form onSubmit={handleApplyCoupon} className="flex flex-col sm:flex-row gap-4 pt-2">
          <input
            type="text"
            placeholder="e.g. FOUNDER50 or STUDENT100"
            value={couponCode}
            onChange={(e) => setCouponCode(e.target.value)}
            className="flex-1 bg-surface-variant border border-border rounded-xl px-5 py-3.5 text-body text-text-primary font-mono font-bold uppercase focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
          />
          <QuovexButton
            type="submit"
            variant="secondary"
            size="lg"
            isLoading={isValidatingCoupon}
            disabled={!couponCode.trim()}
          >
            Redeem Coupon
          </QuovexButton>
        </form>

        {couponStatus && (
          <div
            className={`p-4 rounded-xl text-body font-bold flex items-center gap-3 shadow-sm ${
              couponStatus.success
                ? 'bg-success-container text-success border border-success/30'
                : 'bg-error-container text-error border border-error/30'
            }`}
          >
            <CheckCircle2 className="w-5 h-5 shrink-0" />
            <span>{couponStatus.message}</span>
          </div>
        )}
      </QuovexCard>

      {/* ── 6. Referral Program Banner ──────────────────────────────────────── */}
      <QuovexCard variant="elevated" className="border-primary/40 bg-gradient-to-r from-primary-container/40 to-surface-elevated p-8">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div className="flex items-center gap-5">
            <div className="w-14 h-14 rounded-2xl bg-primary text-primary-foreground flex items-center justify-center shrink-0 shadow-glow">
              <Share2 className="w-7 h-7" />
            </div>
            <div>
              <h3 className="text-title font-bold text-text-primary">Refer a Study Partner — Get 7 Days Pro Free</h3>
              <p className="text-body text-text-secondary mt-1">Your Referral Code: <span className="font-mono font-bold text-primary">{currentUser?.uid.slice(0, 6).toUpperCase()}</span></p>
            </div>
          </div>
          <QuovexButton
            variant="secondary"
            size="lg"
            className="w-full sm:w-auto"
            onClick={() => {
              if (navigator.clipboard) {
                navigator.clipboard.writeText(`Join me on Quovex AI Study Operating System! Use code ${currentUser?.uid.slice(0, 6).toUpperCase()} at https://quovex.online`);
                alert('Referral link copied to clipboard!');
              }
            }}
          >
            Copy Link
          </QuovexButton>
        </div>
      </QuovexCard>

      {/* ── 7. Sign Out Action ──────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row items-center justify-between pt-8 gap-4 border-t border-border">
        <span className="text-label font-bold text-text-tertiary">Thought and crafted with precision in Noida, India 🇮🇳</span>
        <button
          onClick={() => signOut()}
          className="flex items-center gap-2 px-6 py-3 rounded-xl text-body font-bold text-error hover:bg-error-container border border-error/20 transition-colors shadow-sm"
        >
          <LogOut className="w-5 h-5" />
          Sign Out of Account
        </button>
      </div>

      {/* ── 8. Avatar Picker Modal ──────────────────────────────────────────── */}
      {isEditingAvatar && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-3xl max-w-2xl w-full p-8 space-y-8 shadow-2xl animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-headline font-black text-text-primary">Choose Your Scholar Avatar</h3>
                <p className="text-body text-text-secondary mt-1">Select from 12 official transparent illustrated characters</p>
              </div>
              <button
                onClick={() => setIsEditingAvatar(false)}
                className="w-10 h-10 rounded-full bg-surface-variant text-text-secondary hover:text-text-primary flex items-center justify-center transition-colors"
              >
                ✕
              </button>
            </div>

            <div className="grid grid-cols-3 sm:grid-cols-4 gap-4">
              {Array.from({ length: 12 }, (_, i) => i + 1).map((id) => (
                <button
                  key={id}
                  onClick={() => handleSelectAvatar(id)}
                  className={`relative p-4 rounded-2xl border transition-all flex flex-col items-center gap-3 ${
                    profile?.avatarId === id
                      ? 'bg-primary-container border-primary shadow-glow-sm scale-105'
                      : 'bg-surface-variant border-border hover:border-primary/50 hover:bg-surface-elevated'
                  }`}
                >
                  <div className="w-20 h-20 relative drop-shadow-md">
                    <Image
                      src={ASSETS.avatars(id)}
                      alt={`Avatar ${id}`}
                      fill
                      className="object-contain"
                      unoptimized
                    />
                  </div>
                  <span className="text-label font-bold text-text-secondary">Avatar #{id}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* ── 9. Upgrade Modal (Phase 18 Foundation) ──────────────────────────── */}
      {showUpgradeModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-3xl max-w-md w-full p-8 space-y-8 shadow-2xl animate-in fade-in zoom-in-95">
            <div className="text-center space-y-3">
              <div className="w-16 h-16 rounded-2xl bg-primary-container border border-primary text-primary flex items-center justify-center mx-auto shadow-glow">
                <Crown className="w-8 h-8" />
              </div>
              <h3 className="text-headline font-black text-text-primary">Unlock Quovex Pro VIP</h3>
              <p className="text-body text-text-secondary leading-relaxed px-4">
                Unlimited AI tutoring, camera focus mode, Quovex Originals, and 7-day streak protection.
              </p>
            </div>

            <div className="p-5 rounded-2xl bg-surface-variant border border-border space-y-4 shadow-sm">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-title font-bold text-text-primary">Pro Annual Plan</p>
                  <p className="text-body text-text-secondary mt-1">Billed annually (₹83/month)</p>
                </div>
                <span className="text-headline font-black text-primary">₹999 / yr</span>
              </div>
            </div>

            <div className="space-y-3 pt-2">
              <QuovexButton
                variant="primary"
                size="lg"
                className="w-full"
                onClick={async () => {
                  if (currentUser) {
                    await updateUserProfile(currentUser.uid, { subscriptionTier: 'PRO_ANNUAL' });
                    setShowUpgradeModal(false);
                    alert('🎉 Welcome to Quovex Pro VIP! All premium capabilities unlocked.');
                  }
                }}
              >
                Start 7-Day Free Trial
              </QuovexButton>
              <QuovexButton
                variant="ghost"
                size="lg"
                className="w-full text-body"
                onClick={() => setShowUpgradeModal(false)}
              >
                Maybe Later
              </QuovexButton>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
