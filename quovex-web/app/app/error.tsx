'use client';

import React, { useEffect } from 'react';
import { AlertTriangle, RefreshCcw } from 'lucide-react';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';

export default function AppError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    // Log the error to an error reporting service
    console.error('Quovex App Error:', error);
  }, [error]);

  return (
    <div className="flex-1 flex flex-col items-center justify-center min-h-[60vh] p-6 animate-in fade-in zoom-in-95">
      <QuovexCard variant="elevated" className="max-w-md w-full p-8 text-center space-y-6 shadow-sm border-error/20">
        <div className="w-16 h-16 rounded-2xl bg-error-container text-error flex items-center justify-center mx-auto shadow-sm">
          <AlertTriangle className="w-8 h-8" />
        </div>
        
        <div>
          <h2 className="text-display font-black text-text-primary">Oops, something went wrong!</h2>
          <p className="text-body text-text-secondary mt-2">
            A glitch occurred in the matrix. Don't worry, your progress is safely stored.
          </p>
        </div>

        {error.message && (
          <div className="p-4 rounded-xl bg-surface-variant border border-border text-label text-text-secondary font-mono text-left overflow-auto">
            {error.message}
          </div>
        )}

        <QuovexButton
          variant="primary"
          size="lg"
          className="w-full"
          onClick={() => reset()}
          leftIcon={<RefreshCcw className="w-5 h-5" />}
        >
          Try Again
        </QuovexButton>
      </QuovexCard>
    </div>
  );
}
