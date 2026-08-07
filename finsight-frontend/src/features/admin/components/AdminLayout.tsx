import type { ReactNode } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { clearTokens } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'
import type { Translations } from '@/i18n/translations'

type AdminLayoutProps = {
  children: ReactNode
  currentUserName?: string
  currentUserInitials?: string
  onProfileClick?: () => void
  title?: string
  breadcrumb?: string
  t: Translations
}

export default function AdminLayout({
  children,
  currentUserName = 'Admin',
  currentUserInitials = 'AD',
  onProfileClick,
  title,
  breadcrumb,
  t,
}: AdminLayoutProps) {
  const navigate = useNavigate()
  const location = useLocation()
  const isPanelActive = location.pathname === ROUTES.ADMIN_PANEL || location.pathname === ROUTES.ADMIN_DASHBOARD

  function handleLogout() {
    clearTokens()
    navigate(ROUTES.LOGIN)
  }

  return (
    <div className="grid grid-cols-[224px_1fr] min-h-screen">
      {/* ── Sidebar ── */}
      <aside className="bg-admin-ink text-[#EDEBE4] px-4 py-[26px] flex flex-col sticky top-0 h-screen">
        {/* Brand */}
        <div className="flex items-center gap-2.5 px-2 pb-[22px] mb-3 border-b border-admin-ink-line">
          <img
            src="/sidebar-logo.png"
            alt="FI Logo"
            className="w-8 h-8 rounded-[9px] shadow-[0_4px_10px_rgba(185,134,43,0.35)] object-cover shrink-0"
          />
          <div>
            <div className="font-heading text-[15px] font-bold text-[#F6F4EF]">FINSIGHT</div>
            <div className="text-[10px] font-semibold text-admin-gold-soft tracking-[0.12em] uppercase mt-0.5">{t.adminBrand}</div>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex flex-col gap-0.5 mt-1.5">
          <Link
            to={ROUTES.ADMIN_PANEL}
            className={
              isPanelActive
                ? 'flex items-center gap-[11px] px-3 py-2.5 rounded-[10px] text-[#F3E4C4] text-[13.5px] font-medium bg-gradient-to-r from-[rgba(185,134,43,0.16)] to-[rgba(185,134,43,0.03)] shadow-[inset_2px_0_0_var(--color-admin-gold)]'
                : 'flex items-center gap-[11px] px-3 py-2.5 rounded-[10px] text-[#B7BAC6] text-[13.5px] font-medium hover:bg-admin-ink-soft hover:text-[#EDEBE4]'
            }
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className={`w-4 h-4 shrink-0 ${isPanelActive ? 'stroke-admin-gold' : 'opacity-80'}`}>
              <rect x="3" y="3" width="7" height="9" rx="1.5" />
              <rect x="14" y="3" width="7" height="5" rx="1.5" />
              <rect x="14" y="12" width="7" height="9" rx="1.5" />
              <rect x="3" y="16" width="7" height="5" rx="1.5" />
            </svg>
            {t.adminPanel}
          </Link>
          <Link
            to={ROUTES.ADMIN_USERS}
            className={
              !isPanelActive
                ? 'flex items-center gap-[11px] px-3 py-2.5 rounded-[10px] text-[#F3E4C4] text-[13.5px] font-medium bg-gradient-to-r from-[rgba(185,134,43,0.16)] to-[rgba(185,134,43,0.03)] shadow-[inset_2px_0_0_var(--color-admin-gold)]'
                : 'flex items-center gap-[11px] px-3 py-2.5 rounded-[10px] text-[#B7BAC6] text-[13.5px] font-medium hover:bg-admin-ink-soft hover:text-[#EDEBE4]'
            }
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className={`w-4 h-4 shrink-0 ${!isPanelActive ? 'stroke-admin-gold' : 'opacity-80'}`}>
              <circle cx="9" cy="8" r="3.4" />
              <path d="M2.5 20c0-3.6 2.9-6.4 6.5-6.4s6.5 2.8 6.5 6.4" />
              <path d="M17 8.2a3.2 3.2 0 110 6.4" />
              <path d="M20 13.9c2 .5 3.4 2.4 3.4 4.6" />
            </svg>
            {t.adminUserManagement}
          </Link>
        </nav>

        <div className="flex-1" />

        {/* Footer */}
        <div className="border-t border-admin-ink-line pt-2.5">
          <button
            onClick={handleLogout}
            className="flex items-center gap-[11px] w-full px-3 py-2.5 rounded-[10px] text-[#B7BAC6] text-[13.5px] font-medium hover:bg-admin-ink-soft hover:text-[#EDEBE4]"
          >
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-4 h-4 shrink-0 opacity-80">
              <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
              <path d="M16 17l5-5-5-5" />
              <path d="M21 12H9" />
            </svg>
            {t.adminLogout}
          </button>
        </div>
      </aside>

      {/* ── Main ── */}
      <div className="flex flex-col min-w-0">
        {/* Topbar */}
        <div className="flex items-center gap-[18px] px-8 py-5 bg-admin-ivory/90 backdrop-blur-sm border-b border-admin-line sticky top-0 z-20">
          <div>
            <h1 className="font-heading text-[19px] font-bold text-admin-ink whitespace-nowrap">{title ?? t.adminDashboardTitle}</h1>
            <div className="text-xs text-admin-text-faint mt-0.5 font-medium">{breadcrumb ?? t.adminUserManagement}</div>
          </div>

          {/* Topbar actions */}
          <div className="flex items-center gap-3 ml-auto">
            {/* Panel sekmesinde açılacak bir detay paneli yok — tıklanabilir görünmesin diye
                yalnızca onProfileClick verildiğinde buton olarak render ediliyor. */}
            {(() => {
              const badgeContent = (
                <>
                  <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-admin-gold to-[#8f6620] text-[#241a08] flex items-center justify-center font-heading font-bold text-[11.5px]">
                    {currentUserInitials}
                  </div>
                  <div className="text-left">
                    <div className="text-[12.5px] font-semibold text-admin-ink">{currentUserName}</div>
                    <div className="text-[10.5px] text-admin-text-faint font-medium">{t.adminRole}</div>
                  </div>
                </>
              )
              const baseClass =
                'flex items-center gap-[9px] py-[5px] pl-[5px] pr-[11px] rounded-xl bg-white border border-admin-line shadow-sm'

              return onProfileClick ? (
                <button
                  onClick={onProfileClick}
                  className={`${baseClass} hover:border-admin-gold-soft hover:bg-admin-gold-wash transition cursor-pointer`}
                  title={t.adminMyProfile}
                >
                  {badgeContent}
                </button>
              ) : (
                <div className={baseClass}>{badgeContent}</div>
              )
            })()}
          </div>
        </div>

        {/* Content */}
        <div className="px-8 py-[26px] pb-11">
          {children}
        </div>
      </div>
    </div>
  )
}
