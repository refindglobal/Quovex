'use client';

import React, { useState, useEffect, useRef, Suspense } from 'react';
import Image from 'next/image';
import { useSearchParams } from 'next/navigation';
import {
  Bot,
  Send,
  Sparkles,
  Zap,
  BookOpen,
  HelpCircle,
  Clock,
  RotateCcw,
  Plus,
  Trash2,
  MessageSquare,
  ChevronLeft,
  ChevronRight,
  AlertCircle,
  Menu,
  X,
  ShieldCheck,
  Flame,
} from 'lucide-react';
import { getCurrentUser } from '@/lib/firebase/auth';
import {
  subscribeToUserProfile,
  subscribeToQuizHistory,
  subscribeToAiConversations,
  subscribeToAiMessages,
  saveAiConversation,
  saveAiMessage,
  deleteAiConversation,
  AiConversation,
  AiMessage,
  QuizResultRecord,
} from '@/lib/firebase/firestore';
import { QuovexButton } from '@/components/ui/QuovexButton';
import { QuovexBadge } from '@/components/ui/QuovexBadge';
import { LatexRenderer } from '@/components/ui/LatexRenderer';
import { ASSETS } from '@/lib/assets';

const QUICK_PROMPTS = [
  'Explain like I am 10 years old with a simple analogy',
  'Derive the primary formula step-by-step with proofs',
  'What are common exam traps & pitfalls for this concept?',
  'Give me 2 hard practice MCQs with explanations',
  'Explain the physical significance and real-world application',
];

const SUBJECTS = ['Physics', 'Chemistry', 'Mathematics', 'Biology', 'General Study'];

