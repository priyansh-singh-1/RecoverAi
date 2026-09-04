import React from 'react';
import './CustomTooltip.css';

export default function CustomTooltip({ active, payload, label, formatter, showDelta = false }) {
  if (!active || !payload || payload.length === 0) {
    return null;
  }

  return (
    <div className="custom-tooltip">
      {label && <div className="custom-tooltip-label">{label}</div>}
      {payload.map((entry) => {
        const itemKey = entry.dataKey ?? entry.name ?? String(entry.value);
        return <div key={itemKey} className="custom-tooltip-item">
          <div
            className="custom-tooltip-dot"
            style={{ backgroundColor: entry.color || entry.fill }}
          />
          <span className="custom-tooltip-name">{entry.name}:</span>
          <span className="custom-tooltip-value">
            {formatter ? formatter(entry.value) : entry.value}
          </span>
        </div>;
      })}
      {showDelta && <div className="custom-tooltip-delta">AI / Policy Delta: {Number(payload.find((entry) => entry.dataKey === 'final')?.value || 0) - Number(payload.find((entry) => entry.dataKey === 'rule')?.value || 0)}</div>}
    </div>
  );
}
