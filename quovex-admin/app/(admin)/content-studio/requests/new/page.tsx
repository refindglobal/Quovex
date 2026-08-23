'use client';

import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Header from '@/components/Header';
import { BookRequestInput } from '@/lib/types/content-studio';
import {
  Sparkles,
  ArrowLeft,
  BookOpen,
  Layers,
  ShieldCheck,
  CheckCircle2,
} from 'lucide-react';
import Link from 'next/link';

function RequestWizardContent() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [formData, setFormData] = useState<BookRequestInput>({
    title: "Newton's Laws — Made Simple",
    subject: 'Physics',
    topic: "Newton's Laws of Motion & Free Body Diagrams",
    countryRegion: 'IN',
    curriculum: 'CBSE / JEE Main',
    gradeClass: 'Class 11',
    exam: 'JEE Main & Advanced',
    language: 'en',
    difficulty: 'Intermediate',
    targetReadingTimeMinutes: 45,
    chapterCount: 3,
    learningObjectives: [
      "Master Newton's Three Laws of Motion with physical intuition and mathematical rigor",
      "Draw flawless Free Body Diagrams (FBDs) for multi-body and inclined systems",
      "Apply the Impulse-Momentum Theorem to impact, collision, and rocket mechanics",
      "Calculate static and kinetic friction forces on flat and inclined surfaces",
      "Avoid the 4 most common competitive exam trap mistakes in mechanics"
    ],
    prerequisites: [
      "Basic coordinate geometry and vector addition",
      "Elementary differential calculus (rate of change)"
    ],
    isStaging: true, // Default to staging catalog for safety
  });

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const topic = searchParams.get('topic');
    const subject = searchParams.get('subject');
    const curriculum = searchParams.get('curriculum');
    const grade = searchParams.get('grade');
    const demandId = searchParams.get('demandId');

    if (topic || subject) {
      setFormData((prev) => ({
        ...prev,
        title: topic ? `${topic} — Made Simple` : prev.title,
        topic: topic || prev.topic,
        subject: subject || prev.subject,
        curriculum: curriculum || prev.curriculum,
        gradeClass: grade || prev.gradeClass,
        demandSignalId: demandId || prev.demandSignalId,
      }));
    }
  }, [searchParams]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);

    try {
      // 1. Save Request Record
      await fetch('/api/content-studio/book-requests', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });

      // 2. Start Asynchronous Pipeline Worker Job
      const res = await fetch('/api/content-studio/generation-jobs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          request: formData,
          adminId: 'admin_editor',
        }),
      });

      const data = await res.json();
      if (data.success) {
        router.push(`/content-studio/jobs?activeJob=${data.jobId}`);
      } else {
        setError(data.error || 'Failed to start generation job.');
      }
    } catch (err: any) {
      setError(err.message || 'Network error starting generation.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="New Book Request Wizard"
        description="Configure pedagogical parameters & initiate multi-agent generation"
        action={
          <Link
            href="/content-studio/requests"
            className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Back to Requests</span>
          </Link>
        }
      />

      <div className="p-8 max-w-4xl mx-auto w-full space-y-6 flex-1 overflow-y-auto">
        {error && (
          <div className="p-4 rounded-lg bg-destructive/10 border border-destructive/30 text-destructive text-xs">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Section 1: Book Identity */}
          <div className="p-6 rounded-xl bg-card border border-border space-y-4">
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              <BookOpen className="w-4 h-4 text-primary" />
              <span>Book Identity & Scope</span>
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Book Title
                </label>
                <input
                  type="text"
                  required
                  value={formData.title}
                  onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Subject
                </label>
                <select
                  value={formData.subject}
                  onChange={(e) => setFormData({ ...formData, subject: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                >
                  <option value="Physics">Physics</option>
                  <option value="Chemistry">Chemistry</option>
                  <option value="Mathematics">Mathematics</option>
                  <option value="Biology">Biology</option>
                  <option value="Accountancy">Accountancy</option>
                  <option value="Economics">Economics</option>
                  <option value="History">History</option>
                </select>
              </div>

              <div className="md:col-span-2">
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Core Topic Focus
                </label>
                <input
                  type="text"
                  required
                  value={formData.topic}
                  onChange={(e) => setFormData({ ...formData, topic: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                />
              </div>
            </div>
          </div>

          {/* Section 2: Curriculum & Pedagogy */}
          <div className="p-6 rounded-xl bg-card border border-border space-y-4">
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              <Layers className="w-4 h-4 text-primary" />
              <span>Curriculum & Structure</span>
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Curriculum
                </label>
                <input
                  type="text"
                  required
                  value={formData.curriculum}
                  onChange={(e) => setFormData({ ...formData, curriculum: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Grade / Class
                </label>
                <input
                  type="text"
                  required
                  value={formData.gradeClass}
                  onChange={(e) => setFormData({ ...formData, gradeClass: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Target Exam
                </label>
                <input
                  type="text"
                  value={formData.exam || ''}
                  onChange={(e) => setFormData({ ...formData, exam: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Chapter Count (1 - 6)
                </label>
                <input
                  type="number"
                  min="1"
                  max="6"
                  required
                  value={formData.chapterCount}
                  onChange={(e) => setFormData({ ...formData, chapterCount: Number(e.target.value) })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                />
              </div>

              <div>
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Target Difficulty
                </label>
                <select
                  value={formData.difficulty}
                  onChange={(e) => setFormData({ ...formData, difficulty: e.target.value as any })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                >
                  <option value="Simple">Simple (Conceptual Intro)</option>
                  <option value="Intermediate">Intermediate (Standard Curriculum)</option>
                  <option value="Advanced">Advanced (Competitive Mastery)</option>
                </select>
              </div>

              <div>
                <label className="block text-[11px] font-medium text-muted-foreground mb-1.5">
                  Reading Time (Minutes)
                </label>
                <input
                  type="number"
                  min="15"
                  max="180"
                  required
                  value={formData.targetReadingTimeMinutes}
                  onChange={(e) => setFormData({ ...formData, targetReadingTimeMinutes: Number(e.target.value) })}
                  className="w-full px-3 py-2 rounded-lg bg-[#0C120F] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
                />
              </div>
            </div>
          </div>

          {/* Section 3: Safety & Environment */}
          <div className="p-6 rounded-xl bg-card border border-border space-y-4">
            <h2 className="text-sm font-semibold text-white flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-primary" />
              <span>Safety & Deployment Environment</span>
            </h2>

            <div className="flex items-center justify-between p-4 rounded-lg bg-[#0C120F] border border-border">
              <div>
                <div className="text-xs font-semibold text-white">Publish to Test / Staging Catalog First</div>
                <div className="text-[11px] text-muted-foreground mt-0.5">
                  Isolates book to the test catalog until full verification and explicit production sign-off.
                </div>
              </div>
              <input
                type="checkbox"
                checked={formData.isStaging}
                onChange={(e) => setFormData({ ...formData, isStaging: e.target.checked })}
                className="w-4 h-4 text-primary rounded bg-background border-border focus:ring-primary"
              />
            </div>
          </div>

          {/* Submission Bar */}
          <div className="flex items-center justify-end gap-3 pt-4">
            <Link
              href="/content-studio/requests"
              className="px-4 py-2 rounded-lg border border-border text-xs font-medium text-muted-foreground hover:text-white transition-colors"
            >
              Cancel
            </Link>

            <button
              type="submit"
              disabled={submitting}
              className="flex items-center gap-2 px-6 py-2 rounded-lg bg-primary text-primary-foreground font-semibold text-xs hover:bg-primary/90 transition-colors disabled:opacity-50"
            >
              {submitting ? (
                <span>Starting Multi-Agent Worker...</span>
              ) : (
                <>
                  <Sparkles className="w-4 h-4" />
                  <span>Initiate Generation Job</span>
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function NewBookRequestPage() {
  return (
    <Suspense fallback={<div className="p-8 text-xs text-muted-foreground">Loading request wizard...</div>}>
      <RequestWizardContent />
    </Suspense>
  );
}
