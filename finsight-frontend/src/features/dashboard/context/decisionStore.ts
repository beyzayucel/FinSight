import { createContext, useContext } from 'react'
import type { SimulationWindow, Weights } from '@/features/dashboard/lib/simulation'
import type { Fund } from '@/features/dashboard/dashboardApi'

export type DecisionSource = 'ai' | 'manual'
export type DecisionStatus = 'accepted' | 'rejected'

export type Decision = {
  source: DecisionSource
  status: DecisionStatus
  weights: Weights
  decidedAt: string
  note?: string
}

export type ActiveFund = {
  code: string
  name: string
  assetClassCount: number
}

// Gerçek Infina FundInfo API'sinden gelen mevcut portföy ağırlığı + güncel AUM.
export type FundInfoState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; baseValue: number; refWeights: Weights }

export type ActiveFundState =
  | { status: 'loading' }
  | { status: 'error'; message: string }
  | { status: 'ready'; fund: Fund }

export type DecisionContextValue = {
  activeFund: ActiveFund
  fundInfo: FundInfoState
  fundState: ActiveFundState
  reloadFund: () => void
  analysisWindow: SimulationWindow
  setAnalysisWindow: (window: SimulationWindow) => void
  decision: Decision | null
  
  // Ekran 04 takibi için eklenen alanlar
  isPerformanceViewed: boolean
  markPerformanceViewed: () => void

  acceptAiRecommendation: () => void
  /** Backend'de POST /funds/scenarios/apply ile kalıcı kaydeder; K1-K3 ihlalinde reject olur. */
  applyManualScenario: (weights: Weights, note?: string) => Promise<void>
  rejectRecommendation: () => void
  resetDecision: () => void
}

export const DecisionContext = createContext<DecisionContextValue | null>(null)

export function useDecision(): DecisionContextValue {
  const ctx = useContext(DecisionContext)
  if (!ctx) throw new Error('useDecision must be used within a DecisionProvider')
  return ctx
}