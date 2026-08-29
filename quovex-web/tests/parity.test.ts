import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { calculateSm2 } from '../lib/sm2';
import { getPricingForCountry } from '../lib/pricing';
import {
  getAvailableClasses,
  getSubjectsForClass,
  getChaptersForClassAndSubject,
  getChapterById,
  NCERT_CATALOG,
} from '../lib/ncertCatalog';
import { extractJsonFromAiResponse } from '../lib/ai-gateway';

describe('Quovex Web — Mathematical & Feature Parity Tests', () => {
  it('SM-2 Spaced Repetition matches Android Sm2Calculator bounds and intervals', () => {
    // Quality 0 (Forgot / Again) -> repetitions reset to 0, interval = 1 day
    const failRes = calculateSm2(0, 5, 20, 2.5);
    assert.equal(failRes.repetitions, 0);
    assert.equal(failRes.intervalDays, 1);
    assert.ok(failRes.easinessFactor >= 1.3, 'EF must never drop below 1.3');

    // Quality 4 (Good) from fresh card (rep 0) -> reps = 1, interval = 1
    const firstRecall = calculateSm2(4, 0, 1, 2.5);
    assert.equal(firstRecall.repetitions, 1);
    assert.equal(firstRecall.intervalDays, 1);

    // Quality 4 (Good) from rep 1 -> reps = 2, interval = 6
    const secondRecall = calculateSm2(4, 1, 1, 2.5);
    assert.equal(secondRecall.repetitions, 2);
    assert.equal(secondRecall.intervalDays, 6);

    // Quality 5 (Easy) from rep 2 -> reps = 3, interval = Math.round(6 * 2.5) = 15
    const thirdRecall = calculateSm2(5, 2, 6, 2.5);
    assert.equal(thirdRecall.repetitions, 3);
    assert.equal(thirdRecall.intervalDays, 15);
    assert.ok(thirdRecall.easinessFactor > 2.5, 'EF should increase on easy recall');
  });

  it('Regional PPP Pricing resolves proper gateways and amounts', () => {
    const inPricing = getPricingForCountry('IN');
    assert.equal(inPricing.currency, 'INR');
    assert.equal(inPricing.symbol, '₹');
    assert.equal(inPricing.annual, 999);
    assert.equal(inPricing.gateway, 'razorpay');

    const usPricing = getPricingForCountry('US');
    assert.equal(usPricing.currency, 'USD');
    assert.equal(usPricing.annual, 34.99);
    assert.equal(usPricing.gateway, 'lemonsqueezy');

    const fallbackPricing = getPricingForCountry('XYZ');
    assert.equal(fallbackPricing.currency, 'USD');
    assert.equal(fallbackPricing.gateway, 'lemonsqueezy');
  });

  it('Security Invariant: Zero server secrets in client bundle or source files', () => {
    const sensitiveTokens = [
      'GROQ_API_KEY',
      'CEREBRAS_API_KEY',
      'RAZORPAY_KEY_SECRET',
      'LEMONSQUEEZY_API_KEY',
      'ADMIN_MASTER_SECRET',
      'FIREBASE_PRIVATE_KEY',
    ];

    const searchDir = (dir: string) => {
      const files = fs.readdirSync(dir);
      for (const file of files) {
        if (file === 'node_modules' || file === '.next' || file === '.git') continue;
        const fullPath = path.join(dir, file);
        const stat = fs.statSync(fullPath);
        if (stat.isDirectory()) {
          searchDir(fullPath);
        } else if (file.endsWith('.ts') || file.endsWith('.tsx') || file.endsWith('.js') || file.endsWith('.json')) {
          const content = fs.readFileSync(fullPath, 'utf8');
          for (const token of sensitiveTokens) {
            const regex = new RegExp(`${token}\\s*=\\s*['"][a-zA-Z0-9_-]{10,}['"]`, 'i');
            assert.ok(
              !regex.test(content),
              `Security violation: ${token} found in client file ${fullPath}`
            );
          }
        }
      }
    };

    searchDir(path.join(__dirname, '..'));
  });

  it('NCERT Catalog Integrity: Class 6 selection contains Science and Mathematics, zero Physics leakage', () => {
    const class6Subjects = getSubjectsForClass('Class 6');
    assert.ok(class6Subjects.includes('Science'), 'Class 6 must have Science');
    assert.ok(class6Subjects.includes('Mathematics'), 'Class 6 must have Mathematics');
    assert.ok(!class6Subjects.includes('Physics'), 'Class 6 must NOT have isolated Physics subject');

    const class6ScienceChapters = getChaptersForClassAndSubject('Class 6', 'Science');
    assert.ok(class6ScienceChapters.length > 0, 'Class 6 Science must have chapters');

    // Assert no Class 12 chapters are present in Class 6
    for (const ch of class6ScienceChapters) {
      assert.equal(ch.gradeClass, 'Class 6');
      assert.notEqual(ch.chapterTitle, 'Electric Charges and Fields');
      assert.notEqual(ch.chapterTitle, 'Current Electricity');
      assert.notEqual(ch.chapterTitle, 'Electrostatic Potential and Capacitance');
      assert.equal(ch.contentType, 'OFFICIAL_RESOURCE');
    }
  });

  it('NCERT Catalog Integrity: Class 12 Physics contains verified electromagnetism chapters', () => {
    const class12PhysicsChapters = getChaptersForClassAndSubject('Class 12', 'Physics');
    const titles = class12PhysicsChapters.map((c) => c.chapterTitle);

    assert.ok(titles.includes('Electric Charges and Fields'));
    assert.ok(titles.includes('Current Electricity'));
    assert.ok(titles.includes('Alternating Current'));

    for (const ch of class12PhysicsChapters) {
      assert.equal(ch.gradeClass, 'Class 12');
      assert.equal(ch.subject, 'Physics');
      assert.equal(ch.contentType, 'OFFICIAL_RESOURCE');
      assert.ok(ch.officialSourceUrl.startsWith('https://ncert.nic.in/'));
    }
  });

  it('AI JSON Extractor: Parses raw AI responses containing markdown code fences and LaTeX math', () => {
    const testAiResponse = `<think>Analyzing problem...</think>\`\`\`json
    {
      "isSolvable": true,
      "coreConcept": "Newton's Second Law",
      "finalAnswer": "$$F = ma$$",
      "steps": ["Step 1: Calculate \\\\vec{F}"]
    }
    \`\`\``;

    const parsed = extractJsonFromAiResponse(testAiResponse);
    assert.equal(parsed.isSolvable, true);
    assert.equal(parsed.coreConcept, "Newton's Second Law");
    assert.equal(parsed.finalAnswer, '$$F = ma$$');
  });
});
