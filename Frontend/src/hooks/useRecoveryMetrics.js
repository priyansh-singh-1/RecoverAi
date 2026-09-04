import { useQuery } from '@tanstack/react-query';
import { getRecoveryMetrics } from '@/api/analytics';

export const useRecoveryMetrics = () => {
  return useQuery({
    queryKey: ['recovery-metrics'],
    queryFn: getRecoveryMetrics,
    refetchInterval: 30000,
    staleTime: 10000,
  });
};
