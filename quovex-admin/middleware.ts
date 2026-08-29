import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  
  // Public routes that don't need authentication
  if (
    pathname.startsWith('/login') ||
    pathname.startsWith('/api/auth/login') ||
    pathname.startsWith('/api/system/health') ||
    pathname.startsWith('/_next') ||
    pathname.includes('.') // static files
  ) {
    return NextResponse.next();
  }

  // Next.js Edge Middleware doesn't support full Firebase Admin SDK.
  // The Firebase session cookie / token verification is enforced securely
  // on API routes in verifyAdminSession().
  // For page rendering, we do a basic check for the session cookie/header.
  // The API routes will ultimately reject unauthorized requests with 401/403.
  const hasSession = request.cookies.has('admin_session') || request.headers.has('authorization');

  if (!hasSession) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('from', pathname);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * Match all request paths except for the ones starting with:
     * - _next/static (static files)
     * - _next/image (image optimization files)
     * - favicon.ico (favicon file)
     */
    '/((?!_next/static|_next/image|favicon.ico).*)',
  ],
};
