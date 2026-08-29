'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  Users,
  BarChart3,
  Activity,
  Layers,
  BookOpen,
  Sparkles,
  Bot,
  Bell,
  ShieldAlert,
  ToggleLeft,
  ScrollText,
  CreditCard,
  Settings,
  ShieldCheck,
  Menu,
  X,
} from 'lucide-react';
import clsx from 'clsx';

const navGroups = [
  {
    title: 'OVERVIEW',
    items: [
      { name: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
      { name: 'Users', href: '/users', icon: Users },
      { name: 'Analytics', href: '/analytics', icon: BarChart3 },
      { name: 'System Health', href: '/system', icon: Activity },
    ],
  },
  {
    title: 'LEARNING',
    items: [
      { name: 'Content Catalog', href: '/content', icon: Layers },
      { name: 'NCERT Library', href: '/ncert', icon: BookOpen },
      { name: 'Content Studio', href: '/content-studio', icon: Sparkles },
    ],
  },
  {
    title: 'AI INFRASTRUCTURE',
    items: [
      { name: 'AI Operations & Keys', href: '/ai', icon: Bot },
    ],
  },
  {
    title: 'OPERATIONS',
    items: [
      { name: 'Notifications', href: '/notifications', icon: Bell },
      { name: 'Moderation Queue', href: '/moderation', icon: ShieldAlert },
      { name: 'Feature Flags', href: '/feature-flags', icon: ToggleLeft },
      { name: 'Audit Logs', href: '/audit-logs', icon: ScrollText },
    ],
  },
  {
    title: 'BUSINESS & SETTINGS',
    items: [
      { name: 'Monetization', href: '/monetization', icon: CreditCard },
      { name: 'Settings', href: '/settings', icon: Settings },
    ],
  },
];

export default function Sidebar() {
  const pathname = usePathname();
  const [mobileOpen, setMobileOpen] = useState(false);

  // Close drawer automatically on route navigation
  useEffect(() => {
    setMobileOpen(false);
  }, [pathname]);

  const navContent = (
    <div className="flex flex-col justify-between h-full">
      <div>
        {/* Brand Header */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-border/80 sticky top-0 bg-[#0C120F] z-10">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-[#00C896]/20 border border-[#00C896]/40 flex items-center justify-center">
              <Layers className="w-4 h-4 text-[#00C896]" />
            </div>
            <span className="font-bold tracking-tight text-white flex items-center gap-1.5 text-base">
              QUOVEX <span className="text-[10px] uppercase font-semibold px-1.5 py-0.5 rounded bg-[#00C896]/10 text-[#00C896] border border-[#00C896]/20">Admin</span>
            </span>
          </div>

          <button
            onClick={() => setMobileOpen(false)}
            className="md:hidden p-1 rounded-lg text-gray-400 hover:text-white hover:bg-white/5"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Navigation links */}
        <div className="px-3 py-4 space-y-6">
          {navGroups.map((group) => (
            <div key={group.title}>
              <div className="px-3 mb-2 text-[10px] font-semibold text-gray-500 uppercase tracking-wider">
                {group.title}
              </div>
              <div className="space-y-1">
                {group.items.map((item) => {
                  const Icon = item.icon;
                  const isActive =
                    pathname === item.href ||
                    (item.href !== '/dashboard' && item.href !== '/content-studio' && pathname.startsWith(item.href));

                  return (
                    <Link
                      key={item.href}
                      href={item.href}
                      className={clsx(
                        'flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-medium transition-colors',
                        isActive
                          ? 'bg-[#00C896]/15 text-[#00C896] border border-[#00C896]/30 font-semibold'
                          : 'text-gray-400 hover:text-white hover:bg-[#151D19]'
                      )}
                    >
                      <Icon className={clsx('w-4 h-4', isActive ? 'text-[#00C896]' : 'text-gray-400')} />
                      <span>{item.name}</span>
                    </Link>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Footer Info */}
      <div className="p-4 border-t border-border/80 sticky bottom-0 bg-[#0C120F]">
        <div className="p-3 rounded-lg bg-[#121815] border border-border/80 flex items-center gap-3">
          <ShieldCheck className="w-4 h-4 text-[#00C896] shrink-0" />
          <div className="text-[11px] leading-tight">
            <div className="text-white font-medium">Control Center</div>
            <div className="text-gray-400 text-[10px]">RBAC Active • Zero PII</div>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <>
      {/* ── Mobile Top Header Bar ────────────────────────────────────────── */}
      <header className="md:hidden flex items-center justify-between h-14 px-4 bg-[#0C120F] border-b border-border fixed top-0 left-0 right-0 z-40">
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-[#00C896]/20 border border-[#00C896]/40 flex items-center justify-center">
            <Layers className="w-3.5 h-3.5 text-[#00C896]" />
          </div>
          <span className="font-bold tracking-tight text-white text-sm">
            QUOVEX <span className="text-[9px] uppercase font-semibold px-1 py-0.2 rounded bg-[#00C896]/10 text-[#00C896]">Admin</span>
          </span>
        </div>

        <button
          onClick={() => setMobileOpen(true)}
          className="p-2 rounded-lg bg-white/5 hover:bg-white/10 text-gray-300 hover:text-white transition-colors"
          aria-label="Open menu"
        >
          <Menu className="w-5 h-5" />
        </button>
      </header>

      {/* ── Mobile Slide-out Drawer & Backdrop ──────────────────────────── */}
      {mobileOpen && (
        <div className="md:hidden fixed inset-0 z-50 flex">
          <div
            className="fixed inset-0 bg-black/80 backdrop-blur-sm transition-opacity"
            onClick={() => setMobileOpen(false)}
          />
          <aside className="relative w-72 bg-[#0C120F] border-r border-border h-full flex flex-col justify-between overflow-y-auto z-10 animate-in slide-in-from-left duration-200">
            {navContent}
          </aside>
        </div>
      )}

      {/* ── Desktop Persistent Sidebar ───────────────────────────────────── */}
      <aside className="hidden md:flex w-64 border-r border-border bg-[#0C120F] flex-col justify-between shrink-0 h-screen overflow-y-auto">
        {navContent}
      </aside>
    </>
  );
}
