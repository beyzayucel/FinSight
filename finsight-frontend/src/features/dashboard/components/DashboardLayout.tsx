import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { IoChevronDownOutline, IoLogOutOutline } from 'react-icons/io5'
import { clearTokens } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'
import { getTranslations } from '@/i18n/translations'

type MenuIndex = 1 | 2 | 3 | 4 | 5

type DashboardLayoutProps = {
  activeMenuIndex: MenuIndex
  onMenuChange: (index: MenuIndex) => void
  children: React.ReactNode
  fundName?: string
  assetClassCount?: number
  analysisPeriod?: string
  onPeriodChange?: (period: string) => void
}

export default function DashboardLayout({
  activeMenuIndex,
  onMenuChange,
  children,
  fundName = 'TIE İş Portföy – BIST 30 Endeksi',
  assetClassCount,
  analysisPeriod = '30',
  onPeriodChange
}: DashboardLayoutProps) {
  const navigate = useNavigate()
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const t = getTranslations()

  function handleLogout() {
    clearTokens()
    navigate(ROUTES.LOGIN, { replace: true })
  }

  const menuItems = [
    { index: 1 as MenuIndex, label: t.navFundDashboard },
    { index: 2 as MenuIndex, label: t.navAiDecision },
    { index: 3 as MenuIndex, label: t.navPerformance },
    { index: 4 as MenuIndex, label: t.navStressTest },
    { index: 5 as MenuIndex, label: t.navDecisionHistory }
  ]

  return (
    <div className="flex min-h-dvh bg-[#f7f6f2] text-[#1c2530] font-ibm md:h-screen">
      {sidebarOpen && (
        <button type="button" aria-label={t.navCloseMenu} onClick={() => setSidebarOpen(false)} className="fixed inset-0 z-30 bg-slate-950/45 md:hidden" />
      )}
      {/* ---------- SOL SIDEBAR ---------- */}
      <aside className={`fixed inset-y-0 left-0 z-40 flex h-dvh w-[264px] flex-shrink-0 flex-col justify-between overflow-y-auto border-r border-[#1e273a]/30 bg-gradient-to-b from-[#12161f] to-[#0d1017] px-[18px] py-[28px] text-[#edeae0] shadow-xl transition-transform md:static md:translate-x-0 ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}`}>
        <div className="space-y-3.5">
          <div className="flex items-center space-x-3 mb-5 mt-1">
            <span className="flex h-14 w-14 flex-shrink-0 items-center justify-center overflow-hidden">
              <img
                src="/sidebar-logo.png"
                alt="Finsight logosu"
                className="h-full w-full scale-[1.6] select-none object-contain"
              />
            </span>
            <div>
              <h2 className="text-base font-extrabold text-[#edeae0] tracking-wider uppercase leading-none">
                FINSIGHT
              </h2>
              <p className="text-[9px] font-bold text-slate-400 tracking-wider uppercase mt-1 leading-tight">
                {t.navBrandSubtitle}
              </p>
            </div>
          </div>

          {/* Analiz Dönemi Seçici (.windowpicker) */}
          <div className="p-2.5 pb-3 bg-white/[0.04] border border-white/[0.08] rounded-xl space-y-1.5">
            <label className="text-[9px] font-extrabold tracking-wider text-slate-400 uppercase block">
              {t.navAnalysisPeriod}
            </label>
            <div className="relative">
              <select
                value={analysisPeriod}
                onChange={(e) => onPeriodChange?.(e.target.value)}
                className="w-full bg-[#1c2438]/30 text-white border border-[#c89834] rounded-lg px-2.5 py-1.5 text-xs appearance-none outline-none focus:ring-1 focus:ring-[#c89834] transition-all font-semibold cursor-pointer"
              >
                {[10, 20, 30, 90].map((days) => <option key={days} value={days}>{t.navAnalysisWindowOption(days)}</option>)}
              </select>
              <div className="absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">
                <IoChevronDownOutline size={14} />
              </div>
            </div>
          </div>

          {/* Aktif Fon */}
          <div className="p-2.5 bg-white/[0.03] border border-white/[0.06] rounded-xl space-y-0.5">
            <span className="text-[9px] font-bold tracking-wider text-slate-400 uppercase block">
              {t.navActiveFund}
            </span>
            <h4 className="text-xs font-bold text-white leading-tight">
              {fundName}
            </h4>
            <span className="text-[9.5px] text-slate-400 font-medium block">
              {assetClassCount ? `${t.navAssetClassCount(assetClassCount)} · ` : ''}{t.navCurrentPortfolio}
            </span>
          </div>

          {/* Menü Linkleri */}
          <nav className="space-y-1 pt-1">
            {menuItems.map((item) => {
              const isActive = activeMenuIndex === item.index
              return (
                <button
                  key={item.index}
                  onClick={() => { onMenuChange(item.index); setSidebarOpen(false) }}
                  className={`w-full flex items-center space-x-2.5 px-3 py-2 rounded-lg transition-all duration-200 text-left outline-none ${
                    isActive
                      ? 'bg-[#c89834]/10 border border-[#c89834]/30 text-white font-semibold shadow-inner'
                      : 'hover:bg-white/[0.03] border border-transparent text-[#edeae0]/70 hover:text-white'
                  }`}
                >
                  <span
                    className={`w-5 h-5 rounded-md flex items-center justify-center text-[10px] font-bold ${
                      isActive
                        ? 'bg-[#c89834] text-slate-900 shadow-sm'
                        : 'bg-white/[0.06] text-[#edeae0]/60'
                    }`}
                  >
                    {item.index}
                  </span>
                  <span className="text-xs font-semibold tracking-wide">{item.label}</span>
                </button>
              )
            })}
          </nav>
        </div>

        {/* Sidebar Alt Bilgi */}
        <div className="pt-3 border-t border-[#1e273a]/50 mt-4 space-y-3">
          <button
            onClick={handleLogout}
            className="w-full flex items-center space-x-2.5 px-3 py-2 rounded-lg text-left text-[#edeae0]/70 hover:text-white hover:bg-white/[0.05] transition-all duration-200 outline-none"
          >
            <IoLogOutOutline size={16} />
            <span className="text-xs font-semibold tracking-wide">{t.navLogout}</span>
          </button>
        </div>
      </aside>

      {/* ---------- SAĞ İÇERİK ALANI ---------- */}
      <main className="min-w-0 flex-1 overflow-y-auto px-4 py-4 sm:px-6 md:px-8 md:py-5">
        <button
          type="button"
          aria-label={t.navOpenMenu}
          onClick={() => setSidebarOpen(true)}
          className="mb-4 grid h-10 w-10 place-items-center rounded-xl border border-slate-200 bg-white text-slate-700 shadow-sm md:hidden"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="h-5 w-5"><path d="M4 7h16M4 12h16M4 17h16" /></svg>
        </button>
        {children}
      </main>
    </div>
  )
}
