'use client';

import React, { useState, useEffect, useRef } from 'react';
import Image from 'next/image';
import {
  Bot,
  Send,
  Sparkles,
  RefreshCw,
  Zap,
  BookOpen,
  HelpCircle,
  Clock,
  AlertCircle,
  CheckCircle2,
  Trash2,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import { subscribeToUserProfile } from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexCard } from '@/components/ui/QuovexCard';
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
];

const SUBJECTS = ['Physics', 'Chemistry', 'Mathematics', 'Biology', 'General Study'];

export default function AiTutorPage() {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      sender: 'ai',
      content: 'Hello! I am your **Quovex AI Study Coach**. Ask me any conceptual doubt, derivation, or theorem — I will explain it with mathematical rigor and intuitive visual analogies.\n\nTry asking: *What is the difference between electromotive force and terminal potential difference?*',
      timestamp: Date.now(),
    },
  ]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [isStreaming, setIsStreaming] = useState(false);
  const [selectedSubject, setSelectedSubject] = useState('Physics');
  const [activeTopic, setActiveTopic] = useState('');
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

      if (lower.includes('derive') || lower.includes('formula') || lower.includes('math')) {
        fullResponse = `### Step-by-Step Mathematical Derivation\n\nFor a system governed by conservation of mechanical energy:\n\n$$E_{\\text{total}} = K + U = \\frac{1}{2}mv^2 + mgh = \\text{constant}$$\n\n1. **Initial Boundary State:** At maximum elevation $h_{\\text{max}}$, kinetic velocity $v = 0$:\n   $$E_1 = mg h_{\\text{max}}$$\n\n2. **Arbitrary Position State:** As the mass descends through height $y$:\n   $$E_2 = \\frac{1}{2}m v(y)^2 + mgy$$\n\n3. **Equating Invariants ($E_1 = E_2$):**\n   $$mg h_{\\text{max}} = \\frac{1}{2}mv(y)^2 + mgy$$\n   $$v(y) = \\sqrt{2g(h_{\\text{max}} - y)}$$\n\n⚠️ **Key Examination Trap:** Remember that non-conservative frictional forces ($W_{\\text{friction}} = -f_k d$) must be subtracted from the right-hand side.`;
      } else if (lower.includes('10') || lower.includes('analogy') || lower.includes('simple')) {
        fullResponse = `### Intuitive Visual Analogy 💡\n\nImagine a **water reservoir** atop a hill connected by pipes to a water wheel:\n\n* **Voltage (Potential Difference $V$):** The height of the hill — how hard gravity pushes the water.\n* **Current ($I$ in Amperes):** The actual liters of water rushing through per second.\n* **Resistance ($R$ in Ohms $\\Omega$):** How narrow the pipe is.\n\nAccording to **Ohm's Law**:\n$$V = I \\cdot R$$\n\nIf you want more water current ($I$) through a tight pipe ($R$), you need a much taller hill ($V$)!`;
      } else {
        fullResponse = `### Conceptual Breakdown: ${prompt}\n\n1. **Core Governing Principle:**\n   In ${selectedSubject}, this phenomenon is governed by the state equation:\n   $$F = -k x \\implies \\omega = \\sqrt{\\frac{k}{m}}$$\n\n2. **Physical Significance:**\n   The restoring force directly opposes the displacement vector $\\vec{x}$, producing simple harmonic oscillation with period $T = 2\\pi \\sqrt{\\frac{m}{k}}$.\n\n3. **High-Yield Exam Strategy:**\n   * Always verify dimensional consistency: $[\\omega] = \\text{rad}\\cdot\\text{s}^{-1} = [T^{-1}]$.\n   * Double-check signs when applying boundary conditions.`;
      }

      // Stream the response tokens
      const words = fullResponse.split(' ');
      for (let i = 0; i < words.length; i++) {
        await new Promise((res) => setTimeout(res, 25));
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

  return (
    <div className="max-w-5xl mx-auto space-y-6 pb-12 flex flex-col h-[calc(100vh-120px)] min-h-[600px]">
      {/* ── 1. Context Grounding Header ───────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-2xl bg-surface border border-border shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 relative shrink-0">
            <Image
              src={ASSETS.icons3d.robotMascot}
              alt="Quovex AI"
              fill
              className="object-contain"
              unoptimized
            />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-title font-black text-text-primary">Quovex AI Study Coach</h1>
              <QuovexBadge variant="emerald" size="sm">LaTeX Grounded</QuovexBadge>
            </div>
            <p className="text-caption text-text-secondary mt-0.5">
              Trained on NCERT, JEE Advanced, and NEET question banks.
            </p>
          </div>
        </div>

        {/* Subject & Quota Selector */}
        <div className="flex items-center gap-3">
          <select
            value={selectedSubject}
            onChange={(e) => setSelectedSubject(e.target.value)}
            className="bg-surface-variant border border-border rounded-xl px-4 py-2 text-label text-text-primary font-bold focus:outline-none focus:border-primary"
          >
            {SUBJECTS.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>

          <div className="px-4 py-2 rounded-xl bg-surface-variant border border-border text-label font-bold text-text-secondary">
            {profile?.subscriptionTier && profile.subscriptionTier !== 'FREE' ? (
              <span className="text-primary flex items-center gap-1.5"><Zap className="w-3.5 h-3.5 fill-current"/> Unlimited Pro</span>
            ) : (
              <span>{queriesLeft} queries left</span>
            )}
          </div>
        </div>
      </div>

      {/* ── 2. Message History View ───────────────────────────────────────── */}
      <div className="flex-1 overflow-y-auto p-4 sm:p-6 rounded-2xl bg-surface/50 border border-border space-y-6">
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex items-start gap-3.5 ${msg.sender === 'user' ? 'flex-row-reverse' : ''}`}
          >
            {msg.sender === 'ai' ? (
              <div className="w-10 h-10 rounded-xl bg-primary-container border border-primary/40 text-primary flex items-center justify-center shrink-0 shadow-glow">
                <Bot className="w-5 h-5" />
              </div>
            ) : (
              <div className="w-10 h-10 rounded-xl bg-surface-variant border border-border text-text-primary flex items-center justify-center shrink-0 font-bold text-label">
                You
              </div>
            )}

            <div
              className={`max-w-[85%] sm:max-w-[75%] p-5 rounded-2xl text-body ${
                msg.sender === 'user'
                  ? 'bg-primary text-primary-foreground font-medium rounded-tr-sm shadow-glow-sm'
                  : 'bg-surface-variant border border-border text-text-primary rounded-tl-sm'
              }`}
            >
              {msg.sender === 'ai' ? (
                <LatexRenderer content={msg.content} />
              ) : (
                <p className="whitespace-pre-wrap">{msg.content}</p>
              )}
            </div>
          </div>
        ))}
        {isStreaming && (
          <div className="flex items-center gap-2 text-label text-primary font-semibold animate-pulse pl-14">
            <Sparkles className="w-4 h-4" />
            <span>Formulating mathematical proof...</span>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* ── 3. Quick Action Chips & Composer ──────────────────────────────── */}
      <div className="space-y-4 shrink-0">
        {/* Quick Prompts */}
        <div className="flex items-center gap-3 overflow-x-auto pb-2 no-scrollbar">
          {QUICK_PROMPTS.map((p, idx) => (
            <button
              key={idx}
              onClick={() => handleSendMessage(p)}
              disabled={isStreaming}
              className="px-4 py-2 rounded-xl bg-surface border border-border text-caption font-semibold text-text-secondary hover:text-text-primary hover:border-primary/50 whitespace-nowrap transition-colors"
            >
              ⚡ {p}
            </button>
          ))}
        </div>

        {/* Input Composer */}
        <div className="flex items-center gap-3 p-2.5 rounded-2xl bg-surface border border-border focus-within:border-primary focus-within:shadow-glow-sm transition-all">
          <input
            type="text"
            placeholder={`Ask any ${selectedSubject} doubt, derivation, or problem...`}
            value={inputPrompt}
            onChange={(e) => setInputPrompt(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                handleSendMessage();
              }
            }}
            disabled={isStreaming}
            className="flex-1 bg-transparent px-4 py-3 text-body text-text-primary placeholder:text-text-tertiary focus:outline-none"
          />
          <QuovexButton
            variant="primary"
            size="lg"
            onClick={() => handleSendMessage()}
            isLoading={isStreaming}
            disabled={!inputPrompt.trim() || isStreaming}
            leftIcon={<Send className="w-5 h-5" />}
          >
            Ask
          </QuovexButton>
        </div>
      </div>
    </div>
  );
}
