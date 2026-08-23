'use client';

import { useEffect, useState } from 'react';
import { ToggleLeft, ToggleRight, ShieldCheck, CheckCircle2, AlertCircle } from 'lucide-react';

export default function FeatureFlagsPage() {
  const [flags, setFlags] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchFlags = () => {
    setLoading(true);
    fetch('/api/feature-flags')
      .then((res) => res.json())
      .then((data) => {
        if (data.success) setFlags(data.flags);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchFlags();
  }, []);

  const handleToggle = async (flagId: string, currentEnabled: boolean, rollout: number) => {
    try {
      const res = await fetch(`/api/feature-flags/${flagId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ enabled: !currentEnabled, rolloutPercentage: rollout }),
      });
      if (res.ok) {
        fetchFlags();
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Feature Toggle & Rollout Center</h1>
          <p className="text-sm text-muted-foreground">Server-side feature gates and staged rollout percentages (All changes are audit logged).</p>
        </div>
      </div>

      {/* Feature Flags Table */}
      <div className="rounded-xl bg-[#111917] border border-border overflow-hidden">
        <table className="w-full text-left text-xs">
          <thead className="bg-[#15201C] text-muted-foreground font-semibold border-b border-border">
            <tr>
              <th className="px-4 py-3">Feature Name / Key</th>
              <th className="px-4 py-3">Description</th>
              <th className="px-4 py-3">Environment</th>
              <th className="px-4 py-3">Rollout %</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3 text-right">Toggle</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border/60">
            {flags.map((flag) => (
              <tr key={flag.id} className="hover:bg-[#15201C]/50 transition-colors">
                <td className="px-4 py-3">
                  <div className="font-bold text-foreground">{flag.name}</div>
                  <div className="font-mono text-[11px] text-primary">{flag.key}</div>
                </td>
                <td className="px-4 py-3 text-muted-foreground max-w-xs">{flag.description}</td>
                <td className="px-4 py-3">
                  <span className="px-2 py-0.5 rounded bg-muted/40 text-foreground border border-border text-[10px]">
                    {flag.environment}
                  </span>
                </td>
                <td className="px-4 py-3 font-semibold text-foreground">{flag.rolloutPercentage}%</td>
                <td className="px-4 py-3">
                  <span
                    className={`px-2 py-0.5 rounded text-[10px] font-semibold ${
                      flag.enabled
                        ? 'bg-primary/10 text-primary border border-primary/20'
                        : 'bg-muted/40 text-muted-foreground border border-border'
                    }`}
                  >
                    {flag.enabled ? 'ACTIVE' : 'DISABLED'}
                  </span>
                </td>
                <td className="px-4 py-3 text-right">
                  <button
                    onClick={() => handleToggle(flag.id, flag.enabled, flag.rolloutPercentage)}
                    className="p-1.5 rounded-lg hover:bg-[#1C2B24] transition-colors"
                  >
                    {flag.enabled ? (
                      <ToggleRight className="w-6 h-6 text-primary" />
                    ) : (
                      <ToggleLeft className="w-6 h-6 text-muted-foreground" />
                    )}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
