import React from 'react';
import { motion } from 'framer-motion';
import { formatINR } from '@/utils/format';
import './RevenueFlow.css';

export default function RevenueFlow({ atRisk = 0, recovered = 0 }) {
  return (
    <div className="revenue-flow">
      <div className="flow-header"><span>Revenue Flow</span><span className="flow-caption">Autonomous recovery path</span></div>
      <div className="flow-visual">
        <div className="flow-value flow-risk"><span>{formatINR(atRisk)}</span><small>Revenue at risk</small></div>
        <svg className="flow-svg" viewBox="0 0 360 100" role="img" aria-label="Revenue moving from at risk to recovered">
          <defs><linearGradient id="flow-line" x1="0" x2="1"><stop offset="0" stopColor="var(--text-muted)" /><stop offset=".55" stopColor="var(--intelligence)" /><stop offset="1" stopColor="var(--recovered)" /></linearGradient></defs>
          <path className="flow-track" d="M4 50 C90 50 110 18 180 50 S270 82 356 50" />
          <motion.path className="flow-signal" d="M4 50 C90 50 110 18 180 50 S270 82 356 50" pathLength="1" initial={{ pathLength: 0, opacity: 0 }} animate={{ pathLength: 1, opacity: 1 }} transition={{ duration: .9, ease: 'easeOut' }} />
          <motion.circle cx="4" cy="50" r="4" className="flow-dot flow-dot-start" initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: .4 }} />
          <motion.circle cx="356" cy="50" r="5" className="flow-dot flow-dot-end" initial={{ scale: 0 }} animate={{ scale: 1 }} transition={{ delay: .8, type: 'spring', stiffness: 180, damping: 20 }} />
        </svg>
        <div className="flow-value flow-recovered"><span>{formatINR(recovered)}</span><small>Recovered</small></div>
      </div>
      <div className="flow-footer"><span>Outstanding</span><strong>{formatINR(Math.max(Number(atRisk || 0) - Number(recovered || 0), 0))}</strong></div>
    </div>
  );
}
