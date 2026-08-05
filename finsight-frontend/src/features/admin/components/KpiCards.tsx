import type { UserStatsResponse } from '../adminApi'

type KpiCardsProps = {
  stats: UserStatsResponse | null
  loading: boolean
}

type CardConfig = {
  label: string
  key: keyof UserStatsResponse
  iconBg: string
  iconColor: string
  tint: string
  icon: JSX.Element
}

const cards: CardConfig[] = [
  {
    label: 'Toplam Kullanıcı',
    key: 'totalUsers',
    iconBg: 'bg-admin-gold-wash',
    iconColor: 'stroke-admin-gold',
    tint: 'bg-admin-gold-wash',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-3.5 h-3.5">
        <circle cx="9" cy="8" r="3.2" />
        <path d="M2.6 19.8c0-3.4 2.9-6.1 6.4-6.1s6.4 2.7 6.4 6.1" />
        <path d="M16.2 8a3 3 0 110 6" />
        <path d="M18.9 13.3c1.9.5 3.3 2.3 3.3 4.4" />
      </svg>
    ),
  },
  {
    label: 'Aktif Kullanıcı',
    key: 'activeUsers',
    iconBg: 'bg-admin-green-wash',
    iconColor: 'stroke-admin-green',
    tint: 'bg-admin-green-wash',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-3.5 h-3.5">
        <path d="M20 6L9 17l-5-5" />
      </svg>
    ),
  },
  {
    label: 'Pasif Kullanıcı',
    key: 'inactiveUsers',
    iconBg: 'bg-admin-red-wash',
    iconColor: 'stroke-admin-red',
    tint: 'bg-admin-red-wash',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-3.5 h-3.5">
        <circle cx="12" cy="12" r="9" />
        <path d="M9 9l6 6M15 9l-6 6" />
      </svg>
    ),
  },
  {
    label: 'Bugünkü Giriş',
    key: 'todayLogins',
    iconBg: 'bg-admin-gold-wash',
    iconColor: 'stroke-admin-gold',
    tint: 'bg-admin-gold-wash',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-3.5 h-3.5">
        <path d="M15 3h4a2 2 0 012 2v4M9 21H5a2 2 0 01-2-2v-4M21 15v4a2 2 0 01-2 2h-4M3 9V5a2 2 0 012-2h4" />
      </svg>
    ),
  },
]

export default function KpiCards({ stats, loading }: KpiCardsProps) {
  return (
    <div className="grid grid-cols-4 gap-4 mb-[22px]">
      {cards.map((card) => (
        <div
          key={card.key}
          className="bg-white rounded-[14px] px-[18px] pt-[18px] pb-4 shadow-[0_2px_4px_rgba(18,22,31,0.04),0_12px_26px_-14px_rgba(18,22,31,0.12)] border border-[rgba(231,226,214,0.7)] relative overflow-hidden"
        >
          {/* Tint circle */}
          <div className={`absolute -right-4 -top-4 w-[58px] h-[58px] rounded-full ${card.tint} opacity-70`} />

          <div className="flex items-center justify-between mb-3 relative z-[1]">
            <div className={`w-7 h-7 rounded-[9px] flex items-center justify-center ${card.iconBg} ${card.iconColor}`}>
              {card.icon}
            </div>
          </div>

          <div className="font-heading text-[25px] font-bold text-admin-ink relative z-[1]">
            {loading ? (
              <div className="h-8 w-12 bg-slate-100 rounded animate-pulse" />
            ) : (
              <span className="font-mono">{stats?.[card.key] ?? 0}</span>
            )}
          </div>
          <div className="text-[11.5px] text-admin-text-mute font-medium mt-1 relative z-[1]">
            {card.label}
          </div>
        </div>
      ))}
    </div>
  )
}
