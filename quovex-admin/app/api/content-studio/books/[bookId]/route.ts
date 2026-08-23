import { NextResponse } from 'next/server';
import { studioStore } from '@/lib/content-studio/pipeline';

export async function GET(
  _req: Request,
  { params }: { params: Promise<{ bookId: string }> }
) {
  const { bookId } = await params;
  const book = studioStore.books.get(bookId);

  if (!book) {
    return NextResponse.json(
      { success: false, error: 'Book not found.' },
      { status: 404 }
    );
  }

  const job = studioStore.jobs.get(book.generationJobId);
  const evidencePack = job?.evidencePackId
    ? studioStore.evidencePacks.get(job.evidencePackId)
    : undefined;
  const blueprint = job?.editorialBlueprintId
    ? studioStore.blueprints.get(job.editorialBlueprintId)
    : undefined;

  return NextResponse.json({
    success: true,
    book,
    job,
    evidencePack,
    blueprint,
  });
}

export async function PATCH(
  req: Request,
  { params }: { params: Promise<{ bookId: string }> }
) {
  const { bookId } = await params;
  const book = studioStore.books.get(bookId);

  if (!book) {
    return NextResponse.json(
      { success: false, error: 'Book not found.' },
      { status: 404 }
    );
  }

  try {
    const updates = await req.json();

    // Security Invariant: Client CANNOT directly overwrite approval/creation fields
    delete updates.approvalStatus;
    delete updates.approvedBy;
    delete updates.approvedAt;
    delete updates.publishedAt;
    delete updates.createdBy;
    delete updates.id;

    Object.assign(book, updates);
    book.version += 1;
    book.updatedAt = Date.now();
    book.versionHistory.push({
      version: book.version,
      generationJobId: book.generationJobId,
      createdAt: Date.now(),
      createdBy: 'admin_editor',
      revisionReason: 'Admin editorial manual update',
    });

    return NextResponse.json({
      success: true,
      book,
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to update book.' },
      { status: 400 }
    );
  }
}
