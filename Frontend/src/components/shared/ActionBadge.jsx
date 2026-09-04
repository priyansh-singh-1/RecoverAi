import React from 'react';
import './ActionBadge.css';
import { formatAction } from '@/utils/format';

export default function ActionBadge({ action, isModified }) {
  if (!action) {
    return <span style={{ color: 'var(--text-muted)' }}>--</span>;
  }

  return (
    <span className="action-badge">
      {isModified && <span className="action-badge-modified-dot"></span>}
      {formatAction(action)}
    </span>
  );
}
