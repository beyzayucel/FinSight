import { useEffect } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import PerformanceComparisonPage, { type ReapplyScenario } from './PerformanceComparisonPage'
import { useDecision } from '../context/decisionStore' // <-- BURASI DÜZELTİLDİ
import { useNavigate } from 'react-router-dom'
import PerformanceComparisonPage from './PerformanceComparisonPage'
import { ROUTES } from '@/lib/routes'

/**
 * PerformanceComparisonPage geçiş fonksiyonlarını prop olarak alıyor.
 * Sayfanın kendisine dokunmamak için yönlendirmeyi bu ince sarmalayıcı yapıyor.
 * Karar Geçmişi'nden "↻ Tekrar Uygula" ile gelindiyse, seçilen karar navigate()'in
 * state'iyle taşınıyor — burada okuyup PerformanceComparisonPage'e prop olarak veriyoruz.
 */
export default function PerformanceComparisonRoute() {
  const navigate = useNavigate()
  const location = useLocation()
  const { markPerformanceViewed } = useDecision()

  const reapplyScenario = (location.state as { reapplyScenario?: ReapplyScenario } | null)?.reapplyScenario ?? null

  useEffect(() => {
    markPerformanceViewed()
  }, [markPerformanceViewed])

  return (
    <PerformanceComparisonPage
      overrideScenario={reapplyScenario}
      onGoToManualScenario={() => navigate(ROUTES.FUND_AI_DECISION)}
      onGoToStressTest={() => navigate(ROUTES.FUND_STRESS_TEST)}
    />
  )
}
