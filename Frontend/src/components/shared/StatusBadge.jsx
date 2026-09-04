import React from 'react';
import { Badge } from '@/components/ui/Badge';
import { STATUS_CONFIG, STATUS_TO_VARIANT, EXECUTION_STATUS_CONFIG } from '@/utils/statusConfig';

export default function StatusBadge({ status }) {
  if (!status) return null;
  const variant = STATUS_TO_VARIANT[status] || ({ SUCCESS: 'success', FAILED: 'danger', EXECUTING: 'info', REQUIRES_APPROVAL: 'purple' }[status] || 'default');
  const label = STATUS_CONFIG[status]?.label || EXECUTION_STATUS_CONFIG[status]?.label || status;
  
  return (
    <Badge variant={variant}>
      {label}
    </Badge>
  );
}
