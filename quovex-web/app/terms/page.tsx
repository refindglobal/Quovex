import React from 'react';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';

export default function TermsOfServicePage() {
  return (
    <div className="min-h-screen bg-background text-text-primary py-16 px-6 transition-colors duration-200">
      <div className="max-w-3xl mx-auto space-y-8">
        <Link href="/" className="inline-flex items-center gap-2 text-label text-primary hover:underline font-semibold">
          <ArrowLeft className="w-4 h-4" /> Back to Quovex
        </Link>

        <div>
          <h1 className="text-display font-extrabold text-text-primary">Terms of Service</h1>
          <p className="text-label text-text-secondary mt-1">Last Updated: August 2026 • Refind Global Studio</p>
        </div>

        <div className="max-w-none text-body text-text-secondary space-y-6 leading-relaxed">
          <p>
            Welcome to Quovex. By accessing or using our website (<a href="https://quovex.online" className="text-primary hover:underline">quovex.online</a>) or the Quovex Android application, you agree to be bound by these Terms of Service.
          </p>

          <h2 className="text-title font-bold text-text-primary">1. Use of Services</h2>
          <p>
            Quovex provides educational tools including AI study tutoring, focus timers, flashcards, and textbook repositories. You agree to use the platform solely for lawful, academic, and personal learning purposes.
          </p>

          <h2 className="text-title font-bold text-text-primary">2. Subscriptions &amp; Free Trial</h2>
          <p>
            The Quovex Pro Annual plan includes a 7-Day Free Trial. Unless cancelled before the trial conclusion, standard subscription fees apply. Subscriptions can be managed directly within your account settings.
          </p>

          <h2 className="text-title font-bold text-text-primary">3. Intellectual Property</h2>
          <p>
            All original learning materials, branding, algorithms, and software code are the proprietary intellectual property of Refind Global Studio.
          </p>

          <h2 className="text-title font-bold text-text-primary">4. Contact Information</h2>
          <p>
            For inquiries regarding these terms, contact us at <a href="mailto:Refindglobalstudio@gmail.com" className="text-primary hover:underline">Refindglobalstudio@gmail.com</a>.
          </p>
        </div>
      </div>
    </div>
  );
}
