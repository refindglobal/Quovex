import { NextResponse } from 'next/server';
import { studioStore } from '@/lib/content-studio/pipeline';

export async function POST() {
  try {
    const result = await studioStore.recoverStuckJobs();
    return NextResponse.json({
      success: true,
      message: `Scanned and recovered ${result.recovered} stuck job(s).`,
      recoveredCount: result.recovered,
      jobIds: result.jobIds,
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to execute stuck job recovery.' },
      { status: 500 }
    );
  }
}
