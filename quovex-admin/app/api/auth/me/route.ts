import { NextResponse } from 'next/server';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req);
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: auth.error || 'Unauthorized' }, { status: auth.statusCode || 401 });
  }

  return NextResponse.json({ success: true, admin: auth.admin });
}
