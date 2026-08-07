import { useNavigate } from 'react-router-dom'
import PerformanceComparisonPage from './PerformanceComparisonPage'
import { ROUTES } from '@/lib/routes'

/**
 * PerformanceComparisonPage geçiş fonksiyonlarını prop olarak alıyor.
 * Sayfanın kendisine dokunmamak için yönlendirmeyi bu ince sarmalayıcı yapıyor.
 */
export default function PerformanceComparisonRoute() {
  const navigate = useNavigate()

  return (
    <PerformanceComparisonPage
      onGoToManualScenario={() => navigate(ROUTES.FUND_AI_DECISION)}
      onGoToStressTest={() => navigate(ROUTES.FUND_STRESS_TEST)}
    />
  )
}
