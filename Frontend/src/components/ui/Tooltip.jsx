import React from 'react';
import './Tooltip.css';

export function Tooltip({ children, content, position = 'top' }) {
  return (
    <span className="tooltip-wrapper">
      {children}
      <span className={`tooltip-content tooltip-${position}`}>
        {content}
      </span>
    </span>
  );
}
