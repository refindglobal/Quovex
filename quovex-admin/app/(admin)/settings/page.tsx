'use client';

import { useState } from 'react';
import { Settings, ShieldCheck, CheckCircle2, AlertTriangle, Key } from 'lucide-react';

export default function SettingsPage() {
  const [dailyQuota, setDailyQuota] = useState('10');
  const [maintenanceMode, setMaintenanceMode] = useState(false);
  const [saved, setSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Admin System Settings</h1>
          <p className="text-sm text-muted-foreground">Configure global limits, platform maintenance toggles, and security policies.</p>
        </div>
      </div>

      <div className="max-w-2xl p-6 rounded-xl bg-[#111917] border border-border space-y-6">
        <div className="flex items-center gap-2">
          <Settings className="w-4 h-4 text-primary" />
          <h2 className="text-sm font-bold text-foreground">Global Platform Policies</h2>
        </div>

        {saved && (
          <div className="p-3 rounded-lg bg-primary/10 border border-primary/30 text-xs text-primary flex items-center gap-2">
            <CheckCircle2 className="w-4 h-4 shrink-0" />
            <span>Settings saved successfully!</span>
          </div>
        )}

        <form onSubmit={handleSave} className="space-y-4 text-xs">
          <div className="space-y-1.5">
            <label className="font-semibold text-muted-foreground uppercase tracking-wider">
              Free Tier Daily AI Request Limit
            </label>
            <input
              type="number"
              value={dailyQuota}
              onChange={(e) => setDailyQuota(e.target.value)}
              className="w-full px-3 py-2 rounded-lg bg-[#15201C] border border-border text-foreground focus:outline-none focus:border-primary"
            />
            <p className="text-[11px] text-muted-foreground">
              Enforced server-side per user UID across Groq / Cerebras rotating gateway.
            </p>
          </div>

          <div className="pt-3 border-t border-border/60 flex items-center justify-between">
            <div>
              <div className="font-semibold text-foreground">Platform Maintenance Mode</div>
              <div className="text-[11px] text-muted-foreground">Temporarily block non-admin logins and generation jobs</div>
            </div>
            <input
              type="checkbox"
              checked={maintenanceMode}
              onChange={(e) => setMaintenanceMode(e.target.checked)}
              className="w-4 h-4 accent-primary"
            />
          </div>

          <button
            type="submit"
            className="px-4 py-2 rounded-lg bg-primary text-black font-semibold hover:bg-primary/90 transition-colors cursor-pointer"
          >
            Save Settings
          </button>
        </form>
      </div>

      {/* Distraction Blocker Quick Link Card */}
      <div className="max-w-2xl p-6 rounded-xl bg-[#111917] border border-border flex items-center justify-between">
        <div>
          <h3 className="text-sm font-bold text-white">Remote Distraction Blocker Config</h3>
          <p className="text-xs text-muted-foreground mt-0.5">
            Configure live package blocklists synced to student devices during strict focus.
          </p>
        </div>
        <a
          href="/settings/blocker"
          className="px-3.5 py-2 rounded-lg bg-[#192721] text-emerald-300 border border-emerald-900/50 hover:bg-[#20322B] text-xs font-semibold transition-colors"
        >
          Manage Blocklist →
        </a>
      </div>
    </div>
  );
}
