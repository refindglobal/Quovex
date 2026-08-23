import { NextResponse } from 'next/server';
import { contentPipeline } from '@/lib/content-studio/pipeline';

export async function POST(
  req: Request,
  { params }: { params: Promise<{ bookId: string }> }
) {
  const { bookId } = await params;

  try {
    const { chapterNumber, sectionNumber, topic, subject } = await req.json();

    if (!chapterNumber || !sectionNumber || !topic) {
      return NextResponse.json(
        { success: false, error: 'chapterNumber, sectionNumber, and topic are required.' },
        { status: 400 }
      );
    }

    const updatedSection = await contentPipeline.regenerateSection(
      bookId,
      Number(chapterNumber),
      String(sectionNumber),
      String(topic),
      String(subject || 'Physics')
    );

    if (!updatedSection) {
      return NextResponse.json(
        { success: false, error: 'Book or chapter not found.' },
        { status: 404 }
      );
    }

    return NextResponse.json({
      success: true,
      section: updatedSection,
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to regenerate section.' },
      { status: 500 }
    );
  }
}
