export interface Sm2CalculationResult {
  repetitions: number;
  intervalDays: number;
  easinessFactor: number;
  nextReviewAtMillis: number;
}

/**
 * SuperMemo SM-2 Spaced Repetition Algorithm
 * Direct mathematical parity with Android's Sm2Calculator.kt
 *
 * @param quality 0..5 rating:
 *   0 = Again / Forgot (<1m)
 *   3 = Hard (Difficult recall)
 *   4 = Good (Successful recall with hesitation)
 *   5 = Easy (Instant, perfect recall)
 */
export function calculateSm2(
  quality: number,
  currentRepetitions: number,
  currentIntervalDays: number,
  currentEf: number = 2.5,
  currentTimeMillis: number = Date.now()
): Sm2CalculationResult {
  const q = Math.max(0, Math.min(5, quality));

  let newRepetitions: number;
  let newIntervalDays: number;

  if (q < 3) {
    // Failed recall: reset repetitions to 0, review again in 1 day
    newRepetitions = 0;
    newIntervalDays = 1;
  } else {
    // Successful recall
    newRepetitions = currentRepetitions + 1;
    if (currentRepetitions === 0) {
      newIntervalDays = 1;
    } else if (currentRepetitions === 1) {
      newIntervalDays = 6;
    } else {
      newIntervalDays = Math.max(1, Math.round(currentIntervalDays * currentEf));
    }
  }

  // Calculate new Easiness Factor (EF)
  // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
  const efDelta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02);
  const newEf = Math.max(1.3, currentEf + efDelta);

  const oneDayMillis = 24 * 60 * 60 * 1000;
  const nextReviewAtMillis = currentTimeMillis + newIntervalDays * oneDayMillis;

  return {
    repetitions: newRepetitions,
    intervalDays: newIntervalDays,
    easinessFactor: Number(newEf.toFixed(2)),
    nextReviewAtMillis,
  };
}
