# Quovex — Production Vercel Deployment Guide

**Target Domains:**
- **Student Web Application:** `https://quovex.online` & `https://www.quovex.online`
- **Admin Control Center:** `https://admin.quovex.online`
- **Firebase Project:** `quovex-f3104` (Google Cloud Run / Firestore / Storage / Auth)

---

## 🏗️ Architecture Overview

The Quovex repository is structured as a unified monorepo containing two independent Next.js applications:
1. `quovex-web` — The student-facing web app (Focus Timer, AI Tutor, Flashcards, NCERT Library, Community, APK Downloads).
2. `quovex-admin` — The internal editorial & operations control center (Content Studio, Moderation, Telemetry, Audit Logs, Push Notifications).

To deploy both apps on Vercel from the same GitHub repository (`https://github.com/refindglobal/Quovex.git`), create **two separate Vercel projects** pointing to their respective root directories.

```
                    GitHub Repository (refindglobal/Quovex)
                                    │
                  ┌─────────────────┴─────────────────┐
                  │                                   │
          Vercel Project 1                    Vercel Project 2
           ("quovex-web")                     ("quovex-admin")
                  │                                   │
        Root Dir: quovex-web                Root Dir: quovex-admin
                  │                                   │
      Domain: quovex.online               Domain: admin.quovex.online
  (Also: www.quovex.online)
```

---

## 🚀 Project 1: Student Web (`quovex-web`)

### 1. Vercel Project Setup
1. In the Vercel Dashboard, click **Add New...** → **Project**.
2. Import `refindglobal/Quovex`.
3. In the project configuration modal:
   - **Project Name:** `quovex-web`
   - **Framework Preset:** `Next.js`
   - **Root Directory:** Click **Edit** and select `quovex-web`.
   - **Build Command:** `npm run build` (or leave default Next.js)
   - **Output Directory:** `.next` (default)
   - **Install Command:** `npm install` (default)
   - **Node.js Version:** `20.x` or `22.x`

### 2. Environment Variables (`quovex-web`)
Navigate to **Project Settings → Environment Variables** and add:

| Variable Name | Type | Value / Description |
|---|---|---|
| `NEXT_PUBLIC_FIREBASE_API_KEY` | Public (Client) | `AIzaSy...` (from Firebase Console Web SDK) |
| `NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN` | Public (Client) | `quovex-f3104.firebaseapp.com` |
| `NEXT_PUBLIC_FIREBASE_PROJECT_ID` | Public (Client) | `quovex-f3104` |
| `NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET` | Public (Client) | `quovex-f3104.firebasestorage.app` |
| `NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID` | Public (Client) | `784018860004` |
| `NEXT_PUBLIC_FIREBASE_APP_ID` | Public (Client) | `1:784018860004:web:...` |
| `NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID` | Public (Client) | `G-XXXXXXXXXX` |
| `NEXT_PUBLIC_API_URL` | Public (Client) | `https://api-dopkbhqrgq-uc.a.run.app` |

> [!NOTE]
> All client variables are prefixed with `NEXT_PUBLIC_` and contain NO private server keys or secrets.

### 3. Custom Domain Configuration (`quovex-web`)
Navigate to **Project Settings → Domains**:
1. Add `quovex.online` (Apex Domain).
   - Configure DNS: Type `A`, Name `@`, Value `76.76.21.21`
2. Add `www.quovex.online` (Redirects to `quovex.online` or serves directly).
   - Configure DNS: Type `CNAME`, Name `www`, Value `cname.vercel-dns.com`

---

## 🛡️ Project 2: Admin Control Center (`quovex-admin`)

### 1. Vercel Project Setup
1. In Vercel Dashboard, click **Add New...** → **Project**.
2. Select the same repository: `refindglobal/Quovex`.
3. In the project configuration modal:
   - **Project Name:** `quovex-admin`
   - **Framework Preset:** `Next.js`
   - **Root Directory:** Click **Edit** and select `quovex-admin`.
   - **Build Command:** `npm run build`
   - **Install Command:** `npm install`
   - **Node.js Version:** `20.x` or `22.x`

### 2. Environment Variables (`quovex-admin`)
Navigate to **Project Settings → Environment Variables** and add:

| Variable Name | Type | Value / Description |
|---|---|---|
| `FIREBASE_PROJECT_ID` | Server Secret | `quovex-f3104` |
| `FIREBASE_CLIENT_EMAIL` | Server Secret | `firebase-adminsdk-...@quovex-f3104.iam.gserviceaccount.com` |
| `FIREBASE_PRIVATE_KEY` | Server Secret | `"-----BEGIN PRIVATE KEY-----\nMIIEvgIB...-----END PRIVATE KEY-----\n"` |
| `ADMIN_SESSION_SECRET` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Random 32-char hex token) |
| `GROQ_API_KEY_1` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Rotated Groq Pool Key 1) |
| `GROQ_API_KEY_2` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Rotated Groq Pool Key 2) |
| `GROQ_API_KEY_3` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Rotated Groq Pool Key 3) |
| `GROQ_API_KEY_4` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Rotated Groq Pool Key 4) |
| `CEREBRAS_API_KEY_1` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Rotated Cerebras Key 1) |
| `CEREBRAS_API_KEY_2` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Rotated Cerebras Key 2) |
| `CEREBRAS_API_KEY_3` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Rotated Cerebras Key 3) |
| `CEREBRAS_API_KEY_4` | Server Secret | `SET_IN_VERCEL_DASHBOARD` (Rotated Cerebras Key 4) |

> [!IMPORTANT]
> `FIREBASE_PRIVATE_KEY` must include standard quotes and escaped newlines (`\n`). In Vercel, paste the full multi-line or escaped string directly into the secret field.

### 3. Custom Domain Configuration (`quovex-admin`)
Navigate to **Project Settings → Domains**:
1. Add `admin.quovex.online`.
2. Configure DNS at your domain registrar:
   - Type: `CNAME`
   - Name / Host: `admin`
   - Value / Target: `cname.vercel-dns.com`

---

## 🔐 Firebase Authentication Authorized Domains

For Google Sign-In and session authentication to work on production web domains:
1. Open [Firebase Console](https://console.firebase.google.com) → Project `quovex-f3104`.
2. Go to **Authentication** → **Settings** → **Authorized domains**.
3. Add the following entries:
   - `quovex.online`
   - `www.quovex.online`
   - `admin.quovex.online`
   - `quovex-web.vercel.app`
   - `quovex-admin.vercel.app`
   - `localhost`

---

## 📦 APK Distribution Configuration

The Android APK is distributed via Firebase Cloud Storage and mirrored on the Web:
- **Primary Public Download URL:**  
  `https://firebasestorage.googleapis.com/v0/b/quovex-f3104.firebasestorage.app/o/releases%2Fandroid%2Fquovex-1.0.0.apk?alt=media`
- **Web Download Page:** `https://quovex.online/download`
- **Storage Rules:** Verified public read for `/releases/{platform}/{fileName}` with write/mutation locked to Admin SDK / Firebase CLI only.

---

## 🔄 Deployment & Rollback Workflow

1. **Automatic Continuous Deployment:** Every push to `main` branch on GitHub automatically triggers a production deployment for both `quovex-web` and `quovex-admin` on Vercel.
2. **Instant Rollbacks:** If an issue occurs in production, open **Deployments** in Vercel, find the previous stable deployment, and click **Promote to Production** for instant 0-downtime rollback.
