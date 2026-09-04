import React from 'react';
import { motion } from 'framer-motion';
import { Activity, ArrowRight, Sparkles } from 'lucide-react';
import TopBar from '@/components/layout/TopBar';
import { Skeleton } from '@/components/ui/Skeleton';
import MetricNumber from '@/components/shared/MetricNumber';
import RuleVsFinalChart from '@/components/charts/RuleVsFinalChart';
import RevenueFlow from '@/components/overview/RevenueFlow';
import RecoveryPulse from '@/components/overview/RecoveryPulse';
import { EmptyState } from '@/components/ui/EmptyState';
import { Button } from '@/components/ui/Button';
import { useRecoveryMetrics } from '@/hooks/useRecoveryMetrics';
import { useRecoveryCases } from '@/hooks/useRecoveryCases';
import { formatAction, formatINR, formatRate, getRecoveryStrategy } from '@/utils/format';
import { useShellActions } from '@/hooks/useShellActions';
import './DashboardPage.css';

export function DashboardPage() {
  const { openNewRecovery, refresh } = useShellActions();
  const { data, isLoading, isError, refetch } = useRecoveryMetrics();
  const { data: cases } = useRecoveryCases();

  if (isLoading) {
    return (
      <div className="dashboard-page">
        <TopBar title="Overview" subtitle="Executive recovery summary" onNewRecovery={openNewRecovery} onRefresh={refresh} />
        <div className="dashboard-content loading">
          <Skeleton height="200px" />
          <div className="metrics-grid">
             <Skeleton height="100px" />
             <Skeleton height="100px" />
             <Skeleton height="100px" />
             <Skeleton height="100px" />
          </div>
          <Skeleton height="300px" />
          <div className="bottom-charts">
            <Skeleton height="300px" />
            <Skeleton height="300px" />
          </div>
        </div>
      </div>
    );
  }

  if (isError || !data) {
    return (
      <div className="dashboard-page">
        <TopBar title="Overview" subtitle="Executive recovery summary" onNewRecovery={openNewRecovery} onRefresh={refresh} />
        <EmptyState
          title="Dashboard data unavailable"
          description="Connect the recovery service to load the latest metrics."
          action={<Button onClick={() => refetch()}>Retry</Button>}
        />
      </div>
    );
  }

  const containerVariants = {
    hidden: { opacity: 0 },
    show: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0 }
  };

  return (
    <div className="dashboard-page">
      <TopBar title="Overview" subtitle="Executive recovery summary" onNewRecovery={openNewRecovery} onRefresh={refresh} />
      <motion.div 
        className="dashboard-content"
        variants={containerVariants}
        initial="hidden"
        animate="show"
      >
        <motion.section className="overview-hero" variants={itemVariants}>
          <div className="overview-hero-copy">
            <span className="eyebrow"><Sparkles size={13} /> Autonomous recovery</span>
            <span className="hero-label">Recovered Revenue</span>
            <strong className="overview-recovered-value">{formatINR(data.recoveredRevenue)}</strong>
            <span className="overview-latest">Revenue returned by autonomous recovery</span>
            <div className="overview-hero-meta"><span>{formatRate(data.recoveryRate)} recovery rate</span><span className="meta-divider" /><span>{formatINR(data.outstandingRevenueAtRisk)} outstanding</span></div>
          </div>
          <RevenueFlow atRisk={data.totalRevenueAtRisk} recovered={data.recoveredRevenue} />
        </motion.section>

        <motion.div variants={itemVariants}><RecoveryPulse metrics={data} /></motion.div>

        <motion.div className="live-recovery-strip" variants={itemVariants}>
          <div className="live-strip-label"><Activity size={15} /><span>Live Recovery</span><i /></div>
          {(() => {
            const liveCase = (cases || []).find((item) => item.status === 'RECOVERED') || (cases || []).find((item) => ['OPEN', 'IN_PROGRESS'].includes(item.status));
            if (!liveCase) return <span className="live-strip-muted">Waiting for the next recovery signal</span>;
            const isRecovered = liveCase.status === 'RECOVERED';
            const strategy = getRecoveryStrategy(liveCase);
            
            const ruleText = liveCase.ruleBasedAction ? formatAction(liveCase.ruleBasedAction) : 'Decision pending';
            let strategyText = 'Policy review';
            if (strategy) strategyText = formatAction(strategy);
            if (!strategy && isRecovered) strategyText = 'Execution';
            const flow = isRecovered 
              ? <>{ruleText} &rarr; {strategyText} &rarr; Recovered</>
              : <>{ruleText} &rarr; {strategyText}</>;

            return <><strong>{liveCase.razorpayPaymentId}</strong><span>{formatINR(liveCase.recoveredAmount || liveCase.revenueAtRisk)} {isRecovered ? 'recovered' : 'in recovery'}</span><ArrowRight size={14} /><span>{flow}</span><span className="live-strip-confidence">ML {liveCase.recoveryProbability == null ? '—' : `${Math.round(liveCase.recoveryProbability * 100)}%`}</span></>;
          })()}
        </motion.div>

        <motion.div className="metrics-grid" variants={itemVariants}>
          <MetricNumber label="Open Cases" value={data.openCases} />
          <MetricNumber label="Recovered Cases" value={data.recoveredCases} />
          <MetricNumber label="Stopped Cases" value={data.stoppedCases} />
          <MetricNumber label="Approval Required" value={data.approvalRequiredCases} />
          <MetricNumber label="Successful Executions" value={data.successfulExecutions} />
          <MetricNumber label="Failed Executions" value={data.failedExecutions} />
          <MetricNumber label="ML Scored Cases" value={data.mlScoredCases} />
          <MetricNumber label="Decision Changes" value={data.aiDecisionChangedCases} />
        </motion.div>

        <motion.div className="main-chart-area" variants={itemVariants}>
          <RuleVsFinalChart ruleBreakdown={data.ruleBasedActionBreakdown} finalBreakdown={data.finalActionBreakdown} />
        </motion.div>

      </motion.div>
    </div>
  );
}
