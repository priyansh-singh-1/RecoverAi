/* ============================================================
   RecoverAI — Formatting Utilities
   ============================================================ */

/**
 * Format a number as Indian Rupees (INR).
 */
export const formatINR = (amount) => {
  if (amount === null || amount === undefined) return '\u2014';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(amount);
};

/**
 * Format a number as INR with paise.
 */
export const formatINRDetailed = (amount) => {
  if (amount === null || amount === undefined) return '\u2014';
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
};

/**
 * Format a LocalDateTime string from the backend.
 */
export const formatDateTime = (dateStr) => {
  if (!dateStr) return '\u2014';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return '\u2014';
  return new Intl.DateTimeFormat('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date);
};

/**
 * Format time only.
 */
export const formatTime = (dateStr) => {
  if (!dateStr) return '\u2014';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return '\u2014';
  return new Intl.DateTimeFormat('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(date);
};

/**
 * Format date only.
 */
export const formatDate = (dateStr) => {
  if (!dateStr) return '\u2014';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return '\u2014';
  return new Intl.DateTimeFormat('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(date);
};

/**
 * Format a relative time from now.
 */
export const formatRelativeTime = (dateStr) => {
  if (!dateStr) return '\u2014';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return '\u2014';

  const now = Date.now();
  const diff = now - date.getTime();
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (seconds < 60) return `${seconds}s ago`;
  if (minutes < 60) return `${minutes}m ago`;
  if (hours < 24) return `${hours}h ago`;
  return `${days}d ago`;
};

/**
 * Format a waiting duration.
 */
export const formatWaitTime = (fromStr) => {
  if (!fromStr) return '\u2014';
  const from = new Date(fromStr);
  if (Number.isNaN(from.getTime())) return '\u2014';
  return formatRelativeTime(fromStr);
};

/**
 * Truncate a payment ID for display.
 */
export const truncatePaymentId = (id) => {
  if (!id) return '\u2014';
  if (id.length <= 16) return id;
  return `${id.slice(0, 8)}...${id.slice(-6)}`;
};

/**
 * Format recovery probability as percentage string.
 */
export const formatProbability = (prob) => {
  if (prob === null || prob === undefined) return '\u2014';
  return `${Math.round(prob * 100)}%`;
};

/**
 * Format a recovery action enum to a human-readable label.
 */
export const formatAction = (action) => {
  if (!action) return '\u2014';
  return action
    .replaceAll('_', ' ')
    .toLowerCase()
    .replace(/^\w/, (c) => c.toUpperCase());
};

/**
 * Format large numbers with Indian comma separators.
 */
export const formatNumber = (n) => {
  if (n === null || n === undefined) return '\u2014';
  return new Intl.NumberFormat('en-IN').format(n);
};

/**
 * Format a recovery rate percentage.
 */
export const formatRate = (rate) => {
  if (rate === null || rate === undefined) return '\u2014';
  return `${rate.toFixed(2)}%`;
};

/**
 * Derives the semantic Recovery Strategy for a given case.
 */
export const getRecoveryStrategy = (c) => {
  if (!c) return null;
  if (c.status === 'RECOVERED') {
    return c.successfulExecutionAction || c.historicalPolicyAction || c.policyAction || c.ruleBasedAction;
  }
  return c.finalAction;
};
