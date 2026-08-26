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

  const book = await studioStore.getBookAsync(job.bookId);
  const evidencePack = job.evidencePackId
    ? await studioStore.getEvidencePackAsync(job.evidencePackId)
    : undefined;
  const blueprint = job.editorialBlueprintId
    ? await studioStore.getBlueprintAsync(job.editorialBlueprintId)
    : undefined;
  const validationReport = job.validationReportId
    ? await studioStore.getValidationReportAsync(job.validationReportId)
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
