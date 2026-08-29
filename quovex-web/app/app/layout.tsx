'use client';

import React, { useState, useEffect } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { subscribeToAuthChanges, UserProfile, TEST_FALLBACK_PROFILE } from '@/lib/firebase/auth';
import { subscribeToUserProfile } from '@/lib/firebase/firestore';
import { AppSidebar } from '@/components/app/AppSidebar';
import { AppHeader } from '@/components/app/AppHeader';
import { MobileNav } from '@/components/app/MobileNav';
import { AndroidBanner } from '@/components/ui/AndroidBanner';

export default function AppLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const [profile, setProfile] = useState<UserProfile>(TEST_FALLBACK_PROFILE);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let unsubscribeProfile: (() => void) | null = null;

    const unsubscribeAuth = subscribeToAuthChanges((user) => {
      if (user) {
        unsubscribeProfile = subscribeToUserProfile(user.uid, (p) => {
          if (p) setProfile(p);
          setLoading(false);
        });
      } else {
        setProfile(TEST_FALLBACK_PROFILE);
        setLoading(false);
      }
    });

    return () => {
      unsubscribeAuth();
      if (unsubscribeProfile) unsubscribeProfile();
    };
  }, [router]);

  if (loading) {
    return (
      <div className="min-h-screen bg-background text-text-primary flex flex-col items-center justify-center gap-4 transition-colors duration-200">
        <div className="w-12 h-12 rounded-2xl bg-primary-container border border-primary/40 flex items-center justify-center text-primary font-bold text-xl animate-pulse shadow-glow">
          Q
        </div>
        <p className="text-xs text-text-secondary font-medium tracking-wide">
          Syncing with Quovex Cloud...
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background text-text-primary flex flex-col lg:flex-row transition-colors duration-200">
      {/* Desktop Sidebar */}
      <AppSidebar />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 pb-20 lg:pb-6">
        <AppHeader profile={profile} />
        <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto">
          {children}
        </main>
      </div>

      {/* Mobile Navigation */}
      <MobileNav />

      {/* Smart Android Download Banner */}
      <AndroidBanner />
    </div>
  );
}
