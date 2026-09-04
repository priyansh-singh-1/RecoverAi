import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { AlertCircle, CheckCircle2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { Drawer } from '@/components/ui/Drawer';
import { Button } from '@/components/ui/Button';
import { createFailedPayment } from '@/api/payments';
import './NewRecoveryDrawer.css';

const FAILURE_REASONS = ['NETWORK_ERROR', 'TIMEOUT', 'INSUFFICIENT_FUNDS', 'DECLINED'];

export default function NewRecoveryDrawer({ isOpen, onClose }) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [form, setForm] = useState({ razorpayPaymentId: '', amount: '', failureReason: 'NETWORK_ERROR', attemptCount: '1' });
  const [created, setCreated] = useState(null);

  const mutation = useMutation({
    mutationFn: createFailedPayment,
    onSuccess: async (payment) => {
      setCreated(payment);
      await queryClient.invalidateQueries({ queryKey: ['recovery-cases'] });
      await queryClient.invalidateQueries({ queryKey: ['recovery-metrics'] });
      const cases = await queryClient.fetchQuery({ queryKey: ['recovery-cases'] });
      const match = cases?.find((item) => item.razorpayPaymentId === payment.razorpayPaymentId);
      if (match) {
        toast.success('Recovery case created');
        onClose();
        navigate(`/cases/${match.id}`);
      }
    },
    onError: (error) => toast.error(error.response?.data?.message || 'Unable to create failed payment'),
  });

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));
  const submit = (event) => {
    event.preventDefault();
    mutation.mutate({ ...form, amount: Number(form.amount), attemptCount: Number(form.attemptCount) });
  };

  return (
    <Drawer isOpen={isOpen} onClose={onClose} title="New Recovery" width="480px">
      {created ? (
        <div className="new-recovery-success">
          <CheckCircle2 size={36} />
          <h3>Recovery case created</h3>
          <p>Payment {created.razorpayPaymentId} is now entering the decision pipeline.</p>
        </div>
      ) : (
        <form className="new-recovery-form" onSubmit={submit}>
          <div className="new-recovery-intro"><AlertCircle size={18} /><p>Create a failed payment to start an end-to-end recovery decision.</p></div>
          <label>Payment ID<input required value={form.razorpayPaymentId} onChange={(event) => update('razorpayPaymentId', event.target.value)} placeholder="pay_demo_001" /></label>
          <label>Amount<input required min="1" step="1" type="number" value={form.amount} onChange={(event) => update('amount', event.target.value)} placeholder="6500" /></label>
          <label>Failure reason<select value={form.failureReason} onChange={(event) => update('failureReason', event.target.value)}>{FAILURE_REASONS.map((reason) => <option key={reason}>{reason}</option>)}</select></label>
          <label>Attempt count<input required min="1" step="1" type="number" value={form.attemptCount} onChange={(event) => update('attemptCount', event.target.value)} /></label>
          <div className="new-recovery-actions"><Button variant="ghost" onClick={onClose}>Cancel</Button><Button type="submit" loading={mutation.isPending}>Create failed payment</Button></div>
        </form>
      )}
    </Drawer>
  );
}
