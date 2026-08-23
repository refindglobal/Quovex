import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req, 'MODERATE_CONTENT');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const reports = Array.from(adminStore.moderationReports.values());
  return NextResponse.json({
    success: true,
    totalReports: reports.length,
    reports,
  });
}

export async function POST(req: Request) {
  try {
    const body = await req.json();
    if (!body.targetId || !body.targetType || !body.reason) {
      return NextResponse.json({ error: 'Missing targetId, targetType, or reason' }, { status: 400 });
    }

    const created = adminStore.submitReport({
      targetId: body.targetId,
      targetType: body.targetType,
      reportedByUid: body.reportedByUid || 'student_reporter_anonymous',
      reason: body.reason,
      details: body.details || '',
    });

    return NextResponse.json({ success: true, report: created });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
