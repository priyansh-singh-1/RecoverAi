import React from 'react';
import { motion } from 'framer-motion';
import { GitBranch, Brain, Shield, MessageSquareText, Zap, CheckCircle2 } from 'lucide-react';
import { extractAgentDecision, extractPolicyDecision, extractMlPrediction, policyChangedBaseline, isPolicyGuardVerified } from '@/utils/parseAuditMetadata';
import { formatAction, formatINR, formatNumber } from '@/utils/format';
import './DecisionPipeline.css';

export default function DecisionPipeline({ recoveryCase, auditLogs = [], executions = [] }) {
  if (!recoveryCase) return null;

  const agentDecision = extractAgentDecision(auditLogs);
  const policyDecision = extractPolicyDecision(auditLogs);
  const mlPrediction = extractMlPrediction(auditLogs);
  const latestExecution = [...executions].pop();
  const isBaselineAdjusted = policyChangedBaseline(policyDecision);
  const isVerified = isPolicyGuardVerified(agentDecision);
  const probability = mlPrediction?.recoveryProbability ?? recoveryCase.recoveryProbability;
  const probabilityLabel = probability === null || probability === undefined ? '\u2014' : `${formatNumber(probability * 100)}%`;

  const stages = [
    {
      id: 'rule',
      label: 'Rule Engine',
      icon: GitBranch,
      value: formatAction(recoveryCase.ruleBasedAction || 'UNKNOWN')
    },
    {
      id: 'ml',
      label: 'ML Model',
      icon: Brain,
      value: probabilityLabel,
    },
    {
      id: 'policy',
      label: 'Policy Engine',
      icon: Shield,
      value: policyDecision?.policyAction ? formatAction(policyDecision.policyAction) : 'Not recorded',
      badge: isBaselineAdjusted ? { type: 'warning', text: 'Baseline adjusted' } : null
    },
    {
      id: 'agent',
      label: 'Gemini Analysis',
      icon: MessageSquareText,
      value: agentDecision?.proposedAction ? formatAction(agentDecision.proposedAction) : formatAction(recoveryCase.finalAction),
      badge: isVerified ? { type: 'success', text: 'Policy aligned' } : null
    },
    {
      id: 'execution',
      label: 'Execution',
      icon: Zap,
      value: latestExecution?.status ? formatAction(latestExecution.status) : 'Not recorded',
      badge: latestExecution?.attemptNumber ? { type: 'info', text: `Attempt #${latestExecution.attemptNumber}` } : null,
    },
    {
      id: 'outcome',
      label: 'Outcome',
      icon: CheckCircle2,
      value: recoveryCase.status === 'RECOVERED' ? formatINR(recoveryCase.recoveredAmount) : formatAction(recoveryCase.status),
      badge: recoveryCase.status === 'RECOVERED' ? { type: 'success', text: 'Recovered', icon: false } : null,
    }
  ];

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.15
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 8 },
    visible: { opacity: 1, y: 0, transition: { duration: 0.4 } }
  };

  const lineVariants = {
    hidden: { opacity: 0, scaleX: 0 },
    visible: { opacity: 1, scaleX: 1, transition: { duration: 0.4 } }
  };

  return (
    <div className="chart-card">
      <h3 className="chart-card-title">Decision Intelligence Pipeline</h3>
      <motion.div 
        className="decision-pipeline"
        variants={containerVariants}
        initial="hidden"
        animate="visible"
      >
        {stages.map((stage, index) => (
          <React.Fragment key={stage.id}>
            <motion.div className="pipeline-stage" variants={itemVariants}>
              <stage.icon className="pipeline-stage-icon" size={24} />
              <div className="pipeline-stage-label">{stage.label}</div>
              <div className="pipeline-stage-value">{stage.value}</div>
              {stage.badge && (
                <div className={`pipeline-badge ${stage.badge.type}`}>
                  {stage.badge.type === 'success' && stage.badge.icon !== false && <CheckCircle2 size={12} />}
                  {stage.badge.text}
                </div>
              )}
            </motion.div>
            {index < stages.length - 1 && <motion.svg className="pipeline-connector" viewBox="0 0 44 8" preserveAspectRatio="none" variants={lineVariants} style={{ originX: 0 }} aria-hidden="true"><motion.path d="M0 4 H44" pathLength="1" /></motion.svg>}
          </React.Fragment>
        ))}
      </motion.div>
    </div>
  );
}
