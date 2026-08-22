# Quovex Admin Panel — Custom Next.js Build Plan

**Version:** 1.0 | **Date:** 2026-08-21  
**Stack:** Next.js 15 (App Router) + TypeScript + Tailwind CSS + Firebase Admin SDK  
**Deployment:** Vercel (free tier)  
**Auth:** Firebase Auth (Google Sign-In, admin emails whitelist)

---

## Why Custom Next.js Over Appsmith

| Factor | Appsmith | Custom Next.js |
|---|---|---|
| Control | Limited | 100% control |
| UI Quality | Generic drag-drop | Pixel-perfect custom design |
| AI Key Manager | Not possible | Fully custom logic |
| Real-time updates | Limited | Full WebSocket / SSE support |
| Cost at scale | Can get expensive | Vercel free → paid gradually |
| Integration depth | Firebase basic | Full Firebase Admin SDK |
| Future-proof | Vendor dependency | Your own codebase |

---

## Tech Stack

| Layer | Technology | Reason |
|---|---|---|
| Framework | Next.js 15 (App Router) | SSR + API routes in one project |
| Language | TypeScript | Type safety, matches Android team mindset |
| Styling | Tailwind CSS + shadcn/ui | Fast, beautiful, accessible components |
| Charts | Recharts | Lightweight, React-native charting |
| Tables | TanStack Table v8 | Feature-rich data tables (sort, filter, paginate) |
| Auth | Firebase Auth + middleware | Admin-only access |
| Backend | Next.js API Routes (Route Handlers) | No separate backend needed |
| Firebase | Firebase Admin SDK (server-side) | Full Firestore/Auth admin access |
| Real-time | Firebase Realtime DB listener | Live AI key status updates |
| Deployment | Vercel | Free tier, instant CI/CD from GitHub |
| Notifications | Nodemailer / Resend | Email alerts for system events |

---

## Project Structure

```
quovex-admin/
├── app/
│   ├── layout.tsx                   ← Root layout (dark theme, sidebar)
│   ├── page.tsx                     ← Redirect to /dashboard
│   ├── login/
│   │   └── page.tsx                 ← Admin login (Google Sign-In)
│   │
│   ├── (admin)/                     ← Protected route group
│   │   ├── layout.tsx               ← Admin layout with sidebar + header
│   │   ├── dashboard/
│   │   │   └── page.tsx             ← Home dashboard with all KPIs
│   │   ├── users/
│   │   │   ├── page.tsx             ← User list (paginated table)
│   │   │   └── [uid]/
│   │   │       └── page.tsx         ← Individual user detail
│   │   ├── ai-keys/
│   │   │   └── page.tsx             ← AI key manager (real-time)
│   │   ├── monetization/
│   │   │   ├── page.tsx             ← Revenue overview
│   │   │   ├── ads/page.tsx         ← AdMob stats
│   │   │   └── subscriptions/page.tsx ← Premium subscribers
│   │   ├── notifications/
│   │   │   └── page.tsx             ← Push notification center
│   │   ├── content/
│   │   │   ├── page.tsx             ← Content manager overview
│   │   │   ├── exams/page.tsx       ← Exam catalog CRUD
│   │   │   ├── quotes/page.tsx      ← Motivational quotes
│   │   │   └── feature-flags/page.tsx ← Feature toggle switches
│   │   ├── analytics/
│   │   │   └── page.tsx             ← Deep analytics dashboard
│   │   ├── moderation/
│   │   │   └── page.tsx             ← User reports, flagged content
│   │   └── system/
│   │       └── page.tsx             ← System health monitor
│   │
│   └── api/                         ← Server-side API routes
│       ├── users/
│       │   ├── route.ts             ← GET /api/users (paginated list)
│       │   └── [uid]/
│       │       ├── route.ts         ← GET/PATCH/DELETE user
│       │       └── grant-premium/route.ts
│       ├── ai-keys/
│       │   ├── route.ts             ← GET all keys + usage stats
│       │   ├── [keyId]/route.ts     ← PATCH (toggle, cooldown)
│       │   └── reset/route.ts       ← Reset cooldown for a key
│       ├── notifications/
│       │   └── send/route.ts        ← POST send FCM notification
│       ├── feature-flags/
│       │   └── route.ts             ← GET/PUT feature flags
│       ├── analytics/
│       │   ├── dashboard/route.ts   ← GET KPI summary
│       │   └── deep/route.ts        ← GET detailed analytics
│       └── content/
│           ├── exams/route.ts
│           └── quotes/route.ts
│
├── components/
│   ├── layout/
│   │   ├── Sidebar.tsx
│   │   ├── Header.tsx
│   │   └── PageHeader.tsx
│   ├── dashboard/
│   │   ├── KpiCard.tsx              ← Single metric card
│   │   ├── KpiGrid.tsx              ← Grid of KPI cards
│   │   ├── DauChart.tsx             ← Daily active users line chart
│   │   └── RevenueChart.tsx
│   ├── users/
│   │   ├── UsersTable.tsx           ← TanStack Table with search
│   │   └── UserDetailPanel.tsx
│   ├── ai-keys/
│   │   ├── KeyStatusBadge.tsx       ← 🟢 Active / 🟡 Near Limit / 🔴 Cooldown
│   │   ├── KeyUsageBar.tsx          ← Usage progress bar
│   │   └── KeyPoolTable.tsx         ← Full key manager table
│   ├── notifications/
│   │   └── NotificationComposer.tsx
│   └── ui/                          ← shadcn/ui components
│       ├── button.tsx
│       ├── card.tsx
│       ├── table.tsx
│       ├── badge.tsx
│       ├── switch.tsx               ← For feature flags
│       └── dialog.tsx
│
├── lib/
│   ├── firebase-admin.ts            ← Firebase Admin SDK singleton
│   ├── auth.ts                      ← Admin auth middleware helpers
│   ├── constants.ts                 ← Admin email whitelist
│   └── utils.ts
│
├── middleware.ts                     ← Route protection (redirect non-admins)
├── .env.local                       ← FIREBASE_ADMIN_KEY, etc. (never committed)
├── next.config.ts
├── tailwind.config.ts
└── package.json
```

