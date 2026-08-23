# Quovex Admin Control Center — Production Environment & Deployment Specification

**Date:** 2026-08-23  
**Status:** `VERIFIED PASS (Zero Secret Leakage)`  
**Platform:** Next.js 15 App Router (`quovex-admin`)  
**Target Host:** Vercel / Google Cloud Run

---

## 1. Environment Variable Inventory & Visibility

| Variable Name | Purpose | Visibility | Required / Optional |
|---|---|---|---|
| `ADMIN_SECRET_TOKEN` | Secure bearer token for internal admin route requests | `Server-Only (process.env)` | Required |
| `ADMIN_EMAILS_WHITELIST`| Comma-separated list of approved admin email addresses | `Server-Only (process.env)` | Required |
| `FIREBASE_PROJECT_ID` | Firebase Project identifier for Admin SDK | `Server-Only (process.env)` | Required |
| `FIREBASE_CLIENT_EMAIL` | Service account client email for Firebase Admin SDK | `Server-Only (process.env)` | Required |
| `FIREBASE_PRIVATE_KEY` | Service account RSA private key for Firebase Admin SDK | `Server-Only (process.env)` | Required |
| `GROQ_API_KEY_1` | Rotating AI Gateway Groq Pool Key 1 | `Server-Only (process.env)` | Required |
| `GROQ_API_KEY_2` | Rotating AI Gateway Groq Pool Key 2 | `Server-Only (process.env)` | Optional (Failover) |
| `GROQ_API_KEY_3` | Rotating AI Gateway Groq Pool Key 3 | `Server-Only (process.env)` | Optional (Failover) |
| `GROQ_API_KEY_4` | Rotating AI Gateway Groq Pool Key 4 | `Server-Only (process.env)` | Optional (Failover) |
| `CEREBRAS_API_KEY_1` | Rotating AI Gateway Cerebras Pool Key 1 | `Server-Only (process.env)` | Required |
| `CEREBRAS_API_KEY_2` | Rotating AI Gateway Cerebras Pool Key 2 | `Server-Only (process.env)` | Optional (Failover) |
| `CEREBRAS_API_KEY_3` | Rotating AI Gateway Cerebras Pool Key 3 | `Server-Only (process.env)` | Optional (Failover) |
| `CEREBRAS_API_KEY_4` | Rotating AI Gateway Cerebras Pool Key 4 | `Server-Only (process.env)` | Optional (Failover) |
| `NEXT_PUBLIC_APP_ENV` | Environment indicator (`production` \| `staging` \| `development`) | `Public Browser Bundle` | Optional |

> [!IMPORTANT]
> **Zero Browser Secret Bundling:** None of the AI provider keys, Firebase service account keys, or admin tokens carry the `NEXT_PUBLIC_` prefix. Next.js compiler eliminates them completely from client-side JS bundles.

---

## 2. Vercel & Production Deployment Steps

1. **Repository Link:** Connect repository to Vercel with Root Directory set to `quovex-admin`.
2. **Framework Preset:** Next.js.
3. **Build Command:** `npm run build` (`next build`).
4. **Environment Variables:** Populate all Server-Only variables in Vercel Project Settings → Environment Variables (`Production` & `Preview` environments).
5. **Admin Whitelist:** Ensure authorized engineering emails are added to `ADMIN_EMAILS_WHITELIST`.
