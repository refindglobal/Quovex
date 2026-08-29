'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  Timer,
  Bot,
  BrainCircuit,
  BookOpen,
  Sparkles,
  Flame,
  HelpCircle,
  Users,
  User,
  Zap,
  BarChart3,
  LogOut,
} from 'lucide-react';
import { signOut } from '@/lib/firebase/auth';
import { ASSETS } from '@/lib/assets';

const NAV_ITEMS = [
  { href: '/app/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/app/timer', label: 'Focus Timer', icon: Timer },
  { href: '/app/ai', label: 'Quovex AI Tutor', icon: Bot, exact: true },
  { href: '/app/ai/doubt', label: 'Photo Doubt Solver', icon: BrainCircuit },
  { href: '/app/knowledge', label: 'Knowledge Hub', icon: BookOpen },
  { href: '/app/flashcards', label: 'Flashcards', icon: Sparkles },
  { href: '/app/planner', label: 'Study Planner', icon: Zap },
  { href: '/app/streaks', label: 'Streaks & Ranks', icon: Flame },
  { href: '/app/quiz', label: 'Diagnostic Quiz', icon: HelpCircle },
  { href: '/app/community', label: 'Study Rooms', icon: Users },
  { href: '/app/analytics', label: 'Analytics Center', icon: BarChart3 },
  { href: '/app/profile', label: 'Scholar Profile', icon: User },
];

export const AppSidebar: React.FC = () => {
  const pathname = usePathname();

  const isRouteActive = (href: string, exact?: boolean) => {
    if (exact || href === '/app/dashboard' || href === '/app/ai') {
      return pathname === href;
    }
    return pathname === href || pathname.startsWith(href + '/');
  };

  return (
    <aside className="w-60 bg-surface border-r border-border hidden lg:flex flex-col shrink-0 h-screen sticky top-0 transition-colors duration-200 select-none">
      {/* Brand Header */}
      <div className="p-4 border-b border-border flex items-center gap-3">
        <div className="w-8 h-8 rounded-xl overflow-hidden bg-primary/10 border border-primary/30 flex items-center justify-center shrink-0 shadow-sm">
          <img
            src={ASSETS.brand.emblem}
            alt="Quovex Logo"
            className="w-6 h-6 object-contain"
          />
        </div>
        <div>
          <span className="text-base font-extrabold tracking-tight text-text-primary flex items-center gap-1.5">
            QUOVEX <span className="text-[9px] bg-primary-container text-primary px-1 py-0.5 rounded font-mono font-bold">OS</span>
          </span>
          <p className="text-[10px] text-text-secondary">Student Operating System</p>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 overflow-y-auto p-3 space-y-0.5">
        {NAV_ITEMS.map((item) => {
          const Icon = item.icon;
          const isActive = isRouteActive(item.href, item.exact);

          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 px-3 py-2 rounded-xl text-xs font-semibold transition-all duration-150 ${
                isActive
                  ? 'bg-primary-container text-primary border border-primary/30 shadow-sm font-bold'
                  : 'text-text-secondary hover:text-text-primary hover:bg-surface-variant/60'
              }`}
            >
              <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-primary' : 'text-text-secondary'}`} />
              <span className="truncate">{item.label}</span>
            </Link>
          );
        })}
      </nav>

      {/* Footer Info & Sign Out */}
      <div className="p-3 border-t border-border space-y-2">
        <button
          onClick={() => signOut()}
          className="w-full flex items-center justify-center gap-2 px-3 py-1.5 rounded-xl text-xs font-semibold text-text-secondary hover:text-error hover:bg-error-container/20 transition-colors"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
