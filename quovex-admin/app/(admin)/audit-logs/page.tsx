'use client';

import { useEffect, useState } from 'react';
import { ScrollText, ShieldCheck, Search, Filter } from 'lucide-react';
import EmptyState from '@/components/EmptyState';

export default function AuditLogsPage() {
  const [logs, setLogs] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionFilter, setActionFilter] = useState('');

  const fetchLogs = () => {
    setLoading(true);
    fetch(`/api/audit-logs?action=${encodeURIComponent(actionFilter)}`)
      .then((res) => res.json())
      .then((data) => {
        if (data.success) setLogs(data.logs);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchLogs();
  }, [actionFilter]);

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Security & Operational Audit Trail</h1>
          <p className="text-sm text-muted-foreground">Immutable, append-only security logs for administrative actions, publications, and security events.</p>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="flex items-center gap-3">
        <select
          value={actionFilter}
          onChange={(e) => setActionFilter(e.target.value)}
          className="px-3 py-2 rounded-lg bg-[#111917] border border-border text-xs text-foreground focus:outline-none focus:border-primary"
        >
          <option value="">All Audit Actions</option>
          <option value="LOGIN">LOGIN</option>
          <option value="USER_SUSPEND">USER_SUSPEND</option>
          <option value="USER_RESTORE">USER_RESTORE</option>
          <option value="FLAG_CHANGE">FLAG_CHANGE</option>
          <option value="CONTENT_APPROVE">CONTENT_APPROVE</option>
          <option value="CONTENT_PUBLISH">CONTENT_PUBLISH</option>
          <option value="CONTENT_UNPUBLISH">CONTENT_UNPUBLISH</option>
          <option value="MODERATION_ACTION">MODERATION_ACTION</option>
          <option value="NOTIFICATION_SEND">NOTIFICATION_SEND</option>
        </select>
      </div>

      {logs.length === 0 ? (
        <EmptyState
          icon={ScrollText}
          title="No Audit Logs Recorded Yet"
          description="Security and operational actions taken by administrators will appear here in chronological order."
        />
      ) : (
        <div className="rounded-xl bg-[#111917] border border-border overflow-hidden">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#15201C] text-muted-foreground font-semibold border-b border-border">
              <tr>
                <th className="px-4 py-3">Timestamp</th>
                <th className="px-4 py-3">Actor / Role</th>
                <th className="px-4 py-3">Action</th>
                <th className="px-4 py-3">Target</th>
                <th className="px-4 py-3">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {logs.map((log) => (
                <tr key={log.id} className="hover:bg-[#15201C]/50 transition-colors font-mono">
                  <td className="px-4 py-3 text-muted-foreground text-[11px]">
                    {new Date(log.timestamp).toLocaleTimeString()}
                  </td>
                  <td className="px-4 py-3">
                    <div className="text-foreground font-sans font-medium">{log.actorEmail}</div>
                    <div className="text-[10px] text-primary">{log.actorRole}</div>
                  </td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-0.5 rounded bg-primary/10 text-primary border border-primary/20 text-[10px] font-semibold">
                      {log.action}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-foreground font-sans">{log.targetType} [{log.targetId}]</td>
                  <td className="px-4 py-3 text-muted-foreground font-sans max-w-md">{log.details}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
