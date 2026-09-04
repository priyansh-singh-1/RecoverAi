import React from 'react';
import { Outlet } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import Sidebar from './Sidebar';
import NewRecoveryDrawer from '@/components/recovery/NewRecoveryDrawer';
import './Shell.css';

export default function Shell() {
  const queryClient = useQueryClient();
  const [newRecoveryOpen, setNewRecoveryOpen] = React.useState(false);
  const refresh = () => queryClient.invalidateQueries();
  return (
    <div className="shell-container">
      <Sidebar />
      <main className="shell-main">
        <div className="shell-content">
          <Outlet context={{ openNewRecovery: () => setNewRecoveryOpen(true), refresh }} />
        </div>
      </main>
      <NewRecoveryDrawer isOpen={newRecoveryOpen} onClose={() => setNewRecoveryOpen(false)} />
    </div>
  );
}
