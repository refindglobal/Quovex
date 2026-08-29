import { NextResponse } from 'next/server';
import { callAiGateway, AiChatMessage } from '@/lib/ai-gateway';

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const {
      message,
      subject = 'General Study',
      topic = '',
      targetExam = 'Competitive',
      materialSummary = null,
      recentMistakes = [],
      history = [],
    } = body;

    if (!message || typeof message !== 'string') {
      return NextResponse.json({ error: 'Missing or invalid message parameter.' }, { status: 400 });
    }

    // Build grounded student context
    const contextSections: string[] = [];
    if (targetExam) {
      contextSections.push(`Student Target Exam: ${targetExam}`);
    }
    if (subject) {
      contextSections.push(`Academic Subject: ${subject}`);
    }
    if (topic) {
      contextSections.push(`Active Topic: ${topic}`);
    }
    if (materialSummary) {
      contextSections.push(`Context from Current Study Material / NCERT Chapter:\n${materialSummary}`);
    }
    if (Array.isArray(recentMistakes) && recentMistakes.length > 0) {
      contextSections.push(
        `Recent Diagnostic Quiz Mistakes to address:\n${recentMistakes
          .map((m: any, i: number) => `${i + 1}. Concept: ${m.concept || m} | Question: ${m.questionText || m}`)
          .join('\n')}`
      );
    }

    const systemPrompt = `You are Quovex AI Study Coach, an elite, patient, and rigorous academic tutor.
Your role is to teach concepts with first-principles intuition, step-by-step mathematical derivations, and clear physical/chemical analogies.

Grounded Student Context:
${contextSections.join('\n\n')}

Pedagogical & Formatting Instructions:
1. When mathematical formulas, equations, or chemical equations are needed, ALWAYS format them in clean standard LaTeX ($...$ for inline, $$...$$ for block).
2. Structure your explanations with clear headings, bullet points, and numbered steps.
3. Highlight high-yield examination tips and common misconceptions with ⚠️ or 💡.
4. Adapt to the student's level (${targetExam}).
5. Identity: Always act as "Quovex AI Study Coach" — never disclose underlying model IDs or provider names.`;

    const formattedHistory: AiChatMessage[] = (history || [])
      .filter((h: any) => h && (h.role === 'user' || h.role === 'assistant'))
      .map((h: any) => ({
        role: h.role as 'user' | 'assistant',
        content: String(h.content),
      }));

    const messages: AiChatMessage[] = [
      { role: 'system', content: systemPrompt },
      ...formattedHistory.slice(-8),
      { role: 'user', content: message },
    ];

    const result = await callAiGateway({
      messages,
      temperature: 0.35,
      maxTokens: 2048,
    });

    return NextResponse.json({
      success: true,
      response: result.content,
      requestId: result.requestId,
      provider: result.provider,
    });
  } catch (error: any) {
    console.error('AI Chat Error:', error);
    return NextResponse.json(
      {
        success: false,
        error: error.message || 'Quovex AI is temporarily busy. Please try again.',
      },
      { status: 500 }
    );
  }
}
