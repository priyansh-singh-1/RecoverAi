import React, { useState } from 'react';
import { ShieldCheck, Flag, CheckCircle2, Ban } from 'lucide-react';
import TopBar from '@/components/layout/TopBar';
import { EmptyState } from '@/components/ui/EmptyState';
import { Button } from '@/components/ui/Button';
import AmountCell from '@/components/shared/AmountCell';
import StatusBadge from '@/components/shared/StatusBadge';
import PriorityBadge from '@/components/shared/PriorityBadge';
import ActionBadge from '@/components/shared/ActionBadge';
import ProbabilityBar from '@/components/shared/ProbabilityBar';
import { useRecoveryCases } from '@/hooks/useRecoveryCases';
import { formatRelativeTime } from '@/utils/format';
import { ReviewDrawer } from './ReviewDrawer';
import './HumanReviewPage.css';
import { useShellActions } from '@/hooks/useShellActions';

export function HumanReviewPage() {
  const { openNewRecovery, refresh } = useShellActions();
  const { data: cases, isLoading, isError, refetch } = useRecoveryCases();
  const [selectedCaseId, setSelectedCaseId] = useState(null);

  const pendingCases = cases?.filter(c => c.status === 'REQUIRES_APPROVAL')
    ?.sort((a, b) => {
      const pMap = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
      return (pMap[b.priority] || 0) - (pMap[a.priority] || 0);
    }) || [];

  const selectedCase = cases?.find(c => String(c.id) === String(selectedCaseId));

  let reviewContent;
  if (isLoading) {
    reviewContent = <div className="loading-state">Loading queue...</div>;
  } else if (isError) {
    reviewContent = (
      <EmptyState
        title="Review queue unavailable"
        description="Connect the recovery service to load pending approvals."
        action={<Button onClick={() => refetch()}>Retry</Button>}
      />
    );
  } else if (pendingCases.length === 0) {
    reviewContent = <div className="review-empty-panel"><ShieldCheck size={42} /><span className="eyebrow">Autonomy boundary</span><h2>No intervention needed.</h2><p>Autonomous recovery is operating within policy boundaries.</p><div className="review-how"><h3>Autonomy Boundary</h3><div className="review-steps"><span><Flag size={16} />AI Decision</span><b>↓</b><span><ShieldCheck size={16} />Policy Guard</span><b>↓</b><span><CheckCircle2 size={16} />Safe → Execute</span><b>·</b><span><Ban size={16} />Risk → Human Review</span></div></div></div>;
  } else {
    reviewContent = (
      <div className="table-container">
        <table className="review-table">
          <thead>
            <tr><th>ID</th><th>Payment ID</th><th>Amount</th><th>Status</th><th>Priority</th><th>Proposed Action</th><th>ML Prob</th><th>Age</th></tr>
          </thead>
          <tbody>
            {pendingCases.map(c => (
              <tr key={c.id} onClick={() => setSelectedCaseId(c.id)} className="clickable-row">
                <td>{c.id}</td>
                <td className="mono">{c.razorpayPaymentId || '\u2014'}</td>
                <td><AmountCell amount={c.revenueAtRisk} recovered={c.recoveredAmount} /></td>
                <td><StatusBadge status={c.status} /></td>
                <td><PriorityBadge priority={c.priority} /></td>
                <td><ActionBadge action={c.finalAction} /></td>
                <td><ProbabilityBar probability={c.recoveryProbability} showLabel /></td>
                <td>{formatRelativeTime(c.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  }

  return (
    <div className="human-review-page">
      <TopBar title="Human Review" subtitle={`${pendingCases.length} cases pending operator attention`} onNewRecovery={openNewRecovery} onRefresh={refresh} />
      
      <div className="review-content">
        {reviewContent}
      </div>

      {selectedCase && (
        <ReviewDrawer 
          isOpen={true} 
          onClose={() => setSelectedCaseId(null)} 
          recoveryCase={selectedCase} 
        />
      )}
    </div>
  );
}
