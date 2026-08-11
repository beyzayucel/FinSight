import api from '@/lib/api/client'

// ── Backend response types ─────────────────────────────────────────────────────

export type CurvePoint = {
  date: string // ISO date — "2026-07-28"
  value: number
}

export type PortfolioMetrics = {
  currentValue: number
  totalReturnPct: number
  maxDrawdownPct: number
  dailyVolatilityPct: number
}

export type PortfolioCurve = {
  points: CurvePoint[]
  metrics: PortfolioMetrics
}

export type ScenarioSource = 'AI' | 'MANUAL'

export type PerformanceComparisonResponse = {
  currentPortfolio: PortfolioCurve
  simulationPortfolio: PortfolioCurve | null
  benchmarkPortfolio: PortfolioCurve | null
  scenarioSource: ScenarioSource | null
  analysisWindow: number
  dataDate: string // ISO date — "2025-06-15"
}

// ── API call ────────────────────────────────────────────────────────────────────

export async function getPerformanceComparison(
  fundCode: string,
  analysisWindow: number
): Promise<PerformanceComparisonResponse> {
  const response = await api.get<{ data: PerformanceComparisonResponse }>(
    `/performance-comparison/${fundCode}`,
    { params: { analysisWindow } }
  )
  return response.data.data
}

// ── Chart data helper ───────────────────────────────────────────────────────────

export type ChartPoint = {
  dateLabel: string
  mevcut: number
  simulasyon: number | null
  benchmark: number | null
}

export function mergeToChartPoints(data: PerformanceComparisonResponse): ChartPoint[] {
  const { currentPortfolio, simulationPortfolio, benchmarkPortfolio } = data

  const benchmarkMap = new Map(
    benchmarkPortfolio?.points.map((p) => [p.date, p.value]) ?? []
  )
  const simulationMap = new Map(
    simulationPortfolio?.points.map((p) => [p.date, p.value]) ?? []
  )

  return currentPortfolio.points.map((cp) => {
    const d = new Date(cp.date + 'T00:00:00')
    const dd = String(d.getDate()).padStart(2, '0')
    const mm = String(d.getMonth() + 1).padStart(2, '0')

    return {
      dateLabel: `${dd}.${mm}`,
      mevcut: cp.value,
      simulasyon: simulationMap.get(cp.date) ?? null,
      benchmark: benchmarkMap.get(cp.date) ?? null,
    }
  })
}
