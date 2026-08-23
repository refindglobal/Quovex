import { NextResponse } from 'next/server';
import { verifyAdminSession } from '@/lib/auth/rbac';
import fs from 'fs';
import path from 'path';

export async function GET(req: Request) {
  const auth = verifyAdminSession(req, 'VIEW_ANALYTICS');
  if (!auth.authorized) {
    return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
  }

  try {
    const catalogPath = path.resolve('../android/app/src/main/assets/ncert/ncert_catalog_v1.json');
    let catalog: any = null;

    if (fs.existsSync(catalogPath)) {
      catalog = JSON.parse(fs.readFileSync(catalogPath, 'utf8'));
    }

    if (!catalog) {
      return NextResponse.json({
        success: true,
        status: 'CATALOG_NOT_FOUND',
        totalBooks: 0,
        totalChapters: 0,
        validationScore: 100,
        issues: [],
      });
    }

    const books = catalog.books || [];
    const chapters = catalog.chapters || [];
    const issues: string[] = [];
    const bookIdSet = new Set<string>();
    const chapterIdSet = new Set<string>();

    for (const b of books) {
      if (bookIdSet.has(b.id)) issues.push(`Duplicate Book ID: ${b.id}`);
      bookIdSet.add(b.id);
    }

    for (const c of chapters) {
      if (chapterIdSet.has(c.id)) issues.push(`Duplicate Chapter ID: ${c.id}`);
      chapterIdSet.add(c.id);

      if (!bookIdSet.has(c.bookId)) {
        issues.push(`Orphan Chapter [${c.id}] references unknown bookId: ${c.bookId}`);
      }

      if (!c.officialPdfUrl || !c.officialPdfUrl.startsWith('http')) {
        issues.push(`Invalid PDF URL for Chapter [${c.id}]: ${c.officialPdfUrl}`);
      }
    }

    return NextResponse.json({
      success: true,
      status: issues.length === 0 ? 'VALID' : 'ISSUES_DETECTED',
      version: catalog.version || 1,
      lastUpdated: catalog.lastUpdated || '2026-08-20',
      totalBooks: books.length,
      totalChapters: chapters.length,
      classesCovered: [9, 10, 11, 12],
      validationScore: Math.max(0, 100 - issues.length * 5),
      issues,
    });
  } catch (error: any) {
    return NextResponse.json({ error: error.message }, { status: 500 });
  }
}
