import { useCallback, useEffect, useState } from 'react'
import { Outlet, useLocation, useNavigate, useOutletContext } from 'react-router-dom'
import DashboardLayout from './components/DashboardLayout'
import { getActiveFund } from './dashboardApi'
import type { Fund } from './dashboardApi'
import { ROUTES } from '@/lib/routes'

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
  reloadFund: () => void
  analysisPeriod: string
  reportAssetClassCount: (count: number) => void
}

export function useDashboardOutlet(): DashboardOutletContext {
  return useOutletContext<DashboardOutletContext>()
}

function resolveActiveMenu(pathname: string): MenuIndex {
  const entry = (Object.entries(MENU_ROUTES) as [string, string][]).find(([, path]) =>
    pathname.startsWith(path)
  )
  return entry ? (Number(entry[0]) as MenuIndex) : 1
}

export default function DashboardShell() {
  const navigate = useNavigate()
  const location = useLocation()

  const [fund, setFund] = useState<Fund | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  const [period, setPeriod] = useState<string>('30')
  const [assetClassCount, setAssetClassCount] = useState<number>()

  const loadData = useCallback(async () => {
    try {
      setLoading(true)
      setError(null)

      const activeFund = await getActiveFund()
      setFund(activeFund)
    } catch (err: any) {
      setError(err?.message || 'Veriler yüklenirken bir hata oluştu.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  const activeMenu = resolveActiveMenu(location.pathname)

  function renderMainContent() {
    if (loading) {
      return (
        <div className="flex flex-col items-center justify-center min-h-[50vh]">
          <div className="h-10 w-10 border-4 border-[#c89834] border-t-transparent rounded-full animate-spin mb-4" />
          <span className="text-slate-500 font-medium">Finsight yükleniyor...</span>
        </div>
      )
    }

    if (error || !fund) {
      return (
        <div className="bg-rose-50 border border-rose-100 rounded-2xl p-6 text-center max-w-xl mx-auto mt-12 space-y-4">
          <h3 className="text-lg font-bold text-rose-800">Sistem Bağlantı Hatası</h3>
          <p className="text-sm text-rose-700">{error || 'Veriler yüklenemedi. Lütfen tekrar deneyin.'}</p>
          <button
            onClick={loadData}
            className="px-5 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold uppercase select-none transition-all shadow-sm"
          >
            Yeniden Dene
          </button>
        </div>
      )
    }

    return (
      <Outlet
        context={
          {
            fund,
            reloadFund: loadData,
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
      onPeriodChange={(newPeriod) => setPeriod(newPeriod)}
    >
      {renderMainContent()}
    </DashboardLayout>
  )
}
