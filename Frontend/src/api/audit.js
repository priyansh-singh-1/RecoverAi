import client from './client';

export const getAllAuditLogs = async () => {
  const { data } = await client.get('/audit-logs');
  return data;
};

export const getAuditLogsByPayment = async (paymentId) => {
  const { data } = await client.get(`/audit-logs/payment/${paymentId}`);
  return data;
};

export const getAuditLogsByRecoveryCase = async (recoveryCaseId) => {
  const { data } = await client.get(`/audit-logs/recovery-case/${recoveryCaseId}`);
  return data;
};
