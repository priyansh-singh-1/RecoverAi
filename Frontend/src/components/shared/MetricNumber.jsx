import React from 'react';
import './MetricNumber.css';
import { useCountUp } from '@/hooks/useCountUp';

export default function MetricNumber({ value, label, prefix, suffix, size = 'md' }) {
  const animatedValue = useCountUp(value, 1000);
  
  return (
    <div className="metric-container">
      <div className="metric-value-container">
        {prefix && <span className="metric-prefix">{prefix}</span>}
        <span className={`metric-value metric-value-${size}`}>
          {animatedValue}
        </span>
        {suffix && <span className="metric-suffix">{suffix}</span>}
      </div>
      {label && <span className="metric-label">{label}</span>}
    </div>
  );
}
