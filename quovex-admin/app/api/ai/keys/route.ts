import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req, 'MANAGE_AI_KEYS');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const keys = adminStore.getAiKeys();
  return NextResponse.json({
    success: true,
    totalKeys: keys.length,
    keys,
  });
}
