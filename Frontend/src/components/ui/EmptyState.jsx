import React from 'react';
import './EmptyState.css';

export function EmptyState({ icon: Icon, title, message, description, action }) {
  const heading = title || message;

  return (
    <div className="empty-state">
      {Icon && (
        <div className="empty-state-icon">
          <Icon size={48} />
        </div>
      )}
      {heading && <h3 className="empty-state-title">{heading}</h3>}
      {description && <p className="empty-state-desc">{description}</p>}
      {action && <div className="empty-state-action">{action}</div>}
    </div>
  );
}
