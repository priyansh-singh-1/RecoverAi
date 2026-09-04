import React from 'react';
import { motion } from 'framer-motion';
import { ArrowUpRight, CircleAlert, Clock3, CheckCircle2, Ban } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import TopBar from '@/components/layout/TopBar';
import { EmptyState } from '@/components/ui/EmptyState';
import AmountCell from '@/components/shared/AmountCell';
import PriorityBadge from '@/components/shared/PriorityBadge';
import StatusBadge from '@/components/shared/StatusBadge';
import { useRecoveryCases } from '@/hooks/useRecoveryCases';
import { useShellActions } from '@/hooks/useShellActions';
import { formatINR, formatRelativeTime, formatAction } from '@/utils/format';
import './WorkspacePage.css';

function CaseRow({ item, navigate, variant }) {
  const isWide = variant === 'attention' || variant === 'active';
  
  if (isWide) {
    return (
      <button className="workspace-case workspace-case-wide" onClick={() => navigate(`/cases/${item.id}`)}>
        <div className="case-col-meta">
          <span className="workspace-case-id">#{item.id}</span>
          <PriorityBadge priority={item.priority} />
          <StatusBadge status={item.status} />
        </div>
        <div className="case-col-payment" title={item.razorpayPaymentId}>
          {item.razorpayPaymentId}
        </div>
        <div className="case-col-decision">
          <div className="decision-line"><span>RULE</span> {formatAction(item.ruleBasedAction)}</div>
          <div className="decision-line"><span>FINAL</span> {formatAction(item.finalAction)}</div>
        </div>
        <div className="case-col-amount">
          <AmountCell amount={item.revenueAtRisk} recovered={item.recoveredAmount} />
        </div>
        <div className="case-col-age">
          {formatRelativeTime(item.updatedAt || item.createdAt)}
        </div>
        <div className="case-col-arrow">
          <ArrowUpRight size={16} />
        </div>
      </button>
    );
  }

  const decisionText = item.ruleBasedAction === item.finalAction 
    ? formatAction(item.finalAction) 
    : `${formatAction(item.ruleBasedAction)} \u2192 ${formatAction(item.finalAction)}`;

  return (
    <button className="workspace-case workspace-case-narrow" onClick={() => navigate(`/cases/${item.id}`)}>
      <div className="case-narrow-top">
        <span className="workspace-case-id">#{item.id}</span>
        <PriorityBadge priority={item.priority} />
        <StatusBadge status={item.status} />
      </div>
      <div className="case-narrow-payment" title={item.razorpayPaymentId}>
        {item.razorpayPaymentId}
      </div>
      <div className="case-narrow-amount">
        <AmountCell amount={item.revenueAtRisk} recovered={item.recoveredAmount} />
      </div>
      <div className="case-narrow-decision">
        {decisionText}
      </div>
      <div className="case-narrow-footer">
        <span className="case-narrow-age">{formatRelativeTime(item.updatedAt || item.createdAt)}</span>
        <ArrowUpRight size={15} />
      </div>
    </button>
  );
}

function WorkSection({ title, description, icon: Icon, items, navigate, empty, variant }) {
  return <section className={`work-section work-${variant}`}><div className="work-section-heading"><div className="work-section-title"><Icon size={17} /><div><h2>{title}</h2><p>{description}</p></div></div><span className="work-count">{items.length}</span></div>{items.length ? <div className="workspace-list">{items.slice(0, 6).map((item) => <CaseRow key={item.id} item={item} navigate={navigate} variant={variant} />)}</div> : <div className="workspace-empty">{empty}</div>}</section>;
}

export function WorkspacePage() {
  const { data: cases, isLoading, isError } = useRecoveryCases();
  const { openNewRecovery, refresh } = useShellActions();
  const navigate = useNavigate();
  const items = cases || [];
  const needsAttention = items.filter((item) => item.status === 'REQUIRES_APPROVAL' || item.priority === 'CRITICAL' || Number(item.revenueAtRisk || 0) >= 50000);
  const active = items.filter((item) => ['OPEN', 'IN_PROGRESS'].includes(item.status));
  const recovered = items.filter((item) => item.status === 'RECOVERED' && Number(item.recoveredAmount || 0) > 0).sort((a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt));
  const stopped = items.filter((item) => item.status === 'STOPPED').sort((a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt));
  const activeRisk = active.reduce((total, item) => total + Number(item.revenueAtRisk || 0), 0);

  let content = <motion.main className="workspace-content" initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}><div className="workspace-intro"><div><span className="eyebrow">Operations inbox</span><h1>Move recoveries forward.</h1><p>Prioritize risk, approve guarded actions, and monitor revenue returning to your business.</p></div><div className="workspace-hero-stats"><div><strong>{active.length}</strong><span>Active Recoveries</span></div><div><strong>{formatINR(activeRisk)}</strong><span>Revenue at risk</span></div></div></div><div className="workspace-sections"><WorkSection variant="attention" title="Needs Attention" description="Critical, high-value, or approval-required cases" icon={CircleAlert} items={needsAttention} navigate={navigate} empty="No cases need immediate attention." /><WorkSection variant="recovered" title="Recently Recovered" description="Revenue returned by successful actions" icon={CheckCircle2} items={recovered} navigate={navigate} empty="Recovered cases will appear here." /><WorkSection variant="stopped" title="Recently Stopped" description="Cases closed without a successful recovery" icon={Ban} items={stopped} navigate={navigate} empty="No stopped recoveries in the current data." /><WorkSection variant="active" title="Active Recovery Stream" description="Cases currently being worked by the recovery policy" icon={Clock3} items={active} navigate={navigate} empty="No active recoveries right now." /></div></motion.main>;
  if (isLoading) content = <div className="workspace-loading">Loading operational queue...</div>;
  if (isError) content = <EmptyState title="Workspace unavailable" description="Connect the recovery service to load operational cases." />;
  return <div className="workspace-page"><TopBar title="Recovery Workspace" subtitle="What requires attention right now" onNewRecovery={openNewRecovery} onRefresh={refresh} />{content}</div>;
}

export default WorkspacePage;
