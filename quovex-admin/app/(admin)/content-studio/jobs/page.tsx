'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import EmptyState from '@/components/EmptyState';
import { ContentGenerationJob } from '@/lib/types/content-studio';
import {
  Cpu,
  Sparkles,
  Clock,
  CheckCircle2,
  AlertTriangle,
  ArrowRight,
  RefreshCw,
  Layers,
} from 'lucide-react';

export default function GenerationJobsPage() {
  const [jobs, setJobs] = useState<ContentGenerationJob[]>([]);
  const [selectedJob, setSelectedJob] = useState<ContentGenerationJob | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchJobs = async () => {
    try {
      const res = await fetch('/api/content-studio/generation-jobs');
      const data = await res.json();
      if (data.success) {
        setJobs(data.jobs || []);
        if (!selectedJob && data.jobs?.length > 0) {
          setSelectedJob(data.jobs[0]);
        } else if (selectedJob) {
          const updated = data.jobs?.find((j: any) => j.jobId === selectedJob.jobId);
          if (updated) setSelectedJob(updated);
        }
      }
    } catch (err) {
      console.error('Failed to fetch generation jobs:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchJobs();
    const interval = setInterval(fetchJobs, 2500); // Polling control plane
    return () => clearInterval(interval);
  }, [selectedJob?.jobId]);

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="Generation Jobs"
        description="Live Asynchronous Multi-Agent Worker Pipeline & Stage Monitor"
        action={
          <button
            onClick={fetchJobs}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg border border-border text-xs text-muted-foreground hover:text-foreground transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Refresh</span>
          </button>
        }
      />

      <div className="p-8 space-y-6 flex-1 flex flex-col min-h-0">
        {loading ? (
          <div className="flex-1 flex items-center justify-center p-12 text-muted-foreground text-xs">
            Loading generation jobs...
          </div>
        ) : jobs.length === 0 ? (
          <EmptyState
            icon={Cpu}
            title="No Generation Jobs Active"
            description="When an administrator initiates a book request, the asynchronous background worker stages will execute and stream progress here."
            action={
              <Link
                href="/content-studio/requests/new"
                className="px-4 py-2 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
              >
                Start New Generation Job
              </Link>
            }
          />
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 flex-1 min-h-0">
            {/* Jobs List */}
            <div className="rounded-xl bg-card border border-border overflow-hidden flex flex-col">
              <div className="p-4 border-b border-border flex items-center justify-between">
                <span className="text-xs font-semibold text-white">Pipeline Jobs ({jobs.length})</span>
                <span className="text-[11px] text-muted-foreground">Auto-refreshing (2.5s)</span>
              </div>

              <div className="divide-y divide-border overflow-y-auto flex-1">
                {jobs.map((job) => {
                  const isSelected = selectedJob?.jobId === job.jobId;
                  const isComplete = job.status === 'READY_FOR_REVIEW';
                  const isFailed = job.status === 'FAILED' || job.status === 'FAILED_AI_UNAVAILABLE' || (job.stage as string) === 'FAILED_AI_UNAVAILABLE';
                  const isAiUnavailable = job.status === 'FAILED_AI_UNAVAILABLE' || (job.stage as string) === 'FAILED_AI_UNAVAILABLE';

                  return (
                    <div
                      key={job.jobId}
                      onClick={() => setSelectedJob(job)}
                      className={`p-4 transition-colors cursor-pointer space-y-2 ${
                        isSelected
                          ? isFailed
                            ? 'bg-red-500/10 border-l-2 border-red-500'
                            : 'bg-primary/10 border-l-2 border-primary'
                          : 'hover:bg-[#151D19]'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-semibold text-white">{job.jobId}</span>
                          <span
                            className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${
                              isComplete
                                ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                                : isFailed
                                ? 'bg-red-500/15 text-red-400 border border-red-500/30'
                                : 'bg-primary/10 text-primary border border-primary/20 animate-pulse'
                            }`}
                          >
                            {isAiUnavailable ? 'FAILED: AI UNAVAILABLE' : isFailed ? 'FAILED' : job.stage}
                          </span>
                        </div>
                        <span className={`text-xs font-bold ${isFailed ? 'text-red-400' : 'text-primary'}`}>
                          {job.progressPercentage}%
                        </span>
                      </div>

                      <div className="w-full bg-[#0C120F] h-1.5 rounded-full overflow-hidden">
                        <div
                          className={`h-full transition-all duration-300 rounded-full ${
                            isFailed ? 'bg-red-500' : 'bg-primary'
                          }`}
                          style={{ width: `${job.progressPercentage}%` }}
                        />
                      </div>

                      <div className="flex items-center justify-between text-[10px] text-muted-foreground">
                        <span>Initiated {new Date(job.createdAt).toLocaleTimeString()}</span>
                        <span>{job.stageLogs.length} stage log events</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Stage Progress & Logs Inspector */}
            <div className="lg:col-span-2 rounded-xl bg-card border border-border p-6 flex flex-col min-h-0 space-y-6">
              {selectedJob ? (
                <>
                  <div className="flex items-center justify-between pb-4 border-b border-border">
                    <div>
                      <div className="text-[10px] uppercase font-semibold text-primary mb-1">
                        Active Worker Job
                      </div>
                      <h3 className="text-sm font-semibold text-white">{selectedJob.jobId}</h3>
                      <p className="text-xs text-muted-foreground mt-0.5">
                        Target Book ID: {selectedJob.bookId}
                      </p>
                    </div>

                    {selectedJob.status === 'READY_FOR_REVIEW' && (
                      <Link
                        href={`/content-studio/books/${selectedJob.bookId}`}
                        className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-primary text-primary-foreground font-semibold text-xs hover:bg-primary/90 transition-colors"
                      >
                        <Sparkles className="w-3.5 h-3.5" />
                        <span>Open Draft Editor</span>
                      </Link>
                    )}

                    {(selectedJob.status === 'FAILED' || selectedJob.status === 'FAILED_AI_UNAVAILABLE' || (selectedJob.stage as string) === 'FAILED_AI_UNAVAILABLE') && (
                      <div className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400 text-xs font-semibold">
                        <AlertTriangle className="w-4 h-4" />
                        <span>Generation Halted — AI Failure</span>
                      </div>
                    )}
                  </div>

                  {/* Failure Alert Banner */}
                  {(selectedJob.status === 'FAILED' || selectedJob.status === 'FAILED_AI_UNAVAILABLE' || (selectedJob.stage as string) === 'FAILED_AI_UNAVAILABLE') && (
                    <div className="p-4 rounded-lg bg-red-950/30 border border-red-800/40 space-y-1.5">
                      <div className="flex items-center gap-2 text-xs font-bold text-red-400">
                        <AlertTriangle className="w-4 h-4 text-red-400" />
                        <span>AI Provider Unavailable — Generation Halted</span>
                      </div>
                      <p className="text-xs text-red-300/80 leading-relaxed">
                        {selectedJob.error || 'Server-side LLM call failed or produced invalid structure. The pipeline was halted to prevent publishing unverified or template filler content.'}
                      </p>
                    </div>
                  )}

                  {/* 16-Stage Visual Timeline */}
                  <div className="space-y-2">
                    <div className="text-xs font-semibold text-white">Pipeline Execution Stages</div>
                    <div className="p-4 rounded-lg bg-[#0C120F] border border-border">
                      <div className="flex items-center justify-between text-xs mb-2">
                        <span className="text-muted-foreground">Current Stage:</span>
                        <span className="text-primary font-bold">{selectedJob.stage}</span>
                      </div>
                      <div className="w-full bg-[#151D19] h-2 rounded-full overflow-hidden">
                        <div
                          className="bg-primary h-full transition-all duration-300 rounded-full"
                          style={{ width: `${selectedJob.progressPercentage}%` }}
                        />
                      </div>
                    </div>
                  </div>

                  {/* Stage Logs Terminal */}
                  <div className="flex-1 flex flex-col min-h-0 space-y-2">
                    <div className="text-xs font-semibold text-white">Live Execution Logs</div>
                    <div className="flex-1 p-4 rounded-lg bg-[#080D0B] border border-border font-mono text-[11px] text-muted-foreground overflow-y-auto space-y-2">
                      {selectedJob.stageLogs.map((log, idx) => (
                        <div key={idx} className="flex items-start gap-2 leading-relaxed">
                          <span className="text-primary/60 shrink-0">
                            [{new Date(log.timestamp).toLocaleTimeString()}]
                          </span>
                          <span className="text-primary/80 font-semibold shrink-0">
                            [{log.stage}]
                          </span>
                          <span className="text-foreground">{log.message}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              ) : (
                <div className="flex-1 flex flex-col items-center justify-center text-center p-6 text-muted-foreground">
                  <Cpu className="w-8 h-8 mb-2 opacity-40 text-primary" />
                  <div className="text-xs font-medium text-white">Select a Job</div>
                  <div className="text-[11px] mt-1">Select a job on the left to monitor live pipeline progress.</div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
