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

  // Manuel senaryoyu backend'e kaydetmek (POST /funds/scenarios/apply) için fonun gerçek UUID'si gerekiyor.
  useEffect(() => {
    let cancelled = false
    getFundIdByCode(ACTIVE_FUND.code)
      .then((id) => {
        if (!cancelled) setFundId(id)
      })
      .catch(() => {
        // Sessizce yutuluyor — fundId sadece manuel senaryo göndermek için gerekli,
        // sayfanın geri kalanı (Performans Karşılaştırması vb.) buna bağlı değil.
      })
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
      acceptAiRecommendation: () =>
        setDecision({ source: 'ai', status: 'accepted', weights: AI_WEIGHTS, decidedAt: new Date().toISOString() }),
      applyManualScenario: async (weights: Weights, note?: string) => {
        if (!fundId) {
          throw new Error('Fon kimliği henüz yüklenmedi, lütfen birazdan tekrar deneyin.')
        }
        await submitManualScenario(fundId, weights, note)
        setDecision({
          source: 'manual',
          status: 'accepted',
          weights,
          note,
          decidedAt: new Date().toISOString(),
        })
      },
      // K4: Reddedilen kararda simülasyon ağırlıkları mevcut portföy ile birebir aynı olmalı
      rejectRecommendation: () => {
        if (fundInfo.status !== 'ready') return
        setDecision({
          source: 'ai',
          status: 'rejected',
          weights: fundInfo.refWeights,
          decidedAt: new Date().toISOString(),
        })
      },
      resetDecision: () => setDecision(null),
    }),
    [analysisWindow, decision, fundInfo, fundId]
  )

  return <DecisionContext.Provider value={value}>{children}</DecisionContext.Provider>
}
