import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req, 'VIEW_ANALYTICS');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const metrics = adminStore.getPlatformMetrics();
  const systemHealth = adminStore.getSystemHealth();
  const recentLogs = adminStore.auditLogs.slice(0, 5);

  return NextResponse.json({
    success: true,
    metrics,
    systemHealth,
    recentLogs,
  });
}
