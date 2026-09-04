import { useQuery } from '@tanstack/react-query';
import { getExecutionHistory } from '@/api/executions';

export const useExecutionHistory = (recoveryCaseId) => {
  return useQuery({
    queryKey: ['executions', recoveryCaseId],
    queryFn: () => getExecutionHistory(recoveryCaseId),
    enabled: !!recoveryCaseId,
  });
};
