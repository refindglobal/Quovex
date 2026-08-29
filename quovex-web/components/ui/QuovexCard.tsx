import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'default' | 'elevated' | 'glass';
  hoverEffect?: boolean;
}

export const QuovexCard: React.FC<CardProps> = ({
  children,
  className,
  variant = 'default',
  hoverEffect = false,
  ...props
}) => {
  const variants = {
    default: 'bg-surface border border-border shadow-card text-text-primary',
    elevated: 'bg-surface-elevated border border-border/80 shadow-lg text-text-primary',
    glass: 'glass-panel text-text-primary',
  };

  const hoverClass = hoverEffect
    ? 'transition-all duration-300 hover:border-primary/50 hover:shadow-card-hover hover:-translate-y-0.5'
    : '';

  return (
    <div
      className={twMerge(clsx('rounded-2xl p-6 transition-colors duration-200', variants[variant], hoverClass, className))}
      {...props}
    >
      {children}
    </div>
  );
};
