/**
 * Safely parse JSON metadata from an audit log.
 */
export const parseMetadata = (metadata) => {
  if (!metadata) return null;
  if (typeof metadata === 'object') return metadata;
  try {
    return JSON.parse(metadata);
  } catch {
    return null;
  }
};

/**
 * Extract the AI agent decision from audit logs for a given recovery case.
 */
export const extractAgentDecision = (auditLogs) => {
  const agentLog = [...(auditLogs || [])].reverse().find(
    (log) => log.eventType === 'AI_AGENT_ANALYSIS_GENERATED'
  );
  if (!agentLog) return null;
  return { ...(parseMetadata(agentLog.metadata) || {}), explanation: agentLog.reason };
};

/**
 * Extract the policy decision from audit logs.
 */
export const extractPolicyDecision = (auditLogs) => {
  const policyLog = [...(auditLogs || [])].reverse().find(
    (log) => log.eventType === 'POLICY_DECISION_GENERATED'
  );
  if (!policyLog) return null;
  return { ...(parseMetadata(policyLog.metadata) || {}), policyAction: policyLog.newState, reason: policyLog.reason };
};

/**
 * Extract ML prediction from audit logs.
 */
export const extractMlPrediction = (auditLogs) => {
  const mlLog = [...(auditLogs || [])].reverse().find(
    (log) => log.eventType === 'ML_PREDICTION_GENERATED'
  );
  if (!mlLog) return null;
  return parseMetadata(mlLog.metadata);
};

/**
 * Check if policy changed the baseline action.
 */
export const policyChangedBaseline = (policyDecision) => {
  if (!policyDecision) return false;
  return (
    policyDecision.baselineAction !== policyDecision.policyAction &&
    !!policyDecision.baselineAction &&
    !!policyDecision.policyAction
  );
};

/**
 * Check if Gemini's proposed action matches the policy action.
 */
export const isPolicyGuardVerified = (agentDecision) => {
  if (!agentDecision?.proposedAction || !agentDecision?.policyAction) return false;
  return agentDecision.proposedAction === agentDecision.policyAction;
};
