import { NextResponse } from 'next/server';
import { studioStore } from '@/lib/content-studio/pipeline';

export async function GET(
  _req: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;
  const book = studioStore.books.get(id);

  // Security Invariant: Non-published books are completely inaccessible via public endpoint
  if (!book || book.approvalStatus !== 'PUBLISHED') {
    return NextResponse.json(
      { success: false, error: 'Book not found or not published.' },
      { status: 404 }
    );
  }

  // Strip internal editorial metadata
  const publicBook = {
    id: book.id,
    contentType: book.contentType,
    title: book.title,
    subtitle: book.subtitle,
    description: book.description,
    subject: book.subject,
    topic: book.topic,
    language: book.language,
    countryRegion: book.countryRegion,
    curriculum: book.curriculum,
    gradeClass: book.gradeClass,
    difficulty: book.difficulty,
    targetReadingTimeMinutes: book.targetReadingTimeMinutes,
    chapterCount: book.chapterCount,
    coverImageUrl: book.coverImageUrl,
    introduction: book.introduction,
    learningObjectives: book.learningObjectives,
    prerequisites: book.prerequisites,
    chapters: book.chapters,
    publishedAt: book.publishedAt,
    isStaging: book.isStaging,
  };

  return NextResponse.json({
    success: true,
    book: publicBook,
  });
}
