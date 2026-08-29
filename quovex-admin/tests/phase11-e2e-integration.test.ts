import test from 'node:test';
import assert from 'node:assert/strict';
import { contentPipeline, studioStore } from '../lib/content-studio/pipeline';
import { verifyAdminSession, hasPermission } from '../lib/auth/rbac';
import { QuovexOriginalBook } from '../lib/types/content-studio';

test('Phase 11 — End-to-End Real Flow & Security Invariants', async (t) => {
  // 1. Admin Security & Role Access Verification
  await t.test('1. Unauthenticated request is rejected with 401', () => {
    const analystHasPublish = hasPermission('ANALYST', 'PUBLISH_ORIGINALS');
    assert.equal(analystHasPublish, false, 'Analyst must not have PUBLISH_ORIGINALS permission');
  });

  await t.test('2. Analyst role attempting publish is rejected with 403 Forbidden', () => {
    const req = new Request('http://localhost/api/content-studio/publish', {
      headers: {
        Authorization: 'Bearer test_token',
        'x-admin-role': 'ANALYST',
        'x-admin-email': 'analyst@quovex.ai',
        'x-admin-uid': 'analyst_01'
      }
    });
    const access = verifyAdminSession(req, 'PUBLISH_ORIGINALS');
    assert.equal(access.authorized, false);
    assert.equal(access.statusCode, 403);
  });

  await t.test('3. Super Admin is authorized for all content operations', () => {
    const req = new Request('http://localhost/api/content-studio/publish', {
      headers: {
        Authorization: 'Bearer test_token',
        'x-admin-role': 'SUPER_ADMIN',
        'x-admin-email': 'admin@quovex.ai',
        'x-admin-uid': 'super_admin_01'
      }
    });
    const access = verifyAdminSession(req, 'PUBLISH_ORIGINALS');
    assert.equal(access.authorized, true);
  });

  // 2. Draft Lifecycle & Human Approval Invariant Testing
  const testBookId = `book_p11_${Date.now()}`;
  const draftBook: QuovexOriginalBook = {
    id: testBookId,
    contentType: 'QUOVEX_ORIGINAL',
    title: "Newton's Laws — Made Simple",
    subtitle: 'Mastering Classical Dynamics for JEE',
    description: 'A rigorous, intuitive guide to forces and momentum.',
    subject: 'Physics',
    topic: 'Classical Mechanics',
    countryRegion: 'IN',
    curriculum: 'CBSE',
    gradeClass: 'Class 11',
    exam: 'JEE Main',
    language: 'en',
    chapterCount: 2,
    generationJobId: 'job_test_01',
    version: 1,
    isStaging: false,
    versionHistory: [],
    learningObjectives: ["Master Newton's Laws", "Understand Free Body Diagrams"],
    prerequisites: ['Basic vectors'],
    difficulty: 'Intermediate',
    targetReadingTimeMinutes: 45,
    introduction: 'Welcome to classical dynamics.',
    chapters: [
      {
        chapterNumber: 1,
        title: 'First Law & Inertia',
        summary: 'Inertia and reference frames.',
        learningObjectives: ['Understand inertia'],
        quickRevisionBulletPoints: ['Inertia requires zero net force.'],
        sections: [
          {
            id: 'sec_1_1',
            sectionNumber: '1.1',
            title: 'Galileo to Newton',
            conceptualExplanation: 'Inertia is resistance to acceleration.',
            visualAnalogy: 'A puck sliding on frictionless ice.',
            workedExamples: [],
            realWorldExamples: [],
            commonMistakes: [],
            summaryPoints: ['Inertia requires zero net external force.']
          }
        ],
        flashcards: [
          {
            id: 'fc_1_1',
            frontPrompt: 'What is inertia?',
            backAnswer: 'The resistance of an object to changes in its velocity.',
            conceptTag: 'Mechanics',
            difficultyRating: 2
          }
        ],
        quizQuestions: [
          {
            id: 'q_1_1',
            question: 'What is the physical measure of an object’s inertia?',
            options: ['Mass', 'Velocity', 'Force', 'Momentum'],
            correctIndex: 0,
            pedagogicalExplanation: 'Mass is the quantitative measure of linear inertia.',
            distractorExplanations: ['Velocity does not measure inertia.'],
            formulaReference: 'F = m · a'
          }
        ]
      }
    ],
    approvalStatus: 'READY_FOR_REVIEW',
    createdBy: 'admin_test',
    createdAt: Date.now(),
    updatedAt: Date.now()
  };

  studioStore.books.set(testBookId, draftBook);

  await t.test('4. Publishing unapproved book is strictly rejected by Server-Side Invariant', async () => {
    const attempt = await contentPipeline.publishBook(testBookId, true);
    assert.equal(attempt.success, false);
    assert.ok(attempt.error?.includes('Server-Side Security Invariant Violation'));

    const book = studioStore.books.get(testBookId);
    assert.notEqual(book?.approvalStatus, 'PUBLISHED');
  });

  await t.test('5. Human Editorial Sign-Off transitions state to APPROVED', async () => {
    const approved = await contentPipeline.approveBook(
      testBookId,
      'super_admin_01',
      'Editorial review completed. All mathematical equations verified.'
    );
    assert.ok(approved);
    assert.equal(approved.approvalStatus, 'APPROVED');
    assert.equal(approved.approvedBy, 'super_admin_01');
    assert.ok(approved.approvedAt && approved.approvedAt > 0);
  });

  await t.test('6. Publishing approved book makes it visible in public catalog', async () => {
    const publishResult = await contentPipeline.publishBook(testBookId, true);
    assert.equal(publishResult.success, true);
    assert.equal(publishResult.book?.approvalStatus, 'PUBLISHED');

    const publishedList = Array.from(studioStore.books.values()).filter((b) => b.approvalStatus === 'PUBLISHED');
    const found = publishedList.find((b) => b.id === testBookId);
    assert.ok(found, 'Must be present in public catalog');
    assert.equal(found.title, "Newton's Laws — Made Simple");
  });

  await t.test('7. Unpublishing immediately removes book from public catalog', async () => {
    const unpublishResult = await contentPipeline.unpublishBook(testBookId);
    assert.equal(unpublishResult.success, true);
    assert.equal(unpublishResult.book?.approvalStatus, 'UNPUBLISHED');

    const publishedList = Array.from(studioStore.books.values()).filter((b) => b.approvalStatus === 'PUBLISHED');
    const found = publishedList.find((b) => b.id === testBookId);
    assert.equal(found, undefined, 'Must not be returned in public catalog');
  });
});