function AiTutorContent() {
  const searchParams = useSearchParams();
  const initialChatId = searchParams.get('chatId');
  const contextSource = searchParams.get('context');
  const contextSubject = searchParams.get('subject');
  const contextTitle = searchParams.get('title');
  const contextClass = searchParams.get('class');

  const [conversations, setConversations] = useState<AiConversation[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<string | null>(initialChatId);
  const [messages, setMessages] = useState<AiMessage[]>([]);
  const [inputPrompt, setInputPrompt] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [selectedSubject, setSelectedSubject] = useState(contextSubject || 'Physics');
  const [profile, setProfile] = useState<any>(null);
  const [quizHistory, setQuizHistory] = useState<QuizResultRecord[]>([]);
  const [queriesLeft, setQueriesLeft] = useState(10);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  const currentUser = getCurrentUser();
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  // 1. Subscribe to User Profile & Quiz Mistakes (for AI Context Grounding)
  useEffect(() => {
    if (!currentUser) return;

    const unsubProfile = subscribeToUserProfile(currentUser.uid, (p) => {
      setProfile(p);
      if (p?.subscriptionTier && p.subscriptionTier !== 'FREE') {
        setQueriesLeft(9999);
      }
    });

    const unsubQuiz = subscribeToQuizHistory(currentUser.uid, (q) => {
      setQuizHistory(q);
    });

    const unsubConversations = subscribeToAiConversations(currentUser.uid, (convs) => {
      setConversations(convs);
      if (!activeConversationId && convs.length > 0 && !initialChatId) {
        setActiveConversationId(convs[0].id);
      }
    });

    return () => {
      unsubProfile();
      unsubQuiz();
      unsubConversations();
    };
  }, [currentUser, activeConversationId, initialChatId]);

  // 2. Subscribe to Active Conversation Messages
  useEffect(() => {
    if (!currentUser || !activeConversationId) {
      setMessages([]);
      return;
    }

    const unsubMessages = subscribeToAiMessages(currentUser.uid, activeConversationId, (msgs) => {
      setMessages(msgs);
    });

    return () => unsubMessages();
  }, [currentUser, activeConversationId]);

  // 3. Scroll to bottom on message update
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isGenerating]);

  // 4. Handle context passed from NCERT / Notes
  useEffect(() => {
    if (contextSubject) {
      setSelectedSubject(contextSubject);
    }
  }, [contextSubject]);

  // Extract recent mistake concepts for grounded tutoring
  const recentMistakes = quizHistory
    .flatMap((q) => q.mistakes || [])
    .slice(0, 4)
    .map((m) => ({ concept: m.concept, questionText: m.questionText }));

  const handleStartNewChat = async () => {
    if (!currentUser) return;
    const newId = `conv_${Date.now()}`;
    const initialTitle = `Discussion in ${selectedSubject}`;

    await saveAiConversation(currentUser.uid, {
      id: newId,
      title: initialTitle,
      subject: selectedSubject,
      sourceType: 'AI_TUTOR',
      createdAt: Date.now(),
      updatedAt: Date.now(),
      lastMessagePreview: 'Started new study discussion',
    });

    setActiveConversationId(newId);
    setIsDrawerOpen(false);
    setErrorBanner(null);
  };

  const handleDeleteChat = async (convId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    if (!currentUser) return;
    if (confirm('Delete this conversation?')) {
      await deleteAiConversation(currentUser.uid, convId);
      if (activeConversationId === convId) {
        const remaining = conversations.filter((c) => c.id !== convId);
        setActiveConversationId(remaining.length > 0 ? remaining[0].id : null);
      }
    }
  };

  const handleSendMessage = async (textToSend?: string) => {
    const prompt = (textToSend || inputPrompt).trim();
    if (!prompt || isGenerating || !currentUser) return;

    setErrorBanner(null);

    // Ensure we have an active conversation
    let convId = activeConversationId;
    if (!convId) {
      convId = `conv_${Date.now()}`;
      await saveAiConversation(currentUser.uid, {
        id: convId,
        title: prompt.slice(0, 35) || `Study in ${selectedSubject}`,
        subject: selectedSubject,
        sourceType: 'AI_TUTOR',
        createdAt: Date.now(),
        updatedAt: Date.now(),
        lastMessagePreview: prompt.slice(0, 60),
      });
      setActiveConversationId(convId);
    }

    const userMsgId = `user_${Date.now()}`;
    const userMsg: AiMessage = {
      id: userMsgId,
      role: 'user',
      content: prompt,
      createdAt: Date.now(),
    };

    // Optimistically update local message state and save to Firestore
    await saveAiMessage(currentUser.uid, convId, userMsg);
    setInputPrompt('');
    setIsGenerating(true);

    try {
      // Build real AI request payload with student context
      const response = await fetch('/api/ai/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: prompt,
          subject: selectedSubject,
          targetExam: profile?.targetExam || 'JEE Advanced',
          topic: contextTitle || '',
          materialSummary: contextTitle ? `${contextClass || ''} • ${contextSubject || ''}: ${contextTitle}` : null,
          recentMistakes: recentMistakes,
          history: messages.map((m) => ({ role: m.role, content: m.content })),
        }),
      });

      const json = await response.json();
      if (!response.ok || !json.success || !json.response) {
        throw new Error(json.error || 'Live AI response failed.');
      }

      const aiMsgId = `ai_${Date.now()}`;
      const aiMsg: AiMessage = {
        id: aiMsgId,
        role: 'assistant',
        content: json.response,
        createdAt: Date.now(),
      };

      await saveAiMessage(currentUser.uid, convId, aiMsg);

      // Decrement query quota for free tier
      if (!profile?.subscriptionTier || profile?.subscriptionTier === 'FREE') {
        setQueriesLeft((prev) => Math.max(0, prev - 1));
      }
    } catch (err: any) {
      console.error('AI Tutor Live Error:', err);
      setErrorBanner(err.message || 'Quovex AI is temporarily unavailable. Please try again.');
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div className="max-w-6xl mx-auto flex flex-col md:flex-row gap-4 h-[calc(100vh-6.5rem)] min-h-[580px] pb-12">
      {/* ── 1. Left Sidebar: Recent Persistent Conversations (Desktop & Mobile Drawer) ─ */}
      <div
        className={`fixed md:static inset-y-0 left-0 z-40 w-72 bg-surface border-r md:border border-border md:rounded-2xl p-4 flex flex-col justify-between transition-transform duration-200 ${
          isDrawerOpen ? 'translate-x-0' : '-translate-x-full md:translate-x-0'
        } shadow-lg md:shadow-sm`}
      >
        <div className="space-y-4 overflow-hidden flex flex-col h-full">
          {/* Sidebar Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <MessageSquare className="w-5 h-5 text-primary" />
              <h3 className="font-bold text-text-primary text-sm">Recent Chats</h3>
            </div>
            <button
              onClick={() => setIsDrawerOpen(false)}
              className="md:hidden p-1 rounded-lg text-text-secondary hover:text-text-primary"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* New Chat Button */}
          <QuovexButton
            variant="primary"
            size="sm"
            className="w-full justify-center"
            onClick={handleStartNewChat}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            New Discussion
          </QuovexButton>

          {/* Persistent Conversations List */}
          <div className="flex-1 overflow-y-auto space-y-1.5 pr-1 -mr-1">
            {conversations.length > 0 ? (
              conversations.map((conv) => (
                <div
                  key={conv.id}
                  onClick={() => {
                    setActiveConversationId(conv.id);
                    setIsDrawerOpen(false);
                    setErrorBanner(null);
                  }}
                  className={`p-2.5 rounded-xl border text-left cursor-pointer transition-all flex items-center justify-between group ${
                    activeConversationId === conv.id
                      ? 'bg-primary-container border-primary text-primary font-bold shadow-xs'
                      : 'bg-surface-variant/50 border-border/60 hover:bg-surface-variant hover:border-primary/40 text-text-primary'
                  }`}
                >
                  <div className="overflow-hidden pr-2">
                    <p className="text-xs font-semibold truncate leading-tight">{conv.title}</p>
                    <p className="text-[10px] text-text-secondary truncate mt-0.5">
                      {conv.subject} • {new Date(conv.updatedAt).toLocaleDateString(undefined, { month: 'short', day: 'numeric' })}
                    </p>
                  </div>

                  <button
                    onClick={(e) => handleDeleteChat(conv.id, e)}
                    title="Delete Chat"
                    className="opacity-0 group-hover:opacity-100 p-1 rounded-md hover:bg-error-container/40 text-text-secondary hover:text-error transition-all"
                  >
                    <Trash2 className="w-3.5 h-3.5" />
                  </button>
                </div>
              ))
            ) : (
              <div className="p-4 text-center text-xs text-text-secondary">
                No recent discussions. Start a new topic!
              </div>
            )}
          </div>
        </div>

        {/* Quota Indicator */}
        <div className="pt-3 border-t border-border mt-3 text-xs flex items-center justify-between text-text-secondary font-bold">
          <span>AI Quota:</span>
          {profile?.subscriptionTier && profile.subscriptionTier !== 'FREE' ? (
            <span className="text-primary flex items-center gap-1 font-bold">
              <Zap className="w-3.5 h-3.5 fill-current" /> Pro VIP
            </span>
          ) : (
            <span className="font-mono">{queriesLeft} Left Today</span>
          )}
        </div>
      </div>

      {/* ── 2. Main AI Chat Studio ────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col bg-surface border border-border rounded-2xl overflow-hidden shadow-sm">
        {/* Top Control & Grounding Context Header */}
        <div className="p-3 sm:p-4 border-b border-border bg-surface-elevated flex flex-wrap items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsDrawerOpen(true)}
              className="md:hidden p-2 rounded-xl bg-surface-variant border border-border text-text-secondary hover:text-text-primary"
            >
              <Menu className="w-4 h-4" />
            </button>

            <div className="w-9 h-9 rounded-xl overflow-hidden bg-primary/10 border border-primary/30 flex items-center justify-center p-1 shrink-0">
              <Image
                src={ASSETS.icons3d.robotMascot}
                alt="Quovex AI"
                width={26}
                height={26}
                className="object-contain"
                unoptimized
              />
            </div>

            <div>
              <div className="flex items-center gap-2">
                <h1 className="text-sm sm:text-base font-bold text-text-primary">
                  Quovex AI Study Coach
                </h1>
                <QuovexBadge variant="emerald" size="sm">Live Backend AI</QuovexBadge>
              </div>
              <p className="text-[11px] text-text-secondary hidden sm:block">
                Grounded in: <strong>{selectedSubject}</strong> • {profile?.targetExam || 'Competitive Exam'}
                {recentMistakes.length > 0 ? ` • ${recentMistakes.length} quiz mistake focal points` : ''}
              </p>
            </div>
          </div>

          {/* Subject Switcher & Reset */}
          <div className="flex items-center gap-2">
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

            <button
              onClick={handleStartNewChat}
              title="Reset conversation"
              className="p-2 rounded-xl bg-surface-variant hover:bg-surface border border-border text-text-secondary hover:text-text-primary transition-colors"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Error Banner (if any) */}
        {errorBanner && (
          <div className="p-3 bg-error-container text-error border-b border-error/30 text-xs font-bold flex items-center justify-between">
            <div className="flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{errorBanner}</span>
            </div>
            <button
              onClick={() => handleSendMessage()}
              className="underline hover:no-underline font-black text-xs ml-2"
            >
              Retry
            </button>
          </div>
        )}

        {/* ── 3. Scrollable Conversation Stream ───────────────────────────── */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 bg-background/50">
          {messages.length > 0 ? (
            messages.map((msg) => (
              <div
                key={msg.id}
                className={`flex items-start gap-3 ${msg.role === 'user' ? 'flex-row-reverse' : ''}`}
              >
                {msg.role === 'assistant' ? (
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
                  className={`max-w-[88%] sm:max-w-[82%] rounded-2xl p-4 sm:p-5 ${
                    msg.role === 'user'
                      ? 'bg-primary text-primary-foreground font-medium rounded-tr-none shadow-sm'
                      : 'bg-surface border border-border/90 text-text-primary rounded-tl-none shadow-sm'
                  }`}
                >
                  {msg.role === 'assistant' ? (
                    <LatexRenderer content={msg.content} />
                  ) : (
                    <p className="whitespace-pre-wrap text-sm sm:text-base leading-relaxed">{msg.content}</p>
                  )}
                </div>
              </div>
            ))
          ) : (
            <div className="h-full flex flex-col items-center justify-center text-center text-text-secondary py-16 space-y-3">
              <div className="w-12 h-12 rounded-2xl bg-primary/10 border border-primary/30 flex items-center justify-center p-2 mx-auto">
                <Bot className="w-8 h-8 text-primary" />
              </div>
              <h3 className="font-bold text-text-primary text-base">
                Welcome to your Quovex AI Study Coach
              </h3>
              <p className="text-xs max-w-md text-text-secondary leading-relaxed">
                Ask any conceptual doubt, step-by-step mathematical proof, or textbook question. All answers are generated live from our academic intelligence gateway.
              </p>
            </div>
          )}

          {isGenerating && (
            <div className="flex items-center gap-2 text-xs text-primary font-semibold animate-pulse pl-11">
              <Sparkles className="w-4 h-4" />
              <span>Formulating step-by-step academic response with live AI...</span>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* ── 4. Anchored Composer & Quick Prompts ─────────────────────────── */}
        <div className="p-3 sm:p-4 bg-surface-elevated border-t border-border space-y-2.5 shrink-0">
          {/* Quick Prompts Ribbon */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1 no-scrollbar">
            {QUICK_PROMPTS.map((p, idx) => (
              <button
                key={idx}
                onClick={() => handleSendMessage(p)}
                disabled={isGenerating}
                className="px-3 py-1.5 rounded-xl bg-surface border border-border text-[11px] font-semibold text-text-secondary hover:text-text-primary hover:border-primary/50 whitespace-nowrap transition-colors shrink-0 shadow-sm"
              >
                ⚡ {p}
              </button>
            ))}
          </div>

          {/* Input Box */}
          <div className="flex items-center gap-2 p-2 rounded-2xl bg-surface border border-border focus-within:border-primary focus-within:ring-1 focus-within:ring-primary/30 transition-all shadow-sm">
            <input
              type="text"
              placeholder={`Ask any ${selectedSubject} question, derivation, or doubt...`}
              value={inputPrompt}
              onChange={(e) => setInputPrompt(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSendMessage();
                }
              }}
              disabled={isGenerating}
              className="flex-1 bg-transparent px-3 py-2 text-xs sm:text-sm text-text-primary placeholder:text-text-tertiary focus:outline-none"
            >
            </input>

            <QuovexButton
              variant="primary"
              size="md"
              onClick={() => handleSendMessage()}
              isLoading={isGenerating}
              disabled={!inputPrompt.trim() || isGenerating}
              leftIcon={<Send className="w-4 h-4" />}
              className="px-4 py-2"
            >
              Ask
            </QuovexButton>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function AiTutorPage() {
  return (
    <Suspense
      fallback={
        <div className="p-12 text-center text-xs text-text-secondary">
          Loading Quovex AI Coach...
        </div>
      }
    >
      <AiTutorContent />
    </Suspense>
  );
}
