import client from './client';

export const getAllRecoveryCases = async () => {
  const { data } = await client.get('/recovery-cases/get');
  return data;
};

export const getOpenRecoveryCases = async () => {
  const { data } = await client.get('/recovery-cases/open');
  return data;
};
