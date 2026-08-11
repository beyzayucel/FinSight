import { useNavigate } from 'react-router-dom'
import FundDashboardPage from './FundDashboardPage'
import { useDashboardOutlet } from '../../DashboardShell'
import { ROUTES } from '@/lib/routes'

export default function FundDashboardRoute() {
  const navigate = useNavigate()
  const { fund, analysisPeriod, reportAssetClassCount } = useDashboardOutlet()

  return (
    <FundDashboardPage
      fundId={fund.id}
      analysisPeriod={analysisPeriod}
      onGoToAiDecision={() => navigate(ROUTES.FUND_AI_DECISION)}
      onAssetClassCountChange={reportAssetClassCount}
    />
  )
}
