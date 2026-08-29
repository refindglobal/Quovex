'use client';

import React, { useEffect } from 'react';
import clsx from 'clsx';
import { X } from 'lucide-react';

export interface QuovexModalProps {
  isOpen: boolean;
  onClose: () => Unit;
  title: string;
  children: React.ReactNode;
  maxWidth?: 'sm' | 'md' | 'lg' | 'xl' | '2xl';
}

type Unit = void;

export function QuovexModal({
  isOpen,
  onClose,
  title,
  children,
  maxWidth = 'lg',
}: QuovexModalProps) {
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && isOpen) {
        onClose();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  const maxWidthClasses = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
    '2xl': 'max-w-2xl',
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/75 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      {/* Modal Dialog */}
      <div
        className={clsx(
          'relative w-full rounded-2xl bg-[#0F1713] border border-emerald-900/50 shadow-2xl p-6 z-10 transition-all transform animate-in fade-in zoom-in-95',
          maxWidthClasses[maxWidth]
        )}
      >
        <div className="flex items-center justify-between pb-4 border-b border-emerald-950/60 mb-5">
          <h2 className="text-lg font-bold text-white tracking-tight">{title}</h2>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-gray-400 hover:text-white hover:bg-white/5 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div>{children}</div>
      </div>
    </div>
  );
}
