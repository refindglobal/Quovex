import React from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface EmptyStateProps extends React.HTMLAttributes<HTMLDivElement> {
  icon: React.ReactNode;
  title: string;
  description?: string;
  action?: React.ReactNode;
}

export const QuovexEmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title,
  description,
  action,
  className,
  ...props
}) => {
  return (
    <div
      className={twMerge(
        clsx(
          'w-full flex flex-col items-center justify-center p-12 text-center rounded-2xl border border-dashed border-border bg-surface-variant/50',
          className
        )
      )}
      {...props}
    >
      <div className="w-16 h-16 rounded-3xl bg-primary-container text-primary flex items-center justify-center mb-6 shadow-glow">
        {icon}
      </div>
      <h3 className="text-title text-text-primary mb-2">{title}</h3>
      {description && (
        <p className="text-body text-text-secondary max-w-sm mb-6">
          {description}
        </p>
      )}
      {action && <div>{action}</div>}
    </div>
  );
};
