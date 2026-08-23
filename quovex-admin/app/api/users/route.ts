import { NextResponse } from 'next/server';
import { adminStore } from '@/lib/admin-store';
import { verifyAdminSession } from '@/lib/auth/rbac';
import { UserAccount } from '@/lib/types/admin';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req, 'MANAGE_USERS');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  const { searchParams } = new URL(req.url);
  const query = (searchParams.get('q') || '').toLowerCase();
  const status = searchParams.get('status');
  const exam = searchParams.get('exam');

  let list = Array.from(adminStore.users.values());

  if (query) {
    list = list.filter(
      (u) =>
        u.email.toLowerCase().includes(query) ||
        u.displayName.toLowerCase().includes(query) ||
        u.uid.toLowerCase().includes(query)
    );
  }

  if (status) {
    list = list.filter((u) => u.status === status);
  }

  if (exam) {
    list = list.filter((u) => u.examTarget === exam);
  }

  return NextResponse.json({
    success: true,
    total: list.length,
    users: list,
  });
}

export async function POST(req: Request) {
  const auth = verifyAdminSession(req, 'MANAGE_USERS');
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  try {
    const body = await req.json();
    if (!body.email || !body.displayName) {
      return NextResponse.json({ error: 'Missing required user fields' }, { status: 400 });
    }

    const newUser: UserAccount = {
      uid: body.uid || `usr_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`,
      email: body.email,
      displayName: body.displayName,
      examTarget: body.examTarget || 'General',
      gradeClass: body.gradeClass || 'Class 11',
      status: body.status || 'ACTIVE',
      createdAt: Date.now(),
      lastActiveAt: Date.now(),
      studyMinutesTotal: 0,
      materialsCount: 0,
      flashcardsCount: 0,
      quizzesTakenCount: 0,
      streakDays: 0,
    };

    const saved = adminStore.upsertUser(newUser);

    adminStore.logAudit({
      actorUid: auth.admin.uid,
      actorEmail: auth.admin.email,
      actorRole: auth.admin.role,
      action: 'ROLE_CHANGE',
      targetId: saved.uid,
      targetType: 'USER',
      details: `Created/Registered student user profile: ${saved.email}`,
      success: true,
    });

    return NextResponse.json({ success: true, user: saved });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
