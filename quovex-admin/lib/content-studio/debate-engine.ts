/**
 * Quovex Content Studio — Multi-Agent Reasoning & Debate Engine
 *
 * Implements 3 live AI reasoning agents:
 * 1. Agent A (Architect): Proposes progressive chapter plan, pedagogical intuition, visual analogies
 * 2. Agent B (Challenger): Challenges rigor, identifies student trap misconceptions, checks edge cases
 * 3. Synthesis Agent: Reconciles both agents into an immutable EditorialBlueprint
 *
 * Confidentiality Guarantee:
 * Internal multi-agent debate logs remain server-side and are NEVER exposed to student clients.
 */

import { EvidencePack, EditorialBlueprint, BookRequestInput } from '../types/content-studio';
import { callAiGateway, extractJsonFromAiResponse } from '../ai-gateway';

export class DebateEngine {
  /**
   * Conducts the live multi-agent debate and synthesizes the EditorialBlueprint.
   */
  public async conductDebate(request: BookRequestInput, evidencePack: EvidencePack): Promise<EditorialBlueprint> {
    const blueprintId = `bp_${Date.now()}_${request.topic.toLowerCase().replace(/[^a-z0-9]+/g, '_')}`;
    const chapterCount = Math.max(1, Math.min(6, request.chapterCount || 3));

    // -------------------------------------------------------------
    // 1. AGENT A (Architect) PROPOSAL
    // -------------------------------------------------------------
    const architectSystemPrompt = `You are Agent A (The Pedagogical Architect) in the Quovex Content Studio.
Your role: Design an optimal learning sequence for a high-yield educational book on "${request.topic}".
Subject: "${request.subject}", Curriculum: "${request.curriculum}", Grade: "${request.gradeClass}".
Evidence Pack verified facts: ${JSON.stringify(evidencePack.items.map(i => i.claim))}.

Output JSON:
{
  "conceptProgression": ["step 1...", "step 2..."],
  "visualAnalogies": ["analogy 1...", "analogy 2..."],
  "proposedChapters": [
    {
      "chapterNumber": 1,
      "title": "Title",
      "targetConcepts": ["concept 1", "concept 2"],
      "difficultyCurve": "Simple",
      "realWorldScenario": "Real world scenario"
    }
  ]
}`;

    let architectResult: any;
    try {
      const architectRaw = await callAiGateway({
        messages: [
          { role: 'system', content: architectSystemPrompt },
          { role: 'user', content: `Design a ${chapterCount}-chapter sequence with progressive difficulty (Simple -> Intermediate -> Advanced) for ${request.topic}. Output JSON.` }
        ],
        temperature: 0.3,
        jsonMode: true,
        maxTokens: 2000
      });
      architectResult = extractJsonFromAiResponse(architectRaw);
    } catch (e: any) {
      console.error('Architect agent failure:', e.message);
      throw new Error(`AI_UNAVAILABLE_ARCHITECT: ${e.message || 'Architect agent LLM call failed'}`);
    }

    // -------------------------------------------------------------
    // 2. AGENT B (Challenger) CRITIQUE & MISCONCEPTIONS
    // -------------------------------------------------------------
    const challengerSystemPrompt = `You are Agent B (The Rigorous Challenger) in the Quovex Content Studio.
Your role: Scrutinize the Pedagogical Architect's proposal for "${request.topic}".
Identify where students get confused, spot mathematical pitfalls, uncover edge cases, and challenge over-simplified analogies.

Architect Proposal: ${JSON.stringify(architectResult)}

Output JSON:
{
  "identifiedRisks": ["risk 1 (e.g. confusing velocity with acceleration)..."],
  "edgeCases": ["edge case 1 (e.g. non-inertial frames, normal force on incline)..."],
  "critiqueNotes": "Specific recommendations for improving conceptual rigor"
}`;

    let challengerResult: any;
    try {
      const challengerRaw = await callAiGateway({
        messages: [
          { role: 'system', content: challengerSystemPrompt },
          { role: 'user', content: `Critique the proposed syllabus for ${request.topic}. Uncover at least 3 critical student misconception traps. Output JSON.` }
        ],
        temperature: 0.4,
        jsonMode: true,
        maxTokens: 1500
      });
      challengerResult = extractJsonFromAiResponse(challengerRaw);
    } catch (e: any) {
      console.error('Challenger agent failure:', e.message);
      throw new Error(`AI_UNAVAILABLE_CHALLENGER: ${e.message || 'Challenger agent LLM call failed'}`);
    }

    // -------------------------------------------------------------
    // 3. SYNTHESIS AGENT (Editorial Blueprint)
    // -------------------------------------------------------------
    const synthesisSystemPrompt = `You are the Synthesis Agent in the Quovex Content Studio.
Reconcile the Architect's progressive structure with the Challenger's rigor requirements.
Topic: "${request.topic}", Chapter Count: ${chapterCount}.

Output JSON matching this exact schema:
{
  "finalObjectives": ["objective 1", "objective 2", "objective 3", "objective 4"],
  "chapters": [
    {
      "chapterNumber": 1,
      "title": "Chapter Title",
      "targetConcepts": ["concept 1", "concept 2"],
      "difficultyCurve": "Simple",
      "realWorldScenario": "Real world scenario"
    }
  ],
  "curriculumAlignmentNotes": "Alignment summary"
}`;

    let synthesisResult: any;
    try {
      const synthesisRaw = await callAiGateway({
        messages: [
          { role: 'system', content: synthesisSystemPrompt },
          { role: 'user', content: `Reconcile Architect: ${JSON.stringify(architectResult)} and Challenger: ${JSON.stringify(challengerResult)}. Produce exactly ${chapterCount} chapters. Output JSON.` }
        ],
        temperature: 0.2,
        jsonMode: true,
        maxTokens: 2500
      });
      synthesisResult = extractJsonFromAiResponse(synthesisRaw);
    } catch (e: any) {
      console.error('Synthesis agent failure:', e.message);
      throw new Error(`AI_UNAVAILABLE_SYNTHESIS: ${e.message || 'Synthesis agent LLM call failed'}`);
    }

    const finalChapters = (synthesisResult.chapters || []).map((ch: any, idx: number) => ({
      chapterNumber: ch.chapterNumber || idx + 1,
      title: ch.title || `Chapter ${idx + 1}: ${request.topic}`,
      targetConcepts: ch.targetConcepts || [`Core principles of ${request.topic}`],
      difficultyCurve: (ch.difficultyCurve as any) || (idx === 0 ? 'Simple' : idx === 1 ? 'Intermediate' : 'Advanced'),
      realWorldScenario: ch.realWorldScenario || `Engineering applications of ${request.topic}.`
    })).slice(0, chapterCount);

    return {
      blueprintId,
      architectConceptProgression: architectResult.conceptProgression || [],
      architectAnalogies: architectResult.visualAnalogies || [],
      challengerIdentifiedRisks: challengerResult.identifiedRisks || [],
      challengerEdgeCases: challengerResult.edgeCases || [],
      synthesisFinalObjectives:
        synthesisResult.finalObjectives ||
        request.learningObjectives ||
        request.targetConcepts ||
        [`Understand core principles of ${request.topic}`, `Apply mathematical derivations and formulas`, `Solve numerical problems accurately`],
      synthesisChapterPlan: finalChapters,
      curriculumAlignmentNotes: synthesisResult.curriculumAlignmentNotes || `Aligned to ${request.curriculum} — ${request.gradeClass}.`,
      createdAt: Date.now()
    };
  }
}
