'use client';

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

  return (
    <aside className="w-64 border-r border-border bg-[#0C120F] flex flex-col justify-between shrink-0 h-screen overflow-y-auto">
      <div>
        {/* Brand Header */}
        <div className="h-16 flex items-center px-6 border-b border-border gap-3 sticky top-0 bg-[#0C120F] z-10">
          <div className="w-8 h-8 rounded-lg bg-primary/20 border border-primary/40 flex items-center justify-center">
            <Layers className="w-4 h-4 text-primary" />
          </div>
          <div>
            <span className="font-bold tracking-tight text-white flex items-center gap-1.5 text-base">
              QUOVEX <span className="text-[10px] uppercase font-semibold px-1.5 py-0.5 rounded bg-primary/10 text-primary border border-primary/20">Admin</span>
            </span>
          </div>
        </div>

        {/* Navigation links */}
        <div className="px-3 py-4 space-y-6">
          {navGroups.map((group) => (
            <div key={group.title}>
              <div className="px-3 mb-2 text-[10px] font-semibold text-muted-foreground/60 uppercase tracking-wider">
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
                          ? 'bg-primary/15 text-primary border border-primary/30 font-semibold'
                          : 'text-muted-foreground hover:text-foreground hover:bg-[#151D19]'
                      )}
                    >
                      <Icon className={clsx('w-4 h-4', isActive ? 'text-primary' : 'text-muted-foreground')} />
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
      <div className="p-4 border-t border-border sticky bottom-0 bg-[#0C120F]">
        <div className="p-3 rounded-lg bg-[#121815] border border-border/80 flex items-center gap-3">
          <ShieldCheck className="w-4 h-4 text-primary shrink-0" />
          <div className="text-[11px] leading-tight">
            <div className="text-foreground font-medium">Control Center</div>
            <div className="text-muted-foreground text-[10px]">RBAC Active • Zero PII</div>
          </div>
        </div>
      </div>
    </aside>
  );
}
