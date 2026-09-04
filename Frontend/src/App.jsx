import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ToastProvider } from '@/components/ui/Toast';

import Shell from '@/components/layout/Shell';
import { DashboardPage } from '@/pages/Dashboard/DashboardPage';
import { CasesPage } from '@/pages/Cases/CasesPage';
import { CaseDetailPage } from '@/pages/CaseDetail/CaseDetailPage';
import { HumanReviewPage } from '@/pages/HumanReview/HumanReviewPage';
import { AnalyticsPage } from '@/pages/Analytics/AnalyticsPage';
import { WorkspacePage } from '@/pages/Workspace/WorkspacePage';
import { ActivityPage } from '@/pages/Activity/ActivityPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Shell />}>
            <Route index element={<DashboardPage />} />
            <Route path="workspace" element={<WorkspacePage />} />
            <Route path="cases" element={<CasesPage />} />
            <Route path="cases/:id" element={<CaseDetailPage />} />
            <Route path="review" element={<HumanReviewPage />} />
            <Route path="analytics" element={<AnalyticsPage />} />
            <Route path="activity" element={<ActivityPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
      <ToastProvider />
    </QueryClientProvider>
  );
}

export default App;
