import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from 'recharts';
import ChartCard from './ChartCard';
import CustomTooltip from './CustomTooltip';

const PRIORITY_COLORS = {
  CRITICAL: '#ef4444',
  HIGH: '#f97316', // Note: use explicit hex instead of var(--chart-3)
  MEDIUM: '#f59e0b',
  LOW: '#6b7280'
};

export default function PriorityChart({ data = {} }) {
  const chartData = useMemo(() => {
    const order = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW'];
    return order.map(priority => ({
      priority,
      count: data[priority] || 0
    })).filter(item => item.count > 0);
  }, [data]);

  return (
    <ChartCard title="Cases by Priority" height={200}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} layout="vertical" margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#52525b" opacity={0.3} horizontal={true} vertical={true} />
          <XAxis type="number" tick={{ fill: '#52525b' }} />
          <YAxis dataKey="priority" type="category" width={80} tick={{ fill: '#a1a1aa', fontSize: 12 }} />
          <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
          <Bar dataKey="count" name="Count" radius={[0, 4, 4, 0]} animationDuration={1000}>
            {chartData.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={PRIORITY_COLORS[entry.priority] || '#6366f1'} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
