import React from 'react';
import './Skeleton.css';

export function Skeleton({ width, height, borderRadius, className = '' }) {
  const style = { width, height, borderRadius };
  return <div className={`skeleton ${className}`} style={style} />;
}

export function SkeletonText({ lines = 3, lastLineWidth = '60%', className = '' }) {
  return (
    <div className={`skeleton-text ${className}`}>
      {Array.from({ length: lines }).map((_, i) => (
        <div
          key={i}
          className="skeleton-line skeleton"
          style={{ width: i === lines - 1 ? lastLineWidth : '100%' }}
        />
      ))}
    </div>
  );
}
