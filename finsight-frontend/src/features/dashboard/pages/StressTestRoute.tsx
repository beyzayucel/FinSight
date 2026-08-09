import { useNavigate } from 'react-router-dom'
import { ROUTES } from '@/lib/routes'
import { useDecision } from '../context/decisionStore'
import { useDashboardOutlet } from '../DashboardShell'
import StressTestPage from '@/features/stresstest/StressTestPage'
import { saveDecisionRecord } from '@/features/stresstest/stressTestService'
import type { PortfolioDataDto } from '@/features/stresstest/types'

export default function StressTestRoute() {
  const navigate = useNavigate()
  // analysisWindow değerini destructure ederek context'ten çekiyoruz
  const { decision, fundInfo, isPerformanceViewed, analysisWindow } = useDecision()
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
    console.log('Kaydedilecek stressTestResultId:', stressTestResultId)
    
    await saveDecisionRecord({
      fundId: fund.id,
      stressTestResultId: stressTestResultId || '00000000-0000-0000-0000-000000000000', // Boşsa fallback
    })

    navigate(ROUTES.FUND_DECISION_HISTORY)
  } catch (error) {
    console.error('Karar kaydedilirken hata oluştu:', error)
  }
}

  return (
    <StressTestPage
      portfolio={portfolio}
      analysisWindow={analysisWindow}
      onSaveAndNavigate={handleSaveAndNavigate}
    />
  )
}