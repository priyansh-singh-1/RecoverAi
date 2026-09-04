import React from 'react';
import './ChartCard.css';

export default function ChartCard({ title, children, height = 280, className = '' }) {
  return (
    <div className={`chart-card ${className}`}>
      {title && <h3 className="chart-card-title">{title}</h3>}
      <div className="chart-card-content" style={{ height: `${height}px` }}>
        {children}
      </div>
    </div>
  );
}
