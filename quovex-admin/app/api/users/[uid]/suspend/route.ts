import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function POST(req: Request, { params }: { params: Promise<{ uid: string }> }) {
  const auth = verifyAdminSession(req, 'MANAGE_USERS');
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const { uid } = await params;
  const { action, reason = 'Administrative review' } = await req.json();

  if (action === 'SUSPEND') {
    const success = await adminStore.suspendUserAsync(uid, auth.admin, reason);
    if (!success) return NextResponse.json({ error: 'User not found' }, { status: 404 });
    return NextResponse.json({ success: true, status: 'SUSPENDED' });
  } else if (action === 'RESTORE') {
    const success = await adminStore.restoreUserAsync(uid, auth.admin);
    if (!success) return NextResponse.json({ error: 'User not found' }, { status: 404 });
    return NextResponse.json({ success: true, status: 'ACTIVE' });
  }

  return NextResponse.json({ error: 'Invalid action: must be SUSPEND or RESTORE' }, { status: 400 });
}
