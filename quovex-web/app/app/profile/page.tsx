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
  'CUET (UG)',
  'CLAT (UG)',
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

  const { themeMode, setThemeMode } = useTheme();
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

  const totalStudyMinutes = sessions.reduce((acc, s) => acc + (s.durationMinutes || 0), 0);
  const totalStudyHours = (totalStudyMinutes / 60).toFixed(1);
  const avgFocusScore = sessions.length > 0
    ? Math.round(sessions.reduce((acc, s) => acc + (s.focusScore || 90), 0) / sessions.length)
    : 95;

  const currentXp = profile?.xp || 100;
  
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
    <div className="max-w-4xl mx-auto space-y-6 pb-20">
      {/* ── 1. Hero Identity & Scholar Rank Card ─────────────────────────────── */}
      <QuovexCard className="p-5 sm:p-8 shadow-sm">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
          <div className="flex items-center gap-4 sm:gap-5">
            {/* Avatar with Click-to-Change */}
            <div className="relative group cursor-pointer shrink-0" onClick={() => setIsEditingAvatar(true)}>
              <div className="w-16 h-16 sm:w-20 sm:h-20 rounded-2xl overflow-hidden bg-primary-container border-2 border-primary shadow-sm flex items-center justify-center relative">
                {profile?.avatarId ? (
                  <Image
                    src={ASSETS.avatars(profile.avatarId)}
                    alt="Scholar Avatar"
                    width={80}
                    height={80}
                    className="object-cover transition-transform group-hover:scale-105"
                    unoptimized
                  />
                ) : (
                  <span className="text-xl sm:text-2xl font-black text-primary">
                    {profile?.name ? profile.name.charAt(0).toUpperCase() : 'S'}
                  </span>
                )}
              </div>
              <div className="absolute inset-0 rounded-2xl bg-black/60 opacity-0 group-hover:opacity-100 flex items-center justify-center text-white text-[10px] font-bold transition-opacity">
                Change
              </div>
            </div>

            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-lg sm:text-xl font-bold text-text-primary">
                  {profile?.name || 'Scholar'}
                </h1>
                {isPro ? (
                  <QuovexBadge variant="gold" size="sm">PRO VIP</QuovexBadge>
                ) : (
                  <QuovexBadge variant="emerald" size="sm">FREE</QuovexBadge>
                )}
              </div>
              <p className="text-xs text-text-secondary mt-0.5">{profile?.email}</p>

              {/* Scholar Rank Chip */}
              <div className="flex items-center gap-2 mt-2">
                <div className="w-5 h-5 relative shrink-0">
                  <Image
                    src={rankInfo.icon}
                    alt={rankInfo.name}
                    fill
                    className="object-contain"
                    unoptimized
                  />
                </div>
                <span className="text-xs font-bold text-primary">
                  Level {rankInfo.level} — {rankInfo.name}
                </span>
              </div>
            </div>
          </div>

          {/* Quick Upgrade Button */}
          <div className="flex flex-col sm:items-end gap-2 w-full sm:w-auto">
            {!isPro && (
              <QuovexButton
                variant="primary"
                size="sm"
                onClick={() => setShowUpgradeModal(true)}
                leftIcon={<Crown className="w-4 h-4" />}
              >
                Upgrade to Pro
              </QuovexButton>
            )}
            <span className="text-[10px] text-text-tertiary font-mono">
              UID: {currentUser?.uid.slice(0, 10)}...
            </span>
          </div>
        </div>

        {/* Scholar XP Progression Bar */}
        <div className="mt-5 pt-4 border-t border-border space-y-1.5">
          <div className="flex items-center justify-between text-xs">
            <span className="font-semibold text-text-secondary">Scholar XP Progress</span>
            <span className="font-mono font-bold text-text-primary">{currentXp} / {rankInfo.nextXp} XP</span>
          </div>
          <div className="w-full h-2.5 rounded-full bg-surface-variant overflow-hidden border border-border">
            <div
              className="h-full bg-primary transition-all duration-500 rounded-full"
              style={{ width: `${rankInfo.progress}%` }}
            />
          </div>
        </div>
      </QuovexCard>

      {/* ── 2. Real Stats 3-Grid ────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 sm:gap-4">
        <QuovexCard className="flex items-center gap-3.5 p-4 shadow-sm">
          <div className="w-11 h-11 rounded-xl bg-primary-container border border-primary/30 flex items-center justify-center shrink-0">
            <Clock className="w-5 h-5 text-primary" />
          </div>
          <div>
            <p className="text-[10px] text-text-secondary font-bold uppercase tracking-wider">Total Focus Time</p>
            <p className="text-lg font-black text-text-primary">{totalStudyHours} hrs</p>
          </div>
        </QuovexCard>

        <QuovexCard className="flex items-center gap-3.5 p-4 shadow-sm">
          <div className="w-11 h-11 rounded-xl bg-[rgba(255,107,53,0.15)] border border-streak-fire/30 flex items-center justify-center shrink-0">
            <Flame className="w-5 h-5 text-streak-fire fill-streak-fire" />
          </div>
          <div>
            <p className="text-[10px] text-text-secondary font-bold uppercase tracking-wider">Active Streak</p>
            <p className="text-lg font-black text-text-primary">{profile?.streakDays || 1} Days</p>
          </div>
        </QuovexCard>

        <QuovexCard className="flex items-center gap-3.5 p-4 shadow-sm">
          <div className="w-11 h-11 rounded-xl bg-warning-container/30 border border-warning/30 flex items-center justify-center shrink-0">
            <Award className="w-5 h-5 text-warning" />
          </div>
          <div>
            <p className="text-[10px] text-text-secondary font-bold uppercase tracking-wider">Average Focus</p>
            <p className="text-lg font-black text-text-primary">{avgFocusScore}%</p>
          </div>
        </QuovexCard>
      </div>

      {/* ── 3. Canonical 3-Mode Theme Selector (Dark / Light / System) ─────── */}
      <QuovexCard className="space-y-4 p-5 sm:p-6 shadow-sm">
        <div>
          <h2 className="text-sm sm:text-base font-bold text-text-primary flex items-center gap-2">
            <Moon className="w-4 h-4 text-primary" />
            Appearance & Theme
          </h2>
          <p className="text-xs text-text-secondary mt-0.5">
            Synchronized seamlessly across all pages and study components.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          <button
            onClick={() => setThemeMode('DARK')}
            className={`p-3.5 rounded-xl border flex items-center gap-3 transition-all text-left ${
              themeMode === 'DARK'
                ? 'bg-primary-container border-primary text-primary font-bold shadow-xs'
                : 'bg-surface-variant border-border text-text-secondary hover:text-text-primary'
            }`}
          >
            <Moon className="w-5 h-5 shrink-0" />
            <div>
              <p className="text-xs font-bold text-text-primary">🌙 Cyber Dark</p>
              <p className="text-[10px] opacity-75">Default palette</p>
            </div>
          </button>

          <button
            onClick={() => setThemeMode('LIGHT')}
            className={`p-3.5 rounded-xl border flex items-center gap-3 transition-all text-left ${
              themeMode === 'LIGHT'
                ? 'bg-primary-container border-primary text-primary font-bold shadow-xs'
                : 'bg-surface-variant border-border text-text-secondary hover:text-text-primary'
            }`}
          >
            <Sun className="w-5 h-5 shrink-0" />
            <div>
              <p className="text-xs font-bold text-text-primary">☀️ Clean Light</p>
              <p className="text-[10px] opacity-75">High-contrast day mode</p>
            </div>
          </button>

          <button
            onClick={() => setThemeMode('SYSTEM')}
            className={`p-3.5 rounded-xl border flex items-center gap-3 transition-all text-left ${
              themeMode === 'SYSTEM'
                ? 'bg-primary-container border-primary text-primary font-bold shadow-xs'
                : 'bg-surface-variant border-border text-text-secondary hover:text-text-primary'
            }`}
          >
            <Monitor className="w-5 h-5 shrink-0" />
            <div>
              <p className="text-xs font-bold text-text-primary">🔄 System Default</p>
              <p className="text-[10px] opacity-75">Follows OS theme</p>
            </div>
          </button>
        </div>
      </QuovexCard>

      {/* ── 4. Study Target & Preferences ─────────────────────────────────── */}
      <QuovexCard className="space-y-4 p-5 sm:p-6 shadow-sm">
        <div>
          <h2 className="text-sm sm:text-base font-bold text-text-primary flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-primary" />
            Academic Target & Goals
          </h2>
          <p className="text-xs text-text-secondary mt-0.5">
            Calibrate your AI coach and roadmap milestones to your exam.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-text-secondary mb-1">
              Target Competitive Exam
            </label>
            <select
              value={targetExam}
              onChange={(e) => setTargetExam(e.target.value)}
              className="w-full bg-surface-variant border border-border rounded-xl px-3 py-2 text-xs sm:text-sm text-text-primary font-bold focus:outline-none focus:border-primary transition-all cursor-pointer"
            >
              {EXAMS.map((ex) => (
                <option key={ex} value={ex} className="bg-surface text-text-primary">
                  {ex}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-text-secondary mb-1">
              Daily Focus Target (Hours)
            </label>
            <input
              type="number"
              min={1}
              max={16}
              value={dailyGoalHours}
              onChange={(e) => setDailyGoalHours(Number(e.target.value))}
              className="w-full bg-surface-variant border border-border rounded-xl px-3 py-2 text-xs sm:text-sm text-text-primary font-bold focus:outline-none focus:border-primary transition-all"
            />
          </div>
        </div>

        <div className="flex justify-end pt-1">
          <QuovexButton
            variant="primary"
            size="sm"
            onClick={handleSavePreferences}
            isLoading={isSavingSettings}
          >
            Save Target Settings
          </QuovexButton>
        </div>
      </QuovexCard>

      {/* ── 5. Promotion Coupon ────────────────────────────────────────────── */}
      <QuovexCard className="space-y-4 p-5 sm:p-6 shadow-sm">
        <div>
          <h2 className="text-sm sm:text-base font-bold text-text-primary flex items-center gap-2">
            <Tag className="w-4 h-4 text-primary" />
            Promo Coupons & Vouchers
          </h2>
          <p className="text-xs text-text-secondary mt-0.5">
            Redeem promo codes, school licenses, or founder passes.
          </p>
        </div>

        <form onSubmit={handleApplyCoupon} className="flex flex-col sm:flex-row gap-2.5">
          <input
            type="text"
            placeholder="e.g. FOUNDER50 or STUDENT100"
            value={couponCode}
            onChange={(e) => setCouponCode(e.target.value)}
            className="flex-1 bg-surface-variant border border-border rounded-xl px-3 py-2 text-xs sm:text-sm text-text-primary font-mono font-bold uppercase focus:outline-none focus:border-primary transition-all"
          />
          <QuovexButton
            type="submit"
            variant="secondary"
            size="sm"
            isLoading={isValidatingCoupon}
            disabled={!couponCode.trim()}
          >
            Redeem
          </QuovexButton>
        </form>

        {couponStatus && (
          <div
            className={`p-3 rounded-xl text-xs font-bold flex items-center gap-2 shadow-xs ${
              couponStatus.success
                ? 'bg-success-container text-success border border-success/30'
                : 'bg-error-container text-error border border-error/30'
            }`}
          >
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>{couponStatus.message}</span>
          </div>
        )}
      </QuovexCard>

      {/* ── 6. Referral Program Banner ──────────────────────────────────────── */}
      <QuovexCard className="p-5 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 border-primary/30">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-primary/10 border border-primary/30 text-primary flex items-center justify-center shrink-0">
            <Share2 className="w-5 h-5" />
          </div>
          <div>
            <h3 className="text-xs sm:text-sm font-bold text-text-primary">Refer a Study Partner — 7 Days Pro Free</h3>
            <p className="text-[11px] text-text-secondary">Your Code: <span className="font-mono font-bold text-primary">{currentUser?.uid.slice(0, 6).toUpperCase()}</span></p>
          </div>
        </div>
        <QuovexButton
          variant="secondary"
          size="sm"
          onClick={() => {
            if (navigator.clipboard) {
              navigator.clipboard.writeText(`Join me on Quovex AI Study Operating System! Use code ${currentUser?.uid.slice(0, 6).toUpperCase()} at https://quovex.online`);
              alert('Referral link copied to clipboard!');
            }
          }}
        >
          Copy Link
        </QuovexButton>
      </QuovexCard>

      {/* ── 7. Sign Out Action ──────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row items-center justify-between pt-4 gap-3 border-t border-border">
        <span className="text-[11px] text-text-tertiary">Crafted with precision in Noida, India 🇮🇳</span>
        <button
          onClick={() => signOut()}
          className="flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold text-error hover:bg-error-container/20 border border-error/20 transition-colors"
        >
          <LogOut className="w-3.5 h-3.5" />
          Sign Out
        </button>
      </div>

      {/* ── 8. Avatar Picker Modal ──────────────────────────────────────────── */}
      {isEditingAvatar && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-2xl max-w-xl w-full p-6 space-y-5 shadow-2xl animate-in zoom-in-95">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-base font-bold text-text-primary">Choose Your Scholar Avatar</h3>
                <p className="text-xs text-text-secondary mt-0.5">Select from 12 official illustrated characters</p>
              </div>
              <button
                onClick={() => setIsEditingAvatar(false)}
                className="w-8 h-8 rounded-full bg-surface-variant text-text-secondary hover:text-text-primary flex items-center justify-center transition-colors text-xs"
              >
                ✕
              </button>
            </div>

            <div className="grid grid-cols-4 sm:grid-cols-6 gap-3">
              {Array.from({ length: 12 }, (_, i) => i + 1).map((id) => (
                <button
                  key={id}
                  onClick={() => handleSelectAvatar(id)}
                  className={`p-2 rounded-xl border transition-all flex flex-col items-center gap-1.5 ${
                    profile?.avatarId === id
                      ? 'bg-primary-container border-primary shadow-xs scale-105'
                      : 'bg-surface-variant border-border hover:border-primary/50'
                  }`}
                >
                  <div className="w-12 h-12 relative">
                    <Image
                      src={ASSETS.avatars(id)}
                      alt={`Avatar ${id}`}
                      fill
                      className="object-contain"
                      unoptimized
                    />
                  </div>
                  <span className="text-[10px] font-bold text-text-secondary">#{id}</span>
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* ── 9. Upgrade Modal ────────────────────────────────────────────────── */}
      {showUpgradeModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <div className="bg-surface border border-border rounded-2xl max-w-sm w-full p-6 space-y-5 shadow-2xl animate-in zoom-in-95">
            <div className="text-center space-y-2">
              <div className="w-12 h-12 rounded-xl bg-primary/10 border border-primary/30 flex items-center justify-center p-2 mx-auto">
                <img
                  src={ASSETS.icons3d.rankGrandmaster}
                  alt="Pro VIP"
                  className="w-8 h-8 object-contain"
                />
              </div>
              <h3 className="text-base font-bold text-text-primary">Unlock Quovex Pro VIP</h3>
              <p className="text-xs text-text-secondary leading-relaxed">
                Unlimited AI tutoring, 6-tier vision proofs, Quovex Originals, and 7-day streak recovery.
              </p>
            </div>

            <div className="p-3.5 rounded-xl bg-surface-variant border border-border flex items-center justify-between">
              <div>
                <p className="text-xs font-bold text-text-primary">Pro Annual Plan</p>
                <p className="text-[10px] text-text-secondary">Billed annually (₹83/mo)</p>
              </div>
              <span className="text-sm font-black text-primary">₹999 / yr</span>
            </div>

            <div className="space-y-2 pt-1">
              <QuovexButton
                variant="primary"
                size="md"
                className="w-full"
                onClick={async () => {
                  if (currentUser) {
                    await updateUserProfile(currentUser.uid, { subscriptionTier: 'PRO_ANNUAL' });
                    setShowUpgradeModal(false);
                    alert('🎉 Welcome to Quovex Pro VIP!');
                  }
                }}
              >
                Start 7-Day Free Trial
              </QuovexButton>
              <QuovexButton
                variant="ghost"
                size="sm"
                className="w-full text-xs"
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
