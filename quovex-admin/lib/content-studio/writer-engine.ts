/**
 * Quovex Content Studio — Original Educational Writer Engine
 *
 * Implements the core authoring philosophy:
 * UNDERSTAND -> RETHINK -> REORGANIZE -> EXPLAIN -> TEACH
 *
 * Generates original educational literature dynamically via live server-side AI:
 * - Clean mathematical formatting (x², √x, sin²θ, F = ma, m/s²)
 * - Authentic real-world engineering and scientific scenarios
 * - Step-by-step worked examples with difficulty progression
 * - Common student traps and misconceptions
 * - Integrated Spaced Repetition Flashcards (SM-2 ready)
 * - Concept-reinforcing practice quizzes with pedagogical explanations
 */

import {
  BookRequestInput,
  EvidencePack,
  EditorialBlueprint,
  QuovexOriginalBook,
  BookChapter,
  ChapterSection,
  WorkedExample,
  RealWorldExample,
  CommonMistake,
  IntegratedFlashcard,
  IntegratedQuizQuestion
} from '../types/content-studio';
import { callAiGateway, extractJsonFromAiResponse } from '../ai-gateway';

export class WriterEngine {
  /**
   * Generates a complete draft book from the Editorial Blueprint and Evidence Pack.
   */
  public async writeDraftBook(
    request: BookRequestInput,
    evidencePack: EvidencePack,
    blueprint: EditorialBlueprint,
    jobId: string,
    existingBookId?: string
  ): Promise<QuovexOriginalBook> {
    const bookId = existingBookId || `book_${Date.now()}_${request.topic.toLowerCase().replace(/[^a-z0-9]+/g, '_')}`;
    const now = Date.now();

    // Generate each chapter in the blueprint via live AI
    const chapters: BookChapter[] = [];
    for (const planItem of blueprint.synthesisChapterPlan) {
      const chapter = await this.generateChapterWithAi(
        planItem.chapterNumber,
        planItem.title,
        planItem.targetConcepts,
        planItem.difficultyCurve,
        planItem.realWorldScenario,
        request.subject,
        request.curriculum,
        request.gradeClass,
        evidencePack
      );
      chapters.push(chapter);
    }

    return {
      id: bookId,
      contentType: 'QUOVEX_ORIGINAL',
      title: request.title,
      subtitle: `${request.subject} • ${request.curriculum} — ${request.gradeClass}`,
      description: `Comprehensive active-learning guide to ${request.topic}, featuring intuitive derivations, free body diagrams, real-world case studies, and integrated spaced repetition.`,
      subject: request.subject,
      topic: request.topic,
      language: request.language || 'en',
      countryRegion: request.countryRegion || 'IN',
      curriculum: request.curriculum,
      gradeClass: request.gradeClass,
      exam: request.exam,
      difficulty: request.difficulty,
      targetReadingTimeMinutes: request.targetReadingTimeMinutes || 45,
      chapterCount: chapters.length,

      generationJobId: jobId,
      version: 1,
      approvalStatus: 'DRAFT',
      isStaging: request.isStaging ?? true,

      coverImageUrl: "https://images.unsplash.com/photo-1635070041078-e363dbe005cb?auto=format&fit=crop&w=1200&q=80",
      introduction: `Welcome to ${request.title}. This Quovex Original is engineered to eliminate confusion and build ironclad conceptual intuition in ${request.topic}. Each chapter blends visual analogies with rigorous mathematical problem solving.`,
      learningObjectives: blueprint.synthesisFinalObjectives,
      prerequisites: request.prerequisites.length > 0 ? request.prerequisites : [
        "Basic algebra and coordinate geometry",
        "Elementary vector addition and resolution",
        "Basic concepts of velocity, speed, and time"
      ],
      chapters: chapters,

      versionHistory: [
        {
          version: 1,
          generationJobId: jobId,
          createdAt: now,
          createdBy: "quovex_ai_content_studio",
          revisionReason: "Initial multi-agent generated draft from verified evidence pack"
        }
      ],

      createdBy: "admin_content_studio",
      createdAt: now,
      updatedAt: now
    };
  }

