'use client';

import React from 'react';
import { Loader2 } from 'lucide-react';

export default function AppLoading() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center min-h-[60vh] animate-in fade-in duration-500">
      <div className="w-16 h-16 rounded-2xl bg-primary-container text-primary flex items-center justify-center shadow-glow mb-6 animate-pulse">
        <Loader2 className="w-8 h-8 animate-spin" />
      </div>
      <h3 className="text-display font-black text-text-primary">Loading Quovex...</h3>
      <p className="text-section text-text-secondary mt-2">
        Syncing your study profile and metrics
      </p>
    </div>
  );
}
