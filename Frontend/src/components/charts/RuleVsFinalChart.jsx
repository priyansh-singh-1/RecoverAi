import React, { useMemo } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import ChartCard from './ChartCard';
import CustomTooltip from './CustomTooltip';
import { formatAction } from '@/utils/format';

export default function RuleVsFinalChart({ ruleBreakdown = {}, finalBreakdown = {} }) {
  const data = useMemo(() => {
    const keys = new Set([...Object.keys(ruleBreakdown), ...Object.keys(finalBreakdown)]);
    return Array.from(keys).map(key => ({
      action: formatAction(key),
      rule: ruleBreakdown[key] || 0,
      final: finalBreakdown[key] || 0
    })).sort((a, b) => (b.final + b.rule) - (a.final + a.rule));
  }, [ruleBreakdown, finalBreakdown]);

  return (
    <ChartCard title="Rule Engine vs Final Decision" height={320}>
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={data} layout="vertical" margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
          <CartesianGrid strokeDasharray="3 4" stroke="rgba(148,163,184,.12)" horizontal={true} vertical={false} />
          <XAxis type="number" tick={{ fill: '#52525b' }} />
          <YAxis dataKey="action" type="category" width={160} tick={{ fill: '#a1a1aa', fontSize: 12 }} />
          <Tooltip content={<CustomTooltip showDelta />} cursor={{ fill: 'rgba(124,92,255,.06)' }} />
          <Legend wrapperStyle={{ paddingTop: '10px' }} />
          <Bar dataKey="rule" name="Rule Engine" fill="#64748b" radius={[0, 4, 4, 0]} animationDuration={1000} />
          <Bar dataKey="final" name="Final Decision" fill="#7c5cff" radius={[0, 4, 4, 0]} animationDuration={1000} />
        </BarChart>
      </ResponsiveContainer>
    </ChartCard>
  );
}
