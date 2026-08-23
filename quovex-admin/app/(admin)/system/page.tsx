'use client';

import { useEffect, useState } from 'react';
import { Activity, CheckCircle2, AlertTriangle, XCircle, RefreshCw, ShieldCheck } from 'lucide-react';

export default function SystemHealthPage() {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  const fetchHealth = () => {
    setLoading(true);
    fetch('/api/system/health')
      .then((res) => res.json())
      .then((d) => {
        if (d.success) setData(d);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">System Health & Telemetry</h1>
          <p className="text-sm text-muted-foreground">Real-time status for Firebase Core, Cloud Functions, AI Gateway, Worker Pipelines, and Proxies.</p>
        </div>
        <button
          onClick={fetchHealth}
          className="flex items-center gap-2 px-3.5 py-2 rounded-lg bg-secondary text-secondary-foreground font-medium text-xs hover:bg-secondary/80 border border-border transition-colors"
        >
          <RefreshCw className="w-3.5 h-3.5" />
          <span>Ping All Services</span>
        </button>
      </div>

      {/* Overall Health Card */}
      <div className="p-6 rounded-xl bg-[#111917] border border-border flex items-center justify-between">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-primary/20 border border-primary/40 flex items-center justify-center text-primary">
            <Activity className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Infrastructure Status</div>
            <div className="text-xl font-bold text-foreground flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-primary animate-pulse" />
              <span>All Systems Operational</span>
            </div>
          </div>
        </div>
        <div className="text-right text-xs text-muted-foreground font-mono">
          Last Check: {data?.timestamp ? new Date(data.timestamp).toLocaleTimeString() : 'Just now'}
        </div>
      </div>

      {/* Service Health Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {data?.services?.map((s: any) => (
          <div key={s.serviceName} className="p-5 rounded-xl bg-[#111917] border border-border space-y-3 text-xs">
            <div className="flex items-center justify-between">
              <span className="font-bold text-foreground text-sm">{s.serviceName}</span>
              <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-primary/10 text-primary border border-primary/20">
                {s.status}
              </span>
            </div>
            <p className="text-muted-foreground">{s.message}</p>
            <div className="flex items-center justify-between pt-2 border-t border-border/50 text-[11px] text-muted-foreground">
              <span>Category: <strong>{s.category}</strong></span>
              <span className="font-mono text-primary">{s.latencyMs}ms latency</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
