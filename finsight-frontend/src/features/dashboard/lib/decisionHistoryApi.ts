import api from '@/lib/api/client'
import type { StressTestInferenceResponseDto } from '@/features/stresstest/types'
import type { AssetCategory } from '../dashboardApi'

export type DecisionRecordWeight = {
  category: AssetCategory
  targetWeight: number
  currentWeight: number
}

export type DecisionRecordStockWeight = {
  /** Hisse kodu (ticker), örn. "ASELS". */
  assetCode: string
  targetWeight: number
  /** Karar anındaki mevcut ağırlık — bugünkü değil, o günkü snapshot. */
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
  /** Yalnızca değiştirilen top-10 hisseler; AI kararlarında şimdilik hep boş. */
  stockWeights: DecisionRecordStockWeight[]
  metrics: DecisionRecordMetrics
  /** Stres Testi ekranından iliştirilmişse dolu; hiç iliştirilmemişse null. */
  stressTest: StressTestInferenceResponseDto | null
}

// GET /funds/decisions/history — AI + Manuel kararları birleşik, en yeniden en eskiye sıralı döner.
export async function getDecisionHistory(fundId: string): Promise<DecisionRecord[]> {
  const response = await api.get<{ data: DecisionRecord[] }>('/funds/decisions/history', {
    params: { fundId },
  })
  return response.data.data
}
