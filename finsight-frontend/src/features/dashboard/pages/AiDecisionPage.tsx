import { useNavigate } from 'react-router-dom'
import ManualScenarioTab from '../components/ManualScenarioTab'
import { useDashboardOutlet } from '../DashboardShell'
import { ROUTES } from '@/lib/routes'

/** Markup, tek sayfalık DashboardPage'in case 2 içeriğinden olduğu gibi taşındı. */
export default function AiDecisionPage() {
  const navigate = useNavigate()
  const { fund, reloadFund } = useDashboardOutlet()

  // Manuel senaryo uygulanınca veriyi tazele ve sonucu görmesi için
  // kullanıcıyı Performans Karşılaştırması'na geçir.
  function handleScenarioApplied() {
    reloadFund()
    navigate(ROUTES.FUND_PERFORMANCE)
  }

  return (
    <div className="space-y-4 max-w-[1100px] animate-fade-in">
      {/* Breadcrumb / Üst Bilgi */}
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-800 select-none tracking-tight">
            Manuel Senaryo Simülasyonu
          </h2>
        </div>
      </div>

      {/* TAB CONTENT */}
      <ManualScenarioTab fund={fund} onScenarioApplied={handleScenarioApplied} />
    </div>
  )
}
