'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { Smartphone, X, Download, ExternalLink } from 'lucide-react';
import { QuovexButton } from './QuovexButton';

export const AndroidBanner: React.FC = () => {
  const [isVisible, setIsVisible] = useState(false);
  const [isAndroid, setIsAndroid] = useState(false);

  useEffect(() => {
    // Check if dismissed in the last 7 days
    const dismissedAt = localStorage.getItem('quovex_android_banner_dismissed');
    if (dismissedAt) {
      const daysSince = (Date.now() - parseInt(dismissedAt, 10)) / (1000 * 60 * 60 * 24);
      if (daysSince < 7) return;
    }

    const ua = navigator.userAgent.toLowerCase();
    const isMobile = /android|iphone|ipad|ipod/.test(ua);
    setIsAndroid(/android/.test(ua));

    if (isMobile) {
      const timer = setTimeout(() => setIsVisible(true), 4000);
      return () => clearTimeout(timer);
    }
  }, []);

  const handleDismiss = () => {
    localStorage.setItem('quovex_android_banner_dismissed', Date.now().toString());
    setIsVisible(false);
  };

  if (!isVisible) return null;

  return (
    <div className="fixed bottom-4 left-4 right-4 md:left-auto md:right-6 md:w-96 z-50 animate-in fade-in slide-in-from-bottom-5 duration-300">
      <div className="bg-surface-elevated border border-primary/30 rounded-2xl p-4 shadow-elevated backdrop-blur-xl relative">
        <button
          onClick={handleDismiss}
          className="absolute top-3 right-3 text-text-tertiary hover:text-text-primary p-1 rounded-lg hover:bg-surface-variant transition-colors"
          aria-label="Dismiss banner"
        >
          <X className="w-4 h-4" />
        </button>

        <div className="flex items-start gap-3.5 pr-6">
          <div className="w-10 h-10 rounded-xl bg-primary-container border border-primary/40 flex items-center justify-center shrink-0 text-primary">
            <Smartphone className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-section font-bold text-text-primary flex items-center gap-1.5">
              Experience the Full App
              <span className="text-[10px] bg-primary-container text-primary px-1.5 py-0.5 rounded font-mono font-semibold border border-primary/20">
                ANDROID
              </span>
            </h4>
            <p className="text-body text-text-secondary mt-1 leading-relaxed">
              Get distraction blocking, camera focus tracking, and offline sync on Android.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2 mt-3.5 pt-3 border-t border-border">
          <Link href="/download" className="flex-1">
            <QuovexButton size="sm" className="w-full text-xs" leftIcon={<Download className="w-3.5 h-3.5" />}>
              Download Android APK
            </QuovexButton>
          </Link>
          <QuovexButton variant="ghost" size="sm" onClick={handleDismiss} className="text-xs">
            Continue on Web
          </QuovexButton>
        </div>
      </div>
    </div>
  );
};
