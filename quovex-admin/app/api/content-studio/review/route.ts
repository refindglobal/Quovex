import { NextResponse } from 'next/server';
import { contentPipeline } from '@/lib/content-studio/pipeline';

export async function POST(req: Request) {
  try {
    const { action, bookId, adminId, notes } = await req.json();

    if (!bookId || !action) {
      return NextResponse.json(
        { success: false, error: 'bookId and action ("APPROVE" | "REQUEST_REVISION") are required.' },
        { status: 400 }
      );
    }

    if (action === 'APPROVE') {
      const book = contentPipeline.approveBook(
        bookId,
        adminId || 'admin_editor',
        notes
      );
      if (!book) {
        return NextResponse.json(
          { success: false, error: 'Book not found.' },
          { status: 404 }
        );
      }
      return NextResponse.json({ success: true, book });
    } else if (action === 'REQUEST_REVISION') {
      const book = contentPipeline.requestRevision(
        bookId,
        adminId || 'admin_editor',
        notes || 'Editorial revision requested by reviewer.'
      );
      if (!book) {
        return NextResponse.json(
          { success: false, error: 'Book not found.' },
          { status: 404 }
        );
      }
      return NextResponse.json({ success: true, book });
    }

    return NextResponse.json(
      { success: false, error: `Unsupported review action: "${action}"` },
      { status: 400 }
    );
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to process review action.' },
      { status: 500 }
    );
  }
}
