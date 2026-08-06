import { useMemo } from 'react'
import { CartesianGrid, Line, LineChart, ResponsiveContainer, YAxis } from 'recharts'
import { MdLockOutline } from 'react-icons/md'
import { getTranslations } from '@/i18n/translations'
import { getLang } from '@/lib/authStore'
import { useDecision } from '@/features/dashboard/context/decisionStore'
import { runSimulation, type PortfolioMetrics, type SimulationWindow } from '@/features/dashboard/lib/simulation'
import { formatCurrency, formatDate, formatSignedPercent, formatUnsignedPercent } from '@/features/dashboard/lib/formatters'
import { getLatestAppliedScenario } from '@/features/dashboard/lib/mockDecisionBridge'

const SERIES = [
  { key: 'mevcut' as const, colorVar: '--color-sidebar-muted', dash: '6 3', width: 2 },
  { key: 'simulasyon' as const, colorVar: '--color-gold', dash: undefined, width: 3 },
  { key: 'benchmark' as const, colorVar: '--color-ink', dash: '3 3', width: 2 },
]

const WINDOW_OPTIONS: SimulationWindow[] = [10, 20, 30, 90]

type PerformanceComparisonPageProps = {
  onGoToManualScenario: () => void
  onGoToStressTest: () => void
}

export default function PerformanceComparisonPage({
  onGoToManualScenario,
  onGoToStressTest,
}: PerformanceComparisonPageProps) {
  const t = getTranslations()
  const lang = getLang() === 'en' ? 'en' : 'tr'
  const { activeFund, fundInfo, analysisWindow, setAnalysisWindow } = useDecision()

  // Beyza'nın Manuel Senaryo akışı (dashboardApi.ts) localStorage'a yazıyor —
  // ona dokunmadan son uygulanan senaryoyu buradan okuyoruz.
  const appliedScenario = useMemo(() => getLatestAppliedScenario(), [])

  const simulation = useMemo(() => {
    if (!appliedScenario || fundInfo.status !== 'ready') return null
    return runSimulation(activeFund.code, analysisWindow, fundInfo.refWeights, appliedScenario.weights, fundInfo.baseValue)
  }, [activeFund.code, analysisWindow, appliedScenario, fundInfo])

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
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-3xl font-semibold text-ink">{t.pcTitle}</h1>
        <select
          value={analysisWindow}
          onChange={(e) => setAnalysisWindow(Number(e.target.value) as SimulationWindow)}
          className="rounded-lg border border-black/10 bg-white px-3 py-2 text-sm font-medium text-ink focus:outline-none focus:ring-2 focus:ring-primary/30"
        >
          {WINDOW_OPTIONS.map((days) => (
            <option key={days} value={days}>
              {t.navAnalysisWindowOption(days)}
            </option>
          ))}
        </select>
      </div>
      <p className="mt-3 max-w-3xl text-sm leading-relaxed text-muted">{t.pcDescription}</p>

      {fundInfo.status === 'loading' ? (
        <p className="mt-8 text-sm text-muted">{t.fundInfoLoadingText}</p>
      ) : fundInfo.status === 'error' ? (
        <div className="mt-8 rounded-2xl bg-red-50 px-6 py-8 text-center text-sm font-medium text-red-600 shadow-sm">
          {t.fundInfoErrorTitle}: {fundInfo.message}
        </div>
      ) : !appliedScenario || !simulation ? (
        <div className="mt-8 flex flex-col items-center gap-3 rounded-2xl border border-black/5 bg-white px-8 py-16 text-center shadow-sm">
          <MdLockOutline className="h-10 w-10 text-muted" />
          <p className="text-base font-semibold text-ink">{t.pcLockedTitle}</p>
          <p className="max-w-md text-sm text-muted">{t.pcLockedMessage}</p>
          <button
            type="button"
            onClick={onGoToManualScenario}
            className="mt-2 rounded-xl bg-primary px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-primary-dark"
          >
            {t.pcLockedGoToDecision}
          </button>
        </div>
      ) : (
        <>
          <div className="mt-8 rounded-2xl border border-black/5 bg-white p-6 shadow-sm sm:p-8">
            <h2 className="text-lg font-bold text-ink">{t.pcChartTitle}</h2>
            <p className="mt-1 text-sm text-muted">
              {t.pcPeriodLabel(formatDate(new Date(), lang), analysisWindow, t.pcSourceManual, t.pcStatusAccepted)}
            </p>

            <div className="mt-4 flex flex-wrap gap-5">
              {legend.map((item) => {
                const series = SERIES.find((s) => s.key === item.key)!
                return (
                  <div key={item.key} className="flex items-center gap-2 text-xs font-medium text-muted">
                    <span
                      className="inline-block h-0 w-5 border-t-2"
                      style={{
                        borderColor: `var(${series.colorVar})`,
                        borderStyle: series.dash ? 'dashed' : 'solid',
                      }}
                    />
                    {item.label}
                  </div>
                )
              })}
            </div>

            <div className="mt-4 h-80 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={simulation.points} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
                  <CartesianGrid vertical={false} stroke="#e5e7eb" />
                  <YAxis hide domain={['dataMin', 'dataMax']} />
                  {SERIES.map((series) => (
                    <Line
                      key={series.key}
                      type="monotone"
                      dataKey={series.key}
                      stroke={`var(${series.colorVar})`}
                      strokeWidth={series.width}
                      strokeDasharray={series.dash}
                      dot={false}
                      isAnimationActive={false}
                    />
                  ))}
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

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
                  {rows.map((row) => (
                    <tr key={row.label} className="border-t border-black/5">
                      <td className="py-3 pr-4 font-medium text-ink">{row.label}</td>
                      {(['mevcut', 'simulasyon', 'benchmark'] as const).map((key) => {
                        const metrics = simulation.metrics[key]
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
                  ))}
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
