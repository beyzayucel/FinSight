import type { ReactNode } from 'react'
import type { AdminDecisionRecord } from '../adminDecisionApi'
import type { Translations } from '@/i18n/translations'

type DecisionKpiCardsProps = {
  decisions: AdminDecisionRecord[]
  loading: boolean
  t: Translations
}

type CardConfig = {
  label: string
  value: number
  iconBg: string
  iconColor: string
  tint: string
  icon: ReactNode
}

export default function DecisionKpiCards({ decisions, loading, t }: DecisionKpiCardsProps) {
  const total = decisions.length
  const aiApproved = decisions.filter((d) => d.decisionType === 'AI_APPROVED').length
  const aiRejected = decisions.filter((d) => d.decisionType === 'AI_REJECTED').length
  const manual = decisions.filter((d) => d.decisionType === 'MANUAL').length

  const cards: CardConfig[] = [
    {
      label: t.adminPanelKpiTotal,
      value: total,
      iconBg: 'bg-admin-gold-wash',
      iconColor: 'stroke-admin-gold',
      tint: 'bg-admin-gold-wash',
      icon: (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-3.5 h-3.5">
          <rect x="3" y="3" width="7" height="9" rx="1.5" />
          <rect x="14" y="3" width="7" height="5" rx="1.5" />
          <rect x="14" y="12" width="7" height="9" rx="1.5" />
          <rect x="3" y="16" width="7" height="5" rx="1.5" />
        </svg>
      ),
    },
    {
      label: t.adminPanelKpiAiApproved,
      value: aiApproved,
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
      label: t.adminPanelKpiAiRejected,
      value: aiRejected,
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
      label: t.adminPanelKpiManual,
      value: manual,
      iconBg: 'bg-admin-gold-wash',
      iconColor: 'stroke-admin-gold',
      tint: 'bg-admin-gold-wash',
      icon: (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-3.5 h-3.5">
          <path d="M12 20h9" />
          <path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4z" />
        </svg>
      ),
    },
  ]

  return (
    <div className="grid grid-cols-4 gap-4 mb-[22px]">
      {cards.map((card) => (
        <div
          key={card.label}
          className="bg-white rounded-[14px] px-[18px] pt-[18px] pb-4 shadow-[0_2px_4px_rgba(18,22,31,0.04),0_12px_26px_-14px_rgba(18,22,31,0.12)] border border-[rgba(231,226,214,0.7)] relative overflow-hidden"
        >
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
              <span className="font-mono">{card.value}</span>
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
