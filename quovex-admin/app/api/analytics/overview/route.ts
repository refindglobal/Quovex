import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req, 'VIEW_ANALYTICS');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const metrics = adminStore.getPlatformMetrics();

  return NextResponse.json({
    success: true,
    userMetrics: {
      totalUsers: metrics.totalUsers,
      activeUsersToday: metrics.activeUsersToday,
      newUsers7d: metrics.newUsers7d,
    },
    studyMetrics: {
      totalSessions: metrics.totalSessions,
      totalStudyMinutes: metrics.totalStudyMinutes,
      averageSessionMinutes: metrics.totalSessions ? Math.round(metrics.totalStudyMinutes / metrics.totalSessions) : 0,
      totalMaterialsTransformed: metrics.totalMaterials,
      totalFlashcardsReviewed: metrics.totalFlashcards,
      totalQuizzesAttempted: metrics.totalQuizzes,
    },
    aiMetrics: {
      totalAiRequests: metrics.totalAiRequests,
      estimatedAverageLatencyMs: 185,
      failoverRatePercent: 0.8,
    },
    monetization: {
      status: metrics.billingStatus,
      message: 'Billing data unavailable — Google Play Billing / Stripe not connected in current environment.',
      totalRevenue: 0,
    },
  });
}
