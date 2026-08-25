import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function PATCH(req: Request, { params }: { params: Promise<{ flagId: string }> }) {
  const auth = verifyAdminSession(req, 'MANAGE_FLAGS');
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const { flagId } = await params;
  const { enabled, rolloutPercentage = 100 } = await req.json();

  const updated = await adminStore.updateFlagAsync(flagId, enabled, rolloutPercentage, auth.admin);

  if (!updated) {
    return NextResponse.json({ error: 'Feature flag not found' }, { status: 404 });
  }

  return NextResponse.json({ success: true, flag: updated });
}
