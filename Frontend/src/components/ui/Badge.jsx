import React from 'react';
import './Badge.css';

export function Badge({ children, variant = 'default', size = 'sm', className = '' }) {
  return (
    <span className={`badge badge-${variant} badge-${size} ${className}`}>
      {children}
    </span>
  );
}
