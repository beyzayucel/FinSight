import {
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type { FundDashboardPeriod } from '../../lib/fund-dashboard/fundDashboardApi'
import {
  formatAxisDate,
  formatIndexChange,
  formatIsoDate,
} from '../../lib/fund-dashboard/fundDashboardFormatters'
import { getTranslations } from '@/i18n/translations'

type BenchmarkComparisonCardProps = {
  period: FundDashboardPeriod
  periodDays: number
}

export default function BenchmarkComparisonCard({
  period,
  periodDays,
}: BenchmarkComparisonCardProps) {
  const t = getTranslations()
  const series = period.series ?? []

  return (
    <div className="lg:col-span-7 bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 flex flex-col">
      <div>
        <h3 className="text-base font-bold text-slate-800">{t.benchmarkComparison}</h3>
        <p className="text-[11px] text-slate-500 font-medium mt-0.5">
          {t.benchmarkSubtitle(periodDays, formatIsoDate(period.previousDate))}
        </p>
      </div>

      {series.length === 0 ? (
        <div className="flex-1 min-h-[200px] flex items-center justify-center">
          <p className="max-w-[260px] text-center text-xs text-slate-400 font-medium leading-relaxed">
            {t.benchmarkEmpty}
          </p>
        </div>
      ) : (
        <>
          <div className="flex items-center gap-5 mt-4">
            <span className="flex items-center gap-2 text-[11px] font-medium text-slate-500">
              <span className="inline-block w-5 border-t-2 border-[#c89834]" /> {t.fundLabel}
            </span>
            <span className="flex items-center gap-2 text-[11px] font-medium text-slate-500">
              <span className="inline-block w-5 border-t-2 border-[#1c2530]" /> Benchmark
            </span>
          </div>

          <div className="flex-1 min-h-[200px] mt-3">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={series} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
                <CartesianGrid vertical={false} stroke="#e5e7eb" />
                <XAxis
                  dataKey="date"
                  tickFormatter={formatAxisDate}
                  tick={{ fontSize: 10, fill: '#94a3b8' }}
                  tickLine={false}
                  axisLine={false}
                  minTickGap={28}
                />
                <YAxis hide domain={['dataMin', 'dataMax']} />
                <Tooltip
                  labelFormatter={(label) =>
                    `${formatIsoDate(String(label))} · ${t.benchmarkSince(formatIsoDate(period.previousDate))}`
                  }
                  formatter={(value, name) => [
                    formatIndexChange(Number(value)),
                    name === 'fund' ? t.fundLabel : t.pcLegendBenchmark,
                  ]}
                  contentStyle={{
                    borderRadius: '0.75rem',
                    border: '1px solid #e2e8f0',
                    fontSize: '11px',
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="fund"
                  stroke="#c89834"
                  strokeWidth={2.5}
                  dot={false}
                  isAnimationActive={false}
                />
                <Line
                  type="monotone"
                  dataKey="benchmark"
                  stroke="#1c2530"
                  strokeWidth={2}
                  dot={false}
                  isAnimationActive={false}
                  connectNulls
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </>
      )}
    </div>
  )
}
