import test from 'node:test';
import assert from 'node:assert/strict';
import { adminStore } from '../lib/admin-store.ts';
import { verifyAdminSession, hasPermission, ROLE_PERMISSIONS } from '../lib/auth/rbac.ts';
import { contentPipeline, studioStore } from '../lib/content-studio/pipeline.ts';

test('Quovex Admin Control Center — Phase 9 Security & Operations Test Suite', async (t) => {
  const superAdmin = {
    uid: 'admin_super_1',
    email: 'superadmin@quovex.ai',
    displayName: 'Super Admin',
    role: 'SUPER_ADMIN',
    createdAt: Date.now(),
    lastLoginAt: Date.now(),
    status: 'ACTIVE',
  };

  const analyst = {
    uid: 'analyst_1',
    email: 'analyst@quovex.ai',
    displayName: 'Analyst User',
    role: 'ANALYST',
    createdAt: Date.now(),
    lastLoginAt: Date.now(),
    status: 'ACTIVE',
  };

  await t.test('1. Unauthenticated admin request is rejected with 401', () => {
    const unauthReq = new Request('http://localhost:3000/api/users', {
      headers: {},
    });
    // In production mode without token
    const origEnv = process.env.NODE_ENV;
    process.env.NODE_ENV = 'production';
    const result = verifyAdminSession(unauthReq);
    process.env.NODE_ENV = origEnv;

    assert.equal(result.authorized, false);
    assert.equal(result.statusCode, 401);
  });

  await t.test('2. Role lacking permission is rejected with 403 Forbidden', () => {
    const analystReq = new Request('http://localhost:3000/api/feature-flags', {
      headers: {
        'x-admin-token': 'quovex_admin_secret_verified',
        'x-admin-role': 'ANALYST',
        'x-admin-email': 'analyst@quovex.ai',
      },
    });

    const result = verifyAdminSession(analystReq, 'MANAGE_FLAGS');
    assert.equal(result.authorized, false);
    assert.equal(result.statusCode, 403);
    assert.ok(result.error?.includes("lacks permission 'MANAGE_FLAGS'"));
  });

  await t.test('3. Authorized Super Admin is accepted with valid permissions', () => {
    const adminReq = new Request('http://localhost:3000/api/feature-flags', {
      headers: {
        'x-admin-token': 'quovex_admin_secret_verified',
        'x-admin-role': 'SUPER_ADMIN',
        'x-admin-email': 'admin@quovex.ai',
      },
    });

    const result = verifyAdminSession(adminReq, 'MANAGE_FLAGS');
    assert.equal(result.authorized, true);
    assert.equal(result.admin?.role, 'SUPER_ADMIN');
  });

  await t.test('4. Feature flag mutation triggers append-only audit log entry', () => {
    const initialLogCount = adminStore.auditLogs.length;
    const updated = adminStore.updateFlag('flag_strict_mode', true, 80, superAdmin);

    assert.ok(updated);
    assert.equal(updated.rolloutPercentage, 80);
    assert.equal(adminStore.auditLogs.length, initialLogCount + 1);

    const latestLog = adminStore.auditLogs[0];
    assert.equal(latestLog.action, 'FLAG_CHANGE');
    assert.equal(latestLog.targetId, 'STRICT_MODE');
    assert.equal(latestLog.actorEmail, 'superadmin@quovex.ai');
  });

  await t.test('5. Publishing an unapproved book is rejected by Server-Side Invariant', () => {
    const draftBookId = 'book_unapproved_test';
    studioStore.books.set(draftBookId, {
      id: draftBookId,
      title: 'Unapproved Quantum Physics',
      subject: 'Physics',
      topic: 'Quantum Mechanics',
      countryRegion: 'IN',
      curriculum: 'CBSE',
      gradeClass: 'Class 12',
      approvalStatus: 'READY_FOR_REVIEW',
      chapterCount: 1,
      chapters: [],
      createdAt: Date.now(),
      updatedAt: Date.now(),
      isStaging: false,
    });

    const result = contentPipeline.publishBook(draftBookId, false);
    assert.equal(result.success, false);
    assert.ok(result.error?.includes('Server-Side Security Invariant Violation'));
  });

  await t.test('6. Publishing succeeds after mandatory human editorial approval sign-off', () => {
    const draftBookId = 'book_unapproved_test';
    const approved = contentPipeline.approveBook(draftBookId, 'editor_lead@quovex.ai', 'Verified math equations');
    assert.ok(approved);
    assert.equal(approved.approvalStatus, 'APPROVED');

    const result = contentPipeline.publishBook(draftBookId, false);
    assert.equal(result.success, true);
    assert.equal(result.book?.approvalStatus, 'PUBLISHED');
  });

  await t.test('7. Push notification campaign dispatch logs audit event and records recipients', () => {
    const initialLogCount = adminStore.auditLogs.length;
    const campaign = adminStore.sendNotification(
      {
        title: 'Exam Flashcards Due Today',
        body: 'Review your 5 due flashcards in Mechanics before 8 PM.',
        targetAudience: 'ALL_USERS',
        sentBy: 'superadmin@quovex.ai',
      },
      superAdmin
    );

    assert.ok(campaign.id.startsWith('notif_'));
    assert.equal(campaign.status, 'SENT');
    assert.equal(adminStore.auditLogs.length, initialLogCount + 1);
    assert.equal(adminStore.auditLogs[0].action, 'NOTIFICATION_SEND');
  });

  await t.test('8. Moderation resolution records resolution notes and audit entry', () => {
    const report = adminStore.submitReport({
      targetId: 'room_study_spammers_1',
      targetType: 'STUDY_ROOM',
      reportedByUid: 'student_user_89',
      reason: 'Spamming advertisement links in chat',
    });

    assert.equal(report.status, 'PENDING');

    const initialLogCount = adminStore.auditLogs.length;
    const resolved = adminStore.resolveReport(report.id, 'REMOVE_CONTENT', 'Removed spam room', superAdmin);

    assert.ok(resolved);
    assert.equal(resolved.status, 'RESOLVED');
    assert.equal(resolved.actionTaken, 'REMOVE_CONTENT');
    assert.equal(adminStore.auditLogs.length, initialLogCount + 1);
    assert.equal(adminStore.auditLogs[0].action, 'MODERATION_ACTION');
  });

  await t.test('9. User suspension updates account status and logs security event', () => {
    const student = adminStore.upsertUser({
      uid: 'student_violator_1',
      email: 'violator@test.com',
      displayName: 'Violator Student',
      status: 'ACTIVE',
      createdAt: Date.now(),
      lastActiveAt: Date.now(),
      studyMinutesTotal: 45,
      materialsCount: 1,
      flashcardsCount: 5,
      quizzesTakenCount: 1,
      streakDays: 2,
    });

    assert.equal(student.status, 'ACTIVE');

    const initialLogCount = adminStore.auditLogs.length;
    const suspended = adminStore.suspendUser('student_violator_1', superAdmin, 'Terms of Service violation');

    assert.equal(suspended, true);
    assert.equal(adminStore.users.get('student_violator_1')?.status, 'SUSPENDED');
    assert.equal(adminStore.auditLogs.length, initialLogCount + 1);
    assert.equal(adminStore.auditLogs[0].action, 'USER_SUSPEND');
  });

  await t.test('10. AI Key status mask hides secrets in all responses', () => {
    const keys = adminStore.getAiKeys();
    assert.ok(keys.length >= 4);
    for (const k of keys) {
      assert.ok(k.maskedKey.includes('••••'));
      assert.ok(!k.maskedKey.startsWith('gsk_1234567890abcdef'));
      assert.ok(k.maskedKey.length <= 40);
    }
  });

  await t.test('11. System Health returns explicit status with latency', () => {
    const health = adminStore.getSystemHealth();
    assert.ok(health.length >= 5);
    for (const s of health) {
      assert.ok(['HEALTHY', 'DEGRADED', 'DOWN', 'UNKNOWN'].includes(s.status));
      assert.ok(s.latencyMs >= 0);
    }
  });

  await t.test('12. Platform metrics calculates only real stored data with zero fake revenue', () => {
    const metrics = adminStore.getPlatformMetrics();
    assert.ok(metrics.totalUsers >= 1);
    assert.equal(metrics.billingStatus, 'UNAVAILABLE');
    assert.ok(metrics.totalRevenueFormatted.includes('Billing Integration Pending'));
  });
});
