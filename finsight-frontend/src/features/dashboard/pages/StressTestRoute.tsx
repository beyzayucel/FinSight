import { useNavigate } from 'react-router-dom'
import { ROUTES } from '@/lib/routes'
import { useDecision } from '../context/decisionStore'
import { useDashboardOutlet } from '../DashboardShell'
import StressTestPage from '@/features/stresstest/StressTestPage'
import { saveDecisionRecord } from '@/features/stresstest/stressTestService'
import type { PortfolioDataDto } from '@/features/stresstest/types'

export default function StressTestRoute() {
  const navigate = useNavigate()
  const { decision, fundInfo, isPerformanceViewed } = useDecision()
  const { fund } = useDashboardOutlet()

  // SADECE Ekran 04 ziyaret edildiğinde ve simülasyon/fon verileri hazır olduğunda kilidi aç
  const isSimulationReady = Boolean(isPerformanceViewed && fundInfo.status === 'ready')

  const effectiveWeights = decision?.weights ?? (fundInfo.status === 'ready' ? fundInfo.refWeights : null)

  const portfolio: PortfolioDataDto | null = isSimulationReady && effectiveWeights
    ? {
        initialValue: fundInfo.status === 'ready' ? fundInfo.baseValue : 0,
        assetWeights: effectiveWeights,
      }
    : null

  // Karara iliştirme fon UUID'si ister — fon kodu ("TIE") kabul edilmiyor.
  async function handleSaveAndNavigate(stressTestResultId: string) {
    try {
      await saveDecisionRecord({
        fundId: fund.id,
        stressTestResultId,
      })

      navigate(ROUTES.FUND_DECISION_HISTORY)
    } catch (error) {
      console.error('Karar kaydedilirken hata oluştu:', error)
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