---

## Page Specifications

---

### 📊 Dashboard Page (`/dashboard`)

**Layout:** 4-column KPI grid + 2 large charts + activity feed

```
┌──────────────────────────────────────────────────────────────┐
│  DAU      │  MAU      │  Premium  │  Today's  │  AI Queries │
│  12,483   │  89,210   │  3,821    │  Revenue  │  Today      │
│  +12% ↑   │  +8% ↑    │  4.3% CR  │  $142     │  48,291     │
└──────────────────────────────────────────────────────────────┘
┌─────────────────────────┐ ┌──────────────────────────────────┐
│  DAU Trend (30 days)    │ │  Revenue: Ads vs Subs            │
│  [Line Chart]           │ │  [Stacked Bar Chart]             │
└─────────────────────────┘ └──────────────────────────────────┘
┌─────────────────────────┐ ┌──────────────────────────────────┐
│  Top Countries           │ │  AI Key Pool Health              │
│  [Bar Chart]            │ │  [Mini key status grid]          │
└─────────────────────────┘ └──────────────────────────────────┘
```

**Data sources (all from Next.js API routes → Firestore):**
- DAU: count users with `lastActiveAt > today 00:00`
- MAU: count users with `lastActiveAt > 30 days ago`
- Revenue: AdMob API + Firestore subscription count

---

### 🤖 AI Key Manager Page (`/ai-keys`)

**The most important page for operations.**

```
┌──────────────────────────────────────────────────────────────┐
│  AI Key Pool Manager                     [+ Add Key] [⚙ Settings]
│                                                              │
│  Strategy: ● Round-Robin  ○ Least-Used  ○ Failover         │
│  Cooldown Duration: [60] minutes                            │
├──────────────────────────────────────────────────────────────┤
│  GROQ KEYS (4/4 keys)                                        │
├──────┬──────────┬────────┬──────────────┬─────────┬─────────┤
│ Key  │ Status   │ Today  │ Limit        │ Usage % │ Actions │
├──────┼──────────┼────────┼──────────────┼─────────┼─────────┤
│ G-1  │ 🟢 Active│ 8,421  │ 14,400/day  │ ▓▓▓▓▓░ 58%│ [Pause]│
│ G-2  │ 🟢 Active│ 7,890  │ 14,400/day  │ ▓▓▓▓▓░ 55%│ [Pause]│
│ G-3  │ 🟡 Limit │ 13,910 │ 14,400/day  │ ▓▓▓▓▓▓ 97%│ [Cool] │
│ G-4  │ 🔴 Cool  │ 14,400 │ 14,400/day  │ ▓▓▓▓▓▓100%│ [Reset]│
├──────────────────────────────────────────────────────────────┤
│  CEREBRAS KEYS (4/4 keys)                                    │
│  ...similar table...                                         │
├──────────────────────────────────────────────────────────────┤
│  📊 Query Volume (last 24h) — [Chart showing key usage]     │
└──────────────────────────────────────────────────────────────┘
```

**Real-time updates:** Firebase Realtime DB listener — key status updates every 30 seconds without page reload.

**Actions:**
- **Pause:** Manually disable a key (bypass rotation)
- **Cooldown:** Force a 60-min cooldown on a key
- **Reset:** Immediately un-cooldown a key
- **Add Key:** Modal to enter new API key + provider

---

### 👥 User Management Page (`/users`)

```
┌──────────────────────────────────────────────────────────────┐
│  Users (89,210 total)  [Search by email/name]  [Export CSV]  │
│  Filter: [All ▾] [Plan: All ▾] [Country: All ▾] [Status ▾] │
├──────┬─────────────┬────────────┬──────┬─────────┬──────────┤
│ Name │ Email       │ Plan       │ Days │ Country │ Actions  │
├──────┼─────────────┼────────────┼──────┼─────────┼──────────┤
│ Arjun│ arj@...    │ 💎 Premium │ 23d  │ 🇮🇳 IN  │ [View]   │
│ Sara │ sara@...   │ Free       │ 5d   │ 🇺🇸 US  │ [View]   │
│ ...  │ ...         │ ...        │ ...  │ ...     │ ...      │
└──────────────────────────────────────────────────────────────┘
```

