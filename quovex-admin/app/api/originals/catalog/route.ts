/**
 * Public Quovex Originals Catalog Endpoint
 *
 * Dedicated public read-only contract for Android and web students.
 *
 * SECURITY INVARIANTS:
 * - Returns ONLY books with approvalStatus == 'PUBLISHED'
 * - Public production queries return ONLY isStaging == false (unless test header/param is provided)
 * - Excludes all internal Content Studio artifacts (evidence packs, debate logs, blueprints, validation reports)
 * - Zero write operations allowed on this public endpoint
 */

import { NextResponse } from 'next/server';
import { studioStore } from '@/lib/content-studio/pipeline';

export async function GET(req: Request) {
  const { searchParams } = new URL(req.url);
  const includeStaging = searchParams.get('staging') === 'true';
  const subject = searchParams.get('subject');
  const curriculum = searchParams.get('curriculum');
  const gradeClass = searchParams.get('grade');

  // Filter strictly for PUBLISHED books
  let publishedBooks = Array.from(studioStore.books.values()).filter(
    (b) => b.approvalStatus === 'PUBLISHED'
  );

  // Exclude staging books from public production catalog unless explicitly requested for verification
  if (!includeStaging) {
    publishedBooks = publishedBooks.filter((b) => !b.isStaging);
  }

  if (subject) {
    publishedBooks = publishedBooks.filter(
      (b) => b.subject.toLowerCase() === subject.toLowerCase()
    );
  }

  if (curriculum) {
    publishedBooks = publishedBooks.filter(
      (b) => b.curriculum.toLowerCase().includes(curriculum.toLowerCase())
    );
  }

  if (gradeClass) {
    publishedBooks = publishedBooks.filter(
      (b) => b.gradeClass.toLowerCase().includes(gradeClass.toLowerCase())
    );
  }

  // Strip internal fields for student consumption
  const sanitizedCatalog = publishedBooks.map((b) => ({
    id: b.id,
    contentType: b.contentType,
    title: b.title,
    subtitle: b.subtitle,
    description: b.description,
    subject: b.subject,
    topic: b.topic,
    language: b.language,
    countryRegion: b.countryRegion,
    curriculum: b.curriculum,
    gradeClass: b.gradeClass,
    difficulty: b.difficulty,
    targetReadingTimeMinutes: b.targetReadingTimeMinutes,
    chapterCount: b.chapterCount,
    coverImageUrl: b.coverImageUrl,
    learningObjectives: b.learningObjectives,
    prerequisites: b.prerequisites,
    publishedAt: b.publishedAt,
    isStaging: b.isStaging,
    chapters: b.chapters.map((ch) => ({
      chapterNumber: ch.chapterNumber,
      title: ch.title,
      summary: ch.summary,
      learningObjectives: ch.learningObjectives,
      sectionsCount: ch.sections.length,
      flashcardsCount: ch.flashcards.length,
      quizQuestionsCount: ch.quizQuestions.length,
    })),
  }));

  return NextResponse.json({
    success: true,
    total: sanitizedCatalog.length,
    catalog: sanitizedCatalog,
  });
}
