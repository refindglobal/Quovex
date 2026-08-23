'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import EmptyState from '@/components/EmptyState';
import { TopicDemandSignal } from '@/lib/types/content-studio';
import {
  TrendingUp,
  Sparkles,
  Search,
  Filter,
  Plus,
  BarChart2,
  ArrowUpRight,
  ShieldCheck,
} from 'lucide-react';

export default function DemandSignalsPage() {
  const [signals, setSignals] = useState<TopicDemandSignal[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSignal, setSelectedSignal] = useState<TopicDemandSignal | null>(null);

  useEffect(() => {
    async function fetchSignals() {
      try {
        const res = await fetch('/api/content-studio/demand-signals');
        const data = await res.json();
        if (data.success) {
          setSignals(data.signals || []);
        }
      } catch (err) {
        console.error('Failed to load demand signals:', err);
      } finally {
        setLoading(false);
      }
    }

    fetchSignals();
  }, []);

  const filteredSignals = signals.filter(
    (s) =>
      s.topicName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.subjectName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.curriculum.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="Demand Intelligence"
        description="Aggregated, Anonymized Curriculum Friction Metrics & Explainable Demand Scores"
        action={
          <Link
            href="/content-studio/requests/new"
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Manual Book Request</span>
          </Link>
        }
      />

      <div className="p-8 space-y-6 flex-1 flex flex-col min-h-0">
        {/* Search & Privacy Guarantee Bar */}
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div className="relative w-full md:w-96">
            <Search className="w-4 h-4 text-muted-foreground absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search topics, subjects, or curricula..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 rounded-lg bg-[#121815] border border-border text-xs text-foreground placeholder:text-muted-foreground focus:outline-none focus:border-primary"
            />
          </div>

          <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-[#121815] border border-border text-[11px] text-muted-foreground">
            <ShieldCheck className="w-3.5 h-3.5 text-primary" />
            <span>Zero PII: Statistics aggregated purely across curriculum nodes</span>
          </div>
        </div>

        {/* Content Area */}
        {loading ? (
          <div className="flex-1 flex items-center justify-center p-12 text-muted-foreground text-xs">
            Loading real demand signals...
          </div>
        ) : filteredSignals.length === 0 ? (
          <EmptyState
            icon={TrendingUp}
            title="No Demand Signals Yet"
            description="Student doubt interactions, quiz errors, and flashcard friction events will automatically aggregate here into explainable demand scores."
            action={
              <Link
                href="/content-studio/requests/new"
                className="px-4 py-2 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
              >
                Create Manual Book Request
              </Link>
            }
          />
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 min-h-0">
            {/* Table of Signals */}
            <div className="lg:col-span-2 rounded-xl bg-card border border-border overflow-hidden flex flex-col">
              <div className="p-4 border-b border-border flex items-center justify-between">
                <span className="text-xs font-semibold text-white">
                  Topic Friction Signals ({filteredSignals.length})
                </span>
                <span className="text-[11px] text-muted-foreground">Ranked by Demand Score</span>
              </div>

              <div className="overflow-y-auto flex-1 divide-y divide-border">
                {filteredSignals.map((signal) => {
                  const isSelected = selectedSignal?.id === signal.id;
                  return (
                    <div
                      key={signal.id}
                      onClick={() => setSelectedSignal(signal)}
                      className={`p-4 transition-colors cursor-pointer flex items-center justify-between ${
                        isSelected ? 'bg-primary/10 border-l-2 border-primary' : 'hover:bg-[#151D19]'
                      }`}
                    >
                      <div className="space-y-1">
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-semibold text-white">{signal.topicName}</span>
                          <span className="px-1.5 py-0.5 rounded bg-border text-[10px] text-muted-foreground">
                            {signal.subjectName}
                          </span>
                        </div>
                        <div className="text-[11px] text-muted-foreground">
                          {signal.curriculum} • {signal.gradeClass} {signal.exam ? `• ${signal.exam}` : ''}
                        </div>
                      </div>

                      <div className="flex items-center gap-4">
                        <div className="text-right">
                          <div className="text-sm font-bold text-primary">{signal.demandScore} / 100</div>
                          <div className="text-[10px] text-muted-foreground">Demand Score</div>
                        </div>
                        <Link
                          href={`/content-studio/requests/new?topic=${encodeURIComponent(signal.topicName)}&subject=${encodeURIComponent(signal.subjectName)}&curriculum=${encodeURIComponent(signal.curriculum)}&grade=${encodeURIComponent(signal.gradeClass)}&demandId=${signal.id}`}
                          onClick={(e) => e.stopPropagation()}
                          className="px-2.5 py-1.5 rounded bg-primary/10 hover:bg-primary/20 text-primary border border-primary/30 text-[11px] font-medium transition-colors"
                        >
                          Generate Draft
                        </Link>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Demand Breakdown Inspector */}
            <div className="rounded-xl bg-card border border-border p-5 flex flex-col">
              {selectedSignal ? (
                <div className="space-y-5 flex-1">
                  <div>
                    <div className="text-[10px] uppercase font-semibold text-primary mb-1">
                      Signal Breakdown
                    </div>
                    <h3 className="text-sm font-semibold text-white">{selectedSignal.topicName}</h3>
                    <p className="text-xs text-muted-foreground mt-0.5">
                      {selectedSignal.curriculum} • {selectedSignal.gradeClass}
                    </p>
                  </div>

                  <div className="p-4 rounded-lg bg-[#0C120F] border border-border space-y-3">
                    <div className="flex items-center justify-between text-xs">
                      <span className="text-muted-foreground font-medium">Explainable Demand Score</span>
                      <span className="font-bold text-primary text-base">{selectedSignal.demandScore} / 100</span>
                    </div>

                    <div className="space-y-2 pt-2 border-t border-border/60 text-xs">
                      <div className="flex items-center justify-between text-[11px]">
                        <span className="text-muted-foreground">AI Doubt Questions</span>
                        <span className="text-white font-medium">
                          {selectedSignal.questionCount} (Norm: {selectedSignal.normalizedQuestions})
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-[11px]">
                        <span className="text-muted-foreground">Quiz Mistakes</span>
                        <span className="text-white font-medium">
                          {selectedSignal.quizMistakeCount} (Norm: {selectedSignal.normalizedMistakes})
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-[11px]">
                        <span className="text-muted-foreground">Flashcard Lapse Rate</span>
                        <span className="text-white font-medium">
                          {selectedSignal.flashcardFailureCount} (Norm: {selectedSignal.normalizedFlashcardFailures})
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-[11px]">
                        <span className="text-muted-foreground">Image Doubts Count</span>
                        <span className="text-white font-medium">
                          {selectedSignal.imageDoubtCount} (Norm: {selectedSignal.normalizedImageDoubts})
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-[11px]">
                        <span className="text-muted-foreground">Affected Students Breadth</span>
                        <span className="text-white font-medium">
                          {selectedSignal.affectedStudents} (Norm: {selectedSignal.normalizedAffectedStudents})
                        </span>
                      </div>
                    </div>
                  </div>

                  <Link
                    href={`/content-studio/requests/new?topic=${encodeURIComponent(selectedSignal.topicName)}&subject=${encodeURIComponent(selectedSignal.subjectName)}&curriculum=${encodeURIComponent(selectedSignal.curriculum)}&grade=${encodeURIComponent(selectedSignal.gradeClass)}&demandId=${selectedSignal.id}`}
                    className="w-full flex items-center justify-center gap-2 py-2.5 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors mt-auto"
                  >
                    <Sparkles className="w-3.5 h-3.5" />
                    <span>Create Book Request from Signal</span>
                  </Link>
                </div>
              ) : (
                <div className="flex-1 flex flex-col items-center justify-center text-center p-6 text-muted-foreground">
                  <BarChart2 className="w-8 h-8 mb-2 opacity-40 text-primary" />
                  <div className="text-xs font-medium text-white">Select a Topic Signal</div>
                  <div className="text-[11px] mt-1">
                    Click any signal on the left to inspect its mathematical score breakdown and contributing metrics.
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
