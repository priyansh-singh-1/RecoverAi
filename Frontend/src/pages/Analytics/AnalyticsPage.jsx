import React from 'react';
import { motion } from 'framer-motion';
import TopBar from '@/components/layout/TopBar';
import MetricNumber from '@/components/shared/MetricNumber';
import RuleVsFinalChart from '@/components/charts/RuleVsFinalChart';
import FailureReasonsChart from '@/components/charts/FailureReasonsChart';
import PriorityChart from '@/components/charts/PriorityChart';
import RecoveryOutcomesChart from '@/components/charts/RecoveryOutcomesChart';
import ExecutionChart from '@/components/charts/ExecutionChart';
import RevenueBar from '@/components/charts/RevenueBar';
import { EmptyState } from '@/components/ui/EmptyState';
import { Button } from '@/components/ui/Button';
import { useRecoveryMetrics } from '@/hooks/useRecoveryMetrics';
import { formatINR, formatRate } from '@/utils/format';
import './AnalyticsPage.css';
import { useShellActions } from '@/hooks/useShellActions';

export function AnalyticsPage() {
  const { openNewRecovery, refresh } = useShellActions();
  const { data, isLoading, isError, refetch } = useRecoveryMetrics();

  if (isLoading) return <div className="analytics-page"><TopBar title="Analytics" subtitle="Recovery intelligence" onNewRecovery={openNewRecovery} onRefresh={refresh} /><div className="analytics-loading">Loading analytics...</div></div>;
  if (isError || !data) return <div className="analytics-page"><TopBar title="Analytics" subtitle="Recovery intelligence" onNewRecovery={openNewRecovery} onRefresh={refresh} /><EmptyState title="Analytics unavailable" description="Connect the recovery service to load live analytics." action={<Button onClick={() => refetch()}>Retry</Button>} /></div>;

  return (
    <div className="analytics-page">
      <TopBar title="Analytics" subtitle="Recovery intelligence" onNewRecovery={openNewRecovery} onRefresh={refresh} />
      <motion.div className="analytics-content" initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}>
        <section className="analytics-section analytics-revenue-section">
          <div className="analytics-section-heading">
            <span className="eyebrow">Revenue performance</span>
            <h2>Money in motion</h2>
          </div>
          <div className="analytics-revenue-container">
            <div className="analytics-summary">
              <div><span>Recovered Revenue</span><strong>{formatINR(data.recoveredRevenue)}</strong></div>
              <div><span>Outstanding Revenue</span><strong>{formatINR(data.outstandingRevenueAtRisk)}</strong></div>
              <div><span>Total Revenue at Risk</span><strong>{formatINR(data.totalRevenueAtRisk)}</strong></div>
              <div><span>Recovery Rate</span><strong>{formatRate(data.recoveryRate)}</strong></div>
            </div>
            <div className="analytics-revenue-bar">
              <RevenueBar recovered={data.recoveredRevenue} outstanding={data.outstandingRevenueAtRisk} total={data.totalRevenueAtRisk} />
            </div>
          </div>
        </section>
        <section className="analytics-metrics">
          <MetricNumber label="ML Scored Cases" value={data.mlScoredCases} />
          <MetricNumber label="Decision Changes" value={data.aiDecisionChangedCases} />
          <MetricNumber label="Successful Executions" value={data.successfulExecutions} />
          <MetricNumber label="Failed Executions" value={data.failedExecutions} />
        </section>
        <section className="analytics-section"><div className="analytics-section-heading"><span className="eyebrow">Decision intelligence</span><h2>How the system changes outcomes</h2></div><div className="analytics-grid analytics-decision-grid"><RuleVsFinalChart ruleBreakdown={data.ruleBasedActionBreakdown} finalBreakdown={data.finalActionBreakdown} /></div></section>
        <section className="analytics-section"><div className="analytics-section-heading"><span className="eyebrow">Risk signals</span><h2>Where attention concentrates</h2></div><div className="analytics-grid"><FailureReasonsChart data={data.failureReasonBreakdown} /><PriorityChart data={data.priorityBreakdown} /></div></section>
        <section className="analytics-section"><div className="analytics-section-heading"><span className="eyebrow">Execution performance</span><h2>From policy to recovered money</h2></div><div className="analytics-grid"><RecoveryOutcomesChart recovered={data.recoveredCases} open={data.openCases} stopped={data.stoppedCases} approvalRequired={data.approvalRequiredCases} /><ExecutionChart success={data.successfulExecutions} failed={data.failedExecutions} /></div></section>
      </motion.div>
    </div>
  );
}

export default AnalyticsPage;
