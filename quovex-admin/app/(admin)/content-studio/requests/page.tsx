'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import EmptyState from '@/components/EmptyState';
import { FilePlus2, Plus, Sparkles, Clock, CheckCircle2 } from 'lucide-react';

export default function BookRequestsPage() {
  const [requests, setRequests] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadRequests() {
      try {
        const res = await fetch('/api/content-studio/book-requests');
        const data = await res.json();
        if (data.success) {
          setRequests(data.requests || []);
        }
      } catch (err) {
        console.error('Failed to load book requests:', err);
      } finally {
        setLoading(false);
      }
    }

    loadRequests();
  }, []);

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="Book Requests"
        description="Admin-Initiated Educational Content Configurations & Generation Triggers"
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

      <div className="p-8 space-y-6 flex-1 flex flex-col min-h-0">
        {loading ? (
          <div className="flex-1 flex items-center justify-center p-12 text-muted-foreground text-xs">
            Loading book requests...
          </div>
        ) : requests.length === 0 ? (
          <EmptyState
            icon={FilePlus2}
            title="No Book Requests Yet"
            description="Create a new educational book request manually or directly from high-friction demand signals."
            action={
              <Link
                href="/content-studio/requests/new"
                className="px-4 py-2 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
              >
                Create First Book Request
              </Link>
            }
          />
        ) : (
          <div className="rounded-xl bg-card border border-border overflow-hidden flex flex-col">
            <div className="p-4 border-b border-border flex items-center justify-between">
              <span className="text-xs font-semibold text-white">Configured Requests ({requests.length})</span>
            </div>

            <div className="divide-y divide-border overflow-y-auto">
              {requests.map((req) => (
                <div key={req.id} className="p-4 flex items-center justify-between hover:bg-[#151D19] transition-colors">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-semibold text-white">{req.title}</span>
                      <span className="px-1.5 py-0.5 rounded bg-border text-[10px] text-muted-foreground">
                        {req.subject}
                      </span>
                    </div>
                    <div className="text-[11px] text-muted-foreground">
                      {req.curriculum} • {req.gradeClass} • {req.chapterCount} Chapters • {req.difficulty}
                    </div>
                  </div>

                  <Link
                    href={`/content-studio/jobs`}
                    className="px-3 py-1.5 rounded-lg bg-primary/10 hover:bg-primary/20 text-primary border border-primary/30 text-xs font-medium transition-colors"
                  >
                    View in Pipeline
                  </Link>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
