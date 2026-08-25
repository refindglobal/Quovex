import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req, 'VIEW_AUDIT_LOGS');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const { searchParams } = new URL(req.url);
  const action = searchParams.get('action');
  const targetType = searchParams.get('targetType');

  await adminStore.loadAuditLogsFromFirestore();
  let logs = adminStore.auditLogs;

  if (action) {
    logs = logs.filter((l) => l.action === action);
  }

  if (targetType) {
    logs = logs.filter((l) => l.targetType === targetType);
  }

  return NextResponse.json({
    success: true,
    totalLogs: logs.length,
    logs,
  });
}
