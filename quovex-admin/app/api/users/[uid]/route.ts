import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function GET(req: Request, { params }: { params: Promise<{ uid: string }> }) {
  const auth = verifyAdminSession(req, 'MANAGE_USERS');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const { uid } = await params;
  const user = adminStore.users.get(uid);

  if (!user) {
    return NextResponse.json({ error: 'User not found' }, { status: 404 });
  }

  return NextResponse.json({ success: true, user });
}
