/**
 * Quovex Demand Intelligence Engine
 *
 * Transforms real, anonymized student learning friction signals into deterministic,
 * explainable, and normalized (0-100) demand scores.
 *
 * ZERO MOCK DATA GUARANTEE:
 * - When no student activity exists in the database, demand signals list is EMPTY ([])
 * - ZERO fake student counts
 * - ZERO fake quiz mistakes
 * - ZERO fake flashcard failures
 * - ZERO PII or chat transcripts
 */

import { TopicDemandSignal, SignalWeights } from './types/content-studio';

export const DEFAULT_SIGNAL_WEIGHTS: SignalWeights = {
  questionWeight: 1.5,
  mistakeWeight: 2.0,
  lowAccuracyWeight: 2.5,
  flashcardFailureWeight: 2.0,
  imageDoubtWeight: 1.8,
  affectedStudentsWeight: 3.0,
};

// Benchmark saturations for normalization curves
const BENCHMARK_QUESTIONS_CAP = 2500;
const BENCHMARK_MISTAKES_CAP = 5000;
const BENCHMARK_FLASHCARD_FAILURES_CAP = 3000;
const BENCHMARK_IMAGE_DOUBTS_CAP = 1500;
const BENCHMARK_STUDENTS_CAP = 1000;

/**
 * Normalizes a raw count to a bounded 0-100 scale using a deterministic linear curve with cap.
 */
export function normalizeCount(rawCount: number, benchmarkCap: number): number {
  if (rawCount <= 0) return 0;
  if (rawCount >= benchmarkCap) return 100;
  const ratio = rawCount / benchmarkCap;
  return Math.min(100, Math.max(0, Math.round(ratio * 100)));
}

/**
 * Normalizes low accuracy rate (0.0 to 1.0) to a 0-100 demand friction scale.
 * Lower accuracy = Higher student struggle = Higher demand.
 */
export function normalizeAccuracyFriction(averageAccuracy: number): number {
  const boundedAccuracy = Math.min(1.0, Math.max(0.0, averageAccuracy));
  return Math.round((1.0 - boundedAccuracy) * 100);
}

/**
 * Normalizes flashcard failure / lapse rate (0.0 to 1.0) to a 0-100 demand friction scale.
 */
export function normalizeFailureRate(failureRate: number): number {
  const boundedRate = Math.min(1.0, Math.max(0.0, failureRate));
  return Math.round(boundedRate * 100);
}

export interface RawDemandData {
  topicId: string;
  topicName: string;
  subjectId: string;
  subjectName: string;
  subjectCategory: string;
  countryRegion: string;
  curriculum: string;
  gradeClass: string;
  exam?: string;
  language?: string;

  questionCount: number;
  quizMistakeCount: number;
  lowAccuracyCount: number;
  flashcardFailureCount: number;
  imageDoubtCount: number;
  affectedStudents: number;
  averageAccuracy: number;
  failureRate: number;

  periodStart?: number;
  periodEnd?: number;
}

/**
 * Calculates a fully explainable, deterministic demand signal from raw aggregated statistics.
 */
export function computeTopicDemandSignal(
  raw: RawDemandData,
  customWeights: SignalWeights = DEFAULT_SIGNAL_WEIGHTS
): TopicDemandSignal {
  const weights = customWeights;

  const normQ = normalizeCount(raw.questionCount, BENCHMARK_QUESTIONS_CAP);
  const normM = normalizeCount(raw.quizMistakeCount, BENCHMARK_MISTAKES_CAP);
  const normAcc = normalizeAccuracyFriction(raw.averageAccuracy);
  const normFC = normalizeCount(raw.flashcardFailureCount, BENCHMARK_FLASHCARD_FAILURES_CAP);
  const normImg = normalizeCount(raw.imageDoubtCount, BENCHMARK_IMAGE_DOUBTS_CAP);
  const normStu = normalizeCount(raw.affectedStudents, BENCHMARK_STUDENTS_CAP);

  const qContribution = normQ * weights.questionWeight;
  const mContribution = normM * weights.mistakeWeight;
  const accContribution = normAcc * weights.lowAccuracyWeight;
  const fcContribution = normFC * weights.flashcardFailureWeight;
  const imgContribution = normImg * weights.imageDoubtWeight;
  const stuContribution = normStu * weights.affectedStudentsWeight;

  const totalWeight =
    weights.questionWeight +
    weights.mistakeWeight +
    weights.lowAccuracyWeight +
    weights.flashcardFailureWeight +
    weights.imageDoubtWeight +
    weights.affectedStudentsWeight;

  const totalScore =
    (qContribution +
      mContribution +
      accContribution +
      fcContribution +
      imgContribution +
      stuContribution) /
    totalWeight;

  const finalDemandScore = Math.min(100, Math.max(0, Math.round(totalScore)));
  const now = Date.now();

  return {
    id: `demand_${raw.topicId}_${raw.gradeClass.replace(/\s+/g, '_')}`,
    topicId: raw.topicId,
    topicName: raw.topicName,
    subjectId: raw.subjectId,
    subjectName: raw.subjectName,
    subjectCategory: raw.subjectCategory,
    countryRegion: raw.countryRegion,
    curriculum: raw.curriculum,
    gradeClass: raw.gradeClass,
    exam: raw.exam,
    language: raw.language || 'en',

    questionCount: raw.questionCount,
    quizMistakeCount: raw.quizMistakeCount,
    lowAccuracyCount: raw.lowAccuracyCount,
    flashcardFailureCount: raw.flashcardFailureCount,
    imageDoubtCount: raw.imageDoubtCount,
    affectedStudents: raw.affectedStudents,
    averageAccuracy: raw.averageAccuracy,
    failureRate: raw.failureRate,

    normalizedQuestions: normQ,
    normalizedMistakes: normM,
    normalizedLowAccuracy: normAcc,
    normalizedFlashcardFailures: normFC,
    normalizedImageDoubts: normImg,
    normalizedAffectedStudents: normStu,

    demandScore: finalDemandScore,
    weights: weights,
    contributingMetrics: {
      questionScore: Math.round((qContribution / totalWeight) * 10) / 10,
      mistakeScore: Math.round((mContribution / totalWeight) * 10) / 10,
      accuracyPenaltyScore: Math.round((accContribution / totalWeight) * 10) / 10,
      flashcardFailureScore: Math.round((fcContribution / totalWeight) * 10) / 10,
      imageDoubtScore: Math.round((imgContribution / totalWeight) * 10) / 10,
      studentBreadthScore: Math.round((stuContribution / totalWeight) * 10) / 10,
    },

    periodStart: raw.periodStart || now - 30 * 24 * 60 * 60 * 1000,
    periodEnd: raw.periodEnd || now,
    updatedAt: now,
  };
}
