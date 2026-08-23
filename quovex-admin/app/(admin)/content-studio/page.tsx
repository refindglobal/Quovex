'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import {
  TrendingUp,
  Cpu,
  CheckCircle2,
  BookOpen,
  ArrowRight,
  Plus,
  Layers,
  Sparkles,
  ShieldCheck,
} from 'lucide-react';

export default function ContentStudioOverviewPage() {
  const [stats, setStats] = useState({
    demandCount: 0,
    activeJobsCount: 0,
    awaitingReviewCount: 0,
    publishedCount: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadStats() {
      try {
        const [demandRes, jobsRes, draftsRes, publishedRes] = await Promise.all([
          fetch('/api/content-studio/demand-signals'),
          fetch('/api/content-studio/generation-jobs'),
          fetch('/api/content-studio/books?status=READY_FOR_REVIEW'),
          fetch('/api/content-studio/books?status=PUBLISHED'),
        ]);

        const demandData = await demandRes.json();
        const jobsData = await jobsRes.json();
        const draftsData = await draftsRes.json();
        const publishedData = await publishedRes.json();

        setStats({
          demandCount: demandData.total || 0,
          activeJobsCount: (jobsData.jobs || []).filter((j: any) => j.status === 'GENERATING').length,
          awaitingReviewCount: draftsData.total || 0,
          publishedCount: publishedData.total || 0,
        });
      } catch (err) {
        console.error('Failed to load studio stats:', err);
      } finally {
        setLoading(false);
      }
    }

    loadStats();
  }, []);

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="Quovex Content Studio"
        description="Demand-Driven Educational Book Authoring & Multi-Agent Editorial Control Plane"
        action={
          <Link
            href="/content-studio/requests/new"
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>New Book Request</span>
          </Link>
        }
      />

      <div className="p-8 space-y-8 flex-1">
        {/* Real-time KPI Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="p-5 rounded-xl bg-card border border-border">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs text-muted-foreground font-medium">Topic Demand Signals</span>
              <div className="w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-400 flex items-center justify-center">
                <TrendingUp className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className="text-2xl font-bold text-white mb-1">
              {loading ? '-' : stats.demandCount}
            </div>
            <div className="text-[11px] text-muted-foreground">
              {stats.demandCount === 0 ? 'No demand signals yet' : `${stats.demandCount} active friction signals`}
            </div>
          </div>

          <div className="p-5 rounded-xl bg-card border border-border">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs text-muted-foreground font-medium">Active Generation Jobs</span>
              <div className="w-7 h-7 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center">
                <Cpu className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className="text-2xl font-bold text-white mb-1">
              {loading ? '-' : stats.activeJobsCount}
            </div>
            <div className="text-[11px] text-muted-foreground">
              {stats.activeJobsCount === 0 ? 'No active generation jobs' : `${stats.activeJobsCount} pipeline worker tasks`}
            </div>
          </div>

          <div className="p-5 rounded-xl bg-card border border-border">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs text-muted-foreground font-medium">Awaiting Editorial Review</span>
              <div className="w-7 h-7 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center">
                <CheckCircle2 className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className="text-2xl font-bold text-white mb-1">
              {loading ? '-' : stats.awaitingReviewCount}
            </div>
            <div className="text-[11px] text-muted-foreground">
              {stats.awaitingReviewCount === 0 ? 'No books awaiting review' : 'Requires human sign-off'}
            </div>
          </div>

          <div className="p-5 rounded-xl bg-card border border-border">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs text-muted-foreground font-medium">Published Quovex Originals</span>
              <div className="w-7 h-7 rounded-lg bg-primary/10 text-primary flex items-center justify-center">
                <BookOpen className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className="text-2xl font-bold text-white mb-1">
              {loading ? '-' : stats.publishedCount}
            </div>
            <div className="text-[11px] text-muted-foreground">
              {stats.publishedCount === 0 ? 'No published books yet' : 'Publicly accessible to students'}
            </div>
          </div>
        </div>

        {/* Pipeline Architecture Banner */}
        <div className="p-6 rounded-xl bg-[#0D1411] border border-border/80 relative overflow-hidden">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-8 h-8 rounded-lg bg-primary/20 border border-primary/30 flex items-center justify-center text-primary">
              <Sparkles className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-sm font-semibold text-white">Quovex Originals Pipeline Architecture</h2>
              <p className="text-xs text-muted-foreground">16-stage asynchronous multi-agent reasoning & verification</p>
            </div>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-6 gap-3 text-xs">
            <div className="p-3 rounded-lg bg-card/60 border border-border/50">
              <div className="text-[10px] text-primary font-semibold mb-1">01. INITIATION</div>
              <div className="font-medium text-white">Demand & Request</div>
              <div className="text-[10px] text-muted-foreground mt-1">Anonymized signals or manual admin input</div>
            </div>
            <div className="p-3 rounded-lg bg-card/60 border border-border/50">
              <div className="text-[10px] text-primary font-semibold mb-1">02. RESEARCH</div>
              <div className="font-medium text-white">Evidence Pack</div>
              <div className="text-[10px] text-muted-foreground mt-1">Verifiable citations & standards</div>
            </div>
            <div className="p-3 rounded-lg bg-card/60 border border-border/50">
              <div className="text-[10px] text-primary font-semibold mb-1">03. REASONING</div>
              <div className="font-medium text-white">Multi-Agent Debate</div>
              <div className="text-[10px] text-muted-foreground mt-1">Architect vs Challenger synthesis</div>
            </div>
            <div className="p-3 rounded-lg bg-card/60 border border-border/50">
              <div className="text-[10px] text-primary font-semibold mb-1">04. AUTHORING</div>
              <div className="font-medium text-white">Original Writer</div>
              <div className="text-[10px] text-muted-foreground mt-1">Pedagogy, math, flashcards & quiz</div>
            </div>
            <div className="p-3 rounded-lg bg-card/60 border border-border/50">
              <div className="text-[10px] text-primary font-semibold mb-1">05. VALIDATION</div>
              <div className="font-medium text-white">5-Tier Inspector</div>
              <div className="text-[10px] text-muted-foreground mt-1">Fact, math, scope & consistency</div>
            </div>
            <div className="p-3 rounded-lg bg-card/60 border border-border/50">
              <div className="text-[10px] text-primary font-semibold mb-1">06. EDITORIAL</div>
              <div className="font-medium text-white">Human Approval</div>
              <div className="text-[10px] text-muted-foreground mt-1">Mandatory sign-off before publish</div>
            </div>
          </div>
        </div>

        {/* Quick Nav Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link
            href="/content-studio/demand"
            className="p-5 rounded-xl bg-card border border-border hover:border-primary/50 transition-colors group flex flex-col justify-between"
          >
            <div>
              <div className="w-8 h-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center mb-3">
                <TrendingUp className="w-4 h-4" />
              </div>
              <h3 className="text-sm font-semibold text-white group-hover:text-primary transition-colors">
                Explore Demand Signals
              </h3>
              <p className="text-xs text-muted-foreground mt-1">
                Inspect aggregated curriculum friction scores and trigger high-yield book drafts.
              </p>
            </div>
            <div className="flex items-center gap-1 text-xs text-primary font-medium mt-4">
              <span>View Signals</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </Link>

          <Link
            href="/content-studio/review"
            className="p-5 rounded-xl bg-card border border-border hover:border-primary/50 transition-colors group flex flex-col justify-between"
          >
            <div>
              <div className="w-8 h-8 rounded-lg bg-amber-500/10 text-amber-400 flex items-center justify-center mb-3">
                <CheckCircle2 className="w-4 h-4" />
              </div>
              <h3 className="text-sm font-semibold text-white group-hover:text-amber-400 transition-colors">
                Human Editorial Review Queue
              </h3>
              <p className="text-xs text-muted-foreground mt-1">
                Review generated drafts, inspect validation tiers, and perform mandatory editorial sign-off.
              </p>
            </div>
            <div className="flex items-center gap-1 text-xs text-amber-400 font-medium mt-4">
              <span>Review Drafts</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </Link>

          <Link
            href="/content-studio/published"
            className="p-5 rounded-xl bg-card border border-border hover:border-primary/50 transition-colors group flex flex-col justify-between"
          >
            <div>
              <div className="w-8 h-8 rounded-lg bg-blue-500/10 text-blue-400 flex items-center justify-center mb-3">
                <BookOpen className="w-4 h-4" />
              </div>
              <h3 className="text-sm font-semibold text-white group-hover:text-blue-400 transition-colors">
                Published Originals Catalog
              </h3>
              <p className="text-xs text-muted-foreground mt-1">
                Manage live and staging Quovex Originals accessible to students in the mobile app.
              </p>
            </div>
            <div className="flex items-center gap-1 text-xs text-blue-400 font-medium mt-4">
              <span>View Catalog</span>
              <ArrowRight className="w-3.5 h-3.5 group-hover:translate-x-1 transition-transform" />
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
}
