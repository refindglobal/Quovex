'use client';

import { useEffect, useState } from 'react';
import { ShieldAlert, CheckCircle2, AlertCircle, UserX, XCircle, ShieldCheck } from 'lucide-react';
import EmptyState from '@/components/EmptyState';

export default function ModerationPage() {
  const [reports, setReports] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchReports = () => {
    setLoading(true);
    fetch('/api/moderation/reports')
      .then((res) => res.json())
      .then((data) => {
        if (data.success) setReports(data.reports);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchReports();
  }, []);

  const handleAction = async (reportId: string, action: 'DISMISS' | 'WARN' | 'SUSPEND' | 'REMOVE_CONTENT') => {
    try {
      const res = await fetch('/api/moderation/action', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reportId, action, notes: `Resolved via admin console with action: ${action}` }),
      });
      if (res.ok) {
        fetchReports();
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
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Content & User Moderation Queue</h1>
          <p className="text-sm text-muted-foreground">Review flagged study rooms, inappropriate community content, and reported user violations.</p>
        </div>
      </div>

      {reports.length === 0 ? (
        <EmptyState
          icon={ShieldCheck}
          title="Moderation Queue is Clear"
          description="Zero pending user flags or reported content incidents. All student study rooms and profiles are in good standing."
        />
      ) : (
        <div className="space-y-3">
          {reports.map((r) => (
            <div key={r.id} className="p-5 rounded-xl bg-[#111917] border border-border space-y-3 text-xs">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="font-semibold px-2 py-0.5 rounded bg-destructive/10 text-destructive border border-destructive/20 text-[10px]">
                    {r.targetType}
                  </span>
                  <span className="font-bold text-foreground">Target ID: {r.targetId}</span>
                </div>
                <span className="px-2 py-0.5 rounded bg-muted/40 text-muted-foreground border border-border text-[10px]">
                  Status: {r.status}
                </span>
              </div>
              <p className="text-muted-foreground">
                <strong className="text-foreground">Reason:</strong> {r.reason} {r.details ? `— ${r.details}` : ''}
              </p>
              <div className="flex items-center justify-between pt-2 border-t border-border/60">
                <span className="text-[11px] text-muted-foreground font-mono">
                  Reported by: {r.reportedByUid} • {new Date(r.createdAt).toLocaleTimeString()}
                </span>
                {r.status === 'PENDING' && (
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleAction(r.id, 'DISMISS')}
                      className="px-2.5 py-1 rounded bg-secondary text-secondary-foreground hover:bg-secondary/80 text-[11px] font-medium"
                    >
                      Dismiss
                    </button>
                    <button
                      onClick={() => handleAction(r.id, 'WARN')}
                      className="px-2.5 py-1 rounded bg-yellow-500/10 text-yellow-400 border border-yellow-500/30 hover:bg-yellow-500/20 text-[11px] font-medium"
                    >
                      Warn User
                    </button>
                    <button
                      onClick={() => handleAction(r.id, 'SUSPEND')}
                      className="px-2.5 py-1 rounded bg-destructive/10 text-destructive border border-destructive/30 hover:bg-destructive/20 text-[11px] font-medium"
                    >
                      Suspend User
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
