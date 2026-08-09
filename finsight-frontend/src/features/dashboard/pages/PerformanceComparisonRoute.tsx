import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import PerformanceComparisonPage from './PerformanceComparisonPage'
import { useDecision } from '../context/decisionStore'
import { ROUTES } from '@/lib/routes'

/**
 * PerformanceComparisonPage geçiş fonksiyonlarını prop olarak alıyor.
 * Sayfanın kendisine dokunmamak için yönlendirmeyi bu ince sarmalayıcı yapıyor.
 * Karar Geçmişi'nden "↻ Tekrar Uygula" ile gelindiyse, seçilen karar navigate()'in
 * state'iyle taşınıyor — burada okuyup PerformanceComparisonPage'e prop olarak veriyoruz.
 */
export default function PerformanceComparisonRoute() {
  const navigate = useNavigate()
  const { markPerformanceViewed } = useDecision()

  useEffect(() => {
    markPerformanceViewed()
  }, [markPerformanceViewed])

  return (
    <PerformanceComparisonPage
      onGoToStressTest={() => navigate(ROUTES.FUND_STRESS_TEST)}
    />
  )
}
