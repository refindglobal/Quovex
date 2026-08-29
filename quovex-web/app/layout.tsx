import type { Metadata } from 'next';
import '../styles/globals.css';
import { ThemeProvider } from '@/components/providers/ThemeProvider';

export const metadata: Metadata = {
  title: 'Quovex — The AI Study Ecosystem for Serious Students',
  description: 'AI flashcards, focus timer, instant doubt solving, NCERT library, and study rooms. Replace Forest, Anki, Notion, and ChatGPT with one platform. Free on Android + Web.',
  keywords: ['study app', 'AI tutor', 'JEE preparation', 'NEET study app', 'flashcards', 'focus timer', 'UPSC', 'NCERT', 'study planner'],
  authors: [{ name: 'Rohit & Kartikey', url: 'https://quovex.online' }],
  creator: 'Refind Global Studio',
  publisher: 'Refind Global Studio',
  metadataBase: new URL('https://quovex.online'),
  openGraph: {
    title: 'Quovex — AI Study Ecosystem',
    description: 'One platform replaces 5 apps. AI tutor · Smart flashcards · Focus timer · NCERT library · Study rooms.',
    url: 'https://quovex.online',
    siteName: 'Quovex',
    images: [
      {
        url: '/assets/brand/hero_mockup.png',
        width: 1200,
        height: 630,
        alt: 'Quovex AI Student Operating System',
      },
    ],
    type: 'website',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Quovex — AI Study Ecosystem',
    description: 'One platform replaces 5 study apps. Free on Android + Web.',
    images: ['/assets/brand/hero_mockup.png'],
  },
  robots: {
    index: true,
    follow: true,
  },
  alternates: {
    canonical: 'https://quovex.online',
  },
};

const jsonLd = {
  '@context': 'https://schema.org',
  '@type': 'SoftwareApplication',
  name: 'Quovex',
  url: 'https://quovex.online',
  operatingSystem: 'ANDROID, WEB',
  applicationCategory: 'EducationalApplication',
  description: 'AI-powered study ecosystem replacing Forest, Anki, Notion, and ChatGPT for serious students worldwide.',
  author: {
    '@type': 'Organization',
    name: 'Refind Global Studio',
    email: 'Refindglobalstudio@gmail.com',
    address: {
      '@type': 'PostalAddress',
      addressLocality: 'Noida',
      addressRegion: 'Uttar Pradesh',
      addressCountry: 'IN',
    },
  },
  offers: [
    { '@type': 'Offer', name: 'Scholar Free', price: '0', priceCurrency: 'INR' },
    { '@type': 'Offer', name: 'Pro Monthly', price: '199', priceCurrency: 'INR' },
    { '@type': 'Offer', name: 'Pro Annual', price: '999', priceCurrency: 'INR' },
    { '@type': 'Offer', name: 'Founder Lifetime', price: '2499', priceCurrency: 'INR' },
  ],
  aggregateRating: {
    '@type': 'AggregateRating',
    ratingValue: '4.8',
    ratingCount: '1420',
  },
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <link rel="preconnect" href="https://fonts.googleapis.com" />
        <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
        <link
          href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800;900&family=JetBrains+Mono:wght@500;700;800&display=swap"
          rel="stylesheet"
        />
        <script
          type="application/ld+json"
          dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
        />
        <script
          dangerouslySetInnerHTML={{
            __html: `
              (function() {
                try {
                  var mode = localStorage.getItem('quovex_theme_mode') || 'DARK';
                  var resolved = mode === 'SYSTEM' 
                    ? (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light')
                    : (mode === 'LIGHT' ? 'light' : 'dark');
                  document.documentElement.setAttribute('data-theme', resolved);
                  document.documentElement.classList.add(resolved);
                } catch (e) {}
              })();
            `,
          }}
        />
      </head>
      <body className="bg-background text-text-primary antialiased selection:bg-primary selection:text-background min-h-screen">
        <ThemeProvider>
          {children}
        </ThemeProvider>
      </body>
    </html>
  );
}
