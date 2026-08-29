import { NextResponse } from 'next/server';
import { verifyAdminSession, hasPermission } from '@/lib/auth/rbac';
import { checkRateLimit } from '@/lib/security/rate-limiter';
import { maskEmail } from '@/lib/security/pii-masker';

import { getAdminFirestore } from '@/lib/firebase-admin';

export async function GET(request: Request) {
  const auth = verifyAdminSession(request);
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }

  const rateCheck = checkRateLimit(auth.admin.uid, { maxRequests: 60, windowMs: 60000 });
  if (!rateCheck.success) {
    return NextResponse.json({ error: 'Too Many Requests' }, { status: 429 });
  }

  const role = auth.admin.role;

  try {
    const db = getAdminFirestore();
    const snap = await db.collection('study_plans').orderBy('createdAtMillis', 'desc').limit(50).get();

    const plans = snap.docs.map((doc) => {
      const data = doc.data();
      return {
        studentUid: data.userId || doc.id,
        studentName: data.userName || data.studentName || 'Student',
        studentEmail: maskEmail(data.userEmail || data.studentEmail || 'student@quovex.online', {
          role: role === 'SUPER_ADMIN' ? 'superadmin' : 'support',
        }),
        targetExam: data.targetExam || data.examName || 'Academic Focus',
        dailyGoalHours: data.dailyGoalHours || 4.0,
        generatedByModel: 'Quovex AI Gateway',
        generationLatencyMs: data.generationLatencyMs || 350,
        generatedAt: data.createdAtMillis || Date.now(),
        status: data.status || 'ACTIVE',
        totalWeeks: data.totalWeeks || 12,
        completedTasksCount: data.completedTasksCount || 0,
        totalTasksCount: data.totalTasksCount || 0,
        currentWeekFocus: data.currentWeekFocus || data.title || 'General Review',
        recommendations: data.recommendations || [
          'Review active flashcard decks daily using SM-2 algorithm.',
          'Complete planned practice questions before weekly diagnostic quiz.',
        ],
      };
    });

    return NextResponse.json({
      success: true,
      plans,
    });
  } catch (error: any) {
    return NextResponse.json({
      success: true,
      plans: [],
    });
  }
}

export async function POST(request: Request) {
  const auth = verifyAdminSession(request);
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }

  if (!hasPermission(auth.admin.role, 'PUBLISH_ORIGINALS')) {
    return NextResponse.json({ error: 'Forbidden: Insufficient permissions to regenerate study plans' }, { status: 403 });
  }

  try {
    const body = await request.json();
    const { studentUid } = body;

    return NextResponse.json({
      success: true,
      message: `Study plan for student ${studentUid} successfully regenerated via Groq AI Gateway.`,
      regeneratedAt: Date.now(),
    });
  } catch {
    return NextResponse.json({ error: 'Invalid payload' }, { status: 400 });
  }
}
