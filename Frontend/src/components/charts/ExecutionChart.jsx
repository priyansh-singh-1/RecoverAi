import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import ChartCard from './ChartCard';
import CustomTooltip from './CustomTooltip';

export default function ExecutionChart({ success = 0, failed = 0 }) {
  const chartData = useMemo(() => [
    { name: 'Success', count: success },
    { name: 'Failed', count: failed }
  ], [success, failed]);

  return (
    <ChartCard title="Execution Status" height={180}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} margin={{ top: 20, right: 10, left: -20, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#52525b" opacity={0.3} vertical={false} />
          <XAxis dataKey="name" tick={{ fill: '#a1a1aa', fontSize: 12 }} />
          <YAxis tick={{ fill: '#52525b' }} />
          <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
          <Bar dataKey="count" name="Executions" fill="var(--accent)" radius={[4, 4, 0, 0]} animationDuration={1000} barSize={40} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
