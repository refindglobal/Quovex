import { NextResponse } from 'next/server';
import { computeTopicDemandSignal } from '@/lib/demand-intelligence';
import { TopicDemandSignal } from '@/lib/types/content-studio';

// Live in-memory / database store for demand signals (Starts completely empty)
const liveDemandSignals: Map<string, TopicDemandSignal> = new Map();

export async function GET() {
  const signals = Array.from(liveDemandSignals.values()).sort(
    (a, b) => b.demandScore - a.demandScore
  );

  return NextResponse.json({
    success: true,
    total: signals.length,
    signals: signals,
  });
}

export async function POST(req: Request) {
  try {
    const body = await req.json();
    const newSignal = computeTopicDemandSignal(body);
    liveDemandSignals.set(newSignal.id, newSignal);

    return NextResponse.json({
      success: true,
      signal: newSignal,
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Invalid demand payload' },
      { status: 400 }
    );
  }
}
