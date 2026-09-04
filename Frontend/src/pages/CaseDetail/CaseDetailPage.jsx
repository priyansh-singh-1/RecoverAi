import React from 'react';
import { useParams } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ShieldCheck } from 'lucide-react';
import toast from 'react-hot-toast';
import TopBar from '@/components/layout/TopBar';
import AmountCell from '@/components/shared/AmountCell';
import StatusBadge from '@/components/shared/StatusBadge';
import PriorityBadge from '@/components/shared/PriorityBadge';
import DecisionPipeline from '@/components/pipeline/DecisionPipeline';
import DecisionDelta from '@/components/pipeline/DecisionDelta';
import AuditTimeline from '@/components/timeline/AuditTimeline';
import { Skeleton } from '@/components/ui/Skeleton';
import { useRecoveryCases } from '@/hooks/useRecoveryCases';
import { useAuditLogsByCase } from '@/hooks/useAuditLogs';
import { useExecutionHistory } from '@/hooks/useExecutions';
import { executeRecoveryAction } from '@/api/executions';
import { Button } from '@/components/ui/Button';
import { extractAgentDecision, extractPolicyDecision, extractMlPrediction } from '@/utils/parseAuditMetadata';
import { formatAction, formatDateTime, formatINR, formatProbability } from '@/utils/format';
import { useShellActions } from '@/hooks/useShellActions';
import './CaseDetailPage.css';

