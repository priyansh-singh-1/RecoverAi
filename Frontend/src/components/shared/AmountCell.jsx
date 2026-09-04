import React from 'react';
import './AmountCell.css';
import { formatINR } from '@/utils/format';

export default function AmountCell({ amount, recovered, variant = 'default' }) {
  const formattedAmount = formatINR(amount);
  
  return (
    <div className="amount-cell">
      <span className={variant === 'hero' ? 'amount-cell-hero' : 'amount-cell-default'}>
        {formattedAmount}
      </span>
      {recovered > 0 && (
        <span className="amount-cell-recovered">
          {formatINR(recovered)}
        </span>
      )}
    </div>
  );
}
