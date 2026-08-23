import { NextResponse } from 'next/server';
import { BookRequestInput } from '@/lib/types/content-studio';

let savedRequests: Array<BookRequestInput & { id: string; createdAt: number }> = [];

export async function GET() {
  return NextResponse.json({
    success: true,
    total: savedRequests.length,
    requests: savedRequests,
  });
}

export async function POST(req: Request) {
  try {
    const body: BookRequestInput = await req.json();
    if (!body.title || !body.subject || !body.topic) {
      return NextResponse.json(
        { success: false, error: 'Missing mandatory fields: title, subject, topic' },
        { status: 400 }
      );
    }

    const item = {
      ...body,
      id: `req_${Date.now()}`,
      createdAt: Date.now(),
    };
    savedRequests.unshift(item);

    return NextResponse.json({
      success: true,
      request: item,
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Invalid request payload' },
      { status: 400 }
    );
  }
}
