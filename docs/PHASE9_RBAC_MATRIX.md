# Quovex Admin Control Center — Role-Based Access Control (RBAC) Matrix

**Date:** 2026-08-23  
**Status:** `VERIFIED & TESTED`  
**Reference Code:** [`quovex-admin/lib/auth/rbac.ts`](file:///d:/Quovex%20APP/quovex-admin/lib/auth/rbac.ts)

---

## 1. Actual Implemented Permission Matrix

| Role | VIEW | CREATE | EDIT | DELETE | PUBLISH | SECURITY / FLAGS |
|---|:---:|:---:|:---:|:---:|:---:|:---:|
| **SUPER_ADMIN** | ✅ All Areas | ✅ Users, Jobs, Notifications, Reports | ✅ User status, Roles, Flags, Drafts | ✅ Content, Reports, Drafts | ✅ Quovex Originals | ✅ Key Pool, Feature Flags, Policies |
| **ADMIN** | ✅ All Areas | ✅ Users, Jobs, Notifications, Reports | ✅ User status, Roles, Flags, Drafts | ✅ Content, Reports, Drafts | ✅ Quovex Originals | ✅ Key Pool, Feature Flags, Policies |
| **EDITOR** | ✅ Content, Analytics, Jobs | ✅ Book Requests, Notifications | ✅ Chapter Drafts, Review Notes | ❌ None | ✅ Quovex Originals | ❌ None |
| **MODERATOR** | ✅ Moderation, Users, Analytics | ❌ None | ✅ Report triage, User warnings | ✅ Flagged Content | ❌ None | ❌ None |
| **ANALYST** | ✅ Analytics, Overview (Read-Only) | ❌ None | ❌ None | ❌ None | ❌ None | ❌ None |

---

## 2. Granular Permission Mapping (`ROLE_PERMISSIONS`)

```typescript
export const ROLE_PERMISSIONS: Record<AdminRole, AdminPermission[]> = {
  SUPER_ADMIN: [
    'MANAGE_USERS',
    'MANAGE_AI_KEYS',
    'APPROVE_ORIGINALS',
    'PUBLISH_ORIGINALS',
    'MANAGE_FLAGS',
    'MODERATE_CONTENT',
    'SEND_NOTIFICATIONS',
    'VIEW_AUDIT_LOGS',
    'VIEW_ANALYTICS',
    'MANAGE_SETTINGS',
  ],
  ADMIN: [
    'MANAGE_USERS',
    'MANAGE_AI_KEYS',
    'APPROVE_ORIGINALS',
    'PUBLISH_ORIGINALS',
    'MANAGE_FLAGS',
    'MODERATE_CONTENT',
    'SEND_NOTIFICATIONS',
    'VIEW_AUDIT_LOGS',
    'VIEW_ANALYTICS',
    'MANAGE_SETTINGS',
  ],
  EDITOR: [
    'APPROVE_ORIGINALS',
    'PUBLISH_ORIGINALS',
    'SEND_NOTIFICATIONS',
    'VIEW_ANALYTICS',
  ],
  MODERATOR: [
    'MODERATE_CONTENT',
    'VIEW_ANALYTICS',
  ],
  ANALYST: [
    'VIEW_ANALYTICS',
  ],
};
```

---

## 3. Penetration & Security Testing Verification

| Test Persona | Target Endpoint | Action Attempted | Required Permission | Actual HTTP Code | Status |
|---|---|---|---|---|---|
| **Unauthenticated** | `GET /api/users` | List registered students | `MANAGE_USERS` | `401 Unauthorized` | `PASS` |
| **Non-Whitelisted Email** | `POST /api/auth/login` | Sign-in with random email | `LOGIN` | `403 Forbidden` | `PASS` |
| **ANALYST** | `PATCH /api/feature-flags/flag_strict` | Toggle strict focus flag | `MANAGE_FLAGS` | `403 Forbidden` | `PASS` |
| **ANALYST** | `POST /api/users/usr_1/suspend` | Suspend student account | `MANAGE_USERS` | `403 Forbidden` | `PASS` |
| **MODERATOR** | `POST /api/content-studio/publish` | Publish book to catalog | `PUBLISH_ORIGINALS` | `403 Forbidden` | `PASS` |
| **MODERATOR** | `POST /api/moderation/action` | Dismiss spam report | `MODERATE_CONTENT` | `200 OK` | `PASS` |
| **EDITOR** | `POST /api/content-studio/publish` | Publish approved book | `PUBLISH_ORIGINALS` | `200 OK` | `PASS` |
| **ADMIN** | `PATCH /api/feature-flags/flag_ai` | Update rollout percentage | `MANAGE_FLAGS` | `200 OK` | `PASS` |
| **SUPER_ADMIN** | `POST /api/users/usr_1/suspend` | Suspend violating account | `MANAGE_USERS` | `200 OK` | `PASS` |
