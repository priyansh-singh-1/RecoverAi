import client from './client';

export const getRecoveryMetrics = async () => {
  const { data } = await client.get('/analytics/recovery');
  return data;
};
