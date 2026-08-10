import { useCallback, useEffect, useState } from 'react'
import { CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { MdInfoOutline } from 'react-icons/md'
import { getTranslations } from '@/i18n/translations'
import { getLang } from '@/lib/authStore'
import { useDecision } from '@/features/dashboard/context/decisionStore'
import {
  formatCurrency,
  formatDate,
  formatSignedPercent,
  formatUnsignedPercent,
  isoToLocalDate,
} from '@/features/dashboard/lib/formatters'
import {
  getPerformanceComparison,
  mergeToChartPoints,
  type ChartPoint,
  type PerformanceComparisonResponse,
  type PortfolioMetrics,
} from '@/features/dashboard/lib/performanceComparisonApi'

const SERIES = [
  { key: 'mevcut' as const, color: '#B9862B', dash: undefined, width: 2.5 },
  { key: 'simulasyon' as const, color: '#172554', dash: '6 3', width: 2.5 },
  { key: 'benchmark' as const, color: '#1c2530', dash: '4 3', width: 2 },
]


type Props = {
  onGoToStressTest: () => void
}

type FetchState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; data: PerformanceComparisonResponse; chartPoints: ChartPoint[] }

export default function PerformanceComparisonPage({ onGoToStressTest }: Props) {
  const t = getTranslations()
  const lang = getLang() === 'en' ? 'en' : 'tr'
  const { activeFund, analysisWindow } = useDecision()

  const [state, setState] = useState<FetchState>({ status: 'idle' })
  const [hiddenSeries, setHiddenSeries] = useState<Set<string>>(new Set())

  const toggleSeries = (key: string) => {
    setHiddenSeries((prev) => {
      const next = new Set(prev)
      if (next.has(key)) next.delete(key)
      else next.add(key)
      return next
    })
  }

  const fetchData = useCallback(async () => {
    setState({ status: 'loading' })
    try {
      const data = await getPerformanceComparison(activeFund.code, analysisWindow)
      const chartPoints = mergeToChartPoints(data)
      setState({ status: 'ready', data, chartPoints })
    } catch (err) {
      const message = err instanceof Error ? err.message : t.fundInfoErrorTitle
      setState({ status: 'error', message })
    }
  }, [activeFund.code, analysisWindow, t.fundInfoErrorTitle])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const legend: { key: 'mevcut' | 'simulasyon' | 'benchmark'; label: string }[] = [
    { key: 'mevcut', label: t.pcLegendMevcut },
    { key: 'simulasyon', label: t.pcLegendSimulasyon },
    { key: 'benchmark', label: t.pcLegendBenchmark },
  ]

  const rows: { label: string; format: (m: PortfolioMetrics) => string; colored: boolean }[] = [
    { label: t.pcMetricCurrentValue, format: (m) => formatCurrency(m.currentValue), colored: false },
    { label: t.pcMetricTotalReturn, format: (m) => formatSignedPercent(m.totalReturnPct), colored: true },
    { label: t.pcMetricMaxDrawdown, format: (m) => formatSignedPercent(m.maxDrawdownPct), colored: true },
    { label: t.pcMetricDailyVolatility, format: (m) => formatUnsignedPercent(m.dailyVolatilityPct), colored: false },
  ]

  function toneClass(value: number): string {
    if (value > 0) return 'text-primary'
    if (value < 0) return 'text-red-600'
    return 'text-ink'
  }

  return (
    <div>
      <h1 className="text-3xl font-semibold text-ink">{t.pcTitle}</h1>
      <p className="mt-3 max-w-3xl text-sm leading-relaxed text-muted">{t.pcDescription}</p>

      {state.status === 'loading' || state.status === 'idle' ? (
        <p className="mt-8 text-sm text-muted">{t.fundInfoLoadingText}</p>
      ) : state.status === 'error' ? (
        <div className="mt-8 rounded-2xl bg-red-50 px-6 py-8 text-center text-sm font-medium text-red-600 shadow-sm">
          {t.fundInfoErrorTitle}: {state.message}
        </div>
      ) : !state.data.simulationPortfolio ? (
        /* Senaryo uygulanmamış — empty state */
        <div className="mt-8 rounded-2xl border border-black/5 bg-[#f5f0e8] px-6 py-5 shadow-sm">
          <div className="flex items-start gap-3">
            <MdInfoOutline className="mt-0.5 h-5 w-5 flex-shrink-0 text-muted" />
            <p className="text-sm text-muted">{t.pcLockedMessage}</p>
          </div>
        </div>
      ) : (
        <>
          {/* ── Kümülatif Getiri grafiği ──────────────────────────────── */}
          <div className="mt-8 rounded-2xl border border-black/5 bg-white p-6 shadow-sm sm:p-8">
            <h2 className="text-lg font-bold text-ink">{t.pcChartTitle}</h2>
            <p className="mt-1 text-sm text-muted">
              {t.pcPeriodLabel(
                formatDate(isoToLocalDate(state.data.dataDate), lang),
                analysisWindow,
                state.data.scenarioSource === 'MANUAL' ? t.pcSourceManual : t.pcSourceAi,
                t.pcStatusAccepted
              )}
            </p>

            <div className="mt-4 flex flex-wrap gap-5">
              {legend.map((item) => {
                const series = SERIES.find((s) => s.key === item.key)!
                const isHidden = hiddenSeries.has(item.key)
                return (
                  <button
                    key={item.key}
                    type="button"
                    onClick={() => toggleSeries(item.key)}
                    className={`flex items-center gap-2 text-xs font-medium transition-opacity ${
                      isHidden ? 'opacity-30' : 'opacity-100'
                    }`}
                    style={{ color: series.color }}
                  >
                    <span
                      className="inline-block h-0 w-5 border-t-2"
                      style={{
                        borderColor: series.color,
                        borderStyle: series.dash ? 'dashed' : 'solid',
                      }}
                    />
                    {item.label}
                  </button>
                )
              })}
            </div>

            <div className="mt-4 h-80 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={state.chartPoints} margin={{ top: 8, right: 16, left: 4, bottom: 8 }}>
                  <CartesianGrid vertical={false} stroke="#e5e7eb" />
                  <YAxis
                    domain={['dataMin', 'dataMax']}
                    tick={{ fontSize: 11, fill: '#6b7683' }}
                    axisLine={false}
                    tickLine={false}
                    tickFormatter={(v: number) => `${v.toFixed(1)}%`}
                    width={52}
                    label={{ value: t.pcChartTitle, angle: -90, position: 'insideLeft', style: { fontSize: 11, fill: '#6b7683' }, offset: 8 }}
                  />
                  <XAxis
                    dataKey="dateLabel"
                    tick={{ fontSize: 11, fill: '#6b7683' }}
                    axisLine={{ stroke: '#e5e7eb' }}
                    tickLine={false}
                    interval="preserveStartEnd"
                  />
                  <Tooltip
                    contentStyle={{ borderRadius: 10, border: '1px solid #e5e7eb', fontSize: 13 }}
                    itemSorter={(item) => {
                      const order: Record<string, number> = { mevcut: 0, simulasyon: 1, benchmark: 2 }
                      return order[item.dataKey as string] ?? 3
                    }}
                    formatter={(value, name) => {
                      const label = name === 'mevcut' ? t.pcLegendMevcut
                        : name === 'simulasyon' ? t.pcLegendSimulasyon
                        : t.pcLegendBenchmark
                      return [`${Number(value ?? 0).toFixed(2)}%`, label]
                    }}
                    labelFormatter={(label) => label}
                  />
                  {SERIES.map((series) => (
                    <Line
                      key={series.key}
                      type="monotone"
                      dataKey={series.key}
                      stroke={series.color}
                      strokeWidth={series.width}
                      strokeDasharray={series.dash}
                      dot={false}
                      isAnimationActive={false}
                      connectNulls={false}
                      hide={hiddenSeries.has(series.key)}
                    />
                  ))}
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* ── Performans Özeti tablosu ──────────────────────────────── */}
          <div className="mt-6 rounded-2xl border border-black/5 bg-white p-6 shadow-sm sm:p-8">
            <h2 className="text-lg font-bold text-ink">{t.pcSummaryTitle}</h2>
            <p className="mt-1 text-sm text-muted">{t.pcSummarySubtitle}</p>

            <div className="mt-5 overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-xs font-semibold tracking-wide text-muted">
                    <th className="pb-3 pr-4">{t.pcMetricHeader}</th>
                    <th className="pb-3 pr-4">{t.pcLegendMevcut.toLocaleUpperCase(lang)}</th>
                    <th className="pb-3 pr-4">{t.pcLegendSimulasyon.toLocaleUpperCase(lang)}</th>
                    <th className="pb-3">{t.pcLegendBenchmark.toLocaleUpperCase(lang)}</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row) => {
                    const metricsMap = {
                      mevcut: state.data.currentPortfolio.metrics,
                      simulasyon: state.data.simulationPortfolio!.metrics,
                      benchmark: state.data.benchmarkPortfolio?.metrics,
                    }

                    return (
                      <tr key={row.label} className="border-t border-black/5">
                        <td className="py-3 pr-4 font-medium text-ink">{row.label}</td>
                        {(['mevcut', 'simulasyon', 'benchmark'] as const).map((key) => {
                          const metrics = metricsMap[key]
                          if (!metrics) {
                            return <td key={key} className="py-3 pr-4 text-muted">—</td>
                          }
                          const value =
                            row.label === t.pcMetricTotalReturn
                              ? metrics.totalReturnPct
                              : row.label === t.pcMetricMaxDrawdown
                                ? metrics.maxDrawdownPct
                                : 0
                          return (
                            <td
                              key={key}
                              className={`py-3 pr-4 font-semibold ${row.colored ? toneClass(value) : 'text-ink'}`}
                            >
                              {row.format(metrics)}
                            </td>
                          )
                        })}
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>

          <p className="mt-4 text-xs text-muted">{t.pcFootnote}</p>

          <button
            type="button"
            onClick={onGoToStressTest}
            className="mt-6 inline-flex items-center gap-2 rounded-xl bg-ink px-5 py-3 text-sm font-semibold text-white transition-colors hover:bg-black"
          >
            {t.pcGoToStressTest}
            <span aria-hidden>→</span>
          </button>
        </>
      )}
    </div>
  )
}
