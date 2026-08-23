import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req, 'VIEW_ANALYTICS');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const flags = Array.from(adminStore.flags.values());
  return NextResponse.json({
    success: true,
    totalFlags: flags.length,
    flags,
  });
}
