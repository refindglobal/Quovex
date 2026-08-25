import { NextResponse } from 'next/server';
import { contentPipeline } from '@/lib/content-studio/pipeline';

export async function POST(req: Request) {
  try {
    const { action, bookId, isStaging } = await req.json();

    if (!bookId || !action) {
      return NextResponse.json(
        { success: false, error: 'bookId and action ("PUBLISH" | "UNPUBLISH" | "ARCHIVE") are required.' },
        { status: 400 }
      );
    }

    if (action === 'PUBLISH') {
      const result = await contentPipeline.publishBook(bookId, isStaging ?? false);
      if (!result.success) {
        return NextResponse.json(
          { success: false, error: result.error },
          { status: 403 }
        );
      }
      return NextResponse.json({ success: true, book: result.book });
    } else if (action === 'UNPUBLISH') {
      const result = await contentPipeline.unpublishBook(bookId);
      if (!result.success) {
        return NextResponse.json(
          { success: false, error: result.error },
          { status: 400 }
        );
      }
      return NextResponse.json({ success: true, book: result.book });
    } else if (action === 'ARCHIVE') {
      const result = await contentPipeline.archiveBook(bookId);
      if (!result.success) {
        return NextResponse.json(
          { success: false, error: result.error },
          { status: 400 }
        );
      }
      return NextResponse.json({ success: true, book: result.book });
    }

    return NextResponse.json(
      { success: false, error: `Unsupported publish action: "${action}"` },
      { status: 400 }
    );
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to process publish action.' },
      { status: 500 }
    );
  }
}
