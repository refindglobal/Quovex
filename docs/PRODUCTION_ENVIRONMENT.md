# Quovex — Production Environment & Deployment Guide

**Date:** 2026-08-23  
**Status:** PRODUCTION STANDARD  

---

## 1. Production Architecture Overview

The Quovex production ecosystem consists of three integrated deployment targets:
1. **Android Native Client (`com.quovex`)**: Native Kotlin / Jetpack Compose application distributed via Google Play Store.
2. **Next.js Admin Control Center (`quovex-admin`)**: Next.js 15 server runtime deployed on Vercel / Cloud Run with Server-Side RBAC.
3. **Firebase & Cloud Functions Backend (`firebase_backend`)**: Firebase Auth, Firestore, Cloud Storage, and Node.js Cloud Functions Gen 2 for AI orchestration and NCERT proxying.

---

## 2. Environment Variables & Secret Configuration

### A. Next.js Admin Panel (`quovex-admin`)
Set in Vercel Project Settings > Environment Variables:

| Variable | Description | Example / Required Format |
|---|---|---|
| `FIREBASE_PROJECT_ID` | Production Firebase Project ID | `quovex-f3104` |
| `FIREBASE_CLIENT_EMAIL` | Service Account Client Email | `firebase-adminsdk-...@quovex-f3104.iam.gserviceaccount.com` |
| `FIREBASE_PRIVATE_KEY` | Private Key for Admin SDK | `"-----BEGIN PRIVATE KEY-----\nMIIE...-----END PRIVATE KEY-----\n"` |
| `ADMIN_MASTER_SECRET` | Secret token for admin API authentication | Secure 64-character random hex string |
| `NEXTAUTH_SECRET` | NextAuth session encryption key | Secure 64-character random hex string |
| `NEXTAUTH_URL` | Canonical Admin Panel URL | `https://admin.quovex.ai` |
| `NODE_ENV` | Runtime environment mode | `production` |

### B. Cloud Functions AI Gateway (`firebase_backend/functions`)
Set via Firebase Functions Secrets (`firebase functions:secrets:set <KEY>`):

| Secret Name | Purpose | Rotation Strategy |
|---|---|---|
| `GROQ_API_KEY_1` | Primary Groq API Key | Key Pool 1 |
| `GROQ_API_KEY_2` | Secondary Groq API Key | Key Pool 2 |
| `GROQ_API_KEY_3` | Tertiary Groq API Key | Key Pool 3 |
| `GROQ_API_KEY_4` | Quaternary Groq API Key | Key Pool 4 |
| `CEREBRAS_API_KEY_1` | Primary Cerebras API Key | Failover Pool 1 |
| `CEREBRAS_API_KEY_2` | Secondary Cerebras API Key | Failover Pool 2 |
| `CEREBRAS_API_KEY_3` | Tertiary Cerebras API Key | Failover Pool 3 |
| `CEREBRAS_API_KEY_4` | Quaternary Cerebras API Key | Failover Pool 4 |

### C. Android Client (`secrets.properties`)
Gitignored local file read by `android/app/build.gradle.kts`:

| Property | Purpose | Source |
|---|---|---|
| `GOOGLE_WEB_CLIENT_ID` | OAuth 2.0 Web Client ID for Google Sign-In | Google Cloud Console > Credentials |

---

## 3. Deployment Procedures

### Step 1: Firebase Security Rules & Backend Deployment
```bash
# From firebase_backend/
firebase use quovex-f3104
firebase deploy --only firestore:rules,storage:rules,firestore:indexes
firebase deploy --only functions
```

### Step 2: Next.js Admin Panel Deployment (Vercel)
```bash
# From quovex-admin/
npm test
npm run build
vercel --prod
```

### Step 3: Android Release Signing & AAB Build
To build a signed Android App Bundle (`.aab`) for Google Play release:
1. Ensure `keystore.properties` is configured in `android/` (gitignored).
2. Run release assembly:
```bash
cd android
./gradlew bundleRelease
```
*Output:* `android/app/build/outputs/bundle/release/app-release.aab`
