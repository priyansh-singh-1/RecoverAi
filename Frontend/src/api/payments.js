import client from './client';

export const createFailedPayment = async (payment) => {
  const { data } = await client.post('/payments/create-payment', {
    ...payment,
    status: 'FAILED',
  });
  return data;
};
