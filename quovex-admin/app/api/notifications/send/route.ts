import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';

export async function POST(req: Request) {
  const auth = verifyAdminSession(req, 'SEND_NOTIFICATIONS');
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  try {
    const { title, body, targetAudience = 'ALL_USERS', targetValue } = await req.json();

    if (!title || !body) {
      return NextResponse.json({ error: 'Missing title or body for notification campaign' }, { status: 400 });
    }

    const campaign = adminStore.sendNotification(
      {
        title,
        body,
        targetAudience,
        targetValue,
        sentBy: auth.admin.email,
      },
      auth.admin
    );

    return NextResponse.json({ success: true, campaign });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
