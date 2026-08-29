'use client';

import React, { useState, useEffect } from 'react';
import clsx from 'clsx';
import { Search, X } from 'lucide-react';

export interface QuovexSearchInputProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  shortcutBadge?: string; // e.g. '⌘K'
  className?: string;
}

export function QuovexSearchInput({
  value,
  onChange,
  placeholder = 'Search records, students, or books...',
  shortcutBadge = '⌘K',
  className,
}: QuovexSearchInputProps) {
  const inputRef = React.useRef<HTMLInputElement>(null);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        inputRef.current?.focus();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  return (
    <div className={clsx('relative flex items-center', className)}>
      <Search className="w-4 h-4 text-gray-500 absolute left-3.5 pointer-events-none" />
      <input
        ref={inputRef}
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full pl-10 pr-16 py-2 rounded-xl bg-[#111917] border border-emerald-950/60 text-sm text-white placeholder-gray-500 focus:outline-none focus:border-[#00C896]/60 focus:ring-1 focus:ring-[#00C896]/30 transition-all"
      />

      <div className="absolute right-2.5 flex items-center gap-1.5">
        {value ? (
          <button
            type="button"
            onClick={() => onChange('')}
            className="p-1 rounded-md text-gray-400 hover:text-white hover:bg-white/5"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        ) : shortcutBadge ? (
          <kbd className="px-1.5 py-0.5 text-[10px] font-mono font-semibold rounded bg-[#192721] text-emerald-400 border border-emerald-900/50 pointer-events-none">
            {shortcutBadge}
          </kbd>
        ) : null}
      </div>
    </div>
  );
}
