/**
 * Quovex Admin — Sliding-Window Rate Limiter & Abuse Shield
 * In-memory sliding-window token bucket preventing brute-force administrative actions.
 */

interface RateLimitRecord {
  timestamps: number[];
}

const rateLimitStore = new Map<string, RateLimitRecord>();

export interface RateLimitConfig {
  maxRequests: number;
  windowMs: number;
}

export interface RateLimitResult {
  success: boolean;
  limit: number;
  remaining: number;
  resetTime: number;
}

/**
 * Checks and updates rate limit for a given key (IP address or user ID)
 */
export function checkRateLimit(
  key: string,
  config: RateLimitConfig = { maxRequests: 60, windowMs: 60_000 }
): RateLimitResult {
  const now = Date.now();
  const windowStart = now - config.windowMs;

  const record = rateLimitStore.get(key) || { timestamps: [] };

  // Filter out timestamps outside the sliding window
  const validTimestamps = record.timestamps.filter((ts) => ts > windowStart);

  if (validTimestamps.length >= config.maxRequests) {
    const oldestTimestamp = validTimestamps[0];
    const resetTime = oldestTimestamp + config.windowMs;

    return {
      success: false,
      limit: config.maxRequests,
      remaining: 0,
      resetTime,
    };
  }

  // Record this request
  validTimestamps.push(now);
  rateLimitStore.set(key, { timestamps: validTimestamps });

  return {
    success: true,
    limit: config.maxRequests,
    remaining: config.maxRequests - validTimestamps.length,
    resetTime: now + config.windowMs,
  };
}

/**
 * Helper to cleanup old rate limit records periodically
 */
export function pruneExpiredRateLimits(): void {
  const now = Date.now();
  const maxWindow = 300_000; // 5 mins

  for (const [key, record] of rateLimitStore.entries()) {
    const validTimestamps = record.timestamps.filter((ts) => now - ts < maxWindow);
    if (validTimestamps.length === 0) {
      rateLimitStore.delete(key);
    } else {
      rateLimitStore.set(key, { timestamps: validTimestamps });
    }
  }
}
