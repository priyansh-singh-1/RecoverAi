import React from 'react';
import './ProbabilityBar.css';

export default function ProbabilityBar({ probability, showLabel }) {
  if (probability == null) {
    return <span style={{ color: 'var(--text-muted)' }}>--</span>;
  }

  const percentage = Math.round(probability * 100);
  const fillWidth = `${percentage}%`;
  
  let color = 'var(--success)';
  if (probability < 0.3) {
    color = 'var(--danger)';
  } else if (probability <= 0.6) {
    color = 'var(--warning)';
  }

  return (
    <div className="probability-wrapper">
      <div className="probability-track">
        <div 
          className="probability-fill" 
          style={{ width: fillWidth, backgroundColor: color }}
        />
      </div>
      {showLabel && (
        <span className="probability-label">{percentage}%</span>
      )}
    </div>
  );
}
