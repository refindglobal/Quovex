'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { Users, Trophy, Sparkles, Lock, Flame, Shield, Play, Plus, Clock } from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import {
  subscribeToStudyRooms,
  subscribeToWeeklyLeaderboard,
  createStudyRoom,
  StudyRoom,
  LeaderboardEntry,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { ASSETS } from '@/lib/assets';

const DEFAULT_ROOMS: StudyRoom[] = [
  { id: 'r1', name: 'JEE Advanced Deep Work Hall (Silent)', targetExam: 'JEE Advanced', activeMembers: 38, isPrivate: false, memberAvatars: [1, 3, 5, 8] },
  { id: 'r2', name: 'NEET Speed Sprint & Active Recall', targetExam: 'NEET UG', activeMembers: 29, isPrivate: false, memberAvatars: [2, 4, 7] },
  { id: 'r3', name: 'UPSC Daily Answer Writing Chamber', targetExam: 'UPSC CSE', activeMembers: 22, isPrivate: false, memberAvatars: [6, 9, 11] },
  { id: 'r4', name: 'Late Night 100% Focus Squad', targetExam: 'General Competitive', activeMembers: 45, isPrivate: true, memberAvatars: [10, 12] },
];

export default function CommunityStudyPage() {
  const [rooms, setRooms] = useState<StudyRoom[]>(DEFAULT_ROOMS);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [tab, setTab] = useState<'rooms' | 'leaderboard'>('rooms');
  const [activeRoom, setActiveRoom] = useState<StudyRoom | null>(null);
  const [isCreatingRoom, setIsCreatingRoom] = useState(false);
  const [newRoomName, setNewRoomName] = useState('');
  const [newRoomExam, setNewRoomExam] = useState('JEE Advanced');

  const currentUser = getCurrentUser();

  useEffect(() => {
    const unsubRooms = subscribeToStudyRooms((list) => {
      if (list.length > 0) setRooms(list);
    });

    const unsubLeaderboard = subscribeToWeeklyLeaderboard((entries) => {
      setLeaderboard(entries);
    });

    return () => {
      unsubRooms();
      unsubLeaderboard();
    };
  }, []);

  const handleCreateRoom = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newRoomName.trim() || !currentUser) return;

    const newRoom: StudyRoom = {
      id: `room_${Date.now()}`,
      name: newRoomName.trim(),
      targetExam: newRoomExam,
      activeMembers: 1,
      isPrivate: false,
      memberAvatars: [1],
    };

    await createStudyRoom(newRoom);
    setNewRoomName('');
    setIsCreatingRoom(false);
  };

  return (
    <div className="max-w-5xl mx-auto space-y-12 pb-24">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div>
          <h1 className="text-display font-black text-text-primary flex items-center gap-4">
            <Users className="w-10 h-10 text-primary" />
            Virtual Study Rooms & Weekly Rank
          </h1>
          <p className="text-section text-text-secondary mt-2">
            Silent accountability rooms with real-time peer timers and weekly competitive leaderboards.
          </p>
        </div>

        <QuovexButton size="lg" variant="primary" onClick={() => setIsCreatingRoom(true)} leftIcon={<Plus className="w-5 h-5" />}>
          Create Study Room
        </QuovexButton>
      </div>

      {/* Navigation Tabs */}
      <div className="flex items-center gap-4 border-b border-border pb-4">
        <button
          onClick={() => setTab('rooms')}
          className={`px-5 py-2.5 rounded-xl text-body font-bold transition-all ${
            tab === 'rooms'
              ? 'bg-primary-container text-primary border border-primary/40 shadow-sm'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          🎧 Live Study Rooms ({rooms.reduce((acc, r) => acc + (r.activeMembers || 0), 0)} Online)
        </button>
        <button
          onClick={() => setTab('leaderboard')}
          className={`px-5 py-2.5 rounded-xl text-body font-bold transition-all ${
            tab === 'leaderboard'
              ? 'bg-primary-container text-primary border border-primary/40 shadow-sm'
              : 'text-text-secondary hover:text-text-primary'
          }`}
        >
          🏆 Weekly Exam Leaderboard
        </button>
      </div>

      {tab === 'rooms' ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {rooms.map((room) => (
            <QuovexCard key={room.id} hoverEffect className="space-y-5 flex flex-col justify-between">
              <div>
                <div className="flex items-center justify-between mb-3">
                  <QuovexBadge variant="emerald" size="md">{room.targetExam}</QuovexBadge>
                  {room.isPrivate && (
                    <span className="text-label text-warning bg-warning-container border border-warning/30 px-2.5 py-1 rounded-md flex items-center gap-1.5 font-bold">
                      <Lock className="w-3 h-3" /> PRIVATE SQUAD
                    </span>
                  )}
                </div>

                <h3 className="text-title font-bold text-text-primary mt-2">{room.name}</h3>
                <p className="text-body text-primary mt-2.5 flex items-center gap-2 font-bold">
                  <span className="w-2.5 h-2.5 rounded-full bg-primary animate-ping" />
                  {room.activeMembers} Scholars currently in deep focus
                </p>

                {/* Peer Avatars */}
                {room.memberAvatars && (
                  <div className="flex items-center -space-x-3 mt-4">
                    {room.memberAvatars.slice(0, 4).map((avId, aIdx) => (
                      <div key={aIdx} className="w-10 h-10 rounded-full bg-surface border-2 border-surface overflow-hidden relative shadow-sm">
                        <Image src={ASSETS.avatars(avId)} alt="Peer" fill className="object-cover" unoptimized />
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="pt-4 border-t border-border flex items-center justify-between">
                <span className="text-label text-text-secondary font-bold">Silent Mode • Cam Optional</span>
                <QuovexButton
                  size="lg"
                  onClick={() => {
                    setActiveRoom(room);
                    alert(`Joined ${room.name}! Your timer is now synchronized with peer scholars.`);
                  }}
                  leftIcon={<Play className="w-4 h-4 fill-current" />}
                >
                  Join Room
                </QuovexButton>
              </div>
            </QuovexCard>
          ))}
        </div>
      ) : (
        /* Weekly Leaderboard Tab */
        <QuovexCard className="p-8 space-y-8 shadow-sm">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6 border-b border-border pb-5">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 relative">
                <Image src={ASSETS.icons3d.tournamentPodium} alt="Tournament Podium" fill className="object-contain" unoptimized />
              </div>
              <div>
                <h3 className="font-bold text-text-primary text-title">Top Scholars This Week</h3>
                <p className="text-body text-text-secondary mt-1">Ranked by verified focus hours & diagnostic accuracy</p>
              </div>
            </div>
            <span className="text-label text-text-secondary font-mono font-bold">Resets Sunday 23:59 IST</span>
          </div>

          <div className="space-y-4">
            {leaderboard.length > 0 ? (
              leaderboard.map((entry, idx) => (
                <div
                  key={entry.userId || idx}
                  className={`p-4 sm:p-5 rounded-2xl border flex items-center justify-between text-body transition-all ${
                    idx === 0
                      ? 'bg-warning-container border-warning/50 text-text-primary font-bold shadow-sm'
                      : 'bg-surface-variant border-border text-text-primary hover:bg-surface-elevated hover:border-primary/30'
                  }`}
                >
                  <div className="flex items-center gap-4 sm:gap-5">
                    <span
                      className={`w-8 h-8 rounded-xl flex items-center justify-center font-bold text-body font-mono shadow-sm ${
                        idx === 0
                          ? 'bg-warning text-black'
                          : idx === 1
                          ? 'bg-slate-300 text-black'
                          : idx === 2
                          ? 'bg-amber-600 text-white'
                          : 'bg-surface border border-border text-text-secondary'
                      }`}
                    >
                      {idx + 1}
                    </span>

                    <div className="w-10 h-10 sm:w-12 sm:h-12 rounded-full overflow-hidden bg-primary-container border-2 border-primary relative shadow-glow-sm">
                      <Image src={ASSETS.avatars(entry.avatarId || 1)} alt="Scholar" fill className="object-cover" unoptimized />
                    </div>

                    <div>
                      <span className="font-bold block text-text-primary text-body sm:text-title">{entry.userName}</span>
                      <span className="text-label text-text-secondary font-bold">{entry.scholarRank || 'Scholar'}</span>
                    </div>
                  </div>

                  <div className="text-right">
                    <span className="font-bold text-primary block font-mono text-body sm:text-title">{(entry.studyMinutes / 60).toFixed(1)} hrs</span>
                    <span className="text-label text-warning font-black">{entry.xp} XP</span>
                  </div>
                </div>
              ))
            ) : (
              <div className="p-12 text-center text-body text-text-secondary space-y-4">
                <Trophy className="w-12 h-12 text-warning/40 mx-auto" />
                <p>No leaderboard standings recorded for this cycle yet. Complete study sessions to claim rank #1!</p>
              </div>
            )}
          </div>
        </QuovexCard>
      )}

      {/* Create Room Modal */}
      {isCreatingRoom && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4">
          <form onSubmit={handleCreateRoom} className="bg-surface border border-border rounded-3xl max-w-md w-full p-8 space-y-6 shadow-2xl animate-in zoom-in-95">
            <div>
              <h3 className="text-headline font-black text-text-primary">Create Study Room</h3>
              <p className="text-body text-text-secondary mt-1">Host a virtual silent accountability room for peers</p>
            </div>

            <div className="space-y-5">
              <div>
                <label className="block text-label font-bold text-text-secondary mb-2">Room Name</label>
                <input
                  type="text"
                  placeholder="e.g. Organic Chemistry Speed Drill"
                  value={newRoomName}
                  onChange={(e) => setNewRoomName(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl px-4 py-3 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
                  required
                />
              </div>

              <div>
                <label className="block text-label font-bold text-text-secondary mb-2">Target Exam Stream</label>
                <select
                  value={newRoomExam}
                  onChange={(e) => setNewRoomExam(e.target.value)}
                  className="w-full bg-surface-variant border border-border rounded-xl px-4 py-3 text-body text-text-primary focus:outline-none focus:border-primary focus:shadow-glow-sm transition-all"
                >
                  <option value="JEE Advanced">JEE Advanced</option>
                  <option value="JEE Mains">JEE Mains</option>
                  <option value="NEET UG">NEET UG</option>
                  <option value="UPSC CSE">UPSC CSE</option>
                  <option value="CBSE Class 12">CBSE Class 12</option>
                  <option value="General Competitive">General Competitive</option>
                </select>
              </div>
            </div>

            <div className="flex gap-3 pt-4">
              <QuovexButton variant="secondary" size="lg" className="flex-1" onClick={() => setIsCreatingRoom(false)}>
                Cancel
              </QuovexButton>
              <QuovexButton type="submit" variant="primary" size="lg" className="flex-1">
                Launch Room
              </QuovexButton>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
