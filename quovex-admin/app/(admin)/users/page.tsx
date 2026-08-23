'use client';

import { useEffect, useState } from 'react';
import { Search, Users, ShieldAlert, CheckCircle2, UserX, UserCheck, Filter, Clock, BookOpen } from 'lucide-react';
import EmptyState from '@/components/EmptyState';

export default function UsersPage() {
  const [users, setUsers] = useState<any[]>([]);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [selectedUser, setSelectedUser] = useState<any | null>(null);

  const fetchUsers = () => {
    setLoading(true);
    fetch(`/api/users?q=${encodeURIComponent(search)}`)
      .then((res) => res.json())
      .then((data) => {
        if (data.success) setUsers(data.users);
        setLoading(false);
      })
      .catch(() => setLoading(false));
  };

  useEffect(() => {
    fetchUsers();
  }, [search]);

  const handleAction = async (uid: string, action: 'SUSPEND' | 'RESTORE') => {
    try {
      const res = await fetch(`/api/users/${uid}/suspend`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action, reason: 'Admin dashboard manual action' }),
      });
      if (res.ok) {
        fetchUsers();
        if (selectedUser?.uid === uid) {
          setSelectedUser(null);
        }
      }
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">User Management</h1>
          <p className="text-sm text-muted-foreground">Search and manage registered students with strict privacy boundaries (zero private chat exposure).</p>
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="flex items-center gap-4">
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 text-muted-foreground absolute left-3 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search students by email, name, or UID..."
            className="w-full pl-9 pr-4 py-2.5 rounded-lg bg-[#111917] border border-border text-sm text-foreground placeholder:text-muted-foreground/50 focus:outline-none focus:border-primary transition-colors"
          />
        </div>
      </div>

      {/* Users Table / Empty State */}
      {users.length === 0 ? (
        <EmptyState
          icon={Users}
          title="No Registered Students Found"
          description={search ? `No student accounts match the query "${search}".` : "No registered student profiles exist in the system yet. User records will appear automatically upon student registration."}
        />
      ) : (
        <div className="rounded-xl bg-[#111917] border border-border overflow-hidden">
          <table className="w-full text-left text-xs">
            <thead className="bg-[#15201C] text-muted-foreground font-semibold border-b border-border">
              <tr>
                <th className="px-4 py-3">Student / Email</th>
                <th className="px-4 py-3">Target Exam / Class</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Study Minutes</th>
                <th className="px-4 py-3">Registered At</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {users.map((user) => (
                <tr key={user.uid} className="hover:bg-[#15201C]/50 transition-colors">
                  <td className="px-4 py-3">
                    <div className="font-semibold text-foreground">{user.displayName || 'Unnamed Student'}</div>
                    <div className="text-[11px] text-muted-foreground font-mono">{user.email}</div>
                  </td>
                  <td className="px-4 py-3">
                    <span className="px-2 py-0.5 rounded bg-muted/40 text-foreground border border-border text-[10px]">
                      {user.examTarget || 'General'} • {user.gradeClass || 'Class 11'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-semibold uppercase border ${
                        user.status === 'ACTIVE'
                          ? 'bg-primary/10 text-primary border-primary/20'
                          : 'bg-destructive/10 text-destructive border-destructive/20'
                      }`}
                    >
                      {user.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-foreground font-medium">
                    {user.studyMinutesTotal || 0} mins ({user.materialsCount || 0} materials)
                  </td>
                  <td className="px-4 py-3 text-muted-foreground text-[11px]">
                    {new Date(user.createdAt).toLocaleDateString()}
                  </td>
                  <td className="px-4 py-3 text-right space-x-2">
                    {user.status === 'ACTIVE' ? (
                      <button
                        onClick={() => handleAction(user.uid, 'SUSPEND')}
                        className="px-2.5 py-1 rounded bg-destructive/10 text-destructive border border-destructive/30 hover:bg-destructive/20 transition-colors text-[11px] font-medium"
                      >
                        Suspend
                      </button>
                    ) : (
                      <button
                        onClick={() => handleAction(user.uid, 'RESTORE')}
                        className="px-2.5 py-1 rounded bg-primary/10 text-primary border border-primary/30 hover:bg-primary/20 transition-colors text-[11px] font-medium"
                      >
                        Restore
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
