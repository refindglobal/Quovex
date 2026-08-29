'use client';

import React, { createContext, useContext, useState, useCallback } from 'react';
import clsx from 'clsx';
import { CheckCircle2, AlertTriangle, AlertCircle, Info, X } from 'lucide-react';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastMessage {
  id: string;
  type: ToastType;
  title: string;
  description?: string;
}

interface ToastContextValue {
  toast: (options: { type?: ToastType; title: string; description?: string; durationMs?: number }) => void;
  success: (title: string, description?: string) => void;
  error: (title: string, description?: string) => void;
  warning: (title: string, description?: string) => void;
  info: (title: string, description?: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    ({
      type = 'info',
      title,
      description,
      durationMs = 4000,
    }: {
      type?: ToastType;
      title: string;
      description?: string;
      durationMs?: number;
    }) => {
      const id = `toast_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`;
      setToasts((prev) => [...prev, { id, type, title, description }]);

      setTimeout(() => {
        removeToast(id);
      }, durationMs);
    },
    [removeToast]
  );

  const value: ToastContextValue = {
    toast: addToast,
    success: (title, description) => addToast({ type: 'success', title, description }),
    error: (title, description) => addToast({ type: 'error', title, description }),
    warning: (title, description) => addToast({ type: 'warning', title, description }),
    info: (title, description) => addToast({ type: 'info', title, description }),
  };

  const icons = {
    success: <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />,
    error: <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />,
    warning: <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0" />,
    info: <Info className="w-4 h-4 text-sky-400 shrink-0" />,
  };

  const borderStyles = {
    success: 'border-emerald-500/40 bg-[#0F1C16]',
    error: 'border-red-500/40 bg-[#1C0F0F]',
    warning: 'border-amber-500/40 bg-[#1C170F]',
    info: 'border-sky-500/40 bg-[#0F161C]',
  };

  return (
    <ToastContext.Provider value={value}>
      {children}

      {/* Floating Toast Notification Container */}
      <div className="fixed bottom-5 right-5 z-50 flex flex-col gap-2.5 max-w-sm w-full pointer-events-none">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={clsx(
              'pointer-events-auto rounded-xl border p-4 shadow-xl backdrop-blur-md flex items-start justify-between gap-3 animate-in slide-in-from-bottom-3 duration-200',
              borderStyles[t.type]
            )}
          >
            <div className="flex items-start gap-2.5">
              {icons[t.type]}
              <div>
                <h4 className="text-xs font-bold text-white">{t.title}</h4>
                {t.description && <p className="text-[11px] text-gray-300 mt-0.5">{t.description}</p>}
              </div>
            </div>

            <button
              onClick={() => removeToast(t.id)}
              className="text-gray-400 hover:text-white p-0.5 rounded"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}
