import React from 'react';
import './PriorityBadge.css';
import { PRIORITY_CONFIG } from '@/utils/statusConfig';

export default function PriorityBadge({ priority }) {
  if (!priority) {
    return <span style={{ color: 'var(--text-muted)' }}>--</span>;
  }

  const config = PRIORITY_CONFIG[priority];
  const color = config?.color || 'var(--text-primary)';
  const label = config?.label || priority;

  return (
    <span className="priority-badge" style={{ color }}>
      <span className="priority-dot" style={{ backgroundColor: color }}></span>
      {label}
    </span>
  );
}
