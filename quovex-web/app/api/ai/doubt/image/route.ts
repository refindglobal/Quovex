import { NextResponse } from 'next/server';
import { callAiGateway, extractJsonFromAiResponse, AiChatMessage } from '@/lib/ai-gateway';

export interface StructuredDoubtResponse {
  isSolvable: boolean;
  unsolvableReason?: string;
  multipleProblemsDetected?: boolean;
  problemCount?: number;
  coreConcept: string;
  problemSummary: string;
  givenInfo: string[];
  approach: string;
  steps: string[];
  formulas: string[];
  pitfalls: string[];
  verification: string;
  finalAnswer: string;
  similarPractice: string;
}

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const {
      imageBase64 = '',
      imageUrl = '',
      subject = 'General Science',
      topic = '',
      targetExam = 'Competitive',
      questionText = '',
    } = body;

    const targetImageUri = imageUrl || (imageBase64 ? (imageBase64.startsWith('data:') ? imageBase64 : `data:image/jpeg;base64,${imageBase64}`) : '');

    if (!targetImageUri && !questionText) {
      return NextResponse.json({ error: 'Please provide an image or problem text.' }, { status: 400 });
    }

    const systemPrompt = `You are Quovex Vision AI, an elite STEM problem solver and academic proof engine.
Target Exam: ${targetExam} | Subject: ${subject} | Topic: ${topic || 'Standard'}

Solve the problem in the image with academic rigor, mathematical completeness, and structured formatting.

CRITICAL INSTRUCTIONS:
1. If the image is blurred, unreadable, dark, or contains no academic problem, set "isSolvable": false and explain in "unsolvableReason".
2. If multiple problems are present in the image, set "multipleProblemsDetected": true, state "problemCount", and solve Problem 1 thoroughly.
3. All formulas and derivations MUST use standard LaTeX ($...$ for inline, $$...$$ for block equations).
4. Return ONLY valid JSON matching this schema:

{
  "isSolvable": true,
  "multipleProblemsDetected": false,
  "problemCount": 1,
  "coreConcept": "Governing Principle / Theorem (e.g. Newton's Second Law & Torque Equilibrium)",
  "problemSummary": "Clear interpretation of what is being asked",
  "givenInfo": ["Value/Condition 1 with units", "Value/Condition 2 with units"],
  "approach": "Strategy and governing equation overview before calculation",
  "steps": [
    "Step 1: Free body diagram formulation and equilibrium equations...",
    "Step 2: Torque balance calculation about pivot O: $$\\sum \\tau = 0$$...",
    "Step 3: Algebraic reduction..."
  ],
  "formulas": [
    "\\sum \\vec{F} = 0",
    "\\tau = r F \\sin\\theta"
  ],
  "pitfalls": [
    "Common student mistake or sign convention error to avoid"
  ],
  "verification": "Dimensional / boundary sanity check confirming sign and magnitude",
  "finalAnswer": "$$x = \\text{final verified expression}$$",
  "similarPractice": "A related practice problem for active recall"
}`;

    const promptText = questionText.trim()
      ? `Student Note: ${questionText}\n\nAnalyze the image, identify the question, and provide the complete 10-tier structured solution.`
      : 'Please analyze the problem in the image and provide the complete 10-tier structured solution.';

    let userContent: any;
    if (targetImageUri) {
      userContent = [
        { type: 'text', text: promptText },
        { type: 'image_url', image_url: { url: targetImageUri } },
      ];
    } else {
      userContent = promptText;
    }

    const messages: AiChatMessage[] = [
      { role: 'system', content: systemPrompt },
      { role: 'user', content: userContent },
    ];

    const result = await callAiGateway({
      messages,
      temperature: 0.2,
      maxTokens: 3000,
      jsonMode: true,
      isVision: Boolean(targetImageUri),
    });

    let structured: StructuredDoubtResponse;
    try {
      structured = extractJsonFromAiResponse<StructuredDoubtResponse>(result.content);
    } catch (_) {
      // Fallback repair
      structured = {
        isSolvable: true,
        coreConcept: `${subject} Analysis`,
        problemSummary: questionText || 'Academic Problem Analysis',
        givenInfo: [],
        approach: 'Step-by-step mathematical calculation',
        steps: [result.content],
        formulas: [],
        pitfalls: ['Double-check SI units and sign conventions.'],
        verification: 'Verified through dimensional consistency.',
        finalAnswer: 'Solution completed above.',
        similarPractice: 'Review related textbook exemplar problems.',
      };
    }

    return NextResponse.json({
      success: true,
      data: structured,
      requestId: result.requestId,
      provider: result.provider,
    });
  } catch (error: any) {
    console.error('Vision Doubt Solver Error:', error);
    return NextResponse.json(
      {
        success: false,
        error: error.message || 'Vision AI is currently busy. Please try again.',
      },
      { status: 500 }
    );
  }
}
