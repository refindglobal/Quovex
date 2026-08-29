'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { 
  Download, 
  Smartphone, 
  ShieldCheck, 
  CheckCircle2, 
  ArrowLeft, 
  Sparkles, 
  Timer, 
  BookOpen, 
  Zap, 
  ExternalLink,
  Copy,
  Check
} from 'lucide-react';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import releaseMeta from '@/lib/release-metadata.json';

export default function DownloadPage() {
  const [downloading, setDownloading] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleDownload = () => {
    setDownloading(true);
    // Trigger download
    const link = document.createElement('a');
    link.href = releaseMeta.downloadUrl;
    link.download = releaseMeta.apkFileName;
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    setTimeout(() => setDownloading(false), 3000);
  };

  const copySha = () => {
    if (releaseMeta.sha256) {
      navigator.clipboard.writeText(releaseMeta.sha256);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="min-h-screen bg-background text-text-primary">
      {/* Top Header */}
      <header className="h-20 border-b border-border bg-background/80 backdrop-blur-xl sticky top-0 z-50">
        <div className="max-w-6xl mx-auto h-full px-6 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl overflow-hidden bg-primary/10 border border-primary/30 flex items-center justify-center shrink-0 shadow-glow">
              <img
                src="/assets/brand/emblem.png"
                alt="Quovex Logo"
                className="w-8 h-8 object-contain"
              />
            </div>
            <div>
              <span className="text-xl font-black tracking-tight text-text-primary">QUOVEX</span>
              <span className="block text-[10px] text-text-secondary font-medium tracking-wide">STUDENT OS</span>
            </div>
          </Link>

          <Link href="/">
            <QuovexButton variant="ghost" size="sm" leftIcon={<ArrowLeft className="w-4 h-4" />}>
              Back to Home
            </QuovexButton>
          </Link>
        </div>
      </header>

      {/* Main Download Hero */}
      <main className="max-w-4xl mx-auto px-6 py-16">
        <div className="text-center space-y-4">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-primary-container border border-primary/30 text-caption font-semibold text-primary shadow-glow">
            <Smartphone className="w-3.5 h-3.5" />
            <span>Official Android APK Distribution</span>
          </div>

          <h1 className="text-display font-black tracking-tight text-text-primary">
            Quovex for Android
          </h1>

          <p className="text-section text-text-secondary max-w-xl mx-auto leading-relaxed">
            The full student operating system with distraction blocker, camera focus tracking, and offline sync.
          </p>
        </div>

        {/* Download Card */}
        <div className="mt-10 p-8 rounded-3xl border border-primary/30 bg-surface-elevated shadow-glow relative overflow-hidden">
          <div className="absolute top-0 right-0 w-64 h-64 bg-primary-glow rounded-full blur-[100px] pointer-events-none" />

          <div className="flex flex-col md:flex-row items-center justify-between gap-8 relative z-10">
            <div className="flex items-center gap-5">
              <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-primary via-emerald-500 to-teal-600 flex items-center justify-center text-background font-black text-3xl shadow-glow shrink-0">
                Q
              </div>
              <div className="text-left space-y-1">
                <div className="flex items-center gap-2">
                  <h2 className="text-title font-bold text-text-primary">Quovex Android Release</h2>
                  <span className="px-2 py-0.5 rounded-full text-[11px] font-mono font-bold bg-primary/20 text-primary border border-primary/30">
                    v{releaseMeta.versionName}
                  </span>
                </div>
                <p className="text-caption text-text-secondary">
                  Build #{releaseMeta.versionCode} • Updated {releaseMeta.releaseDate}
                </p>
                <div className="flex items-center gap-3 text-caption text-text-tertiary pt-1">
                  <span>📱 {releaseMeta.minAndroid}</span>
                  <span>•</span>
                  <span>🔒 Verified Safe Release</span>
                </div>
              </div>
            </div>

            <div className="w-full md:w-auto flex flex-col sm:flex-row md:flex-col gap-3 shrink-0">
              <QuovexButton
                size="lg"
                className="w-full text-base px-8 py-4 shadow-glow"
                onClick={handleDownload}
                leftIcon={<Download className="w-5 h-5" />}
              >
                {downloading ? 'Starting Download...' : 'Download APK'}
              </QuovexButton>
              <a
                href="https://play.google.com/store/apps/details?id=com.quovex"
                target="_blank"
                rel="noopener noreferrer"
                className="w-full"
              >
                <QuovexButton variant="secondary" size="md" className="w-full text-caption" rightIcon={<ExternalLink className="w-3.5 h-3.5" />}>
                  Google Play (Beta)
                </QuovexButton>
              </a>
            </div>
          </div>

          {/* Features in this build */}
          <div className="mt-8 pt-6 border-t border-border grid grid-cols-1 sm:grid-cols-2 gap-3 text-left">
            {releaseMeta.features.map((feature, idx) => (
              <div key={idx} className="flex items-center gap-2.5 text-body text-text-secondary">
                <CheckCircle2 className="w-4 h-4 text-primary shrink-0" />
                <span>{feature}</span>
              </div>
            ))}
          </div>

          {/* SHA-256 Checksum box */}
          {releaseMeta.sha256 && (
            <div className="mt-6 p-3 rounded-xl bg-surface-variant border border-border flex items-center justify-between text-caption font-mono">
              <div className="truncate pr-4 text-text-secondary">
                <span className="text-text-tertiary select-none">SHA256: </span>
                {releaseMeta.sha256}
              </div>
              <button
                onClick={copySha}
                className="text-text-secondary hover:text-primary transition-colors shrink-0 flex items-center gap-1"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-primary" /> : <Copy className="w-3.5 h-3.5" />}
                <span className="text-[11px] font-sans">{copied ? 'Copied' : 'Copy'}</span>
              </button>
            </div>
          )}
        </div>

        {/* Installation Instructions */}
        <div className="mt-16 text-left">
          <h3 className="text-section font-bold text-text-primary mb-6 text-center">
            How to Install Quovex APK on Android
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <QuovexCard className="p-5">
              <div className="w-8 h-8 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center mb-3">
                1
              </div>
              <h4 className="font-bold text-text-primary text-body mb-1">Download APK</h4>
              <p className="text-caption text-text-secondary leading-relaxed">
                Tap the "Download APK" button above. Your browser will download the verified release package.
              </p>
            </QuovexCard>

            <QuovexCard className="p-5">
              <div className="w-8 h-8 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center mb-3">
                2
              </div>
              <h4 className="font-bold text-text-primary text-body mb-1">Allow Install</h4>
              <p className="text-caption text-text-secondary leading-relaxed">
                If prompted by Android, tap "Settings" and enable "Allow from this source" for your browser.
              </p>
            </QuovexCard>

            <QuovexCard className="p-5">
              <div className="w-8 h-8 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center mb-3">
                3
              </div>
              <h4 className="font-bold text-text-primary text-body mb-1">Open & Sign In</h4>
              <p className="text-caption text-text-secondary leading-relaxed">
                Tap "Install" then "Open". Sign in with your Google account to automatically sync your notes and flashcards.
              </p>
            </QuovexCard>
          </div>
        </div>

        {/* Security & Privacy Guarantee */}
        <div className="mt-12 p-6 rounded-2xl bg-surface-variant/40 border border-border flex items-center gap-4 text-left">
          <div className="w-12 h-12 rounded-xl bg-primary-container text-primary flex items-center justify-center shrink-0">
            <ShieldCheck className="w-6 h-6" />
          </div>
          <div>
            <h4 className="font-bold text-text-primary text-body">100% Secure & Privacy-First</h4>
            <p className="text-caption text-text-secondary mt-0.5">
              All APK packages are built directly from verified source with R8 code optimization. No third-party ad trackers or spyware.
            </p>
          </div>
        </div>
      </main>
    </div>
  );
}
