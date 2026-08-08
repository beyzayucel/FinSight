import api from '@/lib/api/client'

// ── Backend response types ─────────────────────────────────────────────────────

export type CurvePoint = {
  day: number
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
  day: number
  dateLabel: string
  mevcut: number
  simulasyon: number | null
  benchmark: number | null
}

/** Backend'den gelen ayrı point dizilerini recharts'ın beklediği tek diziye merge eder. */
export function mergeToChartPoints(data: PerformanceComparisonResponse): ChartPoint[] {
  const { currentPortfolio, simulationPortfolio, benchmarkPortfolio, analysisWindow } = data

  // dataDate backend'den gelebilir veya gelmeyebilir — güvenli fallback: bugünden geriye say
  let endDate: Date
  if (typeof data.dataDate === 'string' && data.dataDate.includes('-')) {
    endDate = new Date(data.dataDate + 'T00:00:00')
  } else {
    endDate = new Date()
  }

  const startDate = new Date(endDate)
  startDate.setDate(endDate.getDate() - analysisWindow)

  return currentPortfolio.points.map((cp, i) => {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + cp.day)
    const dd = String(date.getDate()).padStart(2, '0')
    const mm = String(date.getMonth() + 1).padStart(2, '0')

    return {
      day: cp.day,
      dateLabel: `${dd}.${mm}`,
      mevcut: cp.value,
      simulasyon: simulationPortfolio?.points[i]?.value ?? null,
      benchmark: benchmarkPortfolio?.points[i]?.value ?? null,
    }
  })
}
