'use client';

import React from 'react';
import { Bell, Sparkles, Smartphone, CheckCheck } from 'lucide-react';

export interface PushPreviewCardProps {
  title: string;
  body: string;
  audience: string;
}

export function PushPreviewCard({ title, body, audience }: PushPreviewCardProps) {
  const displayTitle = title.trim() || 'Focus Session Reminder 🎯';
  const displayBody = body.trim() || 'Your daily 2.5 hour goal is waiting. Lock in now to protect your streak flame!';

  return (
    <div className="rounded-2xl bg-gradient-to-b from-[#15201C] to-[#0D1512] border border-emerald-900/50 p-6 flex flex-col items-center justify-between shadow-2xl relative overflow-hidden">
      <div className="w-full flex items-center justify-between pb-3 border-b border-emerald-950/60 mb-6">
        <div className="flex items-center gap-2">
          <Smartphone className="w-4 h-4 text-emerald-400" />
          <span className="text-xs font-bold text-white tracking-wide uppercase">Live Device Mockup</span>
        </div>
        <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
          Android 14 • M3 Banner
        </span>
      </div>

      {/* Smartphone Device Frame */}
      <div className="w-full max-w-[290px] rounded-[36px] bg-[#0A0F0D] border-4 border-[#22332B] shadow-2xl shadow-black/80 p-3 pt-6 pb-6 relative flex flex-col justify-between min-h-[380px]">
        {/* Notch / Speaker Camera */}
        <div className="absolute top-2.5 left-1/2 -translate-x-1/2 w-16 h-3.5 bg-[#17221D] rounded-full flex items-center justify-center">
          <div className="w-2 h-2 rounded-full bg-black/80" />
        </div>

        {/* Lock Screen Status Header */}
        <div className="flex items-center justify-between px-2 pt-2 text-[10px] text-gray-400 font-medium">
          <span>09:41</span>
          <div className="flex items-center gap-1.5">
            <span>5G</span>
            <span>100%</span>
          </div>
        </div>

        {/* Center Clock */}
        <div className="text-center my-4">
          <div className="text-3xl font-extrabold text-white tracking-tight">09:41</div>
          <div className="text-[11px] text-emerald-400/80 font-medium">Wednesday, August 26</div>
        </div>

        {/* Push Notification Card Popup */}
        <div className="rounded-2xl bg-[#17231E]/95 backdrop-blur-md border border-emerald-500/30 p-3.5 shadow-xl shadow-black/60 space-y-2 animate-in fade-in slide-in-from-top-2 duration-300">
          {/* App Header */}
          <div className="flex items-center justify-between text-[11px]">
            <div className="flex items-center gap-1.5">
              <div className="w-4 h-4 rounded-md bg-[#00C896] flex items-center justify-center text-[9px] font-black text-black">
                Q
              </div>
              <span className="font-bold text-white">Quovex</span>
              <span className="text-gray-400 text-[10px]">• now</span>
            </div>
            <Bell className="w-3 h-3 text-emerald-400" />
          </div>

          {/* Title & Body */}
          <div className="space-y-0.5">
            <h4 className="text-xs font-bold text-white leading-snug line-clamp-1">{displayTitle}</h4>
            <p className="text-[11px] text-gray-300 leading-relaxed line-clamp-3">{displayBody}</p>
          </div>

          {/* Quick Action Button */}
          <div className="pt-1.5 flex gap-2">
            <button className="flex-1 py-1 rounded-lg bg-[#00C896]/20 text-[#00C896] border border-[#00C896]/30 text-[10px] font-bold">
              Lock In Now
            </button>
            <button className="px-2 py-1 rounded-lg bg-white/5 text-gray-400 text-[10px] font-medium">
              Snooze
            </button>
          </div>
        </div>

        {/* Home Indicator */}
        <div className="w-24 h-1 bg-gray-600 rounded-full mx-auto mt-6" />
      </div>

      {/* Audience Tag Footer */}
      <div className="mt-5 w-full text-center text-xs text-gray-400 flex items-center justify-center gap-2">
        <CheckCheck className="w-3.5 h-3.5 text-emerald-400" />
        <span>Target: <strong className="text-white">{audience.replace('_', ' ')}</strong></span>
      </div>
    </div>
  );
}
