import { useState } from 'react'
import { Outlet, useLocation, useNavigate, useOutletContext } from 'react-router-dom'
import DashboardLayout from './components/DashboardLayout'
import type { Fund } from './dashboardApi'
import { ROUTES } from '@/lib/routes'
import { useDecision } from './context/decisionStore'
import type { SimulationWindow } from './lib/simulation'
import { getTranslations } from '@/i18n/translations'

type MenuIndex = 1 | 2 | 3 | 4 | 5

/** Sidebar'daki menü sırası ile route eşlemesi. DashboardLayout hâlâ index ile çalışıyor. */
const MENU_ROUTES: Record<MenuIndex, string> = {
  1: ROUTES.FUND_DASHBOARD,
  2: ROUTES.FUND_AI_DECISION,
  3: ROUTES.FUND_PERFORMANCE,
  4: ROUTES.FUND_STRESS_TEST,
  5: ROUTES.FUND_DECISION_HISTORY,
}

export type DashboardOutletContext = {
  fund: Fund
  /** Senaryo uygulandıktan sonra fon verisini tazelemek için. */
  reloadFund: () => void
  /** Sidebar'daki Analiz Dönemi seçimi: '10' | '20' | '30' | '90' */
  analysisPeriod: string
  reportAssetClassCount: (count: number) => void
}

/** Alt sayfaların kabuktaki ortak veriye erişmesi için kısayol. */
export function useDashboardOutlet(): DashboardOutletContext {
  return useOutletContext<DashboardOutletContext>()
}

function resolveActiveMenu(pathname: string): MenuIndex {
  const entry = (Object.entries(MENU_ROUTES) as [string, string][]).find(([, path]) =>
    pathname.startsWith(path)
  )
  return entry ? (Number(entry[0]) as MenuIndex) : 1
}

/**
 * Fon panelinin kabuğu: sidebar + fon verisinin tek seferlik yüklenmesi.
 * Ekranlar artık state ile değil, nested route ile değişiyor.
 */
export default function DashboardShell() {
  const t = getTranslations()
  const navigate = useNavigate()
  const location = useLocation()

  const { analysisWindow, setAnalysisWindow, fundState, reloadFund } = useDecision()
  const period = String(analysisWindow)

  const [assetClassCount, setAssetClassCount] = useState<number>()

  const fund = fundState.status === 'ready' ? fundState.fund : null
  const activeMenu = resolveActiveMenu(location.pathname)

  function renderMainContent() {
    if (fundState.status === 'loading') {
      return (
        <div className="flex flex-col items-center justify-center min-h-[50vh]">
          <div className="h-10 w-10 border-4 border-[#c89834] border-t-transparent rounded-full animate-spin mb-4" />
          <span className="text-slate-500 font-medium">{t.appLoading}</span>
        </div>
      )
    }

    if (!fund) {
      return (
        <div className="bg-rose-50 border border-rose-100 rounded-2xl p-6 text-center max-w-xl mx-auto mt-12 space-y-4">
          <h3 className="text-lg font-bold text-rose-800">{t.appConnectionError}</h3>
          <p className="text-sm text-rose-700">
            {fundState.status === 'error' ? fundState.message : t.appLoadError}
          </p>
          <button
            onClick={reloadFund}
            className="px-5 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold uppercase select-none transition-all shadow-sm"
          >
            {t.retry}
          </button>
        </div>
      )
    }

    return (
      <Outlet
        context={
          {
            fund,
            reloadFund,
            analysisPeriod: period,
            reportAssetClassCount: setAssetClassCount,
          } satisfies DashboardOutletContext
        }
      />
    )
  }

  return (
    <DashboardLayout
      activeMenuIndex={activeMenu}
      onMenuChange={(idx) => {
        navigate(MENU_ROUTES[idx])
      }}
      fundName={fund?.name}
      assetClassCount={assetClassCount}
      analysisPeriod={period}
      onPeriodChange={(newPeriod) => setAnalysisWindow(Number(newPeriod) as SimulationWindow)}
    >
      {renderMainContent()}
    </DashboardLayout>
  )
}
