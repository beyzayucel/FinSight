import { useNavigate } from 'react-router-dom'
import FundDashboardPage from './FundDashboardPage'
import { useDashboardOutlet } from '../DashboardShell'
import { ROUTES } from '@/lib/routes'

/** FundDashboardPage'i route dünyasına bağlayan ince sarmalayıcı. */
export default function FundDashboardRoute() {
  const navigate = useNavigate()
  const { analysisPeriod } = useDashboardOutlet()

  return (
    <FundDashboardPage
      analysisPeriod={analysisPeriod}
      onGoToAiDecision={() => navigate(ROUTES.FUND_AI_DECISION)}
    />
  )
}
