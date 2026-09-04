import React from 'react';
import { motion } from 'framer-motion';
import './RecoveryPulse.css';

const steps = [
  ['Rule Engine', 'rule'],
  ['ML Scored', 'ml'],
  ['AI Adjusted', 'ai'],
  ['Executed', 'executed'],
];

export default function RecoveryPulse({ metrics }) {
  const values = [metrics?.totalRecoveryCases, metrics?.mlScoredCases, metrics?.aiDecisionChangedCases, metrics?.successfulExecutions];
  return <section className="recovery-pulse"><div className="pulse-heading"><div><span className="eyebrow">Decision telemetry</span><h2>Recovery Intelligence Pulse</h2></div><span className="pulse-live"><i />Live signal</span></div><div className="pulse-track"><svg viewBox="0 0 1000 24" preserveAspectRatio="none" aria-hidden="true"><motion.path d="M8 12 H992" initial={{ pathLength: 0 }} animate={{ pathLength: 1 }} transition={{ duration: .8 }} /></svg>{steps.map(([label, key], index) => <motion.div key={key} className={`pulse-step pulse-${key}`} initial={{ opacity: 0, y: 5 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: index * .1 }}><strong>{values[index] ?? '—'}</strong><span>{label}</span></motion.div>)}</div></section>;
}
