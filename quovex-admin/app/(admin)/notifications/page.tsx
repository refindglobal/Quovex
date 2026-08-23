'use client';

import { useEffect, useState } from 'react';
import { Bell, Send, CheckCircle2, Users, AlertCircle, Clock } from 'lucide-react';
import EmptyState from '@/components/EmptyState';

export default function NotificationsPage() {
  const [campaigns, setCampaigns] = useState<any[]>([]);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [audience, setAudience] = useState('ALL_USERS');
  const [targetValue, setTargetValue] = useState('');
  const [sending, setSending] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const fetchCampaigns = () => {
    fetch('/api/notifications/history')
      .then((res) => res.json())
      .then((d) => {
        if (d.success) setCampaigns(d.campaigns);
      })
      .catch(() => {});
  };

  useEffect(() => {
    fetchCampaigns();
  }, []);

  const handleSend = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title || !body) return;

    setSending(true);
    setSuccessMsg(null);

    try {
      const res = await fetch('/api/notifications/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title,
          body,
          targetAudience: audience,
          targetValue: targetValue || undefined,
        }),
      });

      const data = await res.json();
      if (res.ok) {
        setTitle('');
        setBody('');
        setSuccessMsg(`Notification campaign "${data.campaign.title}" successfully dispatched!`);
        fetchCampaigns();
      }
    } catch (err) {
      console.error(err);
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Push Notification Center</h1>
          <p className="text-sm text-muted-foreground">Broadcast motivational reminders, exam tips, and new content alerts to students (FCM Integration).</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Composer Form */}
        <div className="lg:col-span-1 p-6 rounded-xl bg-[#111917] border border-border space-y-4">
          <div className="flex items-center gap-2">
            <Bell className="w-4 h-4 text-primary" />
            <h2 className="text-sm font-bold text-foreground">Compose Campaign</h2>
          </div>

          {successMsg && (
            <div className="p-3 rounded-lg bg-primary/10 border border-primary/30 text-xs text-primary flex items-center gap-2">
              <CheckCircle2 className="w-4 h-4 shrink-0" />
              <span>{successMsg}</span>
            </div>
          )}

          <form onSubmit={handleSend} className="space-y-4 text-xs">
            <div className="space-y-1.5">
              <label className="font-semibold text-muted-foreground uppercase tracking-wider">Target Audience</label>
              <select
                value={audience}
                onChange={(e) => setAudience(e.target.value)}
                className="w-full px-3 py-2 rounded-lg bg-[#15201C] border border-border text-foreground focus:outline-none focus:border-primary"
              >
                <option value="ALL_USERS">All Students</option>
                <option value="CLASS_SPECIFIC">Class Specific (9, 10, 11, 12)</option>
                <option value="EXAM_SPECIFIC">Exam Specific (JEE, NEET, CBSE)</option>
                <option value="INACTIVE_7D">Inactive (7+ Days)</option>
              </select>
            </div>

            {audience === 'CLASS_SPECIFIC' || audience === 'EXAM_SPECIFIC' ? (
              <div className="space-y-1.5">
                <label className="font-semibold text-muted-foreground uppercase tracking-wider">Filter Value</label>
                <input
                  type="text"
                  value={targetValue}
                  onChange={(e) => setTargetValue(e.target.value)}
                  placeholder={audience === 'CLASS_SPECIFIC' ? 'Class 11' : 'JEE Main'}
                  className="w-full px-3 py-2 rounded-lg bg-[#15201C] border border-border text-foreground focus:outline-none focus:border-primary"
                />
              </div>
            ) : null}

            <div className="space-y-1.5">
              <label className="font-semibold text-muted-foreground uppercase tracking-wider">Notification Title</label>
              <input
                type="text"
                required
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="🔥 Today's Physics Quiz is Live!"
                className="w-full px-3 py-2 rounded-lg bg-[#15201C] border border-border text-foreground focus:outline-none focus:border-primary"
              />
            </div>

            <div className="space-y-1.5">
              <label className="font-semibold text-muted-foreground uppercase tracking-wider">Notification Message</label>
              <textarea
                required
                rows={4}
                value={body}
                onChange={(e) => setBody(e.target.value)}
                placeholder="Master Free Body Diagrams in 10 minutes with our new Quovex Original."
                className="w-full px-3 py-2 rounded-lg bg-[#15201C] border border-border text-foreground focus:outline-none focus:border-primary resize-none"
              />
            </div>

            <button
              type="submit"
              disabled={sending}
              className="w-full py-2.5 rounded-lg bg-primary text-black font-semibold hover:bg-primary/90 transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
            >
              <Send className="w-3.5 h-3.5" />
              <span>{sending ? 'Dispatching...' : 'Broadcast Notification'}</span>
            </button>
          </form>
        </div>

        {/* Campaign History */}
        <div className="lg:col-span-2 p-6 rounded-xl bg-[#111917] border border-border space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-bold text-foreground">Campaign History & Delivery Logs</h2>
            <span className="text-xs text-muted-foreground">{campaigns.length} campaigns dispatched</span>
          </div>

          {campaigns.length === 0 ? (
            <EmptyState
              icon={Bell}
              title="No Notifications Dispatched Yet"
              description="Broadcast notifications sent to student devices will appear here with delivery timestamps and audience metrics."
            />
          ) : (
            <div className="space-y-2">
              {campaigns.map((c) => (
                <div key={c.id} className="p-4 rounded-lg bg-[#15201C] border border-border/80 space-y-2 text-xs">
                  <div className="flex items-center justify-between">
                    <div className="font-bold text-foreground">{c.title}</div>
                    <span className="px-2 py-0.5 rounded bg-primary/10 text-primary border border-primary/20 text-[10px] font-semibold">
                      {c.status}
                    </span>
                  </div>
                  <p className="text-muted-foreground">{c.body}</p>
                  <div className="flex items-center justify-between text-[11px] text-muted-foreground/80 pt-1 border-t border-border/50">
                    <span>Audience: <strong>{c.targetAudience}</strong></span>
                    <span>Dispatched: {new Date(c.createdAt).toLocaleTimeString()} • {c.sentBy}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
