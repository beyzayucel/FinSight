import api from '@/lib/api/client'
import type { AssetCategory } from '../dashboardApi'

export type DecisionRecordWeight = {
  category: AssetCategory
  targetWeight: number
  currentWeight: number
}

export type DecisionRecordMetrics = {
  totalReturnPct: number | null
  benchmarkDiffPct: number | null
  maxDrawdownPct: number | null
  dailyVolatilityPct: number | null
  analysisWindowDays: number | null
} | null

export type DecisionRecord = {
  id: string
  source: 'AI' | 'MANUAL'
  status: 'ACCEPTED' | 'REJECTED'
  rationale: string | null
  note: string | null
  createdAt: string
  weights: DecisionRecordWeight[]
  metrics: DecisionRecordMetrics
}

// GET /funds/decisions/history — AI + Manuel kararları birleşik, en yeniden en eskiye sıralı döner.
export async function getDecisionHistory(fundId: string): Promise<DecisionRecord[]> {
  const response = await api.get<{ data: DecisionRecord[] }>('/funds/decisions/history', {
    params: { fundId },
  })
  return response.data.data
}
