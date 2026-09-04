import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { GitBranch, Cpu, Shield, Bot, User, Zap, Settings, Clock } from 'lucide-react';
import { EVENT_LABELS } from '@/utils/statusConfig';
import { formatDateTime } from '@/utils/format';
import { parseMetadata } from '@/utils/parseAuditMetadata';
import './AuditTimeline.css';

const ACTOR_ICONS = {
  SYSTEM: { label: 'System', icon: Settings, color: '#52525b' },
  RAZORPAY: { label: 'Razorpay', icon: Zap, color: '#f59e0b' },
  RULE_ENGINE: { label: 'Rule Engine', icon: GitBranch, color: '#3b82f6' },
  AI_AGENT: { label: 'AI Agent', icon: Bot, color: '#a855f7' },
  POLICY_ENGINE: { label: 'Policy Engine', icon: Shield, color: '#22c55e' },
  HUMAN: { label: 'Human Agent', icon: User, color: '#10b981' }
};

const TimelineEvent = ({ event, index }) => {
  const [expanded, setExpanded] = useState(false);
  
  const actorConf = ACTOR_ICONS[event.actor] || { label: event.actor, icon: Cpu, color: '#52525b' };
  const Icon = actorConf.icon;
  const metadata = event.metadata ? parseMetadata(event.metadata) : null;
  const important = ['ML_PREDICTION_GENERATED', 'POLICY_DECISION_GENERATED', 'AI_AGENT_ANALYSIS_GENERATED', 'RECOVERY_ACTION_SUCCEEDED', 'RECOVERY_MARKED_SUCCESS', 'RECOVERY_STOPPED'].includes(event.eventType);

  return (
    <motion.div 
      className={`timeline-event ${important ? 'important' : ''}`}
      initial={{ opacity: 0, x: -8 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: index * 0.05 }}
    >
      <div className="timeline-line" />
      
      <div className="timeline-icon-container" style={{ borderColor: actorConf.color }}>
        <Icon size={16} color={actorConf.color} />
      </div>
      
      <div className="timeline-content">
        <div className="timeline-header">
          <span className="timeline-title">{EVENT_LABELS[event.eventType] || event.eventType}</span>
          <span className="timeline-timestamp">
            <Clock size={10} style={{ display: 'inline', marginRight: 4 }} />
            {formatDateTime(event.createdAt)}
          </span>
        </div>
        
        <div className="timeline-actor">{actorConf.label}</div>
        
        {event.oldState && event.newState && (
          <div className="timeline-state-change">
            <span className="state-old">{event.oldState}</span>
            <span className="state-arrow">→</span>
            <span className="state-new">{event.newState}</span>
          </div>
        )}
        
        {event.reason && !expanded && (
          <div className="timeline-reason">
            {event.reason.length > 80 ? `${event.reason.substring(0, 80)}...` : event.reason}
          </div>
        )}
        {event.reason && expanded && (
          <div className="timeline-reason">{event.reason}</div>
        )}

        {((metadata && Object.keys(metadata).length > 0) || (event.reason && event.reason.length > 80)) && (
          <>
            <button className="timeline-metadata-toggle" onClick={() => setExpanded(!expanded)}>
              {expanded ? 'Hide details' : 'View details'}
            </button>
            {expanded && metadata && Object.keys(metadata).length > 0 && (
              <pre className="timeline-metadata">
                {JSON.stringify(metadata, null, 2)}
              </pre>
            )}
          </>
        )}
      </div>
    </motion.div>
  );
};

export default function AuditTimeline({ auditLogs = [] }) {
  if (!auditLogs || auditLogs.length === 0) return <div className="empty-text">No audit events available.</div>;
  
  return (
    <div className="chart-card" style={{ height: 'auto' }}>
      <h3 className="chart-card-title">Activity Timeline</h3>
      <div className="audit-timeline">
        {auditLogs.map((event, i) => (
          <TimelineEvent key={event.id || i} event={event} index={i} />
        ))}
      </div>
    </div>
  );
}
