import { useQuery } from '@tanstack/react-query';
import { getAllAuditLogs, getAuditLogsByRecoveryCase } from '@/api/audit';

export const useAuditLogs = () => useQuery({
  queryKey: ['audit-logs'],
  queryFn: getAllAuditLogs,
});

export const useAuditLogsByCase = (recoveryCaseId) => {
  return useQuery({
    queryKey: ['audit-logs', 'case', recoveryCaseId],
    queryFn: () => getAuditLogsByRecoveryCase(recoveryCaseId),
    enabled: !!recoveryCaseId,
  });
};
