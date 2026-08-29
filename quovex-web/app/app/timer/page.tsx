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
import { saveUserSession, subscribeToUserProfile } from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { CircularTimerArc } from '@/components/timer/CircularTimerArc';
import { CustomDurationModal } from '@/components/timer/CustomDurationModal';
import { SessionSummaryModal } from '@/components/timer/SessionSummaryModal';
import { WebFocusShield } from '@/components/timer/WebFocusShield';
import { ASSETS } from '@/lib/assets';

const PRESETS = [
  { id: 'pomodoro', name: 'Pomodoro', focusMinutes: 25, breakMinutes: 5, description: 'Classic 25/5 interval' },
  { id: 'deep_work', name: 'Deep Work', focusMinutes: 50, breakMinutes: 10, description: '50m intense grind' },
  { id: 'long_deep_work', name: 'Long Deep Work', focusMinutes: 90, breakMinutes: 15, description: '90m marathon study' },
  { id: 'custom', name: 'Custom Preset', focusMinutes: 30, breakMinutes: 5, description: 'User-configured' },
];

const SOUNDSCAPES = [
  { id: 'none', title: 'Silence / None', icon: ASSETS.icons3d.stopwatch, freq: 0 },
  { id: 'rain', title: 'Forest Rain & Storm', icon: ASSETS.icons3d.soundscapeRain, freq: 174 },
  { id: 'cafe', title: 'Cafe Ambience', icon: ASSETS.icons3d.soundscapeCoffee, freq: 285 },
  { id: 'clock', title: 'Deep Work Clock', icon: ASSETS.icons3d.soundscapeClock, freq: 432 },
  { id: 'fire', title: 'Emerald Fireplace', icon: ASSETS.icons3d.flameBurning, freq: 528 },
];

const SUBJECTS = ['Physics', 'Chemistry', 'Mathematics', 'Biology', 'General Study', 'Revision'];

