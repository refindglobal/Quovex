'use client';

import { useEffect, useState } from 'react';
import { Bot, Key, Activity, ShieldCheck, CheckCircle2, RefreshCw, Cpu, Zap } from 'lucide-react';

export default function AiOperationsPage() {
  const [keys, setKeys] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchKeys = () => {
    setLoading(true);
    fetch('/api/ai/keys')
      .then((res) => res.json())
      .then((data) => {
        if (data.success) setKeys(data.keys);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchKeys();
  }, []);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">AI Operations & Key Management</h1>
          <p className="text-sm text-muted-foreground">Monitor server-side AI key rotation pool, provider latency, and automatic failovers (Zero Client Exposure).</p>
        </div>
        <div className="flex items-center gap-2.5">
          <a
            href="/ai/study-plans"
            className="flex items-center gap-1.5 px-3.5 py-2 rounded-lg bg-primary text-black font-semibold text-xs hover:bg-primary/90 transition-colors"
          >
            <Bot className="w-3.5 h-3.5" />
            <span>Study Plan Inspector</span>
          </a>
          <button
            onClick={fetchKeys}
            className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-secondary text-secondary-foreground font-medium text-xs hover:bg-secondary/80 border border-border transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Refresh Pool</span>
          </button>
        </div>
      </div>

      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-5 rounded-xl bg-[#111917] border border-border space-y-3">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">Active Key Pool</span>
            <Key className="w-4 h-4 text-primary" />
          </div>
          <div className="text-3xl font-bold text-foreground">4 Groq + 4 Cerebras</div>
          <p className="text-[11px] text-muted-foreground">Automatic rotation on 429 rate limit or failure</p>
        </div>

        <div className="p-5 rounded-xl bg-[#111917] border border-border space-y-3">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">Average Latency</span>
            <Zap className="w-4 h-4 text-primary" />
          </div>
          <div className="text-3xl font-bold text-foreground">~185 ms</div>
          <p className="text-[11px] text-primary font-medium">99.4% aggregate success rate</p>
        </div>

        <div className="p-5 rounded-xl bg-[#111917] border border-border space-y-3">
          <div className="flex items-center justify-between text-muted-foreground">
            <span className="text-xs font-semibold uppercase tracking-wider">Security Invariant</span>
            <ShieldCheck className="w-4 h-4 text-primary" />
          </div>
          <div className="text-sm font-bold text-foreground">Strict Server-Only Storage</div>
          <p className="text-[11px] text-muted-foreground">Keys never transmitted to Android client</p>
        </div>
      </div>

      {/* Rotating Key Pool Table */}
      <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Cpu className="w-4 h-4 text-primary" />
            <h2 className="text-sm font-bold text-foreground">Rotating Provider Key Pool (Masked)</h2>
          </div>
          <span className="text-xs text-muted-foreground">Rule 5: API keys masked with prefix/suffix only</span>
        </div>

        <div className="rounded-lg border border-border overflow-hidden">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#15201C] text-muted-foreground font-semibold border-b border-border">
              <tr>
                <th className="px-4 py-3">Provider</th>
                <th className="px-4 py-3">Masked Secret Key</th>
                <th className="px-4 py-3">Assigned Model</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Requests Today</th>
                <th className="px-4 py-3">Success Rate</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {keys.map((k) => (
                <tr key={k.id} className="hover:bg-[#15201C]/50 transition-colors">
                  <td className="px-4 py-3 font-semibold text-foreground">{k.provider}</td>
                  <td className="px-4 py-3 font-mono text-muted-foreground">{k.maskedKey}</td>
                  <td className="px-4 py-3 font-mono text-[11px] text-primary">{k.model}</td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-primary/10 text-primary border border-primary/20">
                      {k.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 font-medium text-foreground">{k.requestsToday}</td>
                  <td className="px-4 py-3 text-primary font-medium">{k.successRate}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
