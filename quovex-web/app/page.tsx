'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { 
  Timer, 
  Bot, 
  Sparkles, 
  BookOpen, 
  Flame, 
  Users, 
  CheckCircle2, 
  ArrowRight, 
  Download, 
  ShieldCheck, 
  Star, 
  Zap,
  Globe,
  ChevronRight
} from 'lucide-react';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';

export default function LandingPage() {
  const [billingCycle, setBillingCycle] = useState<'monthly' | 'annual'>('annual');

  return (
    <div className="min-h-screen bg-background text-text-primary transition-colors duration-200">
      {/* Navigation */}
      <header className="fixed top-0 left-0 right-0 h-20 bg-background/80 backdrop-blur-xl border-b border-border z-50 transition-colors duration-200">
        <div className="max-w-7xl mx-auto h-full px-6 flex items-center justify-between">
          <Link href="/" className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-primary-container border border-primary/40 flex items-center justify-center text-primary font-extrabold text-xl shadow-glow">
              Q
            </div>
            <div>
              <span className="text-xl font-black tracking-tight text-text-primary">QUOVEX</span>
              <span className="block text-[10px] text-text-secondary font-medium tracking-wide">STUDENT OS</span>
            </div>
          </Link>

          <nav className="hidden md:flex items-center gap-8 text-body font-medium text-text-secondary">
            <a href="#features" className="hover:text-primary transition-colors">Features</a>
            <a href="#comparison" className="hover:text-primary transition-colors">Why Quovex</a>
            <a href="#pricing" className="hover:text-primary transition-colors">Pricing</a>
            <Link href="/download" className="flex items-center gap-1.5 text-primary hover:text-emerald-400 font-semibold transition-colors">
              <Download className="w-4 h-4" />
              <span>Download App</span>
            </Link>
          </nav>

          <div className="flex items-center gap-3">
            <Link href="/auth">
              <QuovexButton variant="secondary" size="sm" className="hidden sm:inline-flex">
                Sign In
              </QuovexButton>
            </Link>
            <Link href="/auth">
              <QuovexButton size="sm" rightIcon={<ArrowRight className="w-4 h-4" />}>
                Get Started Free
              </QuovexButton>
            </Link>
          </div>
        </div>
      </header>

      {/* S1: Hero Section */}
      <section className="pt-36 pb-20 px-6 max-w-7xl mx-auto relative overflow-hidden">
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] bg-primary-glow rounded-full blur-[140px] pointer-events-none" />

        <div className="text-center space-y-6 max-w-4xl mx-auto relative z-10">
          <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-primary-container border border-primary/30 text-caption font-semibold text-primary shadow-glow">
            <Sparkles className="w-3.5 h-3.5" />
            <span>The AI-Powered Student Operating System</span>
          </div>

          <h1 className="text-display font-black tracking-tight text-text-primary leading-[1.1]">
            The Last Study App <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary via-emerald-400 to-teal-300">
              You'll Ever Need.
            </span>
          </h1>

          <p className="text-section sm:text-title text-text-secondary max-w-2xl mx-auto leading-relaxed">
            AI smart flashcards, deep work focus timers, instant doubt solving, and NCERT library. Replace 5 disconnected apps with one synchronized ecosystem.
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 pt-4">
            <Link href="/auth" className="w-full sm:w-auto">
              <QuovexButton size="lg" className="w-full sm:w-auto text-base px-8 py-4" rightIcon={<ArrowRight className="w-5 h-5" />}>
                Start Free with Google
              </QuovexButton>
            </Link>
            <Link href="/download" className="w-full sm:w-auto">
              <QuovexButton variant="secondary" size="lg" className="w-full sm:w-auto text-base px-6 py-4" leftIcon={<Download className="w-5 h-5" />}>
                Get Quovex for Android
              </QuovexButton>
            </Link>
          </div>

          <p className="text-caption text-text-secondary">
            ✨ Free Forever Tier • No Credit Card Required • Instant Sync Across Web & Android
          </p>
        </div>

        {/* Dashboard Mockup Preview */}
        <div className="mt-16 max-w-5xl mx-auto relative z-10">
          <div className="p-3 rounded-3xl border border-primary/20 bg-surface/60 backdrop-blur-xl shadow-glow">
            <div className="bg-surface-elevated rounded-2xl p-6 border border-border grid grid-cols-1 md:grid-cols-3 gap-4 text-left">
              <div className="p-4 rounded-xl bg-surface-variant border border-border">
                <div className="flex items-center justify-between text-caption text-text-secondary mb-2">
                  <span>FOCUS ENGINE</span>
                  <Timer className="w-4 h-4 text-primary" />
                </div>
                <div className="font-mono text-3xl font-bold text-text-primary">25:00</div>
                <p className="text-caption text-primary mt-2">🎧 Binaural Alpha Waves Active</p>
              </div>

              <div className="p-4 rounded-xl bg-surface-variant border border-border">
                <div className="flex items-center justify-between text-caption text-text-secondary mb-2">
                  <span>QUOVEX AI TUTOR</span>
                  <Bot className="w-4 h-4 text-primary" />
                </div>
                <div className="text-body font-semibold text-text-primary">Thermodynamics Heat Engines</div>
                <p className="text-caption text-text-secondary mt-1">
                  Deriving Carnot efficiency: {'$\\eta = 1 - \\frac{T_C}{T_H}$'}
                </p>
              </div>

              <div className="p-4 rounded-xl bg-surface-variant border border-border">
                <div className="flex items-center justify-between text-caption text-text-secondary mb-2">
                  <span>SM-2 FLASHCARDS</span>
                  <Sparkles className="w-4 h-4 text-warning" />
                </div>
                <div className="text-body font-semibold text-text-primary">18 Cards Due Today</div>
                <div className="w-full bg-surface h-2 rounded-full mt-2.5 overflow-hidden border border-border">
                  <div className="bg-primary h-full w-[78%]" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* S2: Problem Counters */}
      <section className="py-16 bg-surface-variant/40 border-y border-border px-6">
        <div className="max-w-7xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
          <div>
            <div className="text-4xl md:text-5xl font-black text-error">90%</div>
            <p className="text-caption md:text-body text-text-secondary mt-2">Students distracted within 15m of study</p>
          </div>
          <div>
            <div className="text-4xl md:text-5xl font-black text-warning">75%</div>
            <p className="text-caption md:text-body text-text-secondary mt-2">Rely on passive re-reading with zero recall</p>
          </div>
          <div>
            <div className="text-4xl md:text-5xl font-black text-primary">65%</div>
            <p className="text-caption md:text-body text-text-secondary mt-2">Juggle 4+ apps causing decision fatigue</p>
          </div>
          <div>
            <div className="text-4xl md:text-5xl font-black text-primary">1 App</div>
            <p className="text-caption md:text-body text-text-secondary mt-2">Quovex replaces them all permanently</p>
          </div>
        </div>
      </section>

      {/* S3: Why Quovex (Comparison) */}
      <section id="comparison" className="py-24 px-6 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <QuovexBadge variant="emerald">5-IN-1 ECOSYSTEM</QuovexBadge>
          <h2 className="text-headline font-extrabold text-text-primary mt-4">
            Stop Paying for 5 Disconnected Subscriptions
          </h2>
          <p className="text-body text-text-secondary mt-3">
            Quovex brings all study essentials into one synchronized workspace.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-4xl mx-auto">
          <QuovexCard className="bg-error-container/20 border-error/30">
            <h3 className="text-section font-bold text-error mb-4 flex items-center gap-2">
              <span>❌ The Fragmented Old Way</span>
            </h3>
            <ul className="space-y-3 text-body text-text-secondary">
              <li className="flex items-center gap-2">Forest ($3.99/mo) — Basic timer without AI notes</li>
              <li className="flex items-center gap-2">Anki ($25 on iOS) — Clunky manual flashcard entry</li>
              <li className="flex items-center gap-2">ChatGPT ($20/mo) — Generic answers without syllabus context</li>
              <li className="flex items-center gap-2">Notion ($10/mo) — Overcomplicated setup</li>
              <li className="flex items-center gap-2 font-bold text-error pt-2">Total: ~$38/month wasted</li>
            </ul>
          </QuovexCard>

          <QuovexCard className="bg-surface-elevated border-primary/40 shadow-glow">
            <h3 className="text-section font-bold text-primary mb-4 flex items-center gap-2">
              <span>✨ The Quovex Unified Way</span>
            </h3>
            <ul className="space-y-3 text-body text-text-primary">
              <li className="flex items-center gap-2">✅ Integrated Focus Timer + Binaural Soundscapes</li>
              <li className="flex items-center gap-2">✅ Automated SM-2 Flashcards generated from notes</li>
              <li className="flex items-center gap-2">✅ Quovex AI Tutor tuned to JEE, NEET, UPSC & CBSE</li>
              <li className="flex items-center gap-2">✅ Full NCERT Library with AI chapter summarization</li>
              <li className="flex items-center gap-2 font-bold text-primary pt-2">Total: Free Forever (or ₹83/mo Pro)</li>
            </ul>
          </QuovexCard>
        </div>
      </section>

      {/* S4: 6 Core Bento Features */}
      <section id="features" className="py-20 px-6 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-16">
          <QuovexBadge variant="emerald">ENGINEERED FOR HIGH PERFORMERS</QuovexBadge>
          <h2 className="text-display font-extrabold text-text-primary mt-4">
            Everything You Need to Top Your Exams
          </h2>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <QuovexCard hoverEffect>
            <div className="w-12 h-12 rounded-2xl bg-primary-container text-primary flex items-center justify-center mb-5 shadow-glow-sm">
              <Timer className="w-6 h-6" />
            </div>
            <h3 className="text-title font-bold text-text-primary mb-2">Focus Engine & Soundscapes</h3>
            <p className="text-body text-text-secondary leading-relaxed">
              Pomodoro and Deep Work timers backed by 9 binaural beat soundscapes to lock your brain into flow state.
            </p>
          </QuovexCard>

          <QuovexCard hoverEffect>
            <div className="w-12 h-12 rounded-2xl bg-primary-container text-primary flex items-center justify-center mb-5 shadow-glow-sm">
              <Bot className="w-6 h-6" />
            </div>
            <h3 className="text-title font-bold text-text-primary mb-2">Quovex AI Doubt Solver</h3>
            <p className="text-body text-text-secondary leading-relaxed">
              Instant conceptual derivations, formula notation, and step-by-step problem breakdown 24/7.
            </p>
          </QuovexCard>

          <QuovexCard hoverEffect>
            <div className="w-12 h-12 rounded-2xl bg-primary-container text-primary flex items-center justify-center mb-5 shadow-glow-sm">
              <Sparkles className="w-6 h-6" />
            </div>
            <h3 className="text-title font-bold text-text-primary mb-2">SM-2 Spaced Repetition</h3>
            <p className="text-body text-text-secondary leading-relaxed">
              Scientifically schedules card reviews at the exact forgetting curve interval so concepts stick forever.
            </p>
          </QuovexCard>

          <QuovexCard hoverEffect>
            <div className="w-12 h-12 rounded-2xl bg-primary-container text-primary flex items-center justify-center mb-5 shadow-glow-sm">
              <BookOpen className="w-6 h-6" />
            </div>
            <h3 className="text-title font-bold text-text-primary mb-2">NCERT & Originals Hub</h3>
            <p className="text-body text-text-secondary leading-relaxed">
              Direct access to rationalised NCERT textbooks and Quovex Originals with visual analogies and formula summaries.
            </p>
          </QuovexCard>

          <QuovexCard hoverEffect>
            <div className="w-12 h-12 rounded-2xl bg-primary-container text-primary flex items-center justify-center mb-5 shadow-glow-sm">
              <Flame className="w-6 h-6" />
            </div>
            <h3 className="text-title font-bold text-text-primary mb-2">Streak Protection & XP</h3>
            <p className="text-body text-text-secondary leading-relaxed">
              Daily habit tracker with Rescue Tokens to prevent broken streak anxiety. Level up your Scholar rank.
            </p>
          </QuovexCard>

          <QuovexCard hoverEffect>
            <div className="w-12 h-12 rounded-2xl bg-primary-container text-primary flex items-center justify-center mb-5 shadow-glow-sm">
              <Users className="w-6 h-6" />
            </div>
            <h3 className="text-title font-bold text-text-primary mb-2">Live Virtual Study Rooms</h3>
            <p className="text-body text-text-secondary leading-relaxed">
              Study silently alongside serious peers preparing for the same target exam. Zero toxicity, pure accountability.
            </p>
          </QuovexCard>
        </div>
      </section>

      {/* S5: Pricing Section */}
      <section id="pricing" className="py-24 px-6 max-w-7xl mx-auto">
        <div className="text-center max-w-3xl mx-auto mb-12">
          <QuovexBadge variant="gold">TRANSPARENT PRICING</QuovexBadge>
          <h2 className="text-display font-extrabold text-text-primary mt-4">
            Invest in Your Rank.
          </h2>
          <p className="text-body text-text-secondary mt-3">
            Fair regional pricing (PPP) with a 7-day free trial on the Annual plan.
          </p>

          <div className="flex items-center justify-center gap-3 mt-8">
            <span className={`text-body ${billingCycle === 'monthly' ? 'text-text-primary font-bold' : 'text-text-secondary'}`}>Monthly</span>
            <button
              onClick={() => setBillingCycle(b => b === 'monthly' ? 'annual' : 'monthly')}
              className="w-14 h-8 rounded-full bg-surface-variant border border-border p-1 transition-colors relative"
            >
              <div className={`w-6 h-6 rounded-full bg-primary transition-transform ${billingCycle === 'annual' ? 'translate-x-6' : 'translate-x-0'}`} />
            </button>
            <span className={`text-body flex items-center gap-1.5 ${billingCycle === 'annual' ? 'text-text-primary font-bold' : 'text-text-secondary'}`}>
              Annual <span className="text-[10px] bg-primary-container text-primary px-1.5 py-0.5 rounded font-mono font-bold border border-primary/20">SAVE 60%</span>
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 max-w-5xl mx-auto">
          {/* Free Tier */}
          <QuovexCard>
            <h3 className="text-title font-bold text-text-primary">Scholar Free</h3>
            <p className="text-caption text-text-secondary mt-1">Essential tools for every student</p>
            <div className="text-3xl font-black text-text-primary mt-4">₹0</div>
            <p className="text-caption text-text-secondary">Free forever</p>

            <ul className="space-y-2.5 text-caption text-text-secondary my-6">
              <li className="flex items-center gap-2">✅ 10 AI queries per day</li>
              <li className="flex items-center gap-2">✅ Unlimited Focus Timer</li>
              <li className="flex items-center gap-2">✅ SM-2 Spaced Repetition</li>
              <li className="flex items-center gap-2">✅ NCERT Book Library</li>
              <li className="flex items-center gap-2">✅ 3 Ambient Soundscapes</li>
            </ul>

            <Link href="/auth">
              <QuovexButton variant="secondary" size="md" className="w-full">
                Get Started Free
              </QuovexButton>
            </Link>
          </QuovexCard>

          {/* Pro Annual (Best Value) */}
          <QuovexCard className="bg-surface-elevated border-primary shadow-glow relative">
            <div className="absolute -top-3.5 left-1/2 -translate-x-1/2 bg-primary text-primary-foreground font-bold text-[11px] uppercase tracking-wider px-3.5 py-1 rounded-full shadow-lg">
              ⭐ 7-Day Free Trial • Save 60%
            </div>

            <h3 className="text-title font-bold text-text-primary mt-2">Pro Annual</h3>
            <p className="text-caption text-text-secondary mt-1">Complete exam season preparation</p>
            <div className="text-3xl font-black text-text-primary mt-4">
              {billingCycle === 'annual' ? '₹999' : '₹199'}
              <span className="text-body font-normal text-text-secondary">{billingCycle === 'annual' ? '/year' : '/month'}</span>
            </div>
            <p className="text-caption text-primary font-semibold">Zero upfront charge for 7 days</p>

            <ul className="space-y-2.5 text-caption text-text-primary my-6">
              <li className="flex items-center gap-2">⚡ Unlimited Quovex AI Tutor</li>
              <li className="flex items-center gap-2">⚡ All 9 Binaural Soundscapes</li>
              <li className="flex items-center gap-2">⚡ Unlimited PDF & Notes OCR</li>
              <li className="flex items-center gap-2">⚡ Cerebras Dynamic Study Planner</li>
              <li className="flex items-center gap-2">⚡ 100% Ad-Free Clean UI</li>
              <li className="flex items-center gap-2">⚡ Private Study Rooms</li>
            </ul>

            <Link href="/auth">
              <QuovexButton size="md" className="w-full">
                Start 7-Day Free Trial
              </QuovexButton>
            </Link>
          </QuovexCard>

          {/* Founder Lifetime */}
          <QuovexCard>
            <h3 className="text-title font-bold text-text-primary">Founder Lifetime</h3>
            <p className="text-caption text-text-secondary mt-1">One-time payment, permanent VIP</p>
            <div className="text-3xl font-black text-warning mt-4">₹2,499</div>
            <p className="text-caption text-text-secondary">Pay once, own forever</p>

            <ul className="space-y-2.5 text-caption text-text-secondary my-6">
              <li className="flex items-center gap-2">🚀 Permanent Unlimited Pro Access</li>
              <li className="flex items-center gap-2">🚀 All Future AI Models Included</li>
              <li className="flex items-center gap-2">🚀 Exclusive Founder Discord VIP Role</li>
              <li className="flex items-center gap-2">🚀 Early Beta Access to New Features</li>
            </ul>

            <Link href="/auth">
              <QuovexButton variant="secondary" size="md" className="w-full">
                Claim Founder Pass
              </QuovexButton>
            </Link>
          </QuovexCard>
        </div>
      </section>

      {/* S6: Final CTA */}
      <section className="py-20 px-6 max-w-5xl mx-auto text-center">
        <div className="p-12 rounded-3xl border border-primary/30 bg-surface-elevated shadow-glow relative overflow-hidden">
          <div className="absolute -right-20 -bottom-20 w-80 h-80 bg-primary-glow rounded-full blur-[100px]" />
          
          <h2 className="text-display font-black text-text-primary mb-4">
            Step Into the Top 1% of Disciplined Students.
          </h2>
          <p className="text-text-secondary max-w-xl mx-auto mb-8 text-body sm:text-section">
            Join thousands of scholars cracking JEE, NEET, CBSE, and international exams with Quovex.
          </p>

          <Link href="/auth">
            <QuovexButton size="lg" className="text-base px-8 py-4" rightIcon={<ArrowRight className="w-5 h-5" />}>
              Start Studying Free with Google
            </QuovexButton>
          </Link>
        </div>
      </section>

      {/* S7: Footer & Indian Heritage Attribution */}
      <footer className="border-t border-border py-12 px-6 bg-surface">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6 text-center md:text-left">
          <div>
            <div className="flex items-center justify-center md:justify-start gap-2">
              <div className="w-6 h-6 rounded-lg bg-primary-container text-primary font-bold text-xs flex items-center justify-center">
                Q
              </div>
              <span className="font-extrabold text-text-primary">QUOVEX</span>
            </div>
            <p className="text-caption text-text-secondary mt-2">
              Thought and crafted in India 🇮🇳 by Rohit & Kartikey (Noida, Uttar Pradesh)
            </p>
            <p className="text-caption text-text-tertiary mt-0.5">
              Product of Refind Global Studio • Founded 2026
            </p>
          </div>

          <div className="flex items-center gap-6 text-caption text-text-secondary">
            <Link href="/download" className="text-primary hover:text-emerald-400 font-medium transition-colors flex items-center gap-1">
              <Download className="w-3.5 h-3.5" />
              <span>Android App</span>
            </Link>
            <Link href="/privacy" className="hover:text-primary transition-colors">Privacy Policy</Link>
            <Link href="/terms" className="hover:text-primary transition-colors">Terms of Service</Link>
            <a href="mailto:supportquovex@gmail.com" className="hover:text-primary transition-colors">Support</a>
            <a href="mailto:Refindglobalstudio@gmail.com" className="hover:text-primary transition-colors">Business</a>
          </div>
        </div>
      </footer>
    </div>
  );
}
