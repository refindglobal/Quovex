import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function POST(req: Request) {
  const auth = verifyAdminSession(req, 'MODERATE_CONTENT');
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  try {
    const { reportId, action, notes = 'Reviewed by admin' } = await req.json();

    if (!reportId || !action) {
      return NextResponse.json({ error: 'Missing reportId or action' }, { status: 400 });
    }

    const resolved = await adminStore.resolveReportAsync(reportId, action, notes, auth.admin);

    if (!resolved) {
      return NextResponse.json({ error: 'Report not found' }, { status: 404 });
    }

    return NextResponse.json({ success: true, report: resolved });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
