# Quovex — Phase 9.1: Firestore Security Rules & Access Control Audit

**Date:** 2026-08-23  
**Status:** `VERIFIED PASS (Strict Isolation Enforced)`  
**Reference File:** [`firebase_backend/firestore.rules`](file:///d:/Quovex%20APP/firebase_backend/firestore.rules)

---

## 1. Collection-by-Collection Security Invariant Table

| Collection / Path | Public Client Read | Public Client Write | Admin / Cloud Functions Access | Security Invariant Checked |
|---|:---:|:---:|:---:|---|
| `/users/{userId}` | ❌ Only if `auth.uid == userId` | ❌ Only if `auth.uid == userId` | ✅ Firebase Admin SDK | Cross-user profile access blocked |
| `/users/{userId}/sessions` | ❌ Own user only | ❌ Own user only | ✅ Firebase Admin SDK | Study telemetry isolated |
| `/users/{userId}/flashcard_decks` | ❌ Own user only | ❌ Own user only | ✅ Firebase Admin SDK | Flashcards isolated |
| `/notes/{noteId}` | ❌ Own `userId` only | ❌ Own `userId` only | ✅ Firebase Admin SDK | User private notes isolated |
| `/study_plans/{planId}` | ❌ Own `userId` only | ❌ Own `userId` only | ✅ Firebase Admin SDK | Study plans isolated |
| `/study_rooms/{roomId}` | ✅ Authenticated users | ❌ Creator / Admin only | ✅ Firebase Admin SDK | Community rooms protected |
| `/quovex_originals/{bookId}` | ⚠️ `approvalStatus == 'PUBLISHED'` only | ❌ `allow write: if false` | ✅ Firebase Admin SDK | Drafts/Unpublished books blocked from students |
| `/topic_demand_signals/*` | ❌ `allow read: if false` | ❌ `allow write: if false` | ✅ Firebase Admin SDK | Raw demand telemetry blocked from client |
| `/content_generation_jobs/*`| ❌ `allow read: if false` | ❌ `allow write: if false` | ✅ Firebase Admin SDK | 16-stage worker jobs blocked from client |
| `/evidence_packs/*` | ❌ `allow read: if false` | ❌ `allow write: if false` | ✅ Firebase Admin SDK | Evidence packs blocked from client |
| `/editorial_blueprints/*` | ❌ `allow read: if false` | ❌ `allow write: if false` | ✅ Firebase Admin SDK | Debate transcripts blocked from client |
| `/validation_reports/*` | ❌ `allow read: if false` | ❌ `allow write: if false` | ✅ Firebase Admin SDK | Validation scores blocked from client |
| `/feature_flags/*` | ✅ Authenticated (Read only) | ❌ `allow write: if false` | ✅ Firebase Admin SDK | Students cannot mutate feature flags |
| `/audit_logs/*` | ❌ `allow read: if false` | ❌ `allow write: if false` | ✅ Firebase Admin SDK | Append-only security logs immutable |
| `/moderation_reports/*` | ❌ `allow read: if false` | ✅ `allow create: if auth != null` | ✅ Firebase Admin SDK | Students can report, cannot read/delete queue |
| `/notification_campaigns/*`| ❌ `allow read: if false` | ❌ `allow write: if false` | ✅ Firebase Admin SDK | Push broadcast mutations exclusive to Admin SDK |

---

## 2. Invariant Proofs

1. **Student Access to Drafts:** `quovex_originals` has rule `allow read: if resource.data.approvalStatus == 'PUBLISHED'`. When `approvalStatus == 'DRAFT'` or `'READY_FOR_REVIEW'`, the Firestore security engine rejects read queries with `PermissionDenied`.
2. **Direct Role Escalation:** No client can write to `/config/`, `/feature_flags/`, or mutate admin custom claims directly through Firestore client SDKs.
3. **Private User Data Protection:** All notes, study plans, and subcollections are constrained by `resource.data.userId == request.auth.uid`. Admin UI queries are performed via Firebase Admin SDK with server-side RBAC protection.
