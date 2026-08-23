'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import EmptyState from '@/components/EmptyState';
import { PostPublicationAnalytics } from '@/lib/types/content-studio';
import {
  BarChart3,
  TrendingUp,
  BookOpen,
  CheckCircle2,
  Clock,
  Sparkles,
} from 'lucide-react';

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<PostPublicationAnalytics[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function loadAnalytics() {
      try {
        const res = await fetch('/api/content-studio/analytics');
        const data = await res.json();
        if (data.success) {
          setAnalytics(data.analytics || []);
        }
      } catch (err) {
        console.error('Failed to load analytics:', err);
      } finally {
        setLoading(false);
      }
    }

    loadAnalytics();
  }, []);

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="Post-Publication Content Analytics"
        description="Active Student Engagement, Quiz Accuracy, and Flashcard Retention Metrics"
      />

      <div className="p-8 space-y-6 flex-1 flex flex-col min-h-0">
        {loading ? (
          <div className="flex-1 flex items-center justify-center p-12 text-muted-foreground text-xs">
            Loading analytics data...
          </div>
        ) : analytics.length === 0 ? (
          <EmptyState
            icon={BarChart3}
            title="No Post-Publication Analytics Yet"
            description="Analytics will automatically compute and populate here once published books are read and studied by students."
            action={
              <Link
                href="/content-studio/published"
                className="px-4 py-2 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
              >
                View Published Books
              </Link>
            }
          />
        ) : (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {analytics.map((item) => (
                <div key={item.bookId} className="p-6 rounded-xl bg-card border border-border space-y-4">
                  <div>
                    <div className="text-[10px] text-primary font-semibold uppercase">{item.subject}</div>
                    <h3 className="text-sm font-semibold text-white mt-0.5">{item.title}</h3>
                  </div>

                  <div className="grid grid-cols-2 gap-3 pt-2 border-t border-border/60 text-xs">
                    <div className="p-3 rounded-lg bg-[#0C120F] border border-border">
                      <div className="text-muted-foreground text-[10px]">Total Views</div>
                      <div className="text-base font-bold text-white mt-0.5">{item.viewsCount}</div>
                    </div>
                    <div className="p-3 rounded-lg bg-[#0C120F] border border-border">
                      <div className="text-muted-foreground text-[10px]">Quiz Accuracy</div>
                      <div className="text-base font-bold text-primary mt-0.5">
                        {item.averageQuizScore > 0 ? `${item.averageQuizScore}%` : 'N/A'}
                      </div>
                    </div>
                    <div className="p-3 rounded-lg bg-[#0C120F] border border-border">
                      <div className="text-muted-foreground text-[10px]">Avg Reading Time</div>
                      <div className="text-base font-bold text-white mt-0.5">
                        {item.averageReadingTimeMinutes}m
                      </div>
                    </div>
                    <div className="p-3 rounded-lg bg-[#0C120F] border border-border">
                      <div className="text-muted-foreground text-[10px]">Retention Rate</div>
                      <div className="text-base font-bold text-emerald-400 mt-0.5">
                        {item.flashcardRetentionRate > 0 ? `${item.flashcardRetentionRate}%` : 'N/A'}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