**User Detail Drawer (slides in from right):**
- Profile info + stats
- Actions: Grant Premium, Ban, Delete Account, Reset AI queries
- Full session history (last 10 sessions)
- AI query count this month

---

### 🏳️ Feature Flags Page (`/content/feature-flags`)

```
┌──────────────────────────────────────────────────────────────┐
│  Feature Flags                         Last updated: 2 min ago│
├───────────────────────────────────────┬──────────────────────┤
│  Feature                              │ Status               │
├───────────────────────────────────────┼──────────────────────┤
│  Focus Camera Detection               │ [●───────────] ON    │
│  Study Rooms (Live Co-Study)          │ [───────────○] OFF   │
│  Website Blocker                      │ [●───────────] ON    │
│  Ads (Free Tier)                      │ [●───────────] ON    │
│  AI Flashcard Generator               │ [●───────────] ON    │
│  Maintenance Mode                     │ [───────────○] OFF   │
│  New Feature: Image Doubt (Beta)      │ [───────────○] OFF   │
└───────────────────────────────────────┴──────────────────────┘
```

Toggling a flag writes instantly to Firestore → Android app reads on next launch or via RemoteConfig.

---

## Authentication & Security

### Admin Whitelist
```typescript
// lib/constants.ts
export const ADMIN_EMAILS = [
  "youremail@gmail.com",
  // add team emails here
]
```

### Middleware (Route Protection)
```typescript
// middleware.ts
export async function middleware(request: NextRequest) {
  const session = await getAdminSession(request)
  
  if (!session || !ADMIN_EMAILS.includes(session.email)) {
    return NextResponse.redirect(new URL('/login', request.url))
  }
  
  return NextResponse.next()
}

export const config = {
  matcher: ['/(admin)/:path*', '/api/:path*']
}
```

### Firebase Admin SDK (Server-Only)
```typescript
// lib/firebase-admin.ts
import { initializeApp, getApps, cert } from 'firebase-admin/app'
import { getFirestore } from 'firebase-admin/firestore'
import { getAuth } from 'firebase-admin/auth'

const adminApp = getApps().length === 0
  ? initializeApp({ credential: cert(JSON.parse(process.env.FIREBASE_ADMIN_KEY!)) })
  : getApps()[0]

export const adminDb = getFirestore(adminApp)
export const adminAuth = getAuth(adminApp)
```

---

## Development Phases

### Phase 1 — Core (2–3 weeks)
- [ ] Project setup: Next.js 15 + TypeScript + Tailwind + shadcn/ui
- [ ] Firebase Admin SDK integration
- [ ] Login page (Google Sign-In + admin whitelist)
- [ ] Layout: Sidebar, Header, responsive
- [ ] Dashboard page (KPI cards + DAU chart)
- [ ] User Management (list + search + detail drawer)
- [ ] AI Key Manager (table + real-time status + actions)

### Phase 2 — Operations (1–2 weeks)
- [ ] Notifications center (compose + send FCM)
- [ ] Feature Flags page (toggle switches → Firestore)
- [ ] Monetization page (subscription table + revenue)

### Phase 3 — Insights (1–2 weeks)
- [ ] Analytics deep dive page
- [ ] Content manager (exam catalog, quotes)
- [ ] Moderation page (user reports)
- [ ] System health page
- [ ] Email alerts (Resend API) for system events

---

## Estimated Timeline

| Phase | Time | Features |
|---|---|---|
| Phase 1 | 2–3 weeks | Dashboard, Users, AI Key Manager |
| Phase 2 | 1–2 weeks | Notifications, Feature Flags, Monetization |
| Phase 3 | 1–2 weeks | Analytics, Content, Moderation, Health |
| **Total** | **4–7 weeks** | **Full admin panel** |

> ⚡ Note: Build the Android app first (MVP). Start admin panel at Phase 3 of Android development so you have real data to display.

---

## Environment Variables (`.env.local`)

```env
# Firebase Admin (service account JSON as string)
FIREBASE_ADMIN_KEY={"type":"service_account","project_id":"..."}

# NextAuth / Session
NEXTAUTH_SECRET=your_random_secret
NEXTAUTH_URL=http://localhost:3000

# FCM Server Key (for push notifications)
FCM_SERVER_KEY=your_fcm_server_key

# Email alerts (optional)
RESEND_API_KEY=your_resend_api_key
ALERT_EMAIL=your@email.com
```

---

## Deployment

```bash
# 1. Push to GitHub
git push origin main

# 2. Connect to Vercel
# vercel.com → Import GitHub repo → Auto-deploy

# 3. Add env vars in Vercel dashboard
# Settings → Environment Variables → add all .env.local vars

# 4. Set custom domain (optional)
# admin.quovex.app → Vercel domain settings
```

**Cost:** $0 on Vercel free tier (up to 100GB bandwidth/month, perfect for admin panel)
