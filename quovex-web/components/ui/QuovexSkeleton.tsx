import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: 'rectangular' | 'circular' | 'text';
}

export const QuovexSkeleton: React.FC<SkeletonProps> = ({
  className,
  variant = 'rectangular',
  ...props
}) => {
  const variants = {
    rectangular: 'rounded-xl',
    circular: 'rounded-full',
    text: 'rounded-md',
  };

  return (
    <div
      className={twMerge(
        clsx(
          'animate-pulse bg-surface-variant/70 border border-border/50',
          variants[variant],
          className
        )
      )}
      {...props}
    />
  );
};
