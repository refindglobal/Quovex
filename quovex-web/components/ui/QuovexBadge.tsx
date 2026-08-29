import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  variant?: 'emerald' | 'gold' | 'fire' | 'muted' | 'outline' | 'danger' | 'info';
  size?: 'sm' | 'md' | 'lg';
}

export const QuovexBadge: React.FC<BadgeProps> = ({
  children,
  className,
  variant = 'emerald',
  size = 'md',
  ...props
}) => {
  const variants = {
    emerald: 'bg-primary-container text-primary border border-primary/30',
    gold: 'bg-warning-container text-warning border border-warning/30',
    fire: 'bg-[rgba(255,107,53,0.15)] text-streak-fire border border-streak-fire/30',
    muted: 'bg-surface-variant text-text-secondary border border-border',
    outline: 'bg-transparent text-primary border border-primary/40',
    danger: 'bg-error-container text-error border border-error/30',
    info: 'bg-[rgba(33,150,243,0.15)] text-[#2196F3] border border-[#2196F3]/30',
  };

  const sizes = {
    sm: 'text-[10px] px-2 py-0.5 rounded-md font-bold tracking-wider uppercase',
    md: 'text-xs px-2.5 py-1 rounded-lg font-semibold',
    lg: 'text-sm px-3.5 py-1.5 rounded-xl font-bold tracking-wide uppercase',
  };

  return (
    <span
      className={twMerge(clsx('inline-flex items-center gap-1.5 shrink-0 transition-colors duration-200', variants[variant], sizes[size], className))}
      {...props}
    >
      {children}
    </span>
  );
};