  /**
   * Generates a single structured educational chapter via live server-side AI.
   */
  public async generateChapterWithAi(
    chapterNumber: number,
    title: string,
    concepts: string[],
    difficultyCurve: 'Simple' | 'Intermediate' | 'Advanced',
    realWorldContext: string,
    subject: string,
    curriculum: string,
    gradeClass: string,
    evidencePack: EvidencePack
  ): Promise<BookChapter> {
    const systemPrompt = `You are the Lead Educational Writer for Quovex Originals.
Core philosophy: UNDERSTAND -> RETHINK -> REORGANIZE -> EXPLAIN -> TEACH.
NEVER copy verbatim or mechanically paraphrase. Use clean unicode math (e.g. x², √x, sin²θ, F = ma, m/s²).

You must write Chapter ${chapterNumber}: "${title}".
Target Concepts: ${JSON.stringify(concepts)}.
Difficulty Curve: ${difficultyCurve}.
Real World Context: "${realWorldContext}".
Subject: "${subject}", Curriculum: "${curriculum}", Grade: "${gradeClass}".

Output ONLY a valid JSON object matching this exact schema:
{
  "summary": "High yield 2-3 sentence overview of this chapter",
  "learningObjectives": ["objective 1", "objective 2", "objective 3"],
  "sections": [
    {
      "sectionNumber": "${chapterNumber}.1",
      "title": "Section Title",
      "conceptualExplanation": "Deep, intuitive pedagogical prose explaining the core mechanism step-by-step...",
      "visualAnalogy": "Vivid physical mental model / analogy...",
      "workedExamples": [
        {
          "problemStatement": "Realistic numerical or conceptual problem...",
          "stepByStepSolution": [
            { "stepNumber": 1, "explanation": "Step 1 text...", "mathFormula": "F_net = m · a" }
          ],
          "keyTakeaway": "Core insight to remember...",
          "difficulty": "${difficultyCurve}"
        }
      ],
      "realWorldExamples": [
        {
          "domain": "AEROSPACE",
          "title": "Real World Case Title",
          "narrative": "How this principle functions in technology or nature...",
          "physicsOrConceptPrinciple": "Scientific principle applied"
        }
      ],
      "commonMistakes": [
        {
          "misconception": "Common trap students fall into...",
          "whyStudentsMakeIt": "Why intuition fails...",
          "correctUnderstanding": "The rigorous correct way...",
          "quickCheck": "Fast mental verification rule..."
        }
      ],
      "summaryPoints": ["Key takeaway 1", "Key takeaway 2"]
    }
  ],
  "quickRevisionBulletPoints": ["Bullet 1", "Bullet 2", "Bullet 3"],
  "flashcards": [
    {
      "frontPrompt": "Active recall question",
      "backAnswer": "Concise answer",
      "conceptTag": "Tag",
      "difficultyRating": 2
    }
  ],
  "quizQuestions": [
    {
      "question": "Conceptual MCQ question testing deep understanding...",
      "options": ["Option A", "Option B", "Option C", "Option D"],
      "correctIndex": 0,
      "pedagogicalExplanation": "Why this answer is mathematically and conceptually correct...",
      "distractorExplanations": ["Why Option B fails...", "Why Option C fails...", "Why Option D fails..."],
      "formulaReference": "Governing formula"
    }
  ]
}`;

    try {
      const rawAiResponse = await callAiGateway({
        messages: [
          { role: 'system', content: systemPrompt },
          { role: 'user', content: `Author Chapter ${chapterNumber}: "${title}". Include 2 comprehensive sections with worked examples, real-world case studies, common mistakes, 3 flashcards, and 1 concept-testing quiz question. Output JSON only.` }
        ],
        temperature: 0.35,
        jsonMode: true,
        maxTokens: 4000
      });

      const parsed = extractJsonFromAiResponse<any>(rawAiResponse);

      const sections: ChapterSection[] = (parsed.sections || []).map((sec: any, idx: number) => ({
        id: `sec_${chapterNumber}_${idx + 1}`,
        sectionNumber: sec.sectionNumber || `${chapterNumber}.${idx + 1}`,
        title: sec.title || `Section ${chapterNumber}.${idx + 1}`,
        conceptualExplanation: sec.conceptualExplanation || `Detailed pedagogical explanation of ${title}.`,
        visualAnalogy: sec.visualAnalogy || `Physical intuitive model for ${title}.`,
        workedExamples: (sec.workedExamples || []).map((ex: any, eIdx: number) => ({
          id: `ex_${chapterNumber}_${idx + 1}_${eIdx + 1}`,
          problemStatement: ex.problemStatement || 'Worked problem statement.',
          stepByStepSolution: ex.stepByStepSolution || [{ stepNumber: 1, explanation: 'Step 1 calculation.', mathFormula: 'F = m · a' }],
          keyTakeaway: ex.keyTakeaway || 'Core lesson.',
          difficulty: (ex.difficulty as any) || difficultyCurve
        })),
        realWorldExamples: (sec.realWorldExamples || []).map((rw: any, rIdx: number) => ({
          id: `rw_${chapterNumber}_${idx + 1}_${rIdx + 1}`,
          domain: (rw.domain as any) || 'TECHNOLOGY',
          title: rw.title || 'Practical Application',
          narrative: rw.narrative || 'Real world engineering application.',
          physicsOrConceptPrinciple: rw.physicsOrConceptPrinciple || 'Applied scientific principle.'
        })),
        commonMistakes: (sec.commonMistakes || []).map((cm: any, cIdx: number) => ({
          id: `cm_${chapterNumber}_${idx + 1}_${cIdx + 1}`,
          misconception: cm.misconception || 'Common misconception.',
          whyStudentsMakeIt: cm.whyStudentsMakeIt || 'Intuition confusion.',
          correctUnderstanding: cm.correctUnderstanding || 'Correct principle.',
          quickCheck: cm.quickCheck || 'Rule to remember.'
        })),
        summaryPoints: sec.summaryPoints || [`Core summary of Section ${chapterNumber}.${idx + 1}`]
      }));

      const flashcards: IntegratedFlashcard[] = (parsed.flashcards || []).map((fc: any, fIdx: number) => ({
        id: `fc_${chapterNumber}_${fIdx + 1}`,
        frontPrompt: fc.frontPrompt || `What is the primary principle of ${title}?`,
        backAnswer: fc.backAnswer || `Fundamental concept of ${title}.`,
        conceptTag: fc.conceptTag || title,
        difficultyRating: Number(fc.difficultyRating) || (difficultyCurve === 'Simple' ? 1 : difficultyCurve === 'Intermediate' ? 2 : 3)
      }));

      const quizQuestions: IntegratedQuizQuestion[] = (parsed.quizQuestions || []).map((q: any, qIdx: number) => ({
        id: `q_${chapterNumber}_${qIdx + 1}`,
        question: q.question || `Practice question for ${title}`,
        options: q.options?.length === 4 ? q.options : ['Option A', 'Option B', 'Option C', 'Option D'],
        correctIndex: typeof q.correctIndex === 'number' ? q.correctIndex : 0,
        pedagogicalExplanation: q.pedagogicalExplanation || `Pedagogical breakdown of ${title}.`,
        distractorExplanations: q.distractorExplanations || ['Incorrect distractor analysis.'],
        formulaReference: q.formulaReference || 'F = m · a'
      }));

      if (flashcards.length === 0) {
        flashcards.push({
          id: `fc_${chapterNumber}_1`,
          frontPrompt: `State the fundamental principle of ${title}.`,
          backAnswer: `Core mathematical law governing ${title}.`,
          conceptTag: title,
          difficultyRating: 2
        });
      }

      if (quizQuestions.length === 0) {
        quizQuestions.push({
          id: `q_${chapterNumber}_1`,
          question: `Which statement correctly describes ${title}?`,
          options: [
            'Correct scientific definition as established by standard physics',
            'Opposite secondary effect',
            'Alternative unrelated phenomenon',
            'None of the above'
          ],
          correctIndex: 0,
          pedagogicalExplanation: `Directly derived from the fundamental equations of ${title}.`,
          distractorExplanations: ['Incorrect distractor analysis.'],
          formulaReference: 'F = m · a'
        });
      }

      return {
        chapterNumber,
        title,
        summary: parsed.summary || `Chapter ${chapterNumber} explores ${title} with physical intuition and mathematical problem solving.`,
        learningObjectives: parsed.learningObjectives || concepts,
        sections,
        quickRevisionBulletPoints: parsed.quickRevisionBulletPoints || [
          `Mastery of ${title} requires rigorous vector decomposition.`,
          `Check units and dimensional consistency in all equations.`
        ],
        flashcards,
        quizQuestions
      };
    } catch (err: any) {
      console.error(`AI Chapter ${chapterNumber} writer failure:`, err.message);
      throw new Error(`AI_UNAVAILABLE_CHAPTER_${chapterNumber}: ${err.message || 'Server-side LLM call failed or produced invalid structure'}`);
    }
  }

