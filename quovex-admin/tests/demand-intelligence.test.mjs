import test from 'node:test';
import assert from 'node:assert/strict';
import {
  normalizeCount,
  normalizeAccuracyFriction,
  normalizeFailureRate,
  computeTopicDemandSignal,
  DEFAULT_SIGNAL_WEIGHTS,
} from '../lib/demand-intelligence.ts';

test('Demand Intelligence — Normalization Bounds & Math', async (t) => {
  await t.test('normalizeCount produces bounded values between 0 and 100', () => {
    assert.equal(normalizeCount(0, 2500), 0);
    assert.equal(normalizeCount(-50, 2500), 0);
    assert.equal(normalizeCount(1250, 2500), 50);
    assert.equal(normalizeCount(2500, 2500), 100);
    assert.equal(normalizeCount(5000, 2500), 100); // Caps at 100
  });

  await t.test('normalizeAccuracyFriction maps lower accuracy to higher demand friction', () => {
    assert.equal(normalizeAccuracyFriction(1.0), 0); // 100% accuracy = 0 friction
    assert.equal(normalizeAccuracyFriction(0.5), 50); // 50% accuracy = 50 friction
    assert.equal(normalizeAccuracyFriction(0.0), 100); // 0% accuracy = 100 friction
    assert.equal(normalizeAccuracyFriction(0.58), 42);
  });

  await t.test('normalizeFailureRate maps directly to percentage', () => {
    assert.equal(normalizeFailureRate(0.0), 0);
    assert.equal(normalizeFailureRate(0.42), 42);
    assert.equal(normalizeFailureRate(1.0), 100);
  });

  await t.test('computeTopicDemandSignal produces deterministic and explainable score', () => {
    const rawData = {
      topicId: 'physics_mechanics_1',
      topicName: "Newton's Laws of Motion",
      subjectId: 'physics',
      subjectName: 'Physics',
      subjectCategory: 'SCIENCE',
      countryRegion: 'IN',
      curriculum: 'CBSE / JEE',
      gradeClass: 'Class 11',
      questionCount: 1840,
      quizMistakeCount: 3950,
      lowAccuracyCount: 1420,
      flashcardFailureCount: 2100,
      imageDoubtCount: 920,
      affectedStudents: 810,
      averageAccuracy: 0.58,
      failureRate: 0.42,
    };

    const signal = computeTopicDemandSignal(rawData);

    assert.ok(signal.demandScore >= 0 && signal.demandScore <= 100, 'Demand score must be 0-100');
    assert.equal(signal.topicId, 'physics_mechanics_1');
    assert.equal(signal.normalizedQuestions, Math.round((1840 / 2500) * 100)); // 74
    assert.equal(signal.normalizedMistakes, Math.round((3950 / 5000) * 100)); // 79
    assert.equal(signal.normalizedLowAccuracy, 42);

    // Verify weights and breakdown explainability
    assert.ok(signal.contributingMetrics.questionScore > 0);
    assert.ok(signal.contributingMetrics.mistakeScore > 0);
    assert.ok(signal.contributingMetrics.studentBreadthScore > 0);
  });
});
