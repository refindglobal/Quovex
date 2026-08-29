import React, { forwardRef } from 'react';
import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export const QuovexInput = forwardRef<HTMLInputElement, InputProps>(
  ({ className, label, error, leftIcon, rightIcon, ...props }, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-label text-text-primary mb-1.5">
            {label}
          </label>
        )}
        <div className="relative">
          {leftIcon && (
            <div className="absolute left-4 top-1/2 -translate-y-1/2 text-text-secondary">
              {leftIcon}
            </div>
          )}
          <input
            ref={ref}
            className={twMerge(
              clsx(
                'w-full bg-surface-elevated border border-border rounded-xl text-body text-text-primary placeholder:text-text-disabled',
                'transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary',
                'disabled:opacity-50 disabled:cursor-not-allowed',
                leftIcon ? 'pl-11' : 'px-4',
                rightIcon ? 'pr-11' : 'px-4',
                error ? 'border-error focus:border-error focus:ring-error/20' : '',
                'py-3',
                className
              )
            )}
            {...props}
          />
          {rightIcon && (
            <div className="absolute right-4 top-1/2 -translate-y-1/2 text-text-secondary">
              {rightIcon}
            </div>
          )}
        </div>
        {error && (
          <p className="mt-1.5 text-caption text-error">{error}</p>
        )}
      </div>
    );
  }
);

QuovexInput.displayName = 'QuovexInput';
