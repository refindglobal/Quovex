'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  Zap,
  Sparkles,
  CheckCircle2,
  Circle,
  Calendar,
  Clock,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import {
  subscribeToUserProfile,
  subscribeToStudyPlan,
  saveStudyPlan,
  toggleStudyPlanTask,
  StudyPlan,
  StudyPlanTask,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { ASSETS } from '@/lib/assets';

export default function StudyPlannerPage() {
  const [profile, setProfile] = useState<any>(null);
  const [studyPlan, setStudyPlan] = useState<StudyPlan | null>(null);
  const [isGenerating, setIsGenerating] = useState(false);

  const currentUser = getCurrentUser();

  useEffect(() => {
    if (!currentUser) return;

    const unsubProfile = subscribeToUserProfile(currentUser.uid, (p) => setProfile(p));
    const unsubPlan = subscribeToStudyPlan(currentUser.uid, (plan) => setStudyPlan(plan));

    return () => {
      unsubProfile();
      unsubPlan();
    };
  }, [currentUser]);

  const handleToggleTask = async (taskId: string, currentStatus: boolean) => {
    if (!currentUser || !studyPlan) return;
    await toggleStudyPlanTask(currentUser.uid, studyPlan.id, taskId, !currentStatus);
  };

  const handleGeneratePlan = async () => {
    if (!currentUser || isGenerating) return;

    setIsGenerating(true);
    const targetExam = profile?.targetExam || 'JEE Advanced';
    const dailyHours = profile?.dailyGoalHours || 3;

    try {
      const res = await fetch('/api/ai/plan/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          targetExam,
          dailyHours,
          subjects: ['Physics', 'Chemistry', 'Mathematics'],
        }),
      });

      const json = await res.json();
      if (!res.ok || !json.success || !json.data) {
        throw new Error(json.error || 'Failed to generate study plan.');
      }

      const generatedTasks: StudyPlanTask[] = (json.data.tasks || []).map((t: any, idx: number) => ({
        id: `task_${Date.now()}_${idx + 1}`,
        dayNumber: t.dayNumber || idx + 1,
        dayName: t.dayName || `Day ${idx + 1}`,
        title: t.title,
        subject: t.subject || 'General',
        durationMinutes: t.durationMinutes || 60,
        isCompleted: false,
        priority: t.priority || 'HIGH',
        category: t.category || 'Core Focus',
      }));

      const plan: StudyPlan = {
        id: `plan_${Date.now()}`,
        userId: currentUser.uid,
        title: `AI Roadmap: ${targetExam}`,
        targetExam,
        dailyGoalHours: dailyHours,
        totalWeeks: 12,
        status: 'ACTIVE',
        tasks: generatedTasks,
        createdAt: Date.now(),
        updatedAt: Date.now(),
      };

      await saveStudyPlan(currentUser.uid, plan);
    } catch (err: any) {
      console.error('Study Plan Generation Error:', err);
      alert(err.message || 'AI Plan generation is temporarily busy. Please try again.');
    } finally {
      setIsGenerating(false);
    }
  };

  const tasks = studyPlan?.tasks || [];
  const completedCount = tasks.filter((t) => t.isCompleted).length;
  const progressPct = tasks.length > 0 ? Math.round((completedCount / tasks.length) * 100) : 0;

  return (
    <div className="max-w-4xl mx-auto space-y-6 pb-20">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
              <Zap className="w-7 h-7 text-primary" />
              AI Study Roadmap & Planner
            </h1>
            <QuovexBadge variant="emerald" size="sm">Dynamic</QuovexBadge>
          </div>
          <p className="text-xs sm:text-sm text-text-secondary mt-1">
            Weekly milestone task schedule customized for <strong className="text-primary">{profile?.targetExam || 'JEE Advanced'}</strong>.
          </p>
        </div>

        <div>
          <QuovexButton
            variant="primary"
            size="sm"
            onClick={handleGeneratePlan}
            isLoading={isGenerating}
            leftIcon={<Sparkles className="w-4 h-4" />}
          >
            {tasks.length > 0 ? 'Regenerate Plan' : 'Generate AI Plan'}
          </QuovexButton>
        </div>
      </div>

      {tasks.length > 0 ? (
        <div className="space-y-4">
          {/* Progress Bar Card */}
          <QuovexCard className="p-4 sm:p-5 space-y-3 shadow-sm">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs sm:text-sm font-bold text-text-primary">
                <Calendar className="w-4 h-4 text-primary" />
                <span>Weekly Milestone Progress</span>
              </div>
              <span className="text-xs font-bold text-primary font-mono">
                {completedCount}/{tasks.length} Done ({progressPct}%)
              </span>
            </div>

            <div className="h-2.5 bg-surface-variant rounded-full overflow-hidden border border-border">
              <div
                className="h-full bg-primary rounded-full transition-all duration-500 shadow-glow-xs"
                style={{ width: `${progressPct}%` }}
              />
            </div>
          </QuovexCard>

          {/* Tasks List */}
          <div className="space-y-2.5">
            <h2 className="text-xs sm:text-sm font-bold text-text-primary">Scheduled Focus Tasks</h2>
            {tasks.map((task) => (
              <div
                key={task.id}
                onClick={() => handleToggleTask(task.id, task.isCompleted)}
                className={`p-3.5 sm:p-4 rounded-xl border transition-all cursor-pointer flex items-center justify-between gap-3 ${
                  task.isCompleted
                    ? 'bg-surface-variant/50 border-border/60 opacity-60'
                    : 'bg-surface border-border hover:border-primary/40 shadow-xs'
                }`}
              >
                <div className="flex items-center gap-3">
                  <button className="text-primary shrink-0">
                    {task.isCompleted ? (
                      <CheckCircle2 className="w-5 h-5 fill-primary text-primary-foreground" />
                    ) : (
                      <Circle className="w-5 h-5 text-text-secondary hover:text-primary" />
                    )}
                  </button>

                  <div>
                    <span className={`text-xs sm:text-sm font-semibold block ${task.isCompleted ? 'line-through text-text-secondary' : 'text-text-primary'}`}>
                      {task.title}
                    </span>
                    <div className="flex items-center gap-2 mt-0.5 text-[11px] text-text-secondary">
                      <span>{task.dayName}</span>
                      <span>•</span>
                      <span>{task.subject}</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2.5 shrink-0">
                  <span className="text-xs text-text-secondary font-mono flex items-center gap-1 font-semibold">
                    <Clock className="w-3.5 h-3.5" /> {task.durationMinutes}m
                  </span>
                  <QuovexBadge variant={task.priority === 'HIGH' ? 'fire' : 'muted'} size="sm">
                    {task.priority}
                  </QuovexBadge>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : (
        /* Empty State */
        <QuovexCard className="p-10 sm:p-14 text-center space-y-4 max-w-md mx-auto shadow-sm">
          <div className="w-20 h-20 relative mx-auto opacity-80">
            <Image
              src={ASSETS.icons3d.calendar}
              alt="No Study Plan"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <h3 className="text-base font-bold text-text-primary">No Active Study Roadmap Yet</h3>
            <p className="text-xs text-text-secondary mt-1 max-w-xs mx-auto">
              Generate a tailored 7-day high-yield study milestone schedule for {profile?.targetExam || 'your competitive exam'}.
            </p>
          </div>
          <div className="pt-2">
            <QuovexButton
              variant="primary"
              size="md"
              className="shadow-glow-xs"
              onClick={handleGeneratePlan}
              isLoading={isGenerating}
              leftIcon={<Sparkles className="w-4 h-4" />}
            >
              Generate AI Study Plan
            </QuovexButton>
          </div>
        </QuovexCard>
      )}
    </div>
  );
}
