import React, { useMemo, useState } from 'react';
import { motion } from 'framer-motion';
import { Clock3, ChevronDown, Filter } from 'lucide-react';
import TopBar from '@/components/layout/TopBar';
import { EmptyState } from '@/components/ui/EmptyState';
import { useAuditLogs } from '@/hooks/useAuditLogs';
import { useShellActions } from '@/hooks/useShellActions';
import { formatDateTime, formatAction } from '@/utils/format';
import { parseMetadata } from '@/utils/parseAuditMetadata';
import './ActivityPage.css';

const actors = ['All actors', 'SYSTEM', 'RULE_ENGINE', 'AI_AGENT', 'HUMAN', 'RAZORPAY'];

function ActivityItem({ event, index }) {
  const [expanded, setExpanded] = useState(false);
  const metadata = parseMetadata(event.metadata);
  return <motion.article className="activity-item" initial={{ opacity: 0, x: -8 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: index * .025 }}><div className="activity-marker" /><div className="activity-item-body"><div className="activity-item-top"><div><span className="activity-actor">{formatAction(event.actor)}</span><h3>{formatAction(event.eventType)}</h3></div><time><Clock3 size={13} />{formatDateTime(event.createdAt)}</time></div>{event.reason && <p>{event.reason}</p>}{(event.oldState || event.newState) && <div className="activity-state"><span>{event.oldState || 'Initial'}</span><b>→</b><strong>{event.newState || 'Recorded'}</strong></div>}{metadata && <button className="activity-details" onClick={() => setExpanded((value) => !value)}><ChevronDown size={14} className={expanded ? 'rotated' : ''} />{expanded ? 'Hide metadata' : 'View metadata'}</button>}{expanded && <pre>{JSON.stringify(metadata, null, 2)}</pre>}</div></motion.article>;
}

export function ActivityPage() {
  const { data: auditLogs, isLoading, isError } = useAuditLogs();
  const { openNewRecovery, refresh } = useShellActions();
  const [actor, setActor] = useState('All actors');
  const [eventType, setEventType] = useState('');
  const [paymentId, setPaymentId] = useState('');
  const [caseId, setCaseId] = useState('');
  const eventTypes = [...new Set((auditLogs || []).map((event) => event.eventType).filter(Boolean))];
  const events = useMemo(() => (auditLogs || []).filter((event) => (actor === 'All actors' || event.actor === actor) && (!eventType || event.eventType === eventType) && (!paymentId || String(event.paymentId || '').includes(paymentId)) && (!caseId || String(event.recoveryCaseId || '').includes(caseId))), [auditLogs, actor, eventType, paymentId, caseId]);
  let feed = <div className="activity-feed">{events.map((event, index) => <ActivityItem key={event.id || `event-${event.eventType}-${event.createdAt}`} event={event} index={index} />)}</div>;
  if (isLoading) feed = <div className="activity-loading">Loading activity...</div>;
  if (isError) feed = <EmptyState title="Activity unavailable" description="Connect the recovery service to load audit events." />;
  if (!isLoading && !isError && events.length === 0) feed = <EmptyState title="No activity matches these filters" />;
  return <div className="activity-page"><TopBar title="Activity" subtitle="A complete record of recovery decisions" onNewRecovery={openNewRecovery} onRefresh={refresh} /><main className="activity-content"><div className="activity-heading"><div><span className="eyebrow">Auditability</span><h1>System activity</h1><p>Every recovery decision, action, and state change in one place.</p></div><span className="activity-total">{events.length} events</span></div><div className="activity-filters"><Filter size={16} /><select value={actor} onChange={(event) => setActor(event.target.value)} aria-label="Filter by actor">{actors.map((item) => <option key={item}>{item}</option>)}</select><select value={eventType} onChange={(event) => setEventType(event.target.value)} aria-label="Filter by event type"><option value="">All event types</option>{eventTypes.map((item) => <option key={item}>{item}</option>)}</select><input value={paymentId} onChange={(event) => setPaymentId(event.target.value)} placeholder="Payment ID" aria-label="Filter by payment ID" /><input value={caseId} onChange={(event) => setCaseId(event.target.value)} placeholder="Case ID" aria-label="Filter by recovery case ID" /></div>{feed}</main></div>;
}

export default ActivityPage;
