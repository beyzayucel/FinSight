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
  /**
   * Pencerenin bittiği veri tarihi (ISO, "2026-08-02") — kararın alındığı gün değil.
   * Simülasyon T-8 veri gecikmesiyle çalıştığından karar tarihinden birkaç gün geridedir.
   * Bu alan eklenmeden önce alınmış kararlarda null.
   */
  dataDate: string | null
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

export type LatestDecisionState = 'PENDING' | 'AI_ACCEPTED' | 'AI_REJECTED' | 'MANUAL'

function toState(record: DecisionRecord | undefined): LatestDecisionState {
  if (!record) return 'PENDING'
  if (record.source === 'MANUAL') return 'MANUAL'
  return record.status === 'ACCEPTED' ? 'AI_ACCEPTED' : 'AI_REJECTED'
}

// Durum rozeti için — geçmişin ilk kaydı zaten "en güncel karar". Bilerek
// /recommendations/pending kullanılmıyor: o endpoint PENDING kayıt yoksa yeni öneri üretiyor.
export async function getLatestDecisionState(fundId: string): Promise<LatestDecisionState> {
  return toState((await getDecisionHistory(fundId))[0])
}
