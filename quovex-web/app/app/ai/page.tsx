'use client';

import React, { useState, useEffect, useRef } from 'react';
import Image from 'next/image';
import {
  Bot,
  Send,
  Sparkles,
  Zap,
  BookOpen,
  HelpCircle,
  Clock,
  RotateCcw,
  Compass,
  CheckCircle2,
  Lightbulb,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import { subscribeToUserProfile } from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import { ASSETS } from '@/lib/assets';

interface ChatMessage {
  id: string;
  sender: 'user' | 'ai';
  content: string;
  timestamp: number;
}

const QUICK_PROMPTS = [
  'Explain like I am 10 years old with a simple analogy',
  'Derive the primary formula step-by-step with proofs',
  'What are common exam traps & pitfalls for this concept?',
  'Give me 2 hard practice MCQs with explanations',
  'Explain the physical significance and real-world application',
];

const SUBJECTS = ['Physics', 'Chemistry', 'Mathematics', 'Biology', 'General Study'];

export default function AiTutorPage() {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      sender: 'ai',
      content: 'Hello! I am your **Quovex AI Study Coach**.\n\nAsk me any conceptual doubt, step-by-step mathematical derivation, or theorem — I explain concepts with academic rigor, LaTeX formulas, and visual analogies.\n\n💡 **Try asking:** *Derive the time period of a simple pendulum from first principles.*',
      timestamp: Date.now(),
    },
  ]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [selectedSubject, setSelectedSubject] = useState('Physics');
  const [profile, setProfile] = useState<any>(null);
  const [queriesLeft, setQueriesLeft] = useState(10);

  const currentUser = getCurrentUser();
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!currentUser) return;
    const unsub = subscribeToUserProfile(currentUser.uid, (p) => {
      setProfile(p);
      if (p?.subscriptionTier && p.subscriptionTier !== 'FREE') {
        setQueriesLeft(9999);
      }
    });
    return () => unsub();
  }, [currentUser]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isStreaming]);

  const handleSendMessage = async (textToSend?: string) => {
    const prompt = (textToSend || inputPrompt).trim();
    if (!prompt || isStreaming) return;

    const userMsg: ChatMessage = {
      id: `usr_${Date.now()}`,
      sender: 'user',
      content: prompt,
      timestamp: Date.now(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInputPrompt('');
    setIsStreaming(true);

    const aiMsgId = `ai_${Date.now()}`;
    const initialAiMsg: ChatMessage = {
      id: aiMsgId,
      sender: 'ai',
      content: '',
      timestamp: Date.now(),
    };
    setMessages((prev) => [...prev, initialAiMsg]);

    // Simulated streamed contextual AI response with real mathematical formatting
    try {
      let fullResponse = '';
      const lower = prompt.toLowerCase();

      if (lower.includes('derive') || lower.includes('formula') || lower.includes('pendulum') || lower.includes('math')) {
        fullResponse = `### Step-by-Step Mathematical Derivation\n\nFor a simple pendulum of length $L$ with bob mass $m$ displaced by angle $\\theta$ from the vertical:\n\n1. **Restoring Torque Formulation:**\n   The restoring gravitational torque about the suspension point $O$ is given by:\n   $$\\tau = -mgL \\sin(\\theta)$$\n\n2. **Rotational Equation of Motion:**\n   Using $\\tau = I\\alpha = I \\frac{d^2\\theta}{dt^2}$ with moment of inertia $I = mL^2$:\n   $$mL^2 \\frac{d^2\\theta}{dt^2} = -mgL \\sin(\\theta)$$\n   $$\\frac{d^2\\theta}{dt^2} + \\frac{g}{L} \\sin(\\theta) = 0$$\n\n3. **Small Angle Approximation ($\\sin\\theta \\approx \\theta$):**\n   For small angular displacements ($\\theta \\ll 1\\text{ rad}$):\n   $$\\frac{d^2\\theta}{dt^2} + \\left(\\frac{g}{L}\\right)\\theta = 0$$\n\n4. **Angular Frequency and Time Period:**\n   Comparing with standard Simple Harmonic Motion $\\ddot{\\theta} + \\omega^2 \\theta = 0$:\n   $$\\omega = \\sqrt{\\frac{g}{L}} \\implies T = \\frac{2\\pi}{\\omega} = 2\\pi \\sqrt{\\frac{L}{g}}$$\n\n⚠️ **Key Examination Trap:** Remember that the time period $T$ is strictly independent of the bob's mass $m$, but depends on the local effective gravitational acceleration $g_{\\text{eff}}$.`;
      } else if (lower.includes('10') || lower.includes('analogy') || lower.includes('simple')) {
        fullResponse = `### Intuitive Visual Analogy 💡\n\nImagine a **water reservoir** atop a hill connected by pipes to a water turbine:\n\n* **Voltage (Potential Difference $V$):** The height of the hill — how hard gravity pushes the water downward.\n* **Current ($I$ in Amperes):** The actual volume of water rushing through per second.\n* **Resistance ($R$ in Ohms $\\Omega$):** How narrow or obstructed the pipe is.\n\nAccording to **Ohm's Law**:\n$$V = I \\cdot R \\implies I = \\frac{V}{R}$$\n\nIf you want more water current ($I$) through a tight pipe ($R$), you need a much taller hill ($V$)!`;
      } else {
        fullResponse = `### Conceptual Breakdown: ${prompt}\n\n1. **Core Governing Principle:**\n   In **${selectedSubject}**, this phenomenon is governed by the invariant equilibrium relation:\n   $$\\oint \\vec{E} \\cdot d\\vec{A} = \\frac{Q_{\\text{enclosed}}}{\\varepsilon_0}$$\n\n2. **Physical Significance & Dynamics:**\n   The field strength depends purely on the net enclosed source charge and spatial symmetry.\n\n3. **High-Yield Exam Strategy:**\n   * Always verify dimensional consistency before numerical computation.\n   * Double check signs when applying Gaussian boundary conditions.`;
      }

      // Stream the response tokens smoothly
      const words = fullResponse.split(' ');
      for (let i = 0; i < words.length; i++) {
        await new Promise((res) => setTimeout(res, 20));
        const partial = words.slice(0, i + 1).join(' ');
        setMessages((prev) =>
          prev.map((m) => (m.id === aiMsgId ? { ...m, content: partial } : m))
        );
      }
    } catch (_) {
      setMessages((prev) =>
        prev.map((m) =>
          m.id === aiMsgId
            ? { ...m, content: '⚠️ An error occurred while generating the response. Please retry.' }
            : m
        )
      );
    } finally {
      setIsStreaming(false);
      setQueriesLeft((prev) => Math.max(0, prev - 1));
    }
  };

  const handleClearHistory = () => {
    setMessages([
      {
        id: `welcome_${Date.now()}`,
        sender: 'ai',
        content: 'Conversation reset. Ask any question, derivation, or concept to begin!',
        timestamp: Date.now(),
      },
    ]);
  };

  return (
    <div className="max-w-4xl mx-auto flex flex-col h-[calc(100vh-6.5rem)] min-h-[560px]">
      {/* ── 1. Compact Control Header ────────────────────────────────────────── */}
      <div className="flex items-center justify-between gap-3 p-3 sm:p-4 rounded-2xl bg-surface border border-border shrink-0 shadow-sm">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl overflow-hidden bg-primary/10 border border-primary/30 flex items-center justify-center p-1 shrink-0">
            <Image
              src={ASSETS.icons3d.robotMascot}
              alt="Quovex AI"
              width={28}
              height={28}
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-sm sm:text-base font-bold text-text-primary">Quovex AI Study Coach</h1>
              <QuovexBadge variant="emerald" size="sm">LaTeX Grounded</QuovexBadge>
            </div>
            <p className="text-[11px] text-text-secondary hidden sm:block">
              NCERT • JEE Advanced • NEET Academic Tutor
            </p>
          </div>
        </div>

        {/* Subject & Quota Controls */}
        <div className="flex items-center gap-2 sm:gap-3">
          <select
            value={selectedSubject}
            onChange={(e) => setSelectedSubject(e.target.value)}
            className="bg-surface-variant border border-border rounded-xl px-2.5 sm:px-3 py-1.5 text-xs text-text-primary font-bold focus:outline-none focus:border-primary cursor-pointer"
          >
            {SUBJECTS.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>

          <div className="px-2.5 sm:px-3 py-1.5 rounded-xl bg-surface-variant border border-border text-xs font-bold text-text-secondary">
            {profile?.subscriptionTier && profile.subscriptionTier !== 'FREE' ? (
              <span className="text-primary flex items-center gap-1"><Zap className="w-3.5 h-3.5 fill-current"/> Pro</span>
            ) : (
              <span>{queriesLeft} left</span>
            )}
          </div>

          <button
            onClick={handleClearHistory}
            title="Reset conversation"
            className="p-1.5 rounded-xl bg-surface-variant hover:bg-surface-elevated border border-border text-text-secondary hover:text-text-primary transition-colors"
          >
            <RotateCcw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* ── 2. Scrollable Conversation Stream ─────────────────────────────── */}
      <div className="flex-1 overflow-y-auto p-3 sm:p-5 my-3 rounded-2xl bg-surface/60 border border-border space-y-4">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex items-start gap-3 ${msg.sender === 'user' ? 'flex-row-reverse' : ''}`}
          >
            {msg.sender === 'ai' ? (
              <div className="w-8 h-8 rounded-xl overflow-hidden bg-primary/10 border border-primary/30 flex items-center justify-center p-1 shrink-0 mt-1">
                <Image
                  src={ASSETS.icons3d.robotMascot}
                  alt="Quovex AI"
                  width={22}
                  height={22}
                  className="object-contain"
                  unoptimized
                />
              </div>
            ) : (
              <div className="w-8 h-8 rounded-xl bg-primary text-primary-foreground flex items-center justify-center shrink-0 font-bold text-xs mt-1 shadow-sm">
                You
              </div>
            )}

            <div
              className={`max-w-[88%] sm:max-w-[80%] rounded-2xl p-4 sm:p-5 ${
                msg.sender === 'user'
                  ? 'bg-primary text-primary-foreground font-medium rounded-tr-none shadow-sm'
                  : 'bg-surface-elevated border border-border/90 text-text-primary rounded-tl-none shadow-sm'
              }`}
            >
              {msg.sender === 'ai' ? (
                <LatexRenderer content={msg.content} />
              ) : (
                <p className="whitespace-pre-wrap text-sm sm:text-base leading-relaxed">{msg.content}</p>
              )}
            </div>
          </div>
        ))}

        {isStreaming && (
          <div className="flex items-center gap-2 text-xs text-primary font-semibold animate-pulse pl-11">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Formulating step-by-step derivation...</span>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* ── 3. Anchored Composer & Quick Prompts ─────────────────────────── */}
      <div className="space-y-2 shrink-0">
        {/* Quick Prompts Strip */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
          {QUICK_PROMPTS.map((p, idx) => (
            <button
              key={idx}
              onClick={() => handleSendMessage(p)}
              disabled={isStreaming}
              className="px-3 py-1.5 rounded-xl bg-surface border border-border text-[11px] font-semibold text-text-secondary hover:text-text-primary hover:border-primary/50 whitespace-nowrap transition-colors shrink-0 shadow-sm"
            >
              ⚡ {p}
            </button>
          ))}
        </div>

        {/* Input Composer */}
        <div className="flex items-center gap-2 p-2 rounded-2xl bg-surface border border-border focus-within:border-primary focus-within:ring-1 focus-within:ring-primary/30 transition-all shadow-sm">
          <input
            type="text"
            placeholder={`Ask any ${selectedSubject} doubt, theorem, or calculation...`}
            value={inputPrompt}
            onChange={(e) => setInputPrompt(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSendMessage();
              }
            }}
            disabled={isStreaming}
            className="flex-1 bg-transparent px-3 py-2 text-sm text-text-primary placeholder:text-text-tertiary focus:outline-none"
          />
          <QuovexButton
            variant="primary"
            size="md"
            onClick={() => handleSendMessage()}
            isLoading={isStreaming}
            disabled={!inputPrompt.trim() || isStreaming}
            leftIcon={<Send className="w-4 h-4" />}
            className="px-4 py-2"
          >
            Ask
          </QuovexButton>
        </div>
      </div>
    </div>
  );
}
