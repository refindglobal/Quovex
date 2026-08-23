'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Layers, BookOpen, Sparkles, ShieldCheck, ArrowUpRight, Lock } from 'lucide-react';

export default function ContentCatalogPage() {
  const [summary, setSummary] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('/api/content/summary')
      .then((res) => res.json())
      .then((data) => {
        if (data.success) setSummary(data);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  }, []);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Unified Content Management</h1>
          <p className="text-sm text-muted-foreground">Governed tripartite separation: Official Resources, Quovex Originals, and Private User Materials.</p>
        </div>
      </div>

      {/* Tripartite Breakdown Cards */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Official Resources Card */}
        <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded bg-blue-500/10 text-blue-400 border border-blue-500/20">
                Official Resource
              </span>
              <BookOpen className="w-4 h-4 text-blue-400" />
            </div>
            <h2 className="text-base font-bold text-foreground">NCERT Curriculum Library</h2>
            <p className="text-xs text-muted-foreground leading-relaxed">
              Official rationalised textbook metadata for Classes 9–12 across Physics, Chemistry, Maths, and Biology. Linked directly to official portals.
            </p>
            <div className="pt-2 text-xs space-y-1.5 text-foreground">
              <div>Books: <strong>{summary?.officialResources?.totalBooks || 14} textbooks</strong></div>
              <div>Chapters: <strong>{summary?.officialResources?.totalChapters || 140} chapters</strong></div>
              <div>License: <strong className="text-muted-foreground">Official Metadata Only</strong></div>
            </div>
          </div>

          <Link
            href="/ncert"
            className="w-full py-2 rounded-lg bg-[#15201C] border border-border text-xs text-center font-medium text-foreground hover:bg-[#1C2A25] transition-colors flex items-center justify-center gap-1.5"
          >
            <span>Inspect NCERT Catalog</span>
            <ArrowUpRight className="w-3.5 h-3.5 text-primary" />
          </Link>
        </div>

        {/* Quovex Originals Card */}
        <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded bg-primary/10 text-primary border border-primary/20">
                Quovex Original
              </span>
              <Sparkles className="w-4 h-4 text-primary" />
            </div>
            <h2 className="text-base font-bold text-foreground">Content Studio Books</h2>
            <p className="text-xs text-muted-foreground leading-relaxed">
              High-yield educational books produced via multi-agent debate and 5-tier validation. Governed by mandatory human editorial approval.
            </p>
            <div className="pt-2 text-xs space-y-1.5 text-foreground">
              <div>Published: <strong>{summary?.quovexOriginals?.published || 0} books</strong></div>
              <div>Review Queue: <strong className="text-yellow-400">{summary?.quovexOriginals?.reviewQueue || 0} pending</strong></div>
              <div>Drafts in Progress: <strong>{summary?.quovexOriginals?.drafts || 0} manuscripts</strong></div>
            </div>
          </div>

          <Link
            href="/content-studio/published"
            className="w-full py-2 rounded-lg bg-[#15201C] border border-border text-xs text-center font-medium text-foreground hover:bg-[#1C2A25] transition-colors flex items-center justify-center gap-1.5"
          >
            <span>Open Content Studio</span>
            <ArrowUpRight className="w-3.5 h-3.5 text-primary" />
          </Link>
        </div>

        {/* Private User Materials Card */}
        <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-4 flex flex-col justify-between">
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded bg-purple-500/10 text-purple-400 border border-purple-500/20">
                User Material
              </span>
              <Lock className="w-4 h-4 text-purple-400" />
            </div>
            <h2 className="text-base font-bold text-foreground">Private Student Materials</h2>
            <p className="text-xs text-muted-foreground leading-relaxed">
              Scans, notes, flashcard decks, and quiz history created by individual students. Strictly isolated with zero admin eavesdropping.
            </p>
            <div className="p-3 rounded-lg bg-[#15201C] border border-border/80 text-[11px] text-muted-foreground leading-relaxed">
              <ShieldCheck className="w-3.5 h-3.5 text-primary inline mr-1" />
              Student notes and chat histories remain strictly client-side and encrypted in user subcollections.
            </div>
          </div>

          <div className="w-full py-2 rounded-lg bg-[#15201C]/60 border border-border/40 text-xs text-center font-medium text-muted-foreground cursor-not-allowed">
            Privacy Shielded (No Global Admin Access)
          </div>
        </div>
      </div>
    </div>
  );
}
