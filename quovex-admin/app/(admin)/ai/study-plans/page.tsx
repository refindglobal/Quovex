'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Bot, ArrowLeft, RefreshCw, Sparkles, CheckCircle2, User, Calendar, BookOpen, Clock } from 'lucide-react';
import { QuovexCard, QuovexCardHeader, QuovexCardTitle } from '@/components/ui/QuovexCard';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { QuovexSearchInput } from '@/components/ui/QuovexSearchInput';

export default function StudyPlanInspectorPage() {
  const [plans, setPlans] = useState<any[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [regeneratingUid, setRegeneratingUid] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);

  const fetchPlans = () => {
    setLoading(true);
    fetch('/api/ai/study-plans')
      .then((res) => res.json())
      .then((data) => {
        if (data.success) setPlans(data.plans);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchPlans();
  }, []);

  const handleRegenerate = async (studentUid: string) => {
    setRegeneratingUid(studentUid);
    setFeedback(null);

    try {
      const res = await fetch('/api/ai/study-plans', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ studentUid }),
      });
      const data = await res.json();
      if (res.ok) {
        setFeedback(data.message);
        fetchPlans();
      }
    } catch {
      // Error handled
    } finally {
      setRegeneratingUid(null);
    }
  };

  const filteredPlans = plans.filter(
    (p) =>
      p.studentName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.targetExam.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.studentUid.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-8">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link
            href="/ai"
            className="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-gray-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
              <Bot className="w-6 h-6 text-emerald-400" />
              Student Study Plan Inspector
            </h1>
            <p className="text-sm text-gray-400">
              Inspect, analyze, and regenerate AI-synthesized student study roadmaps (Groq & Cerebras Gateway).
            </p>
          </div>
        </div>

        <QuovexButton variant="secondary" size="sm" onClick={fetchPlans} leftIcon={<RefreshCw className="w-3.5 h-3.5" />}>
          Refresh Plans
        </QuovexButton>
      </div>

      {feedback && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-xs text-emerald-400 flex items-center gap-2.5">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{feedback}</span>
        </div>
      )}

      {/* Search Bar */}
      <div className="max-w-md">
        <QuovexSearchInput
          value={searchQuery}
          onChange={setSearchQuery}
          placeholder="Filter by student name, UID, or target exam..."
        />
      </div>

      {/* Study Plans List */}
      <div className="space-y-4">
        {filteredPlans.map((plan) => (
          <QuovexCard key={plan.studentUid} hoverGlow>
            <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pb-4 border-b border-emerald-950/40">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-xl bg-[#00C896]/15 border border-[#00C896]/30 flex items-center justify-center text-[#00C896]">
                  <User className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-white flex items-center gap-2">
                    {plan.studentName}
                    <QuovexBadge variant="emerald">{plan.targetExam}</QuovexBadge>
                  </h3>
                  <p className="text-xs text-gray-400 font-mono mt-0.5">
                    UID: {plan.studentUid} • {plan.studentEmail}
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <QuovexButton
                  variant="outline"
                  size="sm"
                  isLoading={regeneratingUid === plan.studentUid}
                  onClick={() => handleRegenerate(plan.studentUid)}
                  leftIcon={<Sparkles className="w-3.5 h-3.5 text-emerald-400" />}
                >
                  Regenerate via AI
                </QuovexButton>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 my-4 text-xs">
              <div className="p-3 rounded-lg bg-[#0C1410] border border-emerald-950/40">
                <span className="text-gray-400 font-semibold block mb-1">Generated By</span>
                <span className="text-emerald-300 font-mono">{plan.generatedByModel}</span>
              </div>

              <div className="p-3 rounded-lg bg-[#0C1410] border border-emerald-950/40">
                <span className="text-gray-400 font-semibold block mb-1">Daily Commitment</span>
                <span className="text-white font-bold">{plan.dailyGoalHours} Hours / Day</span>
              </div>

              <div className="p-3 rounded-lg bg-[#0C1410] border border-emerald-950/40">
                <span className="text-gray-400 font-semibold block mb-1">Roadmap Progress</span>
                <span className="text-emerald-400 font-bold">
                  {plan.completedTasksCount} / {plan.totalTasksCount} Tasks ({Math.round((plan.completedTasksCount / plan.totalTasksCount) * 100)}%)
                </span>
              </div>

              <div className="p-3 rounded-lg bg-[#0C1410] border border-emerald-950/40">
                <span className="text-gray-400 font-semibold block mb-1">Current Week Focus</span>
                <span className="text-white font-medium truncate block">{plan.currentWeekFocus}</span>
              </div>
            </div>

            <div className="mt-2 space-y-1.5">
              <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">AI Pedagogical Recommendations:</span>
              <ul className="list-disc list-inside space-y-1 text-xs text-gray-300">
                {plan.recommendations.map((rec: string, idx: number) => (
                  <li key={idx}>{rec}</li>
                ))}
              </ul>
            </div>
          </QuovexCard>
        ))}
      </div>
    </div>
  );
}
