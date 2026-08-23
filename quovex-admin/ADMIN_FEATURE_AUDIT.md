# Quovex Admin Control Center — Initial Reality Audit

**Date:** 2026-08-23  
**Phase:** Phase 9 — Admin Control Center Completion  
**Scope:** `quovex-admin/`, `firebase_backend/`, `android/`, Firestore Rules, API Routes, Navigation

---

## 1. Feature Audit Matrix

| # | Feature Area | Existing | Partial | Missing | Action Required |
|---|---|---|---|---|---|
| **1** | **Admin Authentication** | ❌ | 🟡 Dev header | Missing login page, token verification & cookie sessions | Build server-side admin auth middleware, login page (`/login`), session handling |
| **2** | **Role Authorization** | ❌ | ❌ | Missing role hierarchy (`SUPER_ADMIN`, `ADMIN`, `EDITOR`, `MODERATOR`, `ANALYST`) | Implement RBAC authorization helper (`lib/auth/rbac.ts`) & check permissions on all mutations |
| **3** | **Admin Dashboard** | ❌ | 🟡 Redirects to Content Studio | Missing unified platform overview (`/dashboard`) | Build `/dashboard` with platform KPIs, real counts, system status & monetization indicator |
| **4** | **User Management** | ❌ | ❌ | Missing user list, filters, details, suspension (`/users`) | Build `/users` & `/users/[uid]` with pagination, exam/class filters, suspension & role change |
| **5** | **AI Operations & Keys** | ❌ | 🟡 Gateway in `lib/` | Missing AI monitoring & key masking UI (`/ai`) | Build `/ai` for request monitoring, latency, masked keys (`••••••••a92f`), provider toggling |
| **6** | **Content Management** | ❌ | ❌ | Missing unified catalog view (`/content`) | Build `/content` with tabbed breakdown across `OFFICIAL_RESOURCE`, `QUOVEX_ORIGINAL`, `USER_MATERIAL` |
| **7** | **NCERT Admin** | ❌ | 🟡 Catalog JSON in `assets/` | Missing catalog inspection & URL validator (`/ncert`) | Build `/ncert` with Classes 9–12 catalog inspection, URL validation, duplicate detection, and sync |
| **8** | **Quovex Originals** | ✅ Phase 8 complete | ❌ | ❌ | Retain existing Phase 8 pipeline; link directly in unified navigation |
| **9** | **Content Studio** | ✅ Phase 8 complete | ❌ | ❌ | Retain existing 8 sub-routes (`/content-studio/*`); preserve all generation engines |
| **10** | **Demand Intelligence** | ✅ Phase 8 complete | ❌ | ❌ | Retain deterministic normalization & zero-PII aggregation (`/content-studio/demand`) |
| **11** | **Platform Analytics** | ❌ | 🟡 Studio analytics only | Missing global analytics (`/analytics`) | Build `/analytics` with DAU/WAU/MAU, session retention, study minutes & zero-mock empty states |
| **12** | **Push Notifications** | ❌ | ❌ | Missing campaign composer (`/notifications`) | Build `/notifications` with audience targeting (All, Class, Exam, Inactive), history & delivery log |
| **13** | **Monetization** | ❌ | ❌ | Missing monetization view (`/monetization`) | Build `/monetization` with explicit "Billing data unavailable" empty state (zero fake revenue) |
| **14** | **Moderation Queue** | ❌ | ❌ | Missing report queue (`/moderation`) | Build `/moderation` for reported users/rooms/content with review, warn, suspend actions & audit logs |
| **15** | **System Health** | ❌ | ❌ | Missing health monitor (`/system`) | Build `/system` with real status checks for Firebase, Firestore, Cloud Functions, AI Gateway, Worker |
| **16** | **Feature Flags** | ❌ | ❌ | Missing toggle center (`/feature-flags`) | Build `/feature-flags` with server-side flags (`AI_CHAT`, `IMAGE_DOUBT`, `NCERT`, etc.) & audit log |
| **17** | **Audit Logs** | ❌ | ❌ | Missing immutable audit log (`/audit-logs`) | Build `/audit-logs` recording actor UID, role, action, target, timestamp, success/failure |
| **18** | **Admin Settings** | ❌ | ❌ | Missing admin config (`/settings`) | Build `/settings` for platform maintenance mode, AI quota defaults, and system thresholds |
| **19** | **Firestore Security** | ✅ Basic rules | 🟡 Admin SDK exclusive | Needs audit log & flag rules | Update `firestore.rules` for audit logs, feature flags, user roles |
| **20** | **Zero-Mock Compliance** | ✅ Phase 8 verified | ❌ | ❌ | Maintain 0 mock data, 0 fake users, 0 fake analytics across all new Phase 9 components |

---

## 2. Implementation Blueprint

1. **Core Types & Stores**:
   - `lib/types/admin.ts`: Define `AdminUser`, `AdminRole`, `AuditLogEntry`, `FeatureFlag`, `ModerationReport`, `NotificationCampaign`, `SystemHealthStatus`, `PlatformMetrics`.
   - `lib/admin-store.ts`: Server-side state store and Firestore connector for admin entities.
   - `lib/auth/rbac.ts`: Role-based access control and session management with `SUPER_ADMIN`, `ADMIN`, `EDITOR`, `MODERATOR`, `ANALYST`.
   - `lib/audit-logger.ts`: Append-only audit logging helper.

2. **Server API Routes**:
   - `/api/auth/login`, `/api/auth/me`, `/api/auth/logout`
   - `/api/dashboard/kpis`
   - `/api/users`, `/api/users/[uid]`, `/api/users/[uid]/suspend`, `/api/users/[uid]/role`
   - `/api/ai/metrics`, `/api/ai/keys`
   - `/api/content/summary`
   - `/api/ncert/validate`, `/api/ncert/sync`
   - `/api/analytics/overview`
   - `/api/notifications/send`, `/api/notifications/history`
   - `/api/feature-flags`, `/api/feature-flags/[flagId]`
   - `/api/moderation/reports`, `/api/moderation/action`
   - `/api/system/health`
   - `/api/audit-logs`
   - `/api/settings`

3. **Admin UI Pages**:
   - `/login` (Admin Sign-In)
   - `/dashboard` (Platform Overview & Real KPIs)
   - `/users` & `/users/[uid]` (Searchable Users & Profiles)
   - `/ai` (AI Operations & Key Manager)
   - `/content` (Unified Content Catalog)
   - `/ncert` (NCERT Classes 9–12 Management & Validator)
   - `/analytics` (Platform Metrics & Learning Analytics)
   - `/notifications` (Notification Composer & History)
   - `/feature-flags` (Toggle Switches & Rollouts)
   - `/moderation` (Content & User Moderation Queue)
   - `/system` (Real-Time Service Health Monitor)
   - `/audit-logs` (Immutable Security Audit Trail)
   - `/monetization` (Factual Billing Status)
   - `/settings` (Platform Configuration)

4. **Sidebar Navigation**:
   - Update `components/Sidebar.tsx` with all 6 categories: Overview, Learning, AI, Operations, Business, Settings.
