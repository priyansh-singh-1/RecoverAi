import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { Drawer } from '@/components/ui/Drawer';
import { Button } from '@/components/ui/Button';
import { useAuditLogsByCase } from '@/hooks/useAuditLogs';
import { extractAgentDecision, extractPolicyDecision } from '@/utils/parseAuditMetadata';
import AmountCell from '@/components/shared/AmountCell';
import PriorityBadge from '@/components/shared/PriorityBadge';
import { approveRecoveryCase, rejectRecoveryCase } from '@/api/approvals';
import toast from 'react-hot-toast';
import { formatAction, formatProbability } from '@/utils/format';
import './ReviewDrawer.css';

export function ReviewDrawer({ isOpen, onClose, recoveryCase }) {
  const { data: auditLogs, isLoading } = useAuditLogsByCase(recoveryCase?.id);
  const queryClient = useQueryClient();
  const [reviewedBy, setReviewedBy] = useState('Operator');
  const [reason, setReason] = useState('');

  const approveMutation = useMutation({
    mutationFn: (id) => approveRecoveryCase(id, { reviewedBy, reason }),
    onSuccess: () => {
      toast.success('Case approved');
      queryClient.invalidateQueries({ queryKey: ['recovery-cases'] });
      queryClient.invalidateQueries({ queryKey: ['recovery-metrics'] });
      onClose();
    },
    onError: (error) => toast.error(error.response?.data?.message || 'Approval failed'),
  });

  const rejectMutation = useMutation({
    mutationFn: (id) => rejectRecoveryCase(id, { reviewedBy, reason }),
    onSuccess: () => {
      toast.success('Case rejected');
      queryClient.invalidateQueries({ queryKey: ['recovery-cases'] });
      queryClient.invalidateQueries({ queryKey: ['recovery-metrics'] });
      onClose();
    },
    onError: (error) => toast.error(error.response?.data?.message || 'Rejection failed'),
  });

  const handleApprove = () => approveMutation.mutate(recoveryCase.id);
  const handleReject = () => rejectMutation.mutate(recoveryCase.id);

  const agentDecision = extractAgentDecision(auditLogs);
  const policyDecision = extractPolicyDecision(auditLogs);
  let agentContent = <p className="muted-text">No AI analysis available.</p>;
  if (isLoading) agentContent = <p className="loading-text">Loading explanation...</p>;
  if (!isLoading && agentDecision) agentContent = <div className="explanation-box ai-box">{agentDecision.explanation || 'No explanation provided.'}</div>;
  let policyContent = <p className="muted-text">No policy guard log found.</p>;
  if (isLoading) policyContent = <p className="loading-text">Loading policy details...</p>;
  if (!isLoading && policyDecision) policyContent = <div className="explanation-box policy-box">{policyDecision.reason || 'Flagged by policy rules.'}</div>;

  return (
    <Drawer isOpen={isOpen} onClose={onClose} title="Review Case">
      <div className="drawer-content">
        <div className="review-summary">
          <div className="summary-item">
            <span className="summary-label">Amount at Risk</span>
            <AmountCell amount={recoveryCase.revenueAtRisk} />
          </div>
          <div className="summary-item"><span className="summary-label">Recovery Probability</span><span>{formatProbability(recoveryCase.recoveryProbability)}</span></div>
          <div className="summary-item"><span className="summary-label">Rule Action</span><span>{formatAction(recoveryCase.ruleBasedAction)}</span></div>
          <div className="summary-item"><span className="summary-label">Policy Action</span><span>{formatAction(policyDecision?.policyAction)}</span></div>
          <div className="summary-item">
            <span className="summary-label">Payment ID</span>
            <span className="mono">{recoveryCase.razorpayPaymentId || '\u2014'}</span>
          </div>
          <div className="summary-item">
            <span className="summary-label">Priority</span>
            <PriorityBadge priority={recoveryCase.priority} />
          </div>
        </div>

        <div className="review-details">
          <div className="detail-section">
            <h4>Gemini Agent Analysis</h4>
            {agentContent}
          </div>
          <div className="detail-section">
            <h4>Review Decision</h4>
            <input value={reviewedBy} onChange={(e) => setReviewedBy(e.target.value)} placeholder="Reviewed by" aria-label="Reviewed by" />
            <textarea value={reason} onChange={(e) => setReason(e.target.value)} placeholder="Reason (optional)" aria-label="Review reason" rows="3" />
          </div>

          <div className="detail-section">
            <h4>Policy Guard Reason</h4>
            {policyContent}
          </div>
        </div>

        <div className="drawer-actions">
          <Button variant="ghost" onClick={onClose} disabled={approveMutation.isPending || rejectMutation.isPending}>
            Cancel
          </Button>
          <div className="action-group">
            <Button 
              variant="danger" 
              onClick={handleReject}
              disabled={approveMutation.isPending || rejectMutation.isPending}
            >
              {rejectMutation.isPending ? 'Rejecting...' : 'Reject & Stop'}
            </Button>
            <Button 
              variant="primary" 
              onClick={handleApprove}
              disabled={approveMutation.isPending || rejectMutation.isPending}
            >
              {approveMutation.isPending ? 'Approving...' : 'Approve Action'}
            </Button>
          </div>
        </div>
      </div>
    </Drawer>
  );
}
