import React, { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import TopBar from '@/components/layout/TopBar';
import { EmptyState } from '@/components/ui/EmptyState';
import { Button } from '@/components/ui/Button';
import AmountCell from '@/components/shared/AmountCell';
import StatusBadge from '@/components/shared/StatusBadge';
import PriorityBadge from '@/components/shared/PriorityBadge';
import ActionBadge from '@/components/shared/ActionBadge';
import ProbabilityBar from '@/components/shared/ProbabilityBar';
import { useRecoveryCases } from '@/hooks/useRecoveryCases';
import { formatAction, formatRelativeTime, getRecoveryStrategy } from '@/utils/format';
import './CasesPage.css';
import { useShellActions } from '@/hooks/useShellActions';

const getOutcome = (c) => {
  if (c.status === 'RECOVERED') return 'RECOVERED';
  if (c.status === 'STOPPED') return 'STOPPED';
  return c.status;
};
export function CasesPage() {
  const { openNewRecovery, refresh } = useShellActions();
  const { data: cases, isLoading, isError, refetch } = useRecoveryCases();
  const navigate = useNavigate();
  const [statusFilter, setStatusFilter] = useState('All');
  const [priorityFilter, setPriorityFilter] = useState('All');
  const [actionFilter, setActionFilter] = useState('All');
  const [finalActionFilter, setFinalActionFilter] = useState('All');
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState('createdAt');

  const actions = [...new Set((cases || []).map(c => c.ruleBasedAction).filter(Boolean))];
  const finalActions = [...new Set((cases || []).map(c => c.finalAction).filter(Boolean))];
  const filteredCases = useMemo(() => (cases || [])
    .filter(c => {
      const query = search.trim().toLowerCase();
      return (!query || `${c.id} ${c.razorpayPaymentId || ''}`.toLowerCase().includes(query)) &&
        (statusFilter === 'All' || c.status === statusFilter) &&
        (priorityFilter === 'All' || c.priority === priorityFilter) &&
        (actionFilter === 'All' || c.ruleBasedAction === actionFilter) &&
        (finalActionFilter === 'All' || c.finalAction === finalActionFilter);
    })
    .sort((a, b) => {
      if (sort === 'amount') return Number(b.revenueAtRisk || 0) - Number(a.revenueAtRisk || 0);
      if (sort === 'priority') return ({ CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 }[a.priority] ?? 4) - ({ CRITICAL: 0, HIGH: 1, MEDIUM: 2, LOW: 3 }[b.priority] ?? 4);
      return new Date(b.createdAt || 0) - new Date(a.createdAt || 0);
    }), [cases, search, statusFilter, priorityFilter, actionFilter, finalActionFilter, sort]);

  let casesContent;
  if (isLoading) {
    casesContent = <div className="loading-state">Loading cases...</div>;
  } else if (isError) {
    casesContent = (
      <EmptyState
        title="Cases unavailable"
        description="Connect the recovery service to load cases."
        action={<Button onClick={() => refetch()}>Retry</Button>}
      />
    );
  } else if (filteredCases.length === 0) {
    casesContent = <EmptyState message="No cases match your filters" />;
  } else {
    casesContent = (
      <div className="table-container">
        <table className="cases-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Razorpay Payment ID</th>
              <th>Amount</th>
              <th>Priority</th>
              <th>Rule Baseline</th>
              <th>Recovery Strategy</th>
              <th>Outcome</th>
              <th>ML Prob</th>
              <th>Attempts</th>
              <th>Age</th>
            </tr>
          </thead>
          <motion.tbody
            initial="hidden"
            animate="show"
            variants={{ show: { transition: { staggerChildren: 0.05 } } }}
          >
            {filteredCases.map(c => (
              <motion.tr
                key={c.id}
                onClick={() => navigate(`/cases/${c.id}`)}
                className="clickable-row"
                variants={{ hidden: { opacity: 0, y: 10 }, show: { opacity: 1, y: 0 } }}
              >
                <td>{c.id}</td>
                <td className="mono">{c.razorpayPaymentId || '\u2014'}</td>
                <td><AmountCell amount={c.revenueAtRisk} recovered={c.recoveredAmount} /></td>
                <td><PriorityBadge priority={c.priority} /></td>
                <td><ActionBadge action={c.ruleBasedAction} /></td>
                <td><ActionBadge action={getRecoveryStrategy(c)} /></td>
                <td><StatusBadge status={getOutcome(c)} /></td>
                <td><ProbabilityBar probability={c.recoveryProbability} showLabel /></td>
                <td>{c.recoveryAttemptCount}</td>
                <td>{formatRelativeTime(c.createdAt)}</td>
              </motion.tr>
            ))}
          </motion.tbody>
        </table>
      </div>
    );
  }

  return (
    <div className="cases-page">
      <TopBar title="Recovery Cases" subtitle="Search and manage active payment recoveries" onNewRecovery={openNewRecovery} onRefresh={refresh} />
      
      <div className="cases-content">
        <div className="filters-bar">
          <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search case or payment ID" aria-label="Search cases" className="filter-select" />
          <select 
            value={statusFilter} 
            onChange={(e) => setStatusFilter(e.target.value)}
            className="filter-select"
          >
            <option value="All">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="RECOVERED">Recovered</option>
            <option value="FAILED">Failed</option>
          </select>
          <select value={actionFilter} onChange={(e) => setActionFilter(e.target.value)} className="filter-select" aria-label="Filter by action">
            <option value="All">All Rule Actions</option>
            {actions.map(action => <option key={action} value={action}>{formatAction(action)}</option>)}
          </select>
          <select value={finalActionFilter} onChange={(e) => setFinalActionFilter(e.target.value)} className="filter-select" aria-label="Filter by final action">
            <option value="All">All Final Actions</option>
            {finalActions.map(action => <option key={action} value={action}>{formatAction(action)}</option>)}
          </select>
          <select value={sort} onChange={(e) => setSort(e.target.value)} className="filter-select" aria-label="Sort cases">
            <option value="createdAt">Newest first</option>
            <option value="amount">Highest amount</option>
            <option value="priority">Highest priority</option>
          </select>
          
          <select 
            value={priorityFilter} 
            onChange={(e) => setPriorityFilter(e.target.value)}
            className="filter-select"
          >
            <option value="All">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
        </div>

        {casesContent}
      </div>
    </div>
  );
}
