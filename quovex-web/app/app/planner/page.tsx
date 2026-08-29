'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import {
  Zap,
  Sparkles,
  CheckCircle2,
  Circle,
  Bot,
  Calendar,
  Clock,
  ArrowRight,
  Plus,
  Compass,
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

const DEFAULT_DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

export default function StudyPlannerPage() {
  const [profile, setProfile] = useState<any>(null);
  const [studyPlan, setStudyPlan] = useState<StudyPlan | null>(null);
  const [loading, setLoading] = useState(false);
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

    setTimeout(async () => {
      const newPlanId = `plan_${Date.now()}`;
      const generatedTasks: StudyPlanTask[] = [
        {
          id: `task_${Date.now()}_1`,
          dayNumber: 1,
          dayName: 'Monday',
          title: `${targetExam}: High-Yield Core Derivations`,
          subject: 'Physics',
          durationMinutes: 60,
          isCompleted: false,
          priority: 'HIGH',
          category: 'Core Derivation',
        },
        {
          id: `task_${Date.now()}_2`,
          dayNumber: 1,
          dayName: 'Monday',
          title: 'Electrochemistry: Nernst equation numerical practice',
          subject: 'Chemistry',
          durationMinutes: 45,
          isCompleted: false,
          priority: 'HIGH',
          category: 'Numerical',
        },
        {
          id: `task_${Date.now()}_3`,
          dayNumber: 2,
          dayName: 'Tuesday',
          title: 'Definite Integrals & Area under curve shortcuts',
          subject: 'Mathematics',
          durationMinutes: 90,
          isCompleted: false,
          priority: 'HIGH',
          category: 'Problem Solving',
        },
        {
          id: `task_${Date.now()}_4`,
          dayNumber: 3,
          dayName: 'Wednesday',
          title: 'Thermodynamics & Heat engine cycle questions',
          subject: 'Physics',
          durationMinutes: 50,
          isCompleted: false,
          priority: 'MEDIUM',
          category: 'Revision',
        },
        {
          id: `task_${Date.now()}_5`,
          dayNumber: 4,
          dayName: 'Thursday',
          title: 'Organic Reaction Mechanisms & Reagents map',
          subject: 'Chemistry',
          durationMinutes: 60,
          isCompleted: false,
          priority: 'HIGH',
          category: 'Active Recall',
        },
        {
          id: `task_${Date.now()}_6`,
          dayNumber: 5,
          dayName: 'Friday',
          title: 'Diagnostic Quiz & Remedial Flashcard Synthesis',
          subject: 'All Subjects',
          durationMinutes: 60,
          isCompleted: false,
          priority: 'HIGH',
          category: 'Assessment',
        },
        {
          id: `task_${Date.now()}_7`,
          dayNumber: 6,
          dayName: 'Saturday',
          title: 'Spaced Repetition Review Queue & Mock Test',
          subject: 'Multi-Disciplinary',
          durationMinutes: 120,
          isCompleted: false,
          priority: 'HIGH',
          category: 'Spaced Repetition',
        },
      ];

      const plan: StudyPlan = {
        id: newPlanId,
        userId: currentUser.uid,
        title: `AI Roadmap: ${targetExam}`,
        targetExam,
        dailyGoalHours: profile?.dailyGoalHours || 4,
        totalWeeks: 12,
        status: 'ACTIVE',
        tasks: generatedTasks,
        createdAt: Date.now(),
        updatedAt: Date.now(),
      };

      await saveStudyPlan(currentUser.uid, plan);
      setIsGenerating(false);
    }, 1200);
  };

  const tasks = studyPlan?.tasks || [];
  const completedCount = tasks.filter((t) => t.isCompleted).length;
  const progressPct = tasks.length > 0 ? Math.round((completedCount / tasks.length) * 100) : 0;

  return (
    <div className="max-w-5xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div>
          <h1 className="text-display font-black text-text-primary flex items-center gap-4">
            <Zap className="w-10 h-10 text-primary" />
            AI Study Roadmap & Planner
          </h1>
          <p className="text-section text-text-secondary mt-2">
            Weekly dynamic task schedule customized for <span className="text-primary font-bold">{profile?.targetExam || 'JEE Advanced'}</span>.
          </p>
        </div>

        <div className="flex items-center gap-4">
          <QuovexButton
            variant="primary"
            size="lg"
            onClick={handleGeneratePlan}
            isLoading={isGenerating}
            leftIcon={<Sparkles className="w-5 h-5" />}
          >
            {tasks.length > 0 ? 'Regenerate Dynamic Plan' : 'Generate AI Study Plan'}
          </QuovexButton>
        </div>
      </div>

      {tasks.length > 0 ? (
        <div className="space-y-8">
          {/* Progress Bar Card */}
          <QuovexCard className="p-8 space-y-4 shadow-sm">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Calendar className="w-5 h-5 text-primary" />
                <h3 className="font-bold text-text-primary text-body">Weekly Milestone Progress</h3>
              </div>
              <span className="text-label font-bold text-primary font-mono">
                {completedCount}/{tasks.length} Completed ({progressPct}%)
              </span>
            </div>

            <div className="h-3 bg-surface-variant rounded-full overflow-hidden border border-border">
              <div
                className="h-full bg-gradient-to-r from-primary to-secondary rounded-full transition-all duration-500 shadow-glow"
                style={{ width: `${progressPct}%` }}
              />
            </div>
          </QuovexCard>

          {/* Tasks List */}
          <div className="space-y-4">
            <h2 className="text-title font-bold text-text-primary mb-3">This Week's High-Yield Tasks</h2>
            {tasks.map((task) => (
              <div
                key={task.id}
                onClick={() => handleToggleTask(task.id, task.isCompleted)}
                className={`p-5 rounded-2xl border transition-all cursor-pointer flex items-center justify-between gap-4 ${
                  task.isCompleted
                    ? 'bg-surface-variant/60 border-border/50 opacity-70'
                    : 'bg-surface border-border hover:border-primary/40 shadow-sm'
                }`}
              >
                <div className="flex items-center gap-4">
                  <button className="text-primary shrink-0">
                    {task.isCompleted ? (
                      <CheckCircle2 className="w-6 h-6 fill-primary text-primary-foreground" />
                    ) : (
                      <Circle className="w-6 h-6 text-text-secondary hover:text-primary" />
                    )}
                  </button>

                  <div>
                    <span className={`text-body font-semibold block ${task.isCompleted ? 'line-through text-text-secondary' : 'text-text-primary'}`}>
                      {task.title}
                    </span>
                    <div className="flex items-center gap-2 mt-1.5 text-label text-text-secondary font-bold">
                      <span>{task.dayName}</span>
                      <span>•</span>
                      <span>{task.subject}</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-3 shrink-0">
                  <span className="text-label text-text-secondary font-mono flex items-center gap-1.5 font-bold">
                    <Clock className="w-4 h-4" /> {task.durationMinutes}m
                  </span>
                  <QuovexBadge variant={task.priority === 'HIGH' ? 'fire' : 'muted'} size="md">
                    {task.priority}
                  </QuovexBadge>
                </div>
              </div>
            ))}
          </div>
        </div>
      ) : (
        /* Empty State (Zero Fake Tasks) */
        <QuovexCard className="p-16 text-center space-y-6 max-w-lg mx-auto shadow-sm">
          <div className="w-32 h-32 relative mx-auto opacity-90">
            <Image
              src={ASSETS.icons3d.calendar}
              alt="No Study Plan"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <h3 className="text-headline font-bold text-text-primary">No Active Study Roadmap Yet</h3>
            <p className="text-body text-text-secondary mt-2 max-w-sm mx-auto">
              Click below to generate a tailored 7-day high-yield study milestone schedule for {profile?.targetExam || 'your competitive exam'}.
            </p>
          </div>
          <div className="pt-4">
            <QuovexButton
              variant="primary"
              size="lg"
              className="shadow-glow"
              onClick={handleGeneratePlan}
              isLoading={isGenerating}
              leftIcon={<Sparkles className="w-5 h-5" />}
            >
              Generate AI Study Plan
            </QuovexButton>
          </div>
        </QuovexCard>
      )}
    </div>
  );
}
