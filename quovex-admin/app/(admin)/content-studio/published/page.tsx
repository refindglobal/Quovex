'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Header from '@/components/Header';
import EmptyState from '@/components/EmptyState';
import { QuovexOriginalBook } from '@/lib/types/content-studio';
import {
  BookOpen,
  Sparkles,
  Eye,
  FileEdit,
  ShieldCheck,
  Archive,
  EyeOff,
  Filter,
} from 'lucide-react';

export default function PublishedCatalogPage() {
  const [books, setBooks] = useState<QuovexOriginalBook[]>([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<'ALL' | 'PRODUCTION' | 'STAGING'>('ALL');
  const [actionMessage, setActionMessage] = useState<string | null>(null);

  const fetchPublished = async () => {
    try {
      const res = await fetch('/api/content-studio/books?status=PUBLISHED');
      const data = await res.json();
      if (data.success) {
        setBooks(data.books || []);
      }
    } catch (err) {
      console.error('Failed to load published catalog:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPublished();
  }, []);

  const handleUnpublish = async (bookId: string) => {
    try {
      const res = await fetch('/api/content-studio/publish', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'UNPUBLISH', bookId }),
      });
      const data = await res.json();
      if (data.success) {
        setActionMessage('✓ Book unpublished and removed from public mobile catalog.');
        await fetchPublished();
      }
    } catch (err: any) {
      setActionMessage(`Error: ${err.message}`);
    }
  };

  const handleArchive = async (bookId: string) => {
    try {
      const res = await fetch('/api/content-studio/publish', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'ARCHIVE', bookId }),
      });
      const data = await res.json();
      if (data.success) {
        setActionMessage('✓ Book archived.');
        await fetchPublished();
      }
    } catch (err: any) {
      setActionMessage(`Error: ${err.message}`);
    }
  };

  const filteredBooks = books.filter((b) => {
    if (tab === 'PRODUCTION') return !b.isStaging;
    if (tab === 'STAGING') return b.isStaging;
    return true;
  });

  return (
    <div className="flex-1 flex flex-col min-h-0">
      <Header
        title="Published Quovex Originals"
        description="Public Educational Catalog Serving Android & Web Mobile Knowledge Hub"
      />

      <div className="p-8 space-y-6 flex-1 flex flex-col min-h-0">
        {/* Controls Bar */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <button
              onClick={() => setTab('ALL')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                tab === 'ALL'
                  ? 'bg-primary text-primary-foreground font-semibold'
                  : 'text-muted-foreground hover:text-white'
              }`}
            >
              All Published ({books.length})
            </button>
            <button
              onClick={() => setTab('PRODUCTION')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                tab === 'PRODUCTION'
                  ? 'bg-primary text-primary-foreground font-semibold'
                  : 'text-muted-foreground hover:text-white'
              }`}
            >
              Live Production ({books.filter((b) => !b.isStaging).length})
            </button>
            <button
              onClick={() => setTab('STAGING')}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-colors ${
                tab === 'STAGING'
                  ? 'bg-primary text-primary-foreground font-semibold'
                  : 'text-muted-foreground hover:text-white'
              }`}
            >
              Test / Staging ({books.filter((b) => b.isStaging).length})
            </button>
          </div>

          <div className="flex items-center gap-2 text-[11px] text-muted-foreground bg-[#121815] border border-border px-3 py-1.5 rounded-lg">
            <ShieldCheck className="w-3.5 h-3.5 text-primary" />
            <span>Public contract returns only approved & published entries</span>
          </div>
        </div>

        {actionMessage && (
          <div className="p-3 rounded-lg bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 text-xs">
            {actionMessage}
          </div>
        )}

        {loading ? (
          <div className="flex-1 flex items-center justify-center p-12 text-muted-foreground text-xs">
            Loading published catalog...
          </div>
        ) : filteredBooks.length === 0 ? (
          <EmptyState
            icon={BookOpen}
            title="No Published Originals Yet"
            description="When human editors approve manuscripts and sign off on publication, they will be listed here and accessible to students."
            action={
              <Link
                href="/content-studio/requests/new"
                className="px-4 py-2 rounded-lg bg-primary text-primary-foreground font-medium text-xs hover:bg-primary/90 transition-colors"
              >
                Create Book Request
              </Link>
            }
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredBooks.map((book) => (
              <div
                key={book.id}
                className="p-6 rounded-xl bg-card border border-border flex flex-col justify-between space-y-4 hover:border-primary/40 transition-colors"
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="px-2 py-0.5 rounded bg-border text-[10px] font-semibold text-muted-foreground">
                      {book.subject}
                    </span>
                    <span
                      className={`px-2 py-0.5 rounded text-[10px] font-semibold ${
                        book.isStaging
                          ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                          : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      }`}
                    >
                      {book.isStaging ? 'STAGING' : 'LIVE PRODUCTION'}
                    </span>
                  </div>

                  <h3 className="text-sm font-semibold text-white">{book.title}</h3>
                  <p className="text-xs text-muted-foreground line-clamp-2">{book.description}</p>

                  <div className="pt-2 border-t border-border/60 text-[11px] text-muted-foreground space-y-1">
                    <div>
                      Curriculum: <span className="text-foreground">{book.curriculum} — {book.gradeClass}</span>
                    </div>
                    <div>
                      Chapters: <span className="text-foreground">{book.chapters?.length || 0}</span> • Reading Time: <span className="text-foreground">{book.targetReadingTimeMinutes} mins</span>
                    </div>
                    <div>
                      Approved By: <span className="text-foreground">{book.approvedBy}</span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 pt-2">
                  <Link
                    href={`/content-studio/books/${book.id}`}
                    className="flex-1 flex items-center justify-center gap-1.5 py-2 rounded-lg bg-[#151D19] hover:bg-[#1A231F] text-foreground text-xs font-medium border border-border transition-colors"
                  >
                    <FileEdit className="w-3.5 h-3.5" />
                    <span>View Book</span>
                  </Link>

                  <button
                    onClick={() => handleUnpublish(book.id)}
                    className="px-3 py-2 rounded-lg bg-destructive/10 hover:bg-destructive/20 text-destructive border border-destructive/30 text-xs font-medium transition-colors"
                    title="Unpublish from mobile students"
                  >
                    <EyeOff className="w-3.5 h-3.5" />
                  </button>

                  <button
                    onClick={() => handleArchive(book.id)}
                    className="px-3 py-2 rounded-lg bg-[#121815] hover:bg-[#18201C] text-muted-foreground border border-border text-xs font-medium transition-colors"
                    title="Archive book"
                  >
                    <Archive className="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
