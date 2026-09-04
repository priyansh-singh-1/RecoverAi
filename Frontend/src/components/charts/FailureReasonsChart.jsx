import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import ChartCard from './ChartCard';
import CustomTooltip from './CustomTooltip';

export default function FailureReasonsChart({ data = {} }) {
  const chartData = useMemo(() => {
    return Object.entries(data).map(([reason, count]) => ({
      reason: reason.length > 30 ? reason.substring(0, 30) + '...' : reason,
      fullReason: reason,
      count
    })).sort((a, b) => b.count - a.count);
  }, [data]);

  return (
    <ChartCard title="Top Failure Reasons" height={250}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} layout="vertical" margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#52525b" opacity={0.3} horizontal={true} vertical={true} />
          <XAxis type="number" tick={{ fill: '#52525b' }} />
          <YAxis dataKey="reason" type="category" width={160} tick={{ fill: '#a1a1aa', fontSize: 12 }} />
          <Tooltip content={<CustomTooltip />} cursor={{ fill: 'rgba(255,255,255,0.04)' }} />
          <Bar dataKey="count" name="Count" fill="#f59e0b" radius={[0, 4, 4, 0]} animationDuration={1000} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
