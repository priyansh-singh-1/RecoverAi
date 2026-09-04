import React from 'react';
import { Plus, RefreshCw } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import './TopBar.css';

export default function TopBar({ title, subtitle, onNewRecovery, onRefresh }) {
  return (
    <header className="topbar">
      <div className="topbar-left">
        <h1 className="topbar-title">{title}</h1>
        {subtitle && <span className="topbar-subtitle">{subtitle}</span>}
      </div>
      
      <div className="topbar-right">
        <div className="pulse-dot"></div>
        <div className="live-text">
          <span className="live-label">Connected</span>
          <span className="refresh-text">Updated just now</span>
        </div>
        <Button variant="ghost" size="sm" onClick={onRefresh} aria-label="Refresh"><RefreshCw size={15} /> Refresh</Button>
        {onNewRecovery && <Button size="sm" onClick={onNewRecovery}><Plus size={15} /> New Recovery</Button>}
      </div>
    </header>
  );
}
