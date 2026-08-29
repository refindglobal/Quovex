'use client';

import React, { useState, useEffect, useRef } from 'react';
import Image from 'next/image';
import {
  Play,
  Pause,
  RotateCcw,
  Volume2,
  VolumeX,
  Sparkles,
  Shield,
  Clock,
  Timer,
  Settings2,
  CheckCircle2,
  Sliders,
  AlertCircle,
  Square,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import { saveUserSession } from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { CircularTimerArc } from '@/components/timer/CircularTimerArc';
import { CustomDurationModal } from '@/components/timer/CustomDurationModal';
import { SessionSummaryModal } from '@/components/timer/SessionSummaryModal';
import { WebFocusShield } from '@/components/timer/WebFocusShield';
import { ASSETS } from '@/lib/assets';

const PRESETS = [
  { id: 'pomodoro', name: 'Pomodoro', focusMinutes: 25, breakMinutes: 5, description: '25/5 interval' },
  { id: 'deep_work', name: 'Deep Work', focusMinutes: 50, breakMinutes: 10, description: '50m grind' },
  { id: 'long_deep_work', name: 'Marathon', focusMinutes: 90, breakMinutes: 15, description: '90m focus' },
  { id: 'custom', name: 'Custom', focusMinutes: 30, breakMinutes: 5, description: 'User-configured' },
];

const SOUNDSCAPES = [
  { id: 'none', title: 'Silence', icon: ASSETS.icons3d.stopwatch, freq: 0 },
  { id: 'rain', title: 'Forest Rain', icon: ASSETS.icons3d.soundscapeRain, freq: 174 },
  { id: 'cafe', title: 'Cafe Vibes', icon: ASSETS.icons3d.soundscapeCoffee, freq: 285 },
  { id: 'clock', title: 'Focus Clock', icon: ASSETS.icons3d.soundscapeClock, freq: 432 },
  { id: 'fire', title: 'Fireplace', icon: ASSETS.icons3d.flameBurning, freq: 528 },
];

const SUBJECTS = ['Physics', 'Chemistry', 'Mathematics', 'Biology', 'General Study', 'Revision'];

export default function TimerPage() {
  const [screenState, setScreenState] = useState<'SETUP' | 'ACTIVE' | 'SUMMARY'>('SETUP');
  
  // Timer Configuration
  const [selectedPresetId, setSelectedPresetId] = useState('pomodoro');
  const [focusDurationMinutes, setFocusDurationMinutes] = useState(25);
  const [breakDurationMinutes, setBreakDurationMinutes] = useState(5);
  const [selectedSubject, setSelectedSubject] = useState('Physics');
  const [isCustomModalOpen, setIsCustomModalOpen] = useState(false);

  // Active Session State
  const [remainingSeconds, setRemainingSeconds] = useState(25 * 60);
  const [totalSessionSeconds, setTotalSessionSeconds] = useState(25 * 60);
  const [isRunning, setIsRunning] = useState(false);
  const [distractionsCount, setDistractionsCount] = useState(0);
  const [sessionStartTime, setSessionStartTime] = useState<number | null>(null);

  // Soundscape State
  const [selectedSoundscape, setSelectedSoundscape] = useState('rain');
  const [soundscapeVolume, setSoundscapeVolume] = useState(0.5);
  const [isSoundMuted, setIsSoundMuted] = useState(false);

  // Summary State
  const [completedSummary, setCompletedSummary] = useState<{
    durationMinutes: number;
    subject: string;
    focusScore: number;
    xpEarned: number;
  } | null>(null);

  const currentUser = getCurrentUser();
  const workerRef = useRef<Worker | null>(null);
  const audioContextRef = useRef<AudioContext | null>(null);
  const oscillatorNodeRef = useRef<OscillatorNode | null>(null);
  const gainNodeRef = useRef<GainNode | null>(null);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      try {
        const worker = new Worker('/workers/timerWorker.js');
        worker.onmessage = (e) => {
          if (e.data.type === 'TICK') {
            setRemainingSeconds((prev) => {
              if (prev <= 1) {
                handleSessionComplete();
                return 0;
              }
              return prev - 1;
            });
          }
        };
        workerRef.current = worker;
      } catch (_) {}
    }

    return () => {
      workerRef.current?.terminate();
      stopSoundscape();
    };
  }, []);

  const startSoundscape = (soundId: string) => {
    stopSoundscape();
    if (soundId === 'none' || isSoundMuted) return;

    const preset = SOUNDSCAPES.find((s) => s.id === soundId);
    if (!preset || preset.freq === 0) return;

    try {
      const AudioCtx = window.AudioContext || (window as any).webkitAudioContext;
      const ctx = new AudioCtx();
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(preset.freq, ctx.currentTime);
      gain.gain.setValueAtTime(soundscapeVolume * 0.05, ctx.currentTime);

      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start();

      audioContextRef.current = ctx;
      oscillatorNodeRef.current = osc;
      gainNodeRef.current = gain;
    } catch (_) {}
  };

  const stopSoundscape = () => {
    try {
      oscillatorNodeRef.current?.stop();
      audioContextRef.current?.close();
    } catch (_) {}
    oscillatorNodeRef.current = null;
    audioContextRef.current = null;
    gainNodeRef.current = null;
  };

  const handleSelectPreset = (preset: typeof PRESETS[0]) => {
    setSelectedPresetId(preset.id);
    if (preset.id === 'custom') {
      setIsCustomModalOpen(true);
    } else {
      setFocusDurationMinutes(preset.focusMinutes);
      setBreakDurationMinutes(preset.breakMinutes);
      setRemainingSeconds(preset.focusMinutes * 60);
      setTotalSessionSeconds(preset.focusMinutes * 60);
    }
  };

  const handleCustomConfirm = (focusMins: number, breakMins: number) => {
    setFocusDurationMinutes(focusMins);
    setBreakDurationMinutes(breakMins);
    setRemainingSeconds(focusMins * 60);
    setTotalSessionSeconds(focusMins * 60);
    setIsCustomModalOpen(false);
  };

  const handleStartSession = () => {
    setScreenState('ACTIVE');
    setIsRunning(true);
    setDistractionsCount(0);
    setSessionStartTime(Date.now());
    workerRef.current?.postMessage({ action: 'START' });
    startSoundscape(selectedSoundscape);
  };

  const handlePauseSession = () => {
    setIsRunning(false);
    workerRef.current?.postMessage({ action: 'PAUSE' });
    stopSoundscape();
  };

  const handleResumeSession = () => {
    setIsRunning(true);
    workerRef.current?.postMessage({ action: 'START' });
    startSoundscape(selectedSoundscape);
  };

  const handleCancelSession = () => {
    if (confirm('Cancel this focus session early?')) {
      handlePauseSession();
      setScreenState('SETUP');
      setRemainingSeconds(focusDurationMinutes * 60);
    }
  };

  const handleSessionComplete = async () => {
    handlePauseSession();

    const elapsedMinutes = Math.max(1, Math.round((totalSessionSeconds - remainingSeconds) / 60));
    const score = Math.max(50, Math.min(100, 100 - (distractionsCount * 5)));
    const baseBonus = elapsedMinutes * 2;
    const focusBonus = score >= 85 ? 50 : 0;
    const totalXp = baseBonus + focusBonus;

    const summary = {
      durationMinutes: elapsedMinutes,
      subject: selectedSubject,
      focusScore: score,
      xpEarned: totalXp,
    };

    setCompletedSummary(summary);
    setScreenState('SUMMARY');

    if (currentUser) {
      await saveUserSession(currentUser.uid, {
        id: `sess_${Date.now()}`,
        startTime: sessionStartTime || Date.now() - (elapsedMinutes * 60000),
        endTime: Date.now(),
        durationMinutes: elapsedMinutes,
        focusScore: score,
        subject: selectedSubject,
        isCompleted: true,
      });
    }
  };

  const formattedTime = `${Math.floor(remainingSeconds / 60)
    .toString()
    .padStart(2, '0')}:${(remainingSeconds % 60).toString().padStart(2, '0')}`;

  const progress = totalSessionSeconds > 0 ? (totalSessionSeconds - remainingSeconds) / totalSessionSeconds : 0;

  return (
    <div className="max-w-4xl mx-auto space-y-6 pb-20">
      {/* ── SCREEN 1: TIMER SETUP ─────────────────────────────────────────── */}
      {screenState === 'SETUP' && (
        <div className="space-y-5 animate-in fade-in">
          <div>
            <h1 className="text-xl sm:text-2xl font-black text-text-primary flex items-center gap-2.5">
              <Timer className="w-7 h-7 text-primary" />
              Focus Engine
            </h1>
            <p className="text-xs sm:text-sm text-text-secondary mt-1">
              Select academic stream, focus interval, and ambient soundscape.
            </p>
          </div>

          {/* Subject Selector */}
          <QuovexCard className="p-4 sm:p-5 space-y-3 shadow-sm">
            <h3 className="text-xs font-bold text-text-secondary uppercase tracking-wider">
              1. Select Subject Stream
            </h3>
            <div className="flex flex-wrap gap-2">
              {SUBJECTS.map((subj) => (
                <button
                  key={subj}
                  onClick={() => setSelectedSubject(subj)}
                  className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all ${
                    selectedSubject === subj
                      ? 'bg-primary-container text-primary border border-primary shadow-xs'
                      : 'bg-surface-variant text-text-secondary border border-border hover:text-text-primary'
                  }`}
                >
                  {subj}
                </button>
              ))}
            </div>
          </QuovexCard>

          {/* Preset Selector */}
          <QuovexCard className="p-4 sm:p-5 space-y-3 shadow-sm">
            <h3 className="text-xs font-bold text-text-secondary uppercase tracking-wider">
              2. Focus Interval Preset
            </h3>
            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
              {PRESETS.map((p) => (
                <button
                  key={p.id}
                  onClick={() => handleSelectPreset(p)}
                  className={`p-3.5 rounded-xl border text-left transition-all ${
                    selectedPresetId === p.id
                      ? 'bg-primary-container border-primary text-primary font-bold shadow-xs'
                      : 'bg-surface-variant border-border hover:border-primary/40'
                  }`}
                >
                  <span className={`text-xs sm:text-sm block ${selectedPresetId === p.id ? 'text-primary font-bold' : 'text-text-primary'}`}>
                    {p.name}
                  </span>
                  <span className="text-[11px] text-text-secondary block mt-1">
                    {p.focusMinutes}m Focus • {p.breakMinutes}m
                  </span>
                </button>
              ))}
            </div>
          </QuovexCard>

          {/* Ambient Soundscapes */}
          <QuovexCard className="p-4 sm:p-5 space-y-4 shadow-sm">
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-bold text-text-secondary uppercase tracking-wider">
                3. Ambient Soundscape
              </h3>
              <div className="flex items-center gap-2">
                <span className="text-[10px] text-text-secondary font-bold">VOL</span>
                <input
                  type="range"
                  min={0}
                  max={1}
                  step={0.05}
                  value={soundscapeVolume}
                  onChange={(e) => setSoundscapeVolume(Number(e.target.value))}
                  className="w-24 accent-primary bg-surface-variant cursor-pointer h-1.5 rounded-lg"
                />
              </div>
            </div>

            <div className="grid grid-cols-3 sm:grid-cols-5 gap-2.5">
              {SOUNDSCAPES.map((snd) => (
                <button
                  key={snd.id}
                  onClick={() => setSelectedSoundscape(snd.id)}
                  className={`p-2.5 rounded-xl border flex flex-col items-center gap-1.5 text-center transition-all ${
                    selectedSoundscape === snd.id
                      ? 'bg-primary-container border-primary shadow-xs'
                      : 'bg-surface-variant border-border hover:border-primary/40'
                  }`}
                >
                  <div className="w-8 h-8 relative">
                    <Image
                      src={snd.icon}
                      alt={snd.title}
                      fill
                      className="object-contain"
                      unoptimized
                    />
                  </div>
                  <span className={`text-[10px] ${selectedSoundscape === snd.id ? 'text-primary font-bold' : 'text-text-secondary'}`}>
                    {snd.title}
                  </span>
                </button>
              ))}
            </div>
          </QuovexCard>

          {/* Start CTA */}
          <QuovexButton
            variant="primary"
            size="lg"
            className="w-full py-3.5 text-sm sm:text-base font-bold shadow-glow"
            onClick={handleStartSession}
            leftIcon={<Play className="w-5 h-5 fill-current" />}
          >
            Start Focus Session ({focusDurationMinutes} Minutes)
          </QuovexButton>
        </div>
      )}

      {/* ── SCREEN 2: ACTIVE TIMER ────────────────────────────────────────── */}
      {screenState === 'ACTIVE' && (
        <div className="space-y-6 animate-in zoom-in-95 pt-4">
          <div className="flex items-center justify-between">
            <QuovexBadge variant="emerald" size="md">
              {selectedSubject.toUpperCase()} FOCUS
            </QuovexBadge>
            <QuovexBadge variant="muted" size="sm">
              {selectedPresetId.toUpperCase()}
            </QuovexBadge>
          </div>

          <CircularTimerArc
            progress={progress}
            formattedTime={formattedTime}
            subject={selectedSubject}
            isPlaying={isRunning}
          />

          <div className="flex items-center justify-center gap-4">
            {isRunning ? (
              <QuovexButton
                variant="secondary"
                size="md"
                onClick={handlePauseSession}
                leftIcon={<Pause className="w-4 h-4" />}
              >
                Pause
              </QuovexButton>
            ) : (
              <QuovexButton
                variant="primary"
                size="md"
                onClick={handleResumeSession}
                leftIcon={<Play className="w-4 h-4 fill-current" />}
              >
                Resume
              </QuovexButton>
            )}

            <QuovexButton
              variant="danger"
              size="md"
              onClick={handleCancelSession}
              leftIcon={<Square className="w-4 h-4 fill-current" />}
            >
              End Early
            </QuovexButton>
          </div>

          <WebFocusShield
            isActive={isRunning}
            onDistractionDetected={(count) => setDistractionsCount(count)}
          />
        </div>
      )}

      {/* ── SCREEN 3: SUMMARY MODAL ───────────────────────────────────────── */}
      {screenState === 'SUMMARY' && completedSummary && (
        <SessionSummaryModal
          durationMinutes={completedSummary.durationMinutes}
          subject={completedSummary.subject}
          focusScore={completedSummary.focusScore}
          xpEarned={completedSummary.xpEarned}
          onDismiss={() => {
            setScreenState('SETUP');
            setRemainingSeconds(focusDurationMinutes * 60);
          }}
        />
      )}

      {isCustomModalOpen && (
        <CustomDurationModal
          initialFocusMinutes={focusDurationMinutes}
          initialBreakMinutes={breakDurationMinutes}
          onConfirm={handleCustomConfirm}
          onClose={() => setIsCustomModalOpen(false)}
        />
      )}
    </div>
  );
}
