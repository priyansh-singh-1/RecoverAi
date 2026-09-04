import client from './client';

export const approveRecoveryCase = async (recoveryCaseId, request) => {
  const { data } = await client.post(
    `/recovery-approvals/${recoveryCaseId}/approve`,
    request
  );
  return data;
};

export const rejectRecoveryCase = async (recoveryCaseId, request) => {
  const { data } = await client.post(
    `/recovery-approvals/${recoveryCaseId}/reject`,
    request
  );
  return data;
};
