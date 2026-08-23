'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import EmptyState from '@/components/EmptyState';
import { QuovexOriginalBook } from '@/lib/types/content-studio';
import {
  CheckCircle2,
  AlertTriangle,
  FileEdit,
  ShieldCheck,
  Sparkles,
  ArrowRight,
  BookOpen,
} from 'lucide-react';

export default function ReviewQueuePage() {
  const [books, setBooks] = useState<QuovexOriginalBook[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedBook, setSelectedBook] = useState<QuovexOriginalBook | null>(null);
  const [reviewNotes, setReviewNotes] = useState('Editorial review completed. Concept explanations, math notation, and practice quizzes verified.');
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  const fetchQueue = async () => {
    try {
      const res = await fetch('/api/content-studio/books');
      const data = await res.json();
      if (data.success) {
        const reviewable = (data.books || []).filter(
          (b: any) =>
            b.approvalStatus === 'READY_FOR_REVIEW' ||
            b.approvalStatus === 'REVISION_REQUESTED' ||
            b.approvalStatus === 'APPROVED'
        );
        setBooks(reviewable);
        if (!selectedBook && reviewable.length > 0) {
          setSelectedBook(reviewable[0]);
        } else if (selectedBook) {
          const updated = reviewable.find((b: any) => b.id === selectedBook.id);
          if (updated) setSelectedBook(updated);
        }
      }
    } catch (err) {
      console.error('Failed to load review queue:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQueue();
  }, []);

  const handleApprove = async () => {
    if (!selectedBook) return;
    setActionLoading(true);
    setMessage(null);

    try {
      const res = await fetch('/api/content-studio/review', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          action: 'APPROVE',
          bookId: selectedBook.id,
          adminId: 'admin_editorial_lead',
          notes: reviewNotes,
        }),
      });

      const data = await res.json();
      if (data.success) {
        setMessage('✓ Book approved successfully! Ready for publishing.');
        await fetchQueue();
      }
    } catch (err: any) {
      setMessage(`Error: ${err.message}`);
    } finally {
      setActionLoading(false);
    }
  };

  const handleRequestRevision = async () => {
    if (!selectedBook) return;
    setActionLoading(true);
    setMessage(null);

    try {
      const res = await fetch('/api/content-studio/review', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          action: 'REQUEST_REVISION',
          bookId: selectedBook.id,
          adminId: 'admin_editorial_lead',
          notes: reviewNotes,
        }),
      });

      const data = await res.json();
      if (data.success) {
        setMessage('Revision requested. Staged for updates.');
        await fetchQueue();
      }
    } catch (err: any) {
      setMessage(`Error: ${err.message}`);
    } finally {
      setActionLoading(false);
    }
  };

  const handlePublish = async (isStaging: boolean) => {
    if (!selectedBook) return;
    setActionLoading(true);
    setMessage(null);

    try {
      const res = await fetch('/api/content-studio/publish', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          action: 'PUBLISH',
          bookId: selectedBook.id,
          isStaging,
        }),
      });

      const data = await res.json();
      if (data.success) {
        setMessage(`✓ Published to ${isStaging ? 'TEST / STAGING' : 'LIVE PRODUCTION'} catalog!`);
        await fetchQueue();
      } else {
        setMessage(`Publish failed: ${data.error}`);
      }
    } catch (err: any) {
      setMessage(`Error: ${err.message}`);
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="Human Editorial Review Queue"
        description="Mandatory Human Sign-Off & Verification Control Plane"
      />

      <div className="p-8 space-y-6 flex-1 flex flex-col min-h-0">
        {loading ? (
          <div className="flex-1 flex items-center justify-center p-12 text-muted-foreground text-xs">
            Loading review queue...
          </div>
        ) : books.length === 0 ? (
          <EmptyState
            icon={CheckCircle2}
            title="Review Queue is Clear"
            description="When AI generation jobs finish multi-agent authoring and validation, manuscripts appear here for mandatory human review."
            action={
              <Link
                href="/content-studio/requests/new"
                className="px-4 py-2 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
              >
                Create Book Request
              </Link>
            }
          />
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 min-h-0">
            {/* Manuscripts List */}
            <div className="rounded-xl bg-card border border-border overflow-hidden flex flex-col">
              <div className="p-4 border-b border-border flex items-center justify-between">
                <span className="text-xs font-semibold text-white">Queue Manuscripts ({books.length})</span>
                <span className="text-[11px] text-muted-foreground">Mandatory Human Gate</span>
              </div>

              <div className="divide-y divide-border overflow-y-auto flex-1">
                {books.map((b) => {
                  const isSelected = selectedBook?.id === b.id;
                  return (
                    <div
                      key={b.id}
                      onClick={() => setSelectedBook(b)}
                      className={`p-4 transition-colors cursor-pointer space-y-1.5 ${
                        isSelected ? 'bg-primary/10 border-l-2 border-primary' : 'hover:bg-[#151D19]'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold text-white truncate max-w-[180px]">
                          {b.title}
                        </span>
                        <span
                          className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${
                            b.approvalStatus === 'APPROVED'
                              ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                              : b.approvalStatus === 'REVISION_REQUESTED'
                              ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                              : 'bg-primary/10 text-primary border border-primary/20'
                          }`}
                        >
                          {b.approvalStatus}
                        </span>
                      </div>

                      <div className="text-[11px] text-muted-foreground">
                        {b.subject} • {b.curriculum} — {b.gradeClass}
                      </div>

                      {b.validationReport && (
                        <div className="text-[10px] text-primary">
                          Validation Score: <span className="font-bold">{b.validationReport.overallScore}/100</span>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Editorial Sign-off Panel */}
            <div className="lg:col-span-2 rounded-xl bg-card border border-border p-6 flex flex-col min-h-0 space-y-6">
              {selectedBook ? (
                <>
                  <div className="flex items-center justify-between pb-4 border-b border-border">
                    <div>
                      <div className="text-[10px] uppercase font-semibold text-primary mb-1">
                        Manuscript Sign-off
                      </div>
                      <h3 className="text-base font-bold text-white">{selectedBook.title}</h3>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        {selectedBook.subject} • {selectedBook.curriculum} — {selectedBook.gradeClass} • {selectedBook.chapters?.length || 0} Chapters
                      </p>
                    </div>

                    <Link
                      href={`/content-studio/books/${selectedBook.id}`}
                      className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-border text-xs text-muted-foreground hover:text-white transition-colors"
                    >
                      <FileEdit className="w-3.5 h-3.5" />
                      <span>Inspect Chapters</span>
                    </Link>
                  </div>

                  {message && (
                    <div
                      className={`p-3 rounded-lg text-xs ${
                        message.startsWith('✓')
                          ? 'bg-emerald-500/10 border border-emerald-500/20 text-emerald-400'
                          : 'bg-amber-500/10 border border-amber-500/20 text-amber-400'
                      }`}
                    >
                      {message}
                    </div>
                  )}

                  {/* Validation Summary */}
                  {selectedBook.validationReport && (
                    <div className="p-4 rounded-lg bg-[#0C120F] border border-border space-y-2">
                      <div className="flex items-center justify-between text-xs">
                        <span className="font-semibold text-white">Automated 5-Tier Verification:</span>
                        <span className="font-bold text-primary">{selectedBook.validationReport.overallScore}/100</span>
                      </div>
                      <div className="grid grid-cols-2 md:grid-cols-5 gap-2 text-[11px] text-muted-foreground pt-1">
                        <div>Fact: <span className="text-foreground">{selectedBook.validationReport.factValidation.score}%</span></div>
                        <div>Math: <span className="text-foreground">{selectedBook.validationReport.mathValidation.score}%</span></div>
                        <div>Scope: <span className="text-foreground">{selectedBook.validationReport.curriculumValidation.score}%</span></div>
                        <div>Pedagogy: <span className="text-foreground">{selectedBook.validationReport.pedagogyValidation.score}%</span></div>
                        <div>Notation: <span className="text-foreground">{selectedBook.validationReport.consistencyValidation.score}%</span></div>
                      </div>
                    </div>
                  )}

                  {/* Human Editorial Sign-off Box */}
                  <div className="space-y-3 flex-1 flex flex-col">
                    <label className="block text-xs font-semibold text-white">
                      Reviewer Notes & Verification Sign-Off
                    </label>
                    <textarea
                      rows={4}
                      value={reviewNotes}
                      onChange={(e) => setReviewNotes(e.target.value)}
                      placeholder="Add editorial review notes, corrections, or approval sign-off..."
                      className="w-full p-3 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary resize-none flex-1"
                    />
                  </div>

                  {/* Actions Bar */}
                  <div className="flex flex-wrap items-center justify-between gap-3 pt-4 border-t border-border">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={handleRequestRevision}
                        disabled={actionLoading}
                        className="px-4 py-2 rounded-lg border border-amber-500/30 bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 text-xs font-semibold transition-colors disabled:opacity-50"
                      >
                        Request Revision
                      </button>

                      <button
                        onClick={handleApprove}
                        disabled={actionLoading}
                        className="px-5 py-2 rounded-lg bg-emerald-500 text-black font-semibold text-xs hover:bg-emerald-400 transition-colors disabled:opacity-50"
                      >
                        ✓ Approve Manuscript
                      </button>
                    </div>

                    {selectedBook.approvalStatus === 'APPROVED' && (
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => handlePublish(true)}
                          disabled={actionLoading}
                          className="px-4 py-2 rounded-lg bg-primary/20 hover:bg-primary/30 text-primary border border-primary/40 font-semibold text-xs transition-colors"
                        >
                          Publish to Staging
                        </button>

                        <button
                          onClick={() => handlePublish(false)}
                          disabled={actionLoading}
                          className="px-4 py-2 rounded-lg bg-primary text-primary-foreground font-semibold text-xs hover:bg-primary/90 transition-colors"
                        >
                          Publish to Production
                        </button>
                      </div>
                    )}
                  </div>
                </>
              ) : (
                <div className="flex-1 flex flex-col items-center justify-center text-center p-6 text-muted-foreground">
                  <CheckCircle2 className="w-8 h-8 mb-2 opacity-40 text-primary" />
                  <div className="text-xs font-medium text-white">Select a Manuscript</div>
                  <div className="text-[11px] mt-1">Select a book from the queue to perform human editorial approval.</div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