export default function TimerPage() {
  // Screen state: 'SETUP' | 'ACTIVE' | 'SUMMARY'
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

  // Initialize Web Worker for background ticking
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

  // Web Audio ambient soundscape engine
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
    if (confirm('Are you sure you want to cancel this focus session early?')) {
      handlePauseSession();
      setScreenState('SETUP');
      setRemainingSeconds(focusDurationMinutes * 60);
    }
  };

  const handleSessionComplete = async () => {
    handlePauseSession();

    const elapsedMinutes = Math.max(1, Math.round((totalSessionSeconds - remainingSeconds) / 60));
    // Calculate real focus score from distractions
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

    // Save session to Firestore
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
    <div className="max-w-4xl mx-auto space-y-12 pb-24">
      {/* ── SCREEN 1: TIMER SETUP ─────────────────────────────────────────── */}
      {screenState === 'SETUP' && (
        <div className="space-y-8 animate-in fade-in">
          <div>
            <h1 className="text-display text-text-primary flex items-center gap-4">
              <Timer className="w-10 h-10 text-primary" />
              Focus Engine
            </h1>
            <p className="text-section text-text-secondary mt-2">
              Select your academic subject, focus interval, and ambient binaural soundscape.
            </p>
          </div>

          {/* Subject Selector */}
          <QuovexCard className="space-y-4">
            <h3 className="text-label text-text-secondary uppercase tracking-wider">
              1. SELECT SUBJECT
            </h3>
            <div className="flex flex-wrap gap-3">
              {SUBJECTS.map((subj) => (
                <button
                  key={subj}
                  onClick={() => setSelectedSubject(subj)}
                  className={`px-5 py-2.5 rounded-xl text-body font-bold transition-all ${
                    selectedSubject === subj
                      ? 'bg-primary-container text-primary border border-primary shadow-glow'
                      : 'bg-surface-variant text-text-secondary border border-border hover:text-text-primary hover:border-primary/50'
                  }`}
                >
                  {subj}
                </button>
              ))}
            </div>
          </QuovexCard>

          {/* Preset Selector */}
          <QuovexCard className="space-y-4">
            <h3 className="text-label text-text-secondary uppercase tracking-wider">
              2. FOCUS INTERVAL PRESET
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              {PRESETS.map((p) => (
                <button
                  key={p.id}
                  onClick={() => handleSelectPreset(p)}
                  className={`p-5 rounded-2xl border text-left transition-all ${
                    selectedPresetId === p.id
                      ? 'bg-primary-container border-primary shadow-glow'
                      : 'bg-surface-variant border-border hover:border-primary/40'
                  }`}
                >
                  <span className={`text-title block ${selectedPresetId === p.id ? 'text-primary' : 'text-text-primary'}`}>
                    {p.name}
                  </span>
                  <span className="text-body font-bold text-text-secondary block mt-2">
                    {p.focusMinutes}m Focus • {p.breakMinutes}m Break
                  </span>
                  <p className="text-caption text-text-tertiary mt-1">{p.description}</p>
                </button>
              ))}
            </div>
          </QuovexCard>

          {/* Ambient Soundscapes */}
          <QuovexCard className="space-y-6">
            <div className="flex items-center justify-between">
              <h3 className="text-label text-text-secondary uppercase tracking-wider">
                3. 3D AMBIENT SOUNDSCAPE
              </h3>
              <div className="flex items-center gap-3">
                <span className="text-label text-text-secondary">VOL</span>
                <input
                  type="range"
                  min={0}
                  max={1}
                  step={0.05}
                  value={soundscapeVolume}
                  onChange={(e) => setSoundscapeVolume(Number(e.target.value))}
                  className="w-32 accent-primary bg-surface-variant cursor-pointer"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-5 gap-4">
              {SOUNDSCAPES.map((snd) => (
                <button
                  key={snd.id}
                  onClick={() => setSelectedSoundscape(snd.id)}
                  className={`p-4 rounded-2xl border flex flex-col items-center gap-3 text-center transition-all ${
                    selectedSoundscape === snd.id
                      ? 'bg-primary-container border-primary shadow-glow'
                      : 'bg-surface-variant border-border hover:border-primary/40'
                  }`}
                >
                  <div className="w-12 h-12 relative">
                    <Image
                      src={snd.icon}
                      alt={snd.title}
                      fill
                      className="object-contain"
                      unoptimized
                    />
                  </div>
                  <span className={`text-label ${selectedSoundscape === snd.id ? 'text-primary font-bold' : 'text-text-secondary'}`}>
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
            className="w-full py-5 text-title shadow-glow-lg"
            onClick={handleStartSession}
            leftIcon={<Play className="w-6 h-6 fill-current" />}
          >
            Start Focus Session ({focusDurationMinutes} Minutes)
          </QuovexButton>
        </div>
      )}

      {/* ── SCREEN 2: ACTIVE TIMER ────────────────────────────────────────── */}
      {screenState === 'ACTIVE' && (
        <div className="space-y-12 animate-in zoom-in-95 pt-8">
          {/* Active Header Tag */}
          <div className="flex items-center justify-between">
            <QuovexBadge variant="emerald" size="lg">
              {selectedSubject.toUpperCase()} FOCUS
            </QuovexBadge>
            <QuovexBadge variant="muted" size="md">
              {selectedPresetId.toUpperCase()} MODE
            </QuovexBadge>
          </div>

          {/* Circular Visualizer Arc */}
          <CircularTimerArc
            progress={progress}
            formattedTime={formattedTime}
            subject={selectedSubject}
            isPlaying={isRunning}
          />

          {/* Play / Pause / Cancel Controls */}
          <div className="flex items-center justify-center gap-6">
            {isRunning ? (
              <QuovexButton
                variant="secondary"
                size="lg"
                onClick={handlePauseSession}
                leftIcon={<Pause className="w-5 h-5" />}
              >
                Pause Session
              </QuovexButton>
            ) : (
              <QuovexButton
                variant="primary"
                size="lg"
                onClick={handleResumeSession}
                leftIcon={<Play className="w-5 h-5 fill-current" />}
              >
                Resume Focus
              </QuovexButton>
            )}

            <QuovexButton
              variant="danger"
              size="lg"
              onClick={handleCancelSession}
              leftIcon={<Square className="w-4 h-4 fill-current" />}
            >
              End Early
            </QuovexButton>
          </div>

          {/* Web Focus Shield Card */}
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

      {/* Custom Duration Dialog */}
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
