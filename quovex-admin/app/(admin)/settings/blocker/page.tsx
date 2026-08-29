'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Shield, ArrowLeft, Plus, Trash2, CheckCircle2, ShieldAlert, Smartphone } from 'lucide-react';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { QuovexSearchInput } from '@/components/ui/QuovexSearchInput';

export default function RemoteBlockerSettingsPage() {
  const [packages, setPackages] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [packageName, setPackageName] = useState('');
  const [appName, setAppName] = useState('');
  const [category, setCategory] = useState('SOCIAL_MEDIA');
  const [search, setSearch] = useState('');
  const [feedback, setFeedback] = useState<string | null>(null);

  const fetchPackages = () => {
    setLoading(true);
    fetch('/api/settings/blocker')
      .then((res) => res.json())
      .then((data) => {
        if (data.success) setPackages(data.packages);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchPackages();
  }, []);

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!packageName || !appName) return;

    try {
      const res = await fetch('/api/settings/blocker', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ packageName, appName, category }),
      });
      const data = await res.json();
      if (res.ok) {
        setPackageName('');
        setAppName('');
        setFeedback(data.message);
        fetchPackages();
      }
    } catch {
      // Error
    }
  };

  const handleDelete = async (pkgName: string) => {
    try {
      const res = await fetch(`/api/settings/blocker?packageName=${encodeURIComponent(pkgName)}`, {
        method: 'DELETE',
      });
      if (res.ok) {
        fetchPackages();
      }
    } catch {
      // Error
    }
  };

  const filtered = packages.filter(
    (p) =>
      p.appName.toLowerCase().includes(search.toLowerCase()) ||
      p.packageName.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-8">
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link
            href="/settings"
            className="p-2 rounded-xl bg-white/5 hover:bg-white/10 text-gray-400 hover:text-white transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
          </Link>
          <div>
            <h1 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
              <Shield className="w-6 h-6 text-emerald-400" />
              Remote Distraction Blocker Config
            </h1>
            <p className="text-sm text-gray-400">
              Dynamically append or remove distracting Android packages blocked during strict focus sessions.
            </p>
          </div>
        </div>
      </div>

      {feedback && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-xs text-emerald-400 flex items-center gap-2.5">
          <CheckCircle2 className="w-4 h-4 shrink-0" />
          <span>{feedback}</span>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Add Package Form (5 cols) */}
        <div className="lg:col-span-5 p-6 rounded-xl bg-[#111917] border border-border space-y-4">
          <div className="flex items-center gap-2">
            <ShieldAlert className="w-4 h-4 text-[#00C896]" />
            <h2 className="text-sm font-bold text-white">Append Distracting App</h2>
          </div>

          <form onSubmit={handleAdd} className="space-y-4 text-xs">
            <div className="space-y-1.5">
              <label className="font-semibold text-gray-400 uppercase tracking-wider">App Display Name</label>
              <input
                type="text"
                required
                value={appName}
                onChange={(e) => setAppName(e.target.value)}
                placeholder="e.g. Threads, BeReal"
                className="w-full px-3 py-2 rounded-lg bg-[#15201C] border border-border text-white focus:outline-none focus:border-[#00C896]"
              />
            </div>

            <div className="space-y-1.5">
              <label className="font-semibold text-gray-400 uppercase tracking-wider">Android Package Name</label>
              <input
                type="text"
                required
                value={packageName}
                onChange={(e) => setPackageName(e.target.value)}
                placeholder="e.g. com.instagram.barcelona"
                className="w-full px-3 py-2 rounded-lg bg-[#15201C] border border-border text-white font-mono focus:outline-none focus:border-[#00C896]"
              />
            </div>

            <div className="space-y-1.5">
              <label className="font-semibold text-gray-400 uppercase tracking-wider">Category</label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full px-3 py-2 rounded-lg bg-[#15201C] border border-border text-white focus:outline-none focus:border-[#00C896]"
              >
                <option value="SHORT_FORM_VIDEO">Short-Form Video (Reels/Shorts)</option>
                <option value="SOCIAL_MEDIA">Social Media</option>
                <option value="GAMING">Gaming</option>
                <option value="ENTERTAINMENT">Entertainment & Streaming</option>
                <option value="MESSAGING">Messaging & Chat</option>
              </select>
            </div>

            <QuovexButton type="submit" variant="primary" className="w-full" leftIcon={<Plus className="w-4 h-4" />}>
              Append to Live Blocklist
            </QuovexButton>
          </form>
        </div>

        {/* Current Remote Blocklist (7 cols) */}
        <div className="lg:col-span-7 p-6 rounded-xl bg-[#111917] border border-border space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-white">Active Blocked Packages ({packages.length})</h2>
            <div className="w-64">
              <QuovexSearchInput value={search} onChange={setSearch} placeholder="Search packages..." />
            </div>
          </div>

          <div className="space-y-2.5 max-h-[480px] overflow-y-auto pr-1">
            {filtered.map((pkg) => (
              <div
                key={pkg.packageName}
                className="p-3.5 rounded-lg bg-[#15201C] border border-border flex items-center justify-between text-xs"
              >
                <div className="flex items-center gap-3">
                  <div className="p-2 rounded-lg bg-red-500/10 text-red-400 border border-red-500/20">
                    <Smartphone className="w-4 h-4" />
                  </div>
                  <div>
                    <h4 className="font-bold text-white flex items-center gap-2">
                      {pkg.appName}
                      <QuovexBadge variant="slate">{pkg.category.replace(/_/g, ' ')}</QuovexBadge>
                    </h4>
                    <p className="text-[11px] text-gray-400 font-mono mt-0.5">{pkg.packageName}</p>
                  </div>
                </div>

                <button
                  onClick={() => handleDelete(pkg.packageName)}
                  className="p-1.5 rounded-lg text-gray-400 hover:text-red-400 hover:bg-red-500/10 transition-colors"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
