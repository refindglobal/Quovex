import { NextResponse } from 'next/server';
import { studioStore } from '@/lib/content-studio/pipeline';

export async function GET(
  _req: Request,
  { params }: { params: Promise<{ jobId: string }> }
) {
  const { jobId } = await params;
  let job = studioStore.jobs.get(jobId);
  if (!job) {
    job = (await studioStore.getJobAsync(jobId)) || undefined;
  }

  if (!job) {
    return NextResponse.json(
      { success: false, error: 'Generation job not found.' },
      { status: 404 }
    );
  }

  const book = studioStore.books.get(job.bookId);
  const evidencePack = job.evidencePackId
    ? studioStore.evidencePacks.get(job.evidencePackId)
    : undefined;
  const blueprint = job.editorialBlueprintId
    ? studioStore.blueprints.get(job.editorialBlueprintId)
    : undefined;
  const validationReport = job.validationReportId
    ? studioStore.validationReports.get(job.validationReportId)
    : undefined;

  return NextResponse.json({
    success: true,
    job,
    book,
    evidencePack,
    blueprint,
    validationReport,
  });
}
