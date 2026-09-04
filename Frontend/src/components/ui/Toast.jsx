import React from 'react';
import { Toaster } from 'react-hot-toast';

export function ToastProvider() {
  return (
    <Toaster
      position="bottom-right"
      toastOptions={{
        duration: 4000,
        style: {
          background: 'var(--bg-elevated)',
          color: 'var(--text-primary)',
          border: '1px solid var(--border-strong)',
          borderRadius: 'var(--radius-md)',
          fontSize: 'var(--text-base)',
          boxShadow: 'var(--shadow-lg)'
        },
        success: {
          iconTheme: {
            primary: 'var(--success)',
            secondary: 'var(--bg-elevated)'
          }
        },
        error: {
          iconTheme: {
            primary: 'var(--danger)',
            secondary: 'var(--bg-elevated)'
          }
        }
      }}
    />
  );
}
