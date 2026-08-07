import React from 'react'
import { useNavigate } from 'react-router-dom'
import { IoChevronDownOutline, IoLogOutOutline } from 'react-icons/io5'
import { clearTokens } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'

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

  function handleLogout() {
    clearTokens()
    navigate(ROUTES.LOGIN, { replace: true })
  }

  const menuItems = [
    { index: 1 as MenuIndex, label: 'Fon Dashboard' },
    { index: 2 as MenuIndex, label: 'AI Önerisi & Karar' },
    { index: 3 as MenuIndex, label: 'Performans Karşılaştırması' },
    { index: 4 as MenuIndex, label: 'Stres Testi' },
    { index: 5 as MenuIndex, label: 'Karar Geçmişi' }
  ]

  return (
    <div className="flex h-screen bg-[#f7f6f2] text-[#1c2530] font-ibm">
      {/* ---------- SOL SIDEBAR ---------- */}
      <aside className="w-[264px] flex-shrink-0 bg-gradient-to-b from-[#12161f] to-[#0d1017] text-[#edeae0] flex flex-col justify-between px-[18px] py-[28px] select-none border-r border-[#1e273a]/30 shadow-xl">
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
                Karar Destek Platformu
              </p>
            </div>
          </div>

          {/* Analiz Dönemi Seçici (.windowpicker) */}
          <div className="p-2.5 pb-3 bg-white/[0.04] border border-white/[0.08] rounded-xl space-y-1.5">
            <label className="text-[9px] font-extrabold tracking-wider text-slate-400 uppercase block">
              Analiz Dönemi
            </label>
            <div className="relative">
              <select
                value={analysisPeriod}
                onChange={(e) => onPeriodChange?.(e.target.value)}
                className="w-full bg-[#1c2438]/30 text-white border border-[#c89834] rounded-lg px-2.5 py-1.5 text-xs appearance-none outline-none focus:ring-1 focus:ring-[#c89834] transition-all font-semibold cursor-pointer"
              >
                <option value="10">Son 10 gün</option>
                <option value="20">Son 20 gün</option>
                <option value="30">Son 30 gün</option>
                <option value="90">Son 90 gün</option>
              </select>
              <div className="absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">
                <IoChevronDownOutline size={14} />
              </div>
            </div>
          </div>

          {/* Aktif Fon */}
          <div className="p-2.5 bg-white/[0.03] border border-white/[0.06] rounded-xl space-y-0.5">
            <span className="text-[9px] font-bold tracking-wider text-slate-400 uppercase block">
              Aktif Fon
            </span>
            <h4 className="text-xs font-bold text-white leading-tight">
              {fundName}
            </h4>
            <span className="text-[9.5px] text-slate-400 font-medium block">
              {assetClassCount ? `${assetClassCount} varlık sınıfı · ` : ''}Mevcut Portföy
            </span>
          </div>

          {/* Menü Linkleri */}
          <nav className="space-y-1 pt-1">
            {menuItems.map((item) => {
              const isActive = activeMenuIndex === item.index
              return (
                <button
                  key={item.index}
                  onClick={() => onMenuChange(item.index)}
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
            <span className="text-xs font-semibold tracking-wide">Çıkış Yap</span>
          </button>
        </div>
      </aside>

      {/* ---------- SAĞ İÇERİK ALANI ---------- */}
      <main className="flex-1 overflow-y-auto px-8 py-5">
        {children}
      </main>
    </div>
  )
}
