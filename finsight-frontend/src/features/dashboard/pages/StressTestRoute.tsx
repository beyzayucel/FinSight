import { useNavigate } from 'react-router-dom'
import { ROUTES } from '@/lib/routes'
import { useDecision } from '../context/decisionStore'
import { useDashboardOutlet } from '../DashboardShell'
import StressTestPage from '@/features/stresstest/StressTestPage'
import { saveDecisionRecord } from '@/features/stresstest/stressTestService'
import type { PortfolioDataDto, ScenarioKey } from '@/features/stresstest/types'

export default function StressTestRoute() {
  const navigate = useNavigate()
  const { decision, fundInfo, isPerformanceViewed } = useDecision()
  const { fund } = useDashboardOutlet()

  // SADECE Ekran 04 ziyaret edildiğinde ve simülasyon/fon verileri hazır olduğunda kilidi aç[cite: 1]
  const isSimulationReady = Boolean(isPerformanceViewed && fundInfo.status === 'ready')

  const effectiveWeights = decision?.weights ?? (fundInfo.status === 'ready' ? fundInfo.refWeights : null)

  const portfolio: PortfolioDataDto | null = isSimulationReady && effectiveWeights
    ? {
        initialValue: fundInfo.status === 'ready' ? fundInfo.baseValue : 0,
        assetWeights: effectiveWeights,
      }
    : null

  const fundId = fund?.code || 'TIE'

 
  

async function handleSaveAndNavigate(scenarioKey: ScenarioKey, llmComment?: string) {
  if (!portfolio) return

  try {
    await saveDecisionRecord({
      fundId: fundId,
      scenarioKey: scenarioKey,
      portfolioData: {
        initialValue: portfolio.initialValue,
        assetWeights: portfolio.assetWeights,
      },
      llmComment: llmComment, // 👈 Artık parametreden geliyor!
    })

    navigate(ROUTES.FUND_DECISION_HISTORY)
  } catch (error) {
    console.error("Karar kaydedilirken hata oluştu:", error)
    throw error
  }
}

  return (
    <StressTestPage
      portfolio={portfolio}
      onSaveAndNavigate={handleSaveAndNavigate}
    />
  )
}