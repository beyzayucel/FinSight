import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  BenchmarkComparisonCard,
  FundMetricCard,
  LatestAiSuggestionCard,
  PortfolioDistributionCard,
  StockBreakdownModal,
  TotalValueFlipCard,
} from '../../components/fund-dashboard'
import { getFundDashboard } from '../../lib/fund-dashboard/fundDashboardApi'
import type { FundDashboard, FundDashboardPeriod } from '../../lib/fund-dashboard/fundDashboardApi'
import { getLatestDecisionState } from '../../lib/decisionHistoryApi'
import type { LatestDecisionState } from '../../lib/decisionHistoryApi'
import { formatBps, nominalDays } from '../../lib/fund-dashboard/fundDashboardFormatters'
import { formatSignedPercent } from '../../lib/formatters'
import { getTranslations } from '@/i18n/translations'

type FundDashboardPageProps = {
  fundId: string
  analysisPeriod: string
  onGoToAiDecision: () => void
  onAssetClassCountChange: (count: number) => void
}

export default function FundDashboardPage({
  fundId,
  analysisPeriod,
  onGoToAiDecision,
  onAssetClassCountChange,
}: FundDashboardPageProps) {
  const t = getTranslations()
  const [data, setData] = useState<FundDashboard | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)
  const [breakdownOpen, setBreakdownOpen] = useState(false)
  const [decisionState, setDecisionState] = useState<LatestDecisionState | null>(null)

  const loadData = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)
      const dashboard = await getFundDashboard('TIE')
      setData(dashboard)
      onAssetClassCountChange(dashboard.distribution.length)
    } catch (err: any) {
      setError(
        err?.response?.status === 404
          ? t.fundNotSynced
          : err?.message || t.fundDashboardLoadError
      )
    } finally {
      setLoading(false)
    }
  }, [onAssetClassCountChange, t.fundDashboardLoadError, t.fundNotSynced])

  useEffect(() => {
    loadData()
  }, [loadData])

  useEffect(() => {
    let cancelled = false
    getLatestDecisionState(fundId)
      .then((state) => !cancelled && setDecisionState(state))
      .catch(() => !cancelled && setDecisionState('PENDING'))
    return () => {
      cancelled = true
    }
  }, [fundId])

  const period: FundDashboardPeriod | null = useMemo(() => {
    if (!data || data.periods.length === 0) return null
    return data.periods.find((p) => p.code === `P${analysisPeriod}D`) ?? data.periods[0]
  }, [data, analysisPeriod])

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh]">
        <div className="h-10 w-10 border-4 border-[#c89834] border-t-transparent rounded-full animate-spin mb-4" />
        <span className="text-slate-500 font-medium">{t.fundInfoLoadingText}</span>
      </div>
    )
  }

  if (error || !data || !period) {
    return (
      <div className="bg-rose-50 border border-rose-100 rounded-2xl p-6 text-center max-w-xl mx-auto mt-12 space-y-4">
        <h3 className="text-lg font-bold text-rose-800">{t.fundInfoErrorTitle}</h3>
        <p className="text-sm text-rose-700">
          {error || t.fundDashboardEmpty}
        </p>
        <button
          onClick={loadData}
          className="px-5 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold uppercase select-none transition-all shadow-sm"
        >
          {t.retry}
        </button>
      </div>
    )
  }

  const periodDays = nominalDays(period.code)
  const dailyPositive = data.dailyReturn >= 0
  const cumulativePositive = period.cumulativeReturn >= 0
  const benchAbove = period.benchmarkDiffBps >= 0

  return (
    <div className="space-y-5 animate-fade-in">
      <h2 className="text-3xl font-bold font-mono text-slate-800 select-none tracking-tight">
        {t.fundDashboardTitle}
      </h2>

      {/* ---------- KPI KARTLARI ---------- */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <TotalValueFlipCard
          totalValue={data.totalValue}
          dataDate={data.fund.dataDate}
          period={period}
          periodDays={periodDays}
        />

        <FundMetricCard
          label={t.fundDailyReturn}
          value={formatSignedPercent(data.dailyReturn)}
          positive={dailyPositive}
          badge={dailyPositive ? `▲ ${t.fundPositive}` : `▼ ${t.fundNegative}`}
        />

        <FundMetricCard
          label={t.fundCumulativeReturn}
          value={formatSignedPercent(period.cumulativeReturn)}
          positive={cumulativePositive}
          badge={`${cumulativePositive ? '▲' : '▼'} ${t.fundLastDays(periodDays)}`}
        />

        <FundMetricCard
          label={t.fundBenchmarkDifference}
          value={formatBps(period.benchmarkDiffBps)}
          positive={benchAbove}
          badge={`${benchAbove ? `▲ ${t.fundAboveBenchmark}` : `▼ ${t.fundBelowBenchmark}`} · ${formatSignedPercent(
            period.benchmarkDiffBps / 100
          )}`}
        />
      </div>

      {/* ---------- ORTA SIRA: DAĞILIM + BENCHMARK ---------- */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4">
        <PortfolioDistributionCard
          distribution={data.distribution}
          onOpenStockBreakdown={() => setBreakdownOpen(true)}
        />
        <BenchmarkComparisonCard period={period} periodDays={periodDays} />
      </div>

      <LatestAiSuggestionCard state={decisionState} onGoToAiDecision={onGoToAiDecision} />

      {breakdownOpen && (
        <StockBreakdownModal
          period={data.stockBreakdown.period}
          items={data.stockBreakdown.items}
          onClose={() => setBreakdownOpen(false)}
        />
      )}
    </div>
  )
}
