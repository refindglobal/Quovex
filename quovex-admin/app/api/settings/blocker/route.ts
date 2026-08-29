import { NextResponse } from 'next/server';
import { verifyAdminSession, hasPermission } from '@/lib/auth/rbac';
import { checkRateLimit } from '@/lib/security/rate-limiter';

// In-memory / dynamic package blocklist configuration synced to student Android devices
let remoteBlockedPackages = [
  { packageName: 'com.zhiliaoapp.musically', appName: 'TikTok', category: 'SHORT_FORM_VIDEO', addedAt: Date.now() - 86400000 * 5 },
  { packageName: 'com.instagram.android', appName: 'Instagram (Reels)', category: 'SOCIAL_MEDIA', addedAt: Date.now() - 86400000 * 4 },
  { packageName: 'com.google.android.youtube', appName: 'YouTube (Shorts)', category: 'ENTERTAINMENT', addedAt: Date.now() - 86400000 * 3 },
  { packageName: 'com.snapchat.android', appName: 'Snapchat', category: 'MESSAGING', addedAt: Date.now() - 86400000 * 2 },
  { packageName: 'com.facebook.katana', appName: 'Facebook', category: 'SOCIAL_MEDIA', addedAt: Date.now() - 86400000 * 1 },
];

export async function GET(request: Request) {
  const auth = verifyAdminSession(request);
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }

  return NextResponse.json({
    success: true,
    packages: remoteBlockedPackages,
    totalCount: remoteBlockedPackages.length,
    updatedAt: Date.now(),
  });
}

export async function POST(request: Request) {
  const auth = verifyAdminSession(request);
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }

  if (!hasPermission(auth.admin.role, 'MANAGE_FLAGS')) {
    return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
  }

  const rateCheck = checkRateLimit(auth.admin.uid, { maxRequests: 30, windowMs: 60000 });
  if (!rateCheck.success) {
    return NextResponse.json({ error: 'Too Many Requests' }, { status: 429 });
  }

  try {
    const body = await request.json();
    const { packageName, appName, category } = body;

    if (!packageName || !appName) {
      return NextResponse.json({ error: 'Missing required fields' }, { status: 400 });
    }

    const newPkg = {
      packageName: packageName.trim(),
      appName: appName.trim(),
      category: category || 'SOCIAL_MEDIA',
      addedAt: Date.now(),
    };

    remoteBlockedPackages = [newPkg, ...remoteBlockedPackages.filter((p) => p.packageName !== newPkg.packageName)];

    return NextResponse.json({
      success: true,
      message: `App package ${newPkg.packageName} added to remote blocklist and synced to client devices.`,
      package: newPkg,
    });
  } catch {
    return NextResponse.json({ error: 'Invalid payload' }, { status: 400 });
  }
}

export async function DELETE(request: Request) {
  const auth = verifyAdminSession(request);
  if (!auth.authorized || !auth.admin) {
    return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
  }

  if (!hasPermission(auth.admin.role, 'MANAGE_FLAGS')) {
    return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
  }

  try {
    const { searchParams } = new URL(request.url);
    const packageName = searchParams.get('packageName');

    if (!packageName) {
      return NextResponse.json({ error: 'Missing packageName query parameter' }, { status: 400 });
    }

    remoteBlockedPackages = remoteBlockedPackages.filter((p) => p.packageName !== packageName);

    return NextResponse.json({
      success: true,
      message: `App package ${packageName} removed from remote blocklist.`,
    });
  } catch {
    return NextResponse.json({ error: 'Failed to delete' }, { status: 500 });
  }
}
