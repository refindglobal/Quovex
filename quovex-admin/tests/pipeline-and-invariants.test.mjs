import test from 'node:test';
import assert from 'node:assert/strict';
import { contentPipeline, studioStore } from '../lib/content-studio/pipeline.ts';

test('Pipeline Stages & Server-Side Approval Invariants', async (t) => {
  const request = {
    title: "Newton's Laws — Made Simple",
    subject: 'Physics',
    topic: "Newton's Laws of Motion & Free Body Diagrams",
    countryRegion: 'IN',
    curriculum: 'CBSE / JEE Main',
    gradeClass: 'Class 11',
    exam: 'JEE Main & Advanced',
    language: 'en',
    difficulty: 'Intermediate',
    targetReadingTimeMinutes: 45,
    chapterCount: 3,
    learningObjectives: [
      "Master Newton's Three Laws of Motion with physical intuition and mathematical rigor",
      "Draw flawless Free Body Diagrams (FBDs) for multi-body and inclined systems",
      "Apply the Impulse-Momentum Theorem to impact, collision, and rocket mechanics",
      "Calculate static and kinetic friction forces on flat and inclined surfaces",
      "Avoid the 4 most common competitive exam trap mistakes in mechanics"
    ],
    prerequisites: [
      "Basic coordinate geometry and vector addition",
      "Elementary differential calculus"
    ],
    isStaging: true,
  };

  let testJobId = '';
  let testBookId = '';

  await t.test('1. Asynchronous Worker starts and executes 16 stages to READY_FOR_REVIEW', async () => {
    const { jobId, bookId } = await contentPipeline.createAndStartJob(request, 'admin_test_runner');
    testJobId = jobId;
    testBookId = bookId;

    assert.ok(jobId.startsWith('job_'));
    assert.ok(bookId.startsWith('book_'));

    // Wait for the asynchronous pipeline worker to complete
    let attempts = 0;
    while (attempts < 120) {
      const currentJob = studioStore.jobs.get(jobId);
      if (currentJob && (currentJob.status === 'READY_FOR_REVIEW' || currentJob.status === 'FAILED')) {
        break;
      }
      await new Promise((r) => setTimeout(r, 1000));
      attempts++;
    }

    const finalJob = studioStore.jobs.get(jobId);
    assert.ok(finalJob, 'Job should exist in store');
    assert.equal(finalJob.status, 'READY_FOR_REVIEW');
    assert.equal(finalJob.progressPercentage, 100);
    assert.ok(finalJob.stageLogs.length >= 10, 'Should contain detailed stage logs');

    const draftBook = studioStore.books.get(bookId);
    assert.ok(draftBook, 'Draft book must exist');
    assert.equal(draftBook.approvalStatus, 'READY_FOR_REVIEW');
    assert.equal(draftBook.chapters.length, 3);
    assert.ok(draftBook.validationReport, 'Validation report must be attached');
    assert.ok(draftBook.validationReport.overallScore >= 80, 'Validation score should be >= 80%');
  });

  await t.test('2. Server-Side Approval Invariant strictly rejects unapproved publish', async () => {
    // Attempting to publish while status is 'READY_FOR_REVIEW' (not yet approved)
    const result = await contentPipeline.publishBook(testBookId, true);
    assert.equal(result.success, false, 'Must reject publish when not approved');
    assert.ok(result.error?.includes('Server-Side Security Invariant Violation'));

    const book = studioStore.books.get(testBookId);
    assert.notEqual(book?.approvalStatus, 'PUBLISHED');
  });

  await t.test('3. Human Editorial Sign-Off transitions state to APPROVED with audit trail', async () => {
    const approvedBook = await contentPipeline.approveBook(
      testBookId,
      'admin_lead_editor',
      'Verified all 3 chapters, LaTeX formulas, and practice quiz explanations.'
    );

    assert.ok(approvedBook);
    assert.equal(approvedBook.approvalStatus, 'APPROVED');
    assert.equal(approvedBook.approvedBy, 'admin_lead_editor');
    assert.ok(approvedBook.approvedAt > 0);
  });

  await t.test('4. Publish to Test / Staging Catalog succeeds for approved book', async () => {
    const publishResult = await contentPipeline.publishBook(testBookId, true);
    assert.equal(publishResult.success, true);
    assert.equal(publishResult.book?.approvalStatus, 'PUBLISHED');
    assert.equal(publishResult.book?.isStaging, true);
  });

  await t.test('5. Unpublish immediately revokes public visibility', async () => {
    const unpublishResult = await contentPipeline.unpublishBook(testBookId);
    assert.equal(unpublishResult.success, true);
    assert.equal(unpublishResult.book?.approvalStatus, 'UNPUBLISHED');
  });
});
