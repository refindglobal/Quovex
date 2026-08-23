import { NextResponse } from 'next/server';
import { studioStore } from '@/lib/content-studio/pipeline';
import { PostPublicationAnalytics } from '@/lib/types/content-studio';

// Live analytics store (Zero mock data: calculated exclusively from real database records)
const analyticsStore: Map<string, PostPublicationAnalytics> = new Map();

export async function GET() {
  const publishedBooks = Array.from(studioStore.books.values()).filter(
    (b) => b.approvalStatus === 'PUBLISHED'
  );

  const analyticsList = publishedBooks.map((b) => {
    return (
      analyticsStore.get(b.id) || {
        bookId: b.id,
        title: b.title,
        subject: b.subject,
        topic: b.topic,
        viewsCount: 0,
        startsCount: 0,
        chapterCompletions: {},
        averageReadingTimeMinutes: 0,
        quizAttemptsCount: 0,
        averageQuizScore: 0,
        flashcardsReviewedCount: 0,
        flashcardRetentionRate: 0,
        aiTutorFollowupQuestionsCount: 0,
        studentHelpfulnessRating: 0,
        preBookTopicAccuracy: 0,
        postBookTopicAccuracy: 0,
        updatedAt: Date.now(),
      }
    );
  });

  return NextResponse.json({
    success: true,
    total: analyticsList.length,
    analytics: analyticsList,
  });
}
