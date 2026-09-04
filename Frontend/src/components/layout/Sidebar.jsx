import React from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, BriefcaseBusiness, FolderOpen, UserCheck, AreaChart, Activity } from 'lucide-react';
import './Sidebar.css';

export default function Sidebar() {
  const navItems = [
    { path: '/', label: 'Overview', icon: LayoutDashboard },
    { path: '/workspace', label: 'Recovery Workspace', icon: BriefcaseBusiness },
    { path: '/cases', label: 'Recovery Cases', icon: FolderOpen },
    { path: '/review', label: 'Human Review', icon: UserCheck },
    { path: '/analytics', label: 'Analytics', icon: AreaChart },
    { path: '/activity', label: 'Activity', icon: Activity }
  ];
  const renderNavItems = (items) => items.map((item) => {
    const Icon = item.icon;
    return (
      <NavLink key={item.path} to={item.path} className={({ isActive }) => isActive ? 'sidebar-link active' : 'sidebar-link'}>
        <Icon size={18} />
        {item.label}
      </NavLink>
    );
  });

  return (
    <aside className="sidebar">
      <div className="sidebar-logo"><svg className="sidebar-mark" viewBox="0 0 32 32" aria-hidden="true"><path d="M8 24V8h8a5 5 0 0 1 0 10H8m7-5 9 11" /><path d="M18 8h6v6" /></svg><span>Recover<span className="wordmark-ai">AI</span></span></div>
      
      <nav className="sidebar-nav">
        <span className="sidebar-section-label">Operations</span>
        {renderNavItems(navItems.slice(0, 4))}
        <span className="sidebar-section-label sidebar-intelligence-label">Intelligence</span>
        {renderNavItems(navItems.slice(4))}
      </nav>

      <div className="sidebar-bottom">
        <div className="status-dot"></div>
        <span className="status-text">System Online</span>
      </div>
    </aside>
  );
}