export function CaseDetailPage() {
  const { id } = useParams();
  const { openNewRecovery, refresh } = useShellActions();
  const { data: cases, isLoading: casesLoading, isError: casesError } = useRecoveryCases();
  const { data: auditLogs, isLoading: logsLoading, isError: logsError } = useAuditLogsByCase(id);
  const { data: executions, isLoading: execsLoading, isError: execsError } = useExecutionHistory(id);
  const queryClient = useQueryClient();
  const executeMutation = useMutation({
    mutationFn: () => executeRecoveryAction(id),
    onSuccess: () => {
      toast.success('Recovery action executed');
      queryClient.invalidateQueries({ queryKey: ['recovery-cases'] });
      queryClient.invalidateQueries({ queryKey: ['recovery-metrics'] });
      queryClient.invalidateQueries({ queryKey: ['audit-logs', 'case', id] });
      queryClient.invalidateQueries({ queryKey: ['executions', id] });
    },
  });

  const recoveryCase = cases?.find(c => String(c.id) === String(id));

  if (casesLoading || logsLoading || execsLoading) {
    return (
      <div className="case-detail-page">
        <TopBar title={`Case #${id}`} subtitle="Loading..." onNewRecovery={openNewRecovery} onRefresh={refresh} />
        <div className="case-detail-content loading">
          <Skeleton height="120px" />
          <div className="detail-layout">
            <div className="detail-left"><Skeleton height="400px" /></div>
            <div className="detail-right"><Skeleton height="400px" /></div>
          </div>
        </div>
      </div>
    );
  }

  if (casesError || logsError || execsError || !recoveryCase) {
    return <div className="case-detail-page"><TopBar title={`Case #${id}`} subtitle="Unavailable" onNewRecovery={openNewRecovery} onRefresh={refresh} /><div className="case-detail-content"><div className="empty-text">Case details could not be loaded.</div></div></div>;
  }

  const agentDecision = extractAgentDecision(auditLogs);
  const policyDecision = extractPolicyDecision(auditLogs);
  const mlPrediction = extractMlPrediction(auditLogs);
  const executionDisabled = ['RECOVERED', 'STOPPED', 'EXPIRED', 'REQUIRES_APPROVAL'].includes(recoveryCase.status) || (executions || []).length >= 3 || executeMutation.isPending;
  const hasRecoveredRevenue = Number(recoveryCase.recoveredAmount || 0) > 0;
  const primaryAmount = hasRecoveredRevenue ? recoveryCase.recoveredAmount : recoveryCase.revenueAtRisk;
  const primaryAmountLabel = hasRecoveredRevenue ? 'Revenue recovered' : 'Revenue at risk';
  const nextSteps = agentDecision?.nextSteps ? String(agentDecision.nextSteps).split('|').map((step) => step.trim()).filter(Boolean) : [];

  return (
    <div className="case-detail-page">
      <TopBar title={`Case #${id}`} subtitle={recoveryCase.razorpayPaymentId} onNewRecovery={openNewRecovery} onRefresh={refresh} />
      
      <div className="case-detail-content">
        <div className="case-hero">
          <div className="case-hero-main">
            <div className="case-hero-amount-row">
              <AmountCell amount={primaryAmount} variant="hero" />
              <StatusBadge status={recoveryCase.status} />
            </div>
            <div className="case-hero-subtitle">
              <span>Payment ID: {recoveryCase.razorpayPaymentId}</span>
            </div>
          </div>
          <div className="case-hero-meta">
            <div className="hero-meta-item">
              <strong><PriorityBadge priority={recoveryCase.priority} /></strong>
              <span>Priority</span>
            </div>
            <div className="hero-meta-item">
              <strong>{formatProbability(mlPrediction?.recoveryProbability ?? recoveryCase.recoveryProbability)}</strong>
              <span>Recovery Probability</span>
            </div>
            <div className="hero-meta-item">
              <strong>{recoveryCase.recoveryAttemptCount || 0} / 3</strong>
              <span>Attempts</span>
            </div>
            <div className="hero-meta-item">
              <strong>{formatINR(recoveryCase.paymentAmount)}</strong>
              <span>Payment Amount</span>
            </div>
          </div>
        </div>

        <div className="detail-layout">
          <div className="detail-left">
            <div className="detail-card">
              <div className="detail-section-kicker">Decision intelligence</div>
              <h3 className="card-title">Decision Pipeline</h3>
              <DecisionPipeline recoveryCase={recoveryCase} auditLogs={auditLogs} executions={executions} />
              <DecisionDelta recoveryCase={recoveryCase} auditLogs={auditLogs} />
            </div>

            <div className="detail-card">
              <div className="detail-section-kicker">Gemini-powered · Policy constrained</div>
              <h3 className="card-title">AI Recovery Analysis</h3>
              {agentDecision ? <div className="analysis-grid">
                <div><span>Diagnosis</span><strong>{agentDecision.diagnosis || '\u2014'}</strong></div>
                <div><span>Explanation</span><strong>{agentDecision.explanation || '\u2014'}</strong></div>
                <div><span>Confidence</span><strong>{agentDecision.confidenceLevel || '\u2014'}</strong></div>
                <div className="analysis-next-steps"><span>Next steps</span>{nextSteps.length ? <ol>{nextSteps.map((step, index) => <li key={`${step}-${index}`}>{step}</li>)}</ol> : <strong>\u2014</strong>}</div>
                <div><span>Proposed action</span><strong>{formatAction(agentDecision.proposedAction)}</strong></div>
                <div><span>Policy action</span><strong>{formatAction(agentDecision.policyAction || policyDecision?.policyAction)}</strong></div>
              </div> : <div className="empty-text">No Gemini analysis recorded.</div>}
              {agentDecision?.proposedAction && agentDecision?.policyAction && agentDecision.proposedAction === agentDecision.policyAction && <div className="policy-verified-shield"><ShieldCheck size={16} /> <span>Policy Guard Verified</span></div>}
            </div>

            <div className="detail-card">
              <h3 className="card-title">Execution History</h3>
              <div className="execution-toolbar"><span>Completed attempts: {recoveryCase.recoveryAttemptCount || 0} / 3</span><Button onClick={() => executeMutation.mutate()} disabled={executionDisabled} loading={executeMutation.isPending}>{executeMutation.isPending ? 'Executing...' : 'Execute Recovery Action'}</Button></div>
              <div className="execution-list">
                {executions?.length > 0 ? (
                  executions.map((exec, idx) => (
                    <div key={exec.id || idx} className="execution-item">
                      <div><StatusBadge status={exec.status} /><strong>{formatAction(exec.action)}</strong></div>
                      <span className="exec-message">Attempt {exec.attemptNumber || idx + 1}: {exec.message || '\u2014'}</span>
                      <span className="exec-reference">{exec.externalReference || '\u2014'}</span>
                      <small>{formatDateTime(exec.startedAt)} - {formatDateTime(exec.completedAt)}</small>
                    </div>
                  ))
                ) : (
                  <div className="empty-text">No execution history available</div>
                )}
              </div>
            </div>
          </div>
          
          <div className="detail-right">
            <div className="detail-card timeline-card">
              <h3 className="card-title">Audit Timeline</h3>
              <AuditTimeline auditLogs={auditLogs} />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
