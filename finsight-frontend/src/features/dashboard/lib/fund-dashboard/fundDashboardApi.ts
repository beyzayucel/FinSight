import api from '@/lib/api/client'
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
