import React from 'react';
import clsx from 'clsx';
import { TrendingUp, TrendingDown, Minus } from 'lucide-react';
import { QuovexCard } from './QuovexCard';

export interface QuovexStatsCardProps {
  title: string;
  value: string | number;
  change?: number; // e.g. +14.2 or -3.1
  changePeriod?: string; // e.g. 'vs last week'
  icon?: React.ReactNode;
  className?: string;
}

export function QuovexStatsCard({
  title,
  value,
  change,
  changePeriod = 'vs last 7d',
  icon,
  className,
}: QuovexStatsCardProps) {
  const isPositive = change !== undefined && change > 0;
  const isNegative = change !== undefined && change < 0;
  const isNeutral = change !== undefined && change === 0;

  return (
    <QuovexCard className={clsx('relative overflow-hidden', className)} hoverGlow>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider">{title}</p>
          <h3 className="text-2xl font-extrabold text-white mt-1 tracking-tight">{value}</h3>
        </div>

        {icon && (
          <div className="p-2.5 rounded-xl bg-[#00C896]/10 border border-[#00C896]/20 text-[#00C896]">
            {icon}
          </div>
        )}
      </div>

      {change !== undefined && (
        <div className="flex items-center gap-1.5 mt-3 pt-3 border-t border-emerald-950/40 text-xs">
          {isPositive && (
            <span className="flex items-center gap-0.5 font-bold text-emerald-400">
              <TrendingUp className="w-3.5 h-3.5" />
              +{change}%
            </span>
          )}
          {isNegative && (
            <span className="flex items-center gap-0.5 font-bold text-red-400">
              <TrendingDown className="w-3.5 h-3.5" />
              {change}%
            </span>
          )}
          {isNeutral && (
            <span className="flex items-center gap-0.5 font-medium text-gray-400">
              <Minus className="w-3.5 h-3.5" />
              0%
            </span>
          )}
          <span className="text-gray-500 text-[11px]">{changePeriod}</span>
        </div>
      )}
    </QuovexCard>
  );
}
