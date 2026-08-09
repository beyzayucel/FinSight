import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { AI_WEIGHTS, type SimulationWindow, type Weights } from '@/features/dashboard/lib/simulation'
import {
  getFundInfo,
  mapAssetDistributionToWeights,
  submitManualScenario,
} from '@/features/dashboard/lib/fundApi'
import { getActiveFund } from '@/features/dashboard/dashboardApi'
import {
  DecisionContext,
  type ActiveFund,
  type ActiveFundState,
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
  const [fundState, setFundState] = useState<ActiveFundState>({ status: 'loading' })
  const [isPerformanceViewed, setIsPerformanceViewed] = useState<boolean>(false)

  const fundRequestIdRef = useRef(0)

  const loadFund = useCallback(() => {
    const requestId = ++fundRequestIdRef.current
    getActiveFund(ACTIVE_FUND.code)
      .then((fund) => {
        if (requestId !== fundRequestIdRef.current) return
        setFundState({ status: 'ready', fund })
      })
      .catch((err: unknown) => {
        if (requestId !== fundRequestIdRef.current) return
        setFundState({
          status: 'error',
          message: err instanceof Error ? err.message : 'Veriler yüklenirken bir hata oluştu.',
        })
      })
  }, [])

  const reloadFund = useCallback(() => {
    setFundState((prev) => (prev.status === 'ready' ? prev : { status: 'loading' }))
    loadFund()
  }, [loadFund])

  useEffect(() => {
    loadFund()
  }, [loadFund])

  const fundId = fundState.status === 'ready' ? fundState.fund.id : null

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
      fundState,
      reloadFund,
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
    [analysisWindow, decision, fundInfo, fundState, reloadFund, fundId, isPerformanceViewed]
  )

  return <DecisionContext.Provider value={value}>{children}</DecisionContext.Provider>
}