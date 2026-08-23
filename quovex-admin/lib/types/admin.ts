/**
 * Quovex Admin Control Center — Core Domain Types
 */

export type AdminRole = 'SUPER_ADMIN' | 'ADMIN' | 'EDITOR' | 'MODERATOR' | 'ANALYST';

export interface AdminUser {
  uid: string;
  email: string;
  displayName: string;
  role: AdminRole;
  createdAt: number;
  lastLoginAt: number;
  status: 'ACTIVE' | 'SUSPENDED';
}

export type UserStatus = 'ACTIVE' | 'SUSPENDED' | 'INACTIVE';

export interface UserAccount {
  uid: string;
  email: string;
  displayName: string;
  avatarUrl?: string;
  examTarget?: string;
  gradeClass?: string;
  status: UserStatus;
  createdAt: number;
  lastActiveAt: number;
  studyMinutesTotal: number;
  materialsCount: number;
  flashcardsCount: number;
  quizzesTakenCount: number;
  streakDays: number;
}

export type AuditAction =
  | 'LOGIN'
  | 'ROLE_CHANGE'
  | 'USER_SUSPEND'
  | 'USER_RESTORE'
  | 'FLAG_CHANGE'
  | 'CONTENT_APPROVE'
  | 'CONTENT_PUBLISH'
  | 'CONTENT_UNPUBLISH'
  | 'CONTENT_DELETE'
  | 'MODERATION_ACTION'
  | 'KEY_STATUS_CHANGE'
  | 'NOTIFICATION_SEND'
  | 'SETTINGS_CHANGE';

export interface AuditLogEntry {
  id: string;
  timestamp: number;
  actorUid: string;
  actorEmail: string;
  actorRole: AdminRole;
  action: AuditAction;
  targetId: string;
  targetType: 'USER' | 'BOOK' | 'CHAPTER' | 'FEATURE_FLAG' | 'AI_KEY' | 'NOTIFICATION' | 'MODERATION_REPORT' | 'SETTINGS';
  details: string;
  success: boolean;
  ipAddress?: string;
}

export interface FeatureFlag {
  id: string;
  name: string;
  key: string;
  description: string;
  enabled: boolean;
  rolloutPercentage: number;
  environment: 'PRODUCTION' | 'STAGING' | 'ALL';
  lastModifiedAt: number;
  modifiedBy: string;
}

export type ModerationTargetType = 'USER' | 'STUDY_ROOM' | 'MATERIAL' | 'COMMENT';
export type ModerationStatus = 'PENDING' | 'DISMISSED' | 'WARNED' | 'SUSPENDED' | 'RESOLVED';

export interface ModerationReport {
  id: string;
  targetId: string;
  targetType: ModerationTargetType;
  reportedByUid: string;
  reason: string;
  details?: string;
  status: ModerationStatus;
  createdAt: number;
  resolvedAt?: number;
  resolvedBy?: string;
  actionTaken?: 'DISMISS' | 'WARN' | 'SUSPEND' | 'REMOVE_CONTENT';
  resolutionNotes?: string;
}

export interface NotificationCampaign {
  id: string;
  title: string;
  body: string;
  targetAudience: 'ALL_USERS' | 'CLASS_SPECIFIC' | 'EXAM_SPECIFIC' | 'INACTIVE_7D' | 'PREMIUM';
  targetValue?: string; // e.g. "Class 11" or "JEE"
  status: 'DRAFT' | 'SENT' | 'FAILED';
  sentCount: number;
  failureCount: number;
  createdAt: number;
  sentAt?: number;
  sentBy: string;
}

export type ServiceHealthState = 'HEALTHY' | 'DEGRADED' | 'DOWN' | 'UNKNOWN';

export interface ServiceHealth {
  serviceName: string;
  category: 'INFRASTRUCTURE' | 'AI_GATEWAY' | 'DATABASE' | 'SERVICES';
  status: ServiceHealthState;
  latencyMs: number;
  lastCheckedAt: number;
  message: string;
  endpoint?: string;
}

export interface PlatformMetrics {
  totalUsers: number;
  activeUsersToday: number;
  newUsers7d: number;
  totalSessions: number;
  totalStudyMinutes: number;
  totalAiRequests: number;
  totalMaterials: number;
  totalFlashcards: number;
  totalQuizzes: number;
  publishedOriginalsCount: number;
  pendingReviewCount: number;
  activeGenerationJobsCount: number;
  billingStatus: 'CONNECTED' | 'UNAVAILABLE';
  totalRevenueFormatted: string;
}

export interface AiKeyInfo {
  id: string;
  provider: 'Groq' | 'Cerebras';
  maskedKey: string;
  model: string;
  status: 'ACTIVE' | 'COOLDOWN' | 'DISABLED';
  requestsToday: number;
  successRate: number;
  lastUsedAt: number;
  cooldownUntil?: number;
}
