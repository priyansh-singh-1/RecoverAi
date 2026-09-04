import React from 'react';
import { ArrowDown, GitBranch, Brain, ShieldCheck } from 'lucide-react';
import { extractMlPrediction, extractPolicyDecision } from '@/utils/parseAuditMetadata';
import { formatAction, formatProbability } from '@/utils/format';
import './DecisionDelta.css';

export default function DecisionDelta({ recoveryCase, auditLogs = [] }) {
  const prediction = extractMlPrediction(auditLogs);
  const policy = extractPolicyDecision(auditLogs);
  const probability = prediction?.recoveryProbability ?? recoveryCase?.recoveryProbability;
  return <section className="decision-delta"><div className="delta-heading"><span className="eyebrow">Decision intelligence</span><h3>Decision Delta</h3><p>Why the recovery strategy moved beyond the deterministic baseline.</p></div><div className="delta-path"><div className="delta-node"><GitBranch size={17} /><span>Rule Engine</span><strong>{formatAction(recoveryCase?.ruleBasedAction)}</strong></div><ArrowDown className="delta-arrow" size={17} /><div className="delta-node delta-signal"><Brain size={17} /><span>ML Probability</span><strong>{formatProbability(probability)}</strong></div><ArrowDown className="delta-arrow" size={17} /><div className="delta-node delta-final"><ShieldCheck size={17} /><span>Policy Decision</span><strong>{formatAction(policy?.policyAction)}</strong></div></div><div className="delta-reason">{policy?.reason || 'Policy evaluation recorded in the audit trail.'}</div></section>;
}
