'use client';

import { Sparkles, Terminal } from 'lucide-react';

interface HeaderProps {
  title: string;
  description?: string;
  action?: React.ReactNode;
}

export default function Header({ title, description, action }: HeaderProps) {
  return (
    <header className="h-16 border-b border-border bg-[#0C120F] flex items-center justify-between px-8 shrink-0">
      <div>
        <h1 className="text-base font-semibold text-white tracking-tight">{title}</h1>
        {description && (
          <p className="text-xs text-muted-foreground">{description}</p>
        )}
      </div>

      <div className="flex items-center gap-3">
        <div className="flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-primary/10 border border-primary/20 text-primary text-[11px] font-medium">
          <span className="w-1.5 h-1.5 rounded-full bg-primary animate-pulse" />
          <span>AI Multi-Agent Engine Active</span>
        </div>
        {action}
      </div>
    </header>
  );
}
