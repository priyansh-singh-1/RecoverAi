/* ============================================================
   RecoverAI — Status Configuration
   Maps recovery statuses, priorities, actors, events to display properties.
   ============================================================ */

export const STATUS_CONFIG = {
  OPEN: { label: 'Open', color: 'var(--warning-text)', bgColor: 'var(--warning-muted)' },
  IN_PROGRESS: { label: 'In Progress', color: 'var(--info-text)', bgColor: 'var(--info-muted)' },
  RECOVERED: { label: 'Recovered', color: 'var(--success-text)', bgColor: 'var(--success-muted)' },
  FAILED: { label: 'Failed', color: 'var(--danger-text)', bgColor: 'var(--danger-muted)' },
  STOPPED: { label: 'Stopped', color: 'var(--danger-text)', bgColor: 'var(--danger-muted)' },
  EXPIRED: { label: 'Expired', color: 'var(--text-muted)', bgColor: 'var(--status-expired-muted)' },
  REQUIRES_APPROVAL: { label: 'Needs Approval', color: 'var(--purple-text)', bgColor: 'var(--purple-muted)' },
};

export const PRIORITY_CONFIG = {
  CRITICAL: { label: 'Critical', color: 'var(--priority-critical)', bgColor: 'var(--priority-critical-muted)' },
  HIGH: { label: 'High', color: 'var(--priority-high)', bgColor: 'var(--priority-high-muted)' },
  MEDIUM: { label: 'Medium', color: 'var(--priority-medium)', bgColor: 'var(--priority-medium-muted)' },
  LOW: { label: 'Low', color: 'var(--priority-low)', bgColor: 'var(--priority-low-muted)' },
};

export const PRIORITY_ORDER = { CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 };

export const STATUS_TO_VARIANT = {
  RECOVERED: 'success',
  OPEN: 'warning',
  STOPPED: 'danger',
  FAILED: 'danger',
  IN_PROGRESS: 'info',
  REQUIRES_APPROVAL: 'purple',
  EXPIRED: 'muted',
};

export const EXECUTION_STATUS_CONFIG = {
  PENDING: { label: 'Pending', color: 'var(--text-muted)', bgColor: 'var(--status-expired-muted)' },
  EXECUTING: { label: 'Executing', color: 'var(--info-text)', bgColor: 'var(--info-muted)' },
  SUCCESS: { label: 'Success', color: 'var(--success-text)', bgColor: 'var(--success-muted)' },
  FAILED: { label: 'Failed', color: 'var(--danger-text)', bgColor: 'var(--danger-muted)' },
  SKIPPED: { label: 'Skipped', color: 'var(--text-muted)', bgColor: 'var(--status-expired-muted)' },
  REQUIRES_APPROVAL: { label: 'Needs Approval', color: 'var(--purple-text)', bgColor: 'var(--purple-muted)' },
};

export const ACTOR_CONFIG = {
  RAZORPAY: { label: 'Razorpay', icon: 'Zap' },
  SYSTEM: { label: 'System', icon: 'Settings' },
  RULE_ENGINE: { label: 'Rule Engine', icon: 'GitBranch' },
  AI_AGENT: { label: 'Gemini Agent', icon: 'Bot' },
  HUMAN: { label: 'Human', icon: 'User' },
};

export const EVENT_LABELS = {
  PAYMENT_FAILED: 'Payment failed',
  PAYMENT_CAPTURED: 'Payment captured',
  RECOVERY_CASE_CREATED: 'Recovery case created',
  PRIORITY_ASSIGNED: 'Priority assigned',
  ACTION_RECOMMENDED: 'Action recommended',
  RECOVERY_MARKED_SUCCESS: 'Recovery marked success',
  RECOVERY_STOPPED: 'Recovery stopped',
  DUPLICATE_WEBHOOK_IGNORED: 'Duplicate webhook ignored',
  WEBHOOK_RECEIVED: 'Webhook received',
  WEBHOOK_PROCESSED: 'Webhook processed',
  ML_PREDICTION_GENERATED: 'ML prediction generated',
  POLICY_DECISION_GENERATED: 'Policy decision generated',
  AI_AGENT_ANALYSIS_GENERATED: 'AI agent analysis generated',
  RECOVERY_ACTION_STARTED: 'Recovery action started',
  RECOVERY_ACTION_SUCCEEDED: 'Recovery action succeeded',
  RECOVERY_ACTION_FAILED: 'Recovery action failed',
  RECOVERY_ACTION_SKIPPED: 'Recovery action skipped',
  RECOVERY_ACTION_REQUIRES_APPROVAL: 'Recovery action requires approval',
  RECOVERY_APPROVED: 'Recovery approved',
  RECOVERY_REJECTED: 'Recovery rejected',
};
