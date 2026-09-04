import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import ChartCard from './ChartCard';
import CustomTooltip from './CustomTooltip';
import { formatINR } from '@/utils/format';

export default function RevenueBar({ recovered = 0, outstanding = 0, total = 0 }) {
  const data = useMemo(() => [{
    name: 'Revenue',
    recovered: recovered,
    outstanding: outstanding
  }], [recovered, outstanding]);

  return (
    <ChartCard title={`Total Revenue: ${formatINR(total)}`} height={120}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart layout="vertical" data={data} margin={{ top: 10, right: 10, left: 10, bottom: 10 }}>
          <XAxis type="number" hide />
          <YAxis type="category" dataKey="name" hide />
          <Tooltip content={<CustomTooltip formatter={formatINR} />} cursor={{ fill: 'transparent' }} />
          <Legend wrapperStyle={{ paddingTop: '10px' }} />
          <Bar dataKey="recovered" name="Recovered" fill="var(--recovered)" stackId="a" radius={[4, 0, 0, 4]} animationDuration={1000} />
          <Bar dataKey="outstanding" name="Outstanding" fill="#334155" stackId="a" radius={[0, 4, 4, 0]} animationDuration={1000} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
