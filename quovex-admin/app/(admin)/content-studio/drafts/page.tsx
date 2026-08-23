'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import EmptyState from '@/components/EmptyState';
import { QuovexOriginalBook } from '@/lib/types/content-studio';
import {
  FileEdit,
  Sparkles,
  BookOpen,
  ArrowRight,
  ShieldCheck,
  CheckCircle2,
} from 'lucide-react';

export default function DraftBooksPage() {
  const [drafts, setDrafts] = useState<QuovexOriginalBook[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadDrafts() {
      try {
        const res = await fetch('/api/content-studio/books');
        const data = await res.json();
        if (data.success) {
          // Filter for active non-published drafts
          const nonPublished = (data.books || []).filter(
            (b: any) => b.approvalStatus !== 'PUBLISHED'
          );
          setDrafts(nonPublished);
        }
      } catch (err) {
        console.error('Failed to load drafts:', err);
      } finally {
        setLoading(false);
      }
    }

    loadDrafts();
  }, []);

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="Draft Books"
        description="Active Multi-Agent Generated Drafts & Pre-Publication Manuscripts"
        action={
          <Link
            href="/content-studio/requests/new"
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
          >
            <Sparkles className="w-3.5 h-3.5" />
            <span>Generate New Draft</span>
          </Link>
        }
      />

      <div className="p-8 space-y-6 flex-1 flex flex-col min-h-0">
        {loading ? (
          <div className="flex-1 flex items-center justify-center p-12 text-muted-foreground text-xs">
            Loading draft books...
          </div>
        ) : drafts.length === 0 ? (
          <EmptyState
            icon={FileEdit}
            title="No Draft Books in Progress"
            description="When generation jobs complete their multi-agent authoring stages, books appear here ready for inspection and refinement."
            action={
              <Link
                href="/content-studio/requests/new"
                className="px-4 py-2 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
              >
                Initiate Generation Job
              </Link>
            }
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {drafts.map((book) => (
              <div
                key={book.id}
                className="p-6 rounded-xl bg-card border border-border flex flex-col justify-between space-y-4 hover:border-primary/40 transition-colors"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="px-2 py-0.5 rounded bg-border text-[10px] font-semibold text-muted-foreground">
                      {book.subject}
                    </span>
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-semibold ${
                        book.approvalStatus === 'READY_FOR_REVIEW'
                          ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                          : book.approvalStatus === 'APPROVED'
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-primary/10 text-primary border border-primary/20'
                      }`}
                    >
                      {book.approvalStatus}
                    </span>
                  </div>

                  <h3 className="text-sm font-semibold text-white">{book.title}</h3>
                  <p className="text-xs text-muted-foreground line-clamp-2">{book.description}</p>

                  <div className="pt-2 border-t border-border/60 text-[11px] text-muted-foreground space-y-1">
                    <div>
                      Curriculum: <span className="text-foreground">{book.curriculum} — {book.gradeClass}</span>
                    </div>
                    <div>
                      Chapters: <span className="text-foreground">{book.chapters?.length || 0}</span> • Version: <span className="text-foreground">v{book.version}</span>
                    </div>
                    {book.validationReport && (
                      <div>
                        Validation Score: <span className="text-primary font-bold">{book.validationReport.overallScore}/100</span>
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-2">
                  <Link
                    href={`/content-studio/books/${book.id}`}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg bg-primary text-primary-foreground font-semibold text-xs hover:bg-primary/90 transition-colors"
                  >
                    <FileEdit className="w-3.5 h-3.5" />
                    <span>Open Editor</span>
                  </Link>

                  {book.approvalStatus === 'READY_FOR_REVIEW' && (
                    <Link
                      href="/content-studio/review"
                      className="px-3 py-2 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 border border-amber-500/30 text-xs font-semibold transition-colors"
                    >
                      Review
                    </Link>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
