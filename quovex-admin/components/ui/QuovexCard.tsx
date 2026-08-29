import React from 'react';
import clsx from 'clsx';

export interface QuovexCardProps extends React.HTMLAttributes<HTMLDivElement> {
  children: React.ReactNode;
  className?: string;
  hoverGlow?: boolean;
  bordered?: boolean;
}

export function QuovexCard({
  children,
  className,
  hoverGlow = false,
  bordered = true,
  ...props
}: QuovexCardProps) {
  return (
    <div
      className={clsx(
        'rounded-xl bg-[#121A16]/80 backdrop-blur-md p-5 transition-all duration-200',
        bordered && 'border border-emerald-950/60 shadow-lg shadow-black/20',
        hoverGlow && 'hover:border-emerald-500/40 hover:shadow-emerald-950/20 hover:-translate-y-0.5',
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
}

export function QuovexCardHeader({
  children,
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={clsx('flex items-center justify-between pb-3 border-b border-emerald-950/40 mb-4', className)} {...props}>
      {children}
    </div>
  );
}

export function QuovexCardTitle({
  children,
  className,
  ...props
}: React.HTMLAttributes<HTMLHeadingElement>) {
  return (
    <h3 className={clsx('text-base font-bold text-white flex items-center gap-2', className)} {...props}>
      {children}
    </h3>
  );
}
