import client from './client';

export const executeRecoveryAction = async (recoveryCaseId) => {
  const { data } = await client.post(
    `/recovery-actions/recovery-case/${recoveryCaseId}/execute`
  );
  return data;
};

export const getExecutionHistory = async (recoveryCaseId) => {
  const { data } = await client.get(
    `/recovery-actions/recovery-case/${recoveryCaseId}`
  );
  return data;
};
