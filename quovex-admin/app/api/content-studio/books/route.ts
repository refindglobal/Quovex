import { NextResponse } from 'next/server';
import { studioStore } from '@/lib/content-studio/pipeline';

export async function GET(req: Request) {
  const { searchParams } = new URL(req.url);
  const status = searchParams.get('status');
  const isStagingParam = searchParams.get('isStaging');

  let booksList = Array.from(studioStore.books.values());

  if (status) {
    booksList = booksList.filter((b) => b.approvalStatus === status);
  }

  if (isStagingParam !== null && isStagingParam !== undefined) {
    const isStaging = isStagingParam === 'true';
    booksList = booksList.filter((b) => b.isStaging === isStaging);
  }

  booksList.sort((a, b) => b.updatedAt - a.updatedAt);

  return NextResponse.json({
    success: true,
    total: booksList.length,
    books: booksList,
  });
}
