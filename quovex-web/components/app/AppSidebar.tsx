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

const NAV_ITEMS = [
  { href: '/app/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/app/timer', label: 'Focus Timer', icon: Timer },
  { href: '/app/ai', label: 'Quovex AI Tutor', icon: Bot },
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

  return (
    <aside className="w-64 bg-surface border-r border-border hidden lg:flex flex-col shrink-0 h-screen sticky top-0 transition-colors duration-200">
      {/* Brand Header */}
      <div className="p-6 border-b border-border flex items-center gap-3">
        <div className="w-9 h-9 rounded-xl bg-primary-container border border-primary/40 flex items-center justify-center text-primary font-bold text-lg shadow-glow">
          Q
        </div>
        <div>
          <span className="text-lg font-extrabold tracking-tight text-text-primary flex items-center gap-1.5">
            QUOVEX <span className="text-[10px] bg-primary-container text-primary px-1.5 py-0.5 rounded font-mono font-bold">WEB</span>
          </span>
          <p className="text-[11px] text-text-secondary">Student Operating System</p>
        </div>
      </div>

      {/* Nav List */}
      <nav className="flex-1 overflow-y-auto p-4 space-y-1">
        {NAV_ITEMS.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href || (item.href !== '/app/dashboard' && pathname.startsWith(item.href));

          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-primary-container text-primary border border-primary/30 shadow-sm font-bold'
                  : 'text-text-secondary hover:text-text-primary hover:bg-surface-variant/70'
              }`}
            >
              <Icon className={`w-4 h-4 shrink-0 ${isActive ? 'text-primary' : 'text-text-secondary'}`} />
              <span>{item.label}</span>
            </Link>
          );
        })}
      </nav>

      {/* Footer Info & Sign Out */}
      <div className="p-4 border-t border-border space-y-3">
        <div className="p-3 rounded-xl bg-surface-variant border border-border text-xs">
          <div className="flex items-center justify-between text-text-primary">
            <span>Origin</span>
            <span className="font-bold text-primary">Noida, India 🇮🇳</span>
          </div>
          <p className="text-[10px] text-text-secondary mt-1">Refind Global Studio</p>
        </div>

        <button
          onClick={() => signOut()}
          className="w-full flex items-center justify-center gap-2 px-3 py-2 rounded-xl text-xs font-semibold text-text-secondary hover:text-error hover:bg-error-container transition-colors"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};
