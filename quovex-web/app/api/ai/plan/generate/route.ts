import { NextResponse } from 'next/server';
import { callAiGateway, extractJsonFromAiResponse, AiChatMessage } from '@/lib/ai-gateway';

export interface GeneratedPlanResponse {
  examName: string;
  dailyGoalHours: number;
  tasks: Array<{
    dayNumber: number;
    dayName: string;
    title: string;
    subject: string;
    durationMinutes: number;
    priority: 'HIGH' | 'MEDIUM' | 'LOW';
    category: string;
  }>;
}

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const {
      targetExam = 'JEE Advanced',
      dailyHours = 3,
      subjects = ['Physics', 'Chemistry', 'Mathematics'],
    } = body;

    const systemPrompt = `You are Quovex AI Academic Strategist, an elite competitive mentor for ${targetExam}.
Generate a structured, high-yield 7-day study plan allocating ${dailyHours} hours per day across ${subjects.join(', ')}.

Return ONLY valid JSON matching this schema:
{
  "examName": "${targetExam}",
  "dailyGoalHours": ${dailyHours},
  "tasks": [
    {
      "dayNumber": 1,
      "dayName": "Monday",
      "title": "Specific high-yield topic and objective",
      "subject": "Physics",
      "durationMinutes": 60,
      "priority": "HIGH",
      "category": "Core Derivations"
    }
  ]
}`;

    const messages: AiChatMessage[] = [
      { role: 'system', content: systemPrompt },
      { role: 'user', content: `Generate a 7-day strategic study plan for ${targetExam} with ${dailyHours} hours/day.` },
    ];

    const result = await callAiGateway({
      messages,
      temperature: 0.3,
      maxTokens: 3000,
      jsonMode: true,
    });

    const parsed = extractJsonFromAiResponse<GeneratedPlanResponse>(result.content);

    return NextResponse.json({
      success: true,
      data: parsed,
      requestId: result.requestId,
      provider: result.provider,
    });
  } catch (error: any) {
    console.error('Plan Generation Error:', error);
    return NextResponse.json(
      {
        success: false,
        error: error.message || 'AI Plan generation temporarily busy.',
      },
      { status: 500 }
    );
  }
}
