import { useQuery } from '@tanstack/react-query';
import { getAllRecoveryCases, getOpenRecoveryCases } from '@/api/cases';

export const useRecoveryCases = () => {
  return useQuery({
    queryKey: ['recovery-cases'],
    queryFn: getAllRecoveryCases,
  });
};

export const useOpenRecoveryCases = () => {
  return useQuery({
    queryKey: ['recovery-cases', 'open'],
    queryFn: getOpenRecoveryCases,
  });
};
