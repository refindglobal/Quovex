import { NextResponse } from 'next/server';
import { callAiGateway, extractJsonFromAiResponse, AiChatMessage } from '@/lib/ai-gateway';

export interface GeneratedQuizResponse {
  subject: string;
  topic: string;
  questions: Array<{
    id: string;
    question: string;
    options: string[];
    correctIndex: number;
    explanation: string;
    concept: string;
  }>;
}

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const {
      subject = 'Physics',
      topic = 'Mechanics & Laws of Motion',
      targetExam = 'JEE Main & Advanced',
      count = 5,
    } = body;

    const systemPrompt = `You are Quovex AI Quiz Creator, an expert question author for ${targetExam}.
Create ${count} high-yield multiple-choice questions for ${subject} on "${topic}".
All formulas MUST use standard LaTeX ($...$ for inline, $$...$$ for equations).

Return ONLY valid JSON matching this schema:
{
  "subject": "${subject}",
  "topic": "${topic}",
  "questions": [
    {
      "id": "q1",
      "question": "Question text with LaTeX formulas",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0,
      "explanation": "Step-by-step mathematical/conceptual derivation of the correct answer",
      "concept": "Specific subconcept name for remedial flashcards"
    }
  ]
}`;

    const messages: AiChatMessage[] = [
      { role: 'system', content: systemPrompt },
      { role: 'user', content: `Generate ${count} diagnostic MCQs on ${subject}: ${topic} for ${targetExam}.` },
    ];

    const result = await callAiGateway({
      messages,
      temperature: 0.35,
      maxTokens: 3000,
      jsonMode: true,
    });

    const parsed = extractJsonFromAiResponse<GeneratedQuizResponse>(result.content);

    return NextResponse.json({
      success: true,
      data: parsed,
      requestId: result.requestId,
      provider: result.provider,
    });
  } catch (error: any) {
    console.error('Quiz Generation Error:', error);
    return NextResponse.json(
      {
        success: false,
        error: error.message || 'AI Quiz generation temporarily busy.',
      },
      { status: 500 }
    );
  }
}
