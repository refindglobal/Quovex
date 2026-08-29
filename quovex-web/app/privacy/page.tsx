import React from 'react';
import Link from 'next/link';
import { ArrowLeft, ShieldCheck } from 'lucide-react';

export default function PrivacyPolicyPage() {
  return (
    <div className="min-h-screen bg-background text-text-primary py-16 px-6 transition-colors duration-200">
      <div className="max-w-3xl mx-auto space-y-8">
        <Link href="/" className="inline-flex items-center gap-2 text-label text-primary hover:underline font-semibold">
          <ArrowLeft className="w-4 h-4" /> Back to Quovex
        </Link>

        <div>
          <h1 className="text-display font-extrabold text-text-primary">Privacy Policy</h1>
          <p className="text-label text-text-secondary mt-1">Last Updated: August 2026 • Refind Global Studio</p>
        </div>

        <div className="max-w-none text-body text-text-secondary space-y-6 leading-relaxed">
          <p>
            Quovex (&quot;we&quot;, &quot;our&quot;, or &quot;us&quot;), a product of <strong className="text-text-primary">Refind Global Studio</strong> (founded by Rohit &amp; Kartikey in Noida, Uttar Pradesh, India), values your privacy. This policy explains how we handle your data across our Android Application and Web Platform (<a href="https://quovex.online" className="text-primary hover:underline">quovex.online</a>).
          </p>

          <h2 className="text-title font-bold text-text-primary">1. Information We Collect</h2>
          <ul className="list-disc pl-5 space-y-2">
            <li><strong className="text-text-primary">Account Information:</strong> Name and email address provided during Google 1-Tap authentication.</li>
            <li><strong className="text-text-primary">Study Metadata:</strong> Focus timer durations, subjects, flashcard SM-2 review timestamps, and quiz scores.</li>
            <li><strong className="text-text-primary">Learning Materials:</strong> User-created notes and uploaded study PDFs stored securely under your private Firestore UID.</li>
          </ul>

          <h2 className="text-title font-bold text-text-primary">2. On-Device Camera &amp; ML Privacy</h2>
          <p>
            When utilizing Camera Focus Tracking, all face detection is executed <strong className="text-text-primary">100% on-device</strong> using local ML Kit processing. <strong className="text-text-primary">Camera frames and video feeds are never recorded, uploaded, or transmitted to any server.</strong>
          </p>

          <h2 className="text-title font-bold text-text-primary">3. Third-Party Services &amp; AI Providers</h2>
          <p>
            AI study assistance is mediated securely through our private Cloud Function gateway. We do not sell or rent personal data to third-party advertisers.
          </p>

          <h2 className="text-title font-bold text-text-primary">4. Data Deletion &amp; Contact</h2>
          <p>
            You can request complete account deletion at any time by contacting our support team at <a href="mailto:supportquovex@gmail.com" className="text-primary hover:underline">supportquovex@gmail.com</a>.
          </p>
        </div>
      </div>
    </div>
  );
}
