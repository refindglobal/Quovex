# Quovex — Marketing & SEO Landing Page Spec

**Status:** Planned
**Target Domain:** TBD (e.g., `quovex.app` or `quovex.com`)

---

## 1. 🏗️ Architecture & Tech Stack

To achieve a **100/100 Google Lighthouse Score** and maximum SEO performance, the site will be built without heavy JavaScript frameworks.

- **HTML:** Semantic HTML5 (Vanilla)
- **CSS:** Vanilla CSS3 (Custom properties mapped to the Quovex design system)
- **JavaScript:** Minimal vanilla JS for scroll animations and interactions
- **Hosting:** Vercel (Free tier, edge caching) or Firebase Hosting

## 2. 🔍 SEO & LLM Optimization (LLMO)

This site is designed to be perfectly readable by AI search engines (ChatGPT, Perplexity, Claude) and traditional search engines (Google).

### 2.1 JSON-LD Schema Markup
Injected into the `<head>` to define the app for Google:
```json
{
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  "name": "Quovex",
  "operatingSystem": "ANDROID",
  "applicationCategory": "EducationalApplication",
  "description": "An AI-powered study ecosystem featuring smart flashcards, note summarization, and focus tracking.",
  "offers": {
    "@type": "Offer",
    "price": "0",
    "priceCurrency": "USD"
  }
}
```

### 2.2 LLM Optimization (`/llms.txt`)
A specialized markdown-style text file hosted at the root (`quovex.com/llms.txt`) specifically for AI web crawlers to read. It contains:
- App core features
- Target audience (students, competitive exams)
- Pricing (Free tier + Premium)
- Direct download links

### 2.3 Meta Tags
- **Title:** Quovex | The Ultimate AI Study Ecosystem
- **Description:** Supercharge your focus with AI study plans, instant note summaries, and smart flashcards. Download Quovex for Android.
- **Open Graph (OG):** High-res banner image for Twitter/WhatsApp sharing.

## 3. 🎨 Page Structure

The landing page will be a single, high-converting scrollable page (`index.html`).

### Section 1: Hero
- **Headline:** Extreme Focus. Ultimate Results.
- **Subheadline:** The all-in-one AI ecosystem for serious students. Summarize notes, generate flashcards, and study with peers.
- **CTA:** Two large buttons: [Download on Google Play] & [Download APK].
- **Visual:** A sleek 3D-rotated mockup of the Quovex Android app (Home Screen).

### Section 2: Core Features (The "Why")
Grid layout highlighting the 4 pillars:
1. **AI Note Summarizer:** Upload PDFs or scan notebooks. Get instant summaries.
2. **Smart Flashcards:** Spaced repetition (SM-2) that guarantees you never forget.
3. **Focus Timer:** Strict app blocker so you can't be distracted.
4. **Community Rooms:** Study silently alongside thousands of peers.

### Section 3: Social Proof & Leaderboard
- A live-updating (or simulated) view of the weekly leaderboard to trigger competitive motivation.

### Section 4: Footer
- Links to Privacy Policy, Terms of Service, Support Email, and Social Media.

## 4. 📁 File Structure for the Website

```text
website/
├── index.html          # Main landing page
├── styles.css          # Vanilla CSS (Emerald green theme)
├── scripts.js          # Intersection observers for scroll animations
├── llms.txt            # AI search engine optimized text file
├── robots.txt          # Allow all crawlers
├── sitemap.xml         # SEO sitemap
└── assets/
    ├── hero_mockup.webp
    ├── og_banner.jpg
    └── icons/
```
