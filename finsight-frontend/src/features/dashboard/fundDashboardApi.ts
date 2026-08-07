import api from '@/lib/api/client'

// GET /funds/{code}/dashboard cevabının tipleri.
// Tüm yüzde alanları yüzde cinsindendir (96.03 = %96,03), para birimi TRY.

export type FundDashboardPeriodCode = 'P10D' | 'P20D' | 'P30D' | 'P90D'

export type FundDashboardFund = {
  code: string
  name: string
  dataDate: string 
}

export type FundBenchmarkSeriesPoint = {
  date: string 
  fund: number
  benchmark: number | null
}

export type FundDashboardPeriod = {
  code: FundDashboardPeriodCode
  previousTotalValue: number
  previousDate: string // ISO
  /** İşlem günü sayısı — takvim günü değil (P10D için 9 dönebilir). */
  days: number
  change: number
  changePercent: number
  cumulativeReturn: number
  benchmarkReturn: number
  benchmarkDiffBps: number
  series: FundBenchmarkSeriesPoint[]
}

export type FundDistributionItem = {
  category: string
  weight: number
}

export type StockBreakdownItem = {
  assetCode: string 
  weight: number
}

export type FundDashboard = {
  fund: FundDashboardFund
  totalValue: number
  dailyReturn: number
  periods: FundDashboardPeriod[]
  distribution: FundDistributionItem[]
  stockBreakdown: {
    period: string // "2026-06"
    items: StockBreakdownItem[]
  }
}

export async function getFundDashboard(fundCode = 'TIE'): Promise<FundDashboard> {
  const response = await api.get(`/funds/${fundCode}/dashboard`)
  return response.data.data
}