  /**
   * Regenerates a single specific section within a chapter (surgical edit) via live AI.
   */
  public async regenerateSectionWithAi(
    chapterNumber: number,
    sectionNumber: string,
    topic: string,
    subject: string
  ): Promise<ChapterSection> {
    const prompt = `Rewrite Section ${sectionNumber} on "${topic}" in ${subject} with deepened pedagogical rigor, step-by-step worked numericals with clean unicode formulas, a real-world case study, and common student traps. Output valid JSON.`;

    try {
      const rawAiResponse = await callAiGateway({
        messages: [
          { role: 'system', content: 'You are the Quovex Section Editor. Output valid JSON for a ChapterSection.' },
          { role: 'user', content: prompt }
        ],
        temperature: 0.3,
        jsonMode: true,
        maxTokens: 2500
      });

      const sec = extractJsonFromAiResponse<any>(rawAiResponse);
      return {
        id: `sec_${chapterNumber}_${sectionNumber.replace('.', '_')}_revised`,
        sectionNumber,
        title: sec.title || `${topic} — Revised Analysis`,
        conceptualExplanation: sec.conceptualExplanation || `Revised pedagogical breakdown of ${topic}.`,
        visualAnalogy: sec.visualAnalogy || `Mental model for ${topic}.`,
        workedExamples: (sec.workedExamples || []).map((ex: any, idx: number) => ({
          id: `ex_rev_${Date.now()}_${idx + 1}`,
          problemStatement: ex.problemStatement || 'Problem statement.',
          stepByStepSolution: ex.stepByStepSolution || [{ stepNumber: 1, explanation: 'Step 1.', mathFormula: 'F = m · a' }],
          keyTakeaway: ex.keyTakeaway || 'Key takeaway.',
          difficulty: 'Advanced'
        })),
        realWorldExamples: (sec.realWorldExamples || []).map((rw: any, idx: number) => ({
          id: `rw_rev_${Date.now()}_${idx + 1}`,
          domain: (rw.domain as any) || 'TECHNOLOGY',
          title: rw.title || 'Application',
          narrative: rw.narrative || 'Real world context.',
          physicsOrConceptPrinciple: rw.physicsOrConceptPrinciple || 'Principle.'
        })),
        commonMistakes: (sec.commonMistakes || []).map((cm: any, idx: number) => ({
          id: `cm_rev_${Date.now()}_${idx + 1}`,
          misconception: cm.misconception || 'Misconception.',
          whyStudentsMakeIt: cm.whyStudentsMakeIt || 'Reason.',
          correctUnderstanding: cm.correctUnderstanding || 'Correction.',
          quickCheck: cm.quickCheck || 'Check.'
        })),
        summaryPoints: sec.summaryPoints || [`Summary of Section ${sectionNumber}.`]
      };
    } catch (e: any) {
      console.error('Section regeneration AI failure:', e.message);
      throw new Error(`AI_UNAVAILABLE_SECTION_REGEN: ${e.message || 'AI section generation failed'}`);
    }
  }
}

