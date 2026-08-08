import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { AI_WEIGHTS, type SimulationWindow, type Weights } from '@/features/dashboard/lib/simulation'
import {
  getFundIdByCode,
  getFundInfo,
  mapAssetDistributionToWeights,
  submitManualScenario,
} from '@/features/dashboard/lib/fundApi'
import {
  DecisionContext,
  type ActiveFund,
  type Decision,
  type DecisionContextValue,
  type FundInfoState,
} from './decisionStore'

const ACTIVE_FUND: ActiveFund = {
  code: 'TIE',
  name: 'TIE-İş Portföy BIST 30 Endeksi',
  assetClassCount: 4,
}

export function DecisionProvider({ children }: { children: ReactNode }) {
  const [analysisWindow, setAnalysisWindow] = useState<SimulationWindow>(30)
  const [decision, setDecision] = useState<Decision | null>(null)
  const [fundInfo, setFundInfo] = useState<FundInfoState>({ status: 'loading' })
  const [fundId, setFundId] = useState<string | null>(null)
  const [isPerformanceViewed, setIsPerformanceViewed] = useState<boolean>(false)

  useEffect(() => {
    let cancelled = false
    getFundIdByCode(ACTIVE_FUND.code)
      .then((id) => {
        if (!cancelled) setFundId(id)
      })
      .catch(() => {})
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false

    getFundInfo(ACTIVE_FUND.code)
      .then((info) => {
        if (cancelled) return
        const refWeights = mapAssetDistributionToWeights(info.assetDistribution)
        if (!refWeights) {
          setFundInfo({ status: 'error', message: 'Fon varlık dağılımı tanınamayan kategoriler içeriyor.' })
          return
        }
        setFundInfo({ status: 'ready', baseValue: info.totalMarketPrice, refWeights })
      })
      .catch(() => {
        if (!cancelled) {
          setFundInfo({ status: 'error', message: 'Güncel portföy değeri alınamadı, karşılaştırma gösterilemiyor.' })
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  const value = useMemo<DecisionContextValue>(
    () => ({
      activeFund: ACTIVE_FUND,
      fundInfo,
      analysisWindow,
      setAnalysisWindow,
      decision,
      isPerformanceViewed,
      markPerformanceViewed: () => setIsPerformanceViewed(true),

      acceptAiRecommendation: () => {
        setIsPerformanceViewed(false)
        setDecision({ source: 'ai', status: 'accepted', weights: AI_WEIGHTS, decidedAt: new Date().toISOString() })
      },
      applyManualScenario: async (weights: Weights, note?: string) => {
        if (!fundId) {
          throw new Error('Fon kimliği henüz yüklenmedi, lütfen birazdan tekrar deneyin.')
        }
        await submitManualScenario(fundId, weights, note)
        setIsPerformanceViewed(false)
        setDecision({
          source: 'manual',
          status: 'accepted',
          weights,
          note,
          decidedAt: new Date().toISOString(),
        })
      },
      rejectRecommendation: () => {
        if (fundInfo.status !== 'ready') return
        setIsPerformanceViewed(false)
        setDecision({
          source: 'ai',
          status: 'rejected',
          weights: fundInfo.refWeights,
          decidedAt: new Date().toISOString(),
        })
      },
      resetDecision: () => {
        setDecision(null)
        setIsPerformanceViewed(false)
      },
    }),
    [analysisWindow, decision, fundInfo, fundId, isPerformanceViewed]
  )

  return <DecisionContext.Provider value={value}>{children}</DecisionContext.Provider>
}