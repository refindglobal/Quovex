'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutDashboard, Timer, Bot, Sparkles, BookOpen } from 'lucide-react';

const MOBILE_NAV_ITEMS = [
  { href: '/app/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/app/timer', label: 'Timer', icon: Timer },
  { href: '/app/ai', label: 'AI Tutor', icon: Bot },
  { href: '/app/knowledge', label: 'Library', icon: BookOpen },
  { href: '/app/flashcards', label: 'Cards', icon: Sparkles },
];

export const MobileNav: React.FC = () => {
  const pathname = usePathname();

  return (
    <nav className="lg:hidden fixed bottom-0 left-0 right-0 h-16 bg-surface/95 backdrop-blur-xl border-t border-border flex items-center justify-around px-2 z-40 transition-colors duration-200">
      {MOBILE_NAV_ITEMS.map((item) => {
        const Icon = item.icon;
        const isActive = pathname === item.href || (item.href !== '/app/dashboard' && pathname.startsWith(item.href));

        return (
          <Link
            key={item.href}
            href={item.href}
            className={`flex flex-col items-center justify-center gap-1 w-14 py-1 rounded-xl transition-colors ${
              isActive ? 'text-primary font-bold' : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            <Icon className="w-5 h-5" />
            <span className="text-[10px] font-medium tracking-tight">{item.label}</span>
          </Link>
        );
      })}
    </nav>
  );
};
