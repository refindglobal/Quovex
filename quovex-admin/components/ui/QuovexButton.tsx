import React from 'react';
import clsx from 'clsx';
import { Loader2 } from 'lucide-react';

export type ButtonVariant = 'primary' | 'secondary' | 'outline' | 'danger' | 'ghost';
export type ButtonSize = 'sm' | 'md' | 'lg';

export interface QuovexButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export function QuovexButton({
  children,
  className,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled,
  leftIcon,
  rightIcon,
  ...props
}: QuovexButtonProps) {
  const sizeClasses = {
    sm: 'px-2.5 py-1.5 text-xs font-semibold gap-1.5',
    md: 'px-4 py-2 text-sm font-semibold gap-2',
    lg: 'px-6 py-2.5 text-base font-bold gap-2.5',
  };

  const variantClasses = {
    primary:
      'bg-[#00C896] hover:bg-[#00B084] text-[#0A0F0D] shadow-md shadow-emerald-950/30 active:scale-[0.98]',
    secondary:
      'bg-[#192721] hover:bg-[#20322B] text-emerald-300 border border-emerald-900/50 active:scale-[0.98]',
    outline:
      'bg-transparent hover:bg-emerald-950/30 text-white border border-emerald-800/40 active:scale-[0.98]',
    danger:
      'bg-red-500/20 hover:bg-red-500/30 text-red-400 border border-red-500/40 active:scale-[0.98]',
    ghost:
      'bg-transparent hover:bg-white/5 text-gray-300 hover:text-white',
  };

  return (
    <button
      className={clsx(
        'inline-flex items-center justify-center rounded-lg transition-all duration-150 select-none cursor-pointer',
        sizeClasses[size],
        variantClasses[variant],
        (disabled || isLoading) && 'opacity-50 cursor-not-allowed pointer-events-none',
        className
      )}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? (
        <Loader2 className="w-4 h-4 animate-spin text-current" />
      ) : (
        leftIcon
      )}
      {children}
      {!isLoading && rightIcon}
    </button>
  );
}
