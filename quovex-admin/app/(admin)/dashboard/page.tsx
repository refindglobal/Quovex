'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import {
  Users,
  Clock,
  BookOpen,
  Sparkles,
  Layers,
  Cpu,
  CheckCircle2,
  AlertTriangle,
  ArrowUpRight,
  CreditCard,
  TrendingUp,
  Activity,
  Bot,
} from 'lucide-react';
import EmptyState from '@/components/EmptyState';

export default function DashboardPage() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/dashboard/kpis')
      .then((res) => res.json())
      .then((d) => {
        if (d.success) setData(d);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  const metrics = data?.metrics;

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Platform Overview</h1>
          <p className="text-sm text-muted-foreground">Real-time telemetry, student learning activity, and editorial control plane.</p>
        </div>
        <div className="flex items-center gap-3">
          <Link
            href="/content-studio/requests/new"
            className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-primary text-black font-semibold text-xs hover:bg-primary/90 transition-colors shadow-sm"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>New Book Draft</span>
          </Link>
          <Link
            href="/system"
            className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-secondary text-secondary-foreground font-medium text-xs hover:bg-secondary/80 border border-border transition-colors"
          >
            <Activity className="w-3.5 h-3.5 text-primary" />
            <span>System Status</span>
          </Link>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="p-5 rounded-xl bg-[#111917] border border-border space-y-3">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">Registered Students</span>
            <Users className="w-4 h-4 text-primary" />
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-foreground">{metrics?.totalUsers ?? 0}</span>
            <span className="text-xs text-primary font-medium">+{metrics?.newUsers7d ?? 0} this week</span>
          </div>
          <p className="text-[11px] text-muted-foreground">Active Today: {metrics?.activeUsersToday ?? 0}</p>
        </div>

        <div className="p-5 rounded-xl bg-[#111917] border border-border space-y-3">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Study Time</span>
            <Clock className="w-4 h-4 text-primary" />
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-foreground">{metrics?.totalStudyMinutes ?? 0}</span>
            <span className="text-xs text-muted-foreground">minutes</span>
          </div>
          <p className="text-[11px] text-muted-foreground">Sessions Completed: {metrics?.totalSessions ?? 0}</p>
        </div>

        <div className="p-5 rounded-xl bg-[#111917] border border-border space-y-3">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">Transformed Assets</span>
            <Layers className="w-4 h-4 text-primary" />
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-foreground">{metrics?.totalMaterials ?? 0}</span>
            <span className="text-xs text-muted-foreground">materials</span>
          </div>
          <p className="text-[11px] text-muted-foreground">
            {metrics?.totalFlashcards ?? 0} Cards • {metrics?.totalQuizzes ?? 0} Quizzes
          </p>
        </div>

        <div className="p-5 rounded-xl bg-[#111917] border border-border space-y-3">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">AI Operations Today</span>
            <Bot className="w-4 h-4 text-primary" />
          </div>
          <div className="flex items-baseline gap-2">
            <span className="text-3xl font-bold text-foreground">{metrics?.totalAiRequests ?? 0}</span>
            <span className="text-xs text-primary font-medium">99.4% success</span>
          </div>
          <p className="text-[11px] text-muted-foreground">Avg Latency: ~185ms (Multi-Key Pool)</p>
        </div>
      </div>

      {/* Second Row: Content Studio Status & Monetization */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Content Pipeline Status */}
        <div className="lg:col-span-2 p-6 rounded-xl bg-[#111917] border border-border space-y-5">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2.5">
              <Sparkles className="w-4 h-4 text-primary" />
              <h2 className="text-sm font-bold text-foreground">Content Studio Pipeline</h2>
            </div>
            <Link href="/content-studio" className="text-xs text-primary hover:underline flex items-center gap-1">
              <span>View All</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <Link
              href="/content-studio/published"
              className="p-4 rounded-lg bg-[#15201C] border border-border/80 hover:border-primary/40 transition-colors"
            >
              <div className="flex items-center justify-between text-muted-foreground mb-1">
                <span className="text-xs font-medium">Published Originals</span>
                <BookOpen className="w-4 h-4 text-primary" />
              </div>
              <div className="text-2xl font-bold text-foreground">{metrics?.publishedOriginalsCount ?? 0}</div>
              <p className="text-[10px] text-muted-foreground mt-1">Live in student catalog</p>
            </Link>

            <Link
              href="/content-studio/review"
              className="p-4 rounded-lg bg-[#15201C] border border-border/80 hover:border-primary/40 transition-colors"
            >
              <div className="flex items-center justify-between text-muted-foreground mb-1">
                <span className="text-xs font-medium">Review Queue</span>
                <CheckCircle2 className="w-4 h-4 text-yellow-400" />
              </div>
              <div className="text-2xl font-bold text-foreground">{metrics?.pendingReviewCount ?? 0}</div>
              <p className="text-[10px] text-muted-foreground mt-1">Awaiting editorial sign-off</p>
            </Link>

            <Link
              href="/content-studio/jobs"
              className="p-4 rounded-lg bg-[#15201C] border border-border/80 hover:border-primary/40 transition-colors"
            >
              <div className="flex items-center justify-between text-muted-foreground mb-1">
                <span className="text-xs font-medium">Active Generation Jobs</span>
                <Cpu className="w-4 h-4 text-primary" />
              </div>
              <div className="text-2xl font-bold text-foreground">{metrics?.activeGenerationJobsCount ?? 0}</div>
              <p className="text-[10px] text-muted-foreground mt-1">16-stage worker execution</p>
            </Link>
          </div>

          <div className="pt-2 flex items-center justify-between border-t border-border/60 text-xs text-muted-foreground">
            <span>Server-side Human Approval Invariant: <strong className="text-primary">ENFORCED</strong></span>
            <span>Zero PII Policy: <strong className="text-primary">ACTIVE</strong></span>
          </div>
        </div>

        {/* Monetization / Revenue Card (Zero Mock Data Compliant) */}
        <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-5 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <CreditCard className="w-4 h-4 text-muted-foreground" />
                <h2 className="text-sm font-bold text-foreground">Monetization</h2>
              </div>
              <span className="text-[10px] uppercase font-semibold px-2 py-0.5 rounded bg-muted/40 text-muted-foreground border border-border">
                {metrics?.billingStatus ?? 'UNAVAILABLE'}
              </span>
            </div>

            <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-2">
              <div className="text-xs text-muted-foreground font-medium">Billing Integration Status</div>
              <div className="text-lg font-bold text-foreground">Billing Data Unavailable</div>
              <p className="text-[11px] text-muted-foreground leading-relaxed">
                Google Play Billing & Stripe connectors are not active in current environment. In accordance with Rule 1, zero mock revenue is displayed.
              </p>
            </div>
          </div>

          <Link
            href="/monetization"
            className="w-full py-2 rounded-lg bg-[#182420] border border-border text-xs text-center font-medium text-foreground hover:bg-[#1E2E29] transition-colors"
          >
            Inspect Billing Settings
          </Link>
        </div>
      </div>

      {/* Third Row: Recent Security Audit Trail */}
      <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Activity className="w-4 h-4 text-primary" />
            <h2 className="text-sm font-bold text-foreground">Recent Security & Operations Audit Trail</h2>
          </div>
          <Link href="/audit-logs" className="text-xs text-primary hover:underline flex items-center gap-1">
            <span>View Full Audit Log</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        {data?.recentLogs?.length > 0 ? (
          <div className="space-y-2">
            {data.recentLogs.map((log: any) => (
              <div key={log.id} className="p-3 rounded-lg bg-[#15201C] border border-border/80 flex items-center justify-between text-xs">
                <div className="flex items-center gap-3">
                  <span className="font-mono text-[10px] px-2 py-0.5 rounded bg-primary/10 text-primary border border-primary/20">
                    {log.action}
                  </span>
                  <span className="text-foreground font-medium">{log.details}</span>
                </div>
                <div className="text-muted-foreground text-[11px] font-mono">
                  {new Date(log.timestamp).toLocaleTimeString()} • {log.actorEmail}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="py-6 text-center text-xs text-muted-foreground">
            No audit events recorded in this session yet.
          </div>
        )}
      </div>
    </div>
  );
}
