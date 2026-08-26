import { NextResponse } from 'next/server';
import { contentPipeline, studioStore } from '@/lib/content-studio/pipeline';
import { BookRequestInput } from '@/lib/types/content-studio';

export async function GET() {
  await studioStore.recoverStuckJobs();
  await studioStore.loadAllFromFirestore();
  const jobsList = Array.from(studioStore.jobs.values()).sort(
    (a, b) => b.createdAt - a.createdAt
  );

  return NextResponse.json({
    success: true,
    total: jobsList.length,
    jobs: jobsList,
  });
}

export async function POST(req: Request) {
  try {
    const body: { request: BookRequestInput; adminId?: string } = await req.json();
    if (!body.request || !body.request.topic) {
      return NextResponse.json(
        { success: false, error: 'Book request configuration is required.' },
        { status: 400 }
      );
    }

    const { jobId, bookId } = await contentPipeline.createAndStartJob(
      body.request,
      body.adminId || 'admin_editor'
    );

    const job = studioStore.jobs.get(jobId);

    return NextResponse.json({
      success: true,
      jobId,
      bookId,
      job,
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to start generation job.' },
      { status: 500 }
    );
  }
}
