import React from 'react';
import clsx from 'clsx';

export type BadgeVariant = 'emerald' | 'amber' | 'crimson' | 'sky' | 'slate';

export interface QuovexBadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  children: React.ReactNode;
  variant?: BadgeVariant;
  pulse?: boolean;
}

export function QuovexBadge({
  children,
  className,
  variant = 'emerald',
  pulse = false,
  ...props
}: QuovexBadgeProps) {
  const variantStyles = {
    emerald: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/25',
    amber: 'bg-amber-500/10 text-amber-400 border-amber-500/25',
    crimson: 'bg-red-500/10 text-red-400 border-red-500/25',
    sky: 'bg-sky-500/10 text-sky-400 border-sky-500/25',
    slate: 'bg-slate-500/10 text-slate-400 border-slate-500/25',
  };

  return (
    <span
      className={clsx(
        'inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold border tracking-wide uppercase',
        variantStyles[variant],
        className
      )}
      {...props}
    >
      {pulse && (
        <span className="relative flex h-1.5 w-1.5">
          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-current opacity-75"></span>
          <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-current"></span>
        </span>
      )}
      {children}
    </span>
  );
}
