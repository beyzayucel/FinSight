import type { AdminDecisionRecord } from '../adminDecisionApi'
import type { Translations } from '@/i18n/translations'

type DecisionDistributionChartProps = {
  decisions: AdminDecisionRecord[]
  t: Translations
}

const GREEN = '#3E7A56'
const RED = '#B4433A'
const GOLD = '#B9862B'
const EMPTY = '#E7E2D6'

export default function DecisionDistributionChart({ decisions, t }: DecisionDistributionChartProps) {
  const total = decisions.length
  const aiApproved = decisions.filter((d) => d.decisionType === 'AI_APPROVED').length
  const aiRejected = decisions.filter((d) => d.decisionType === 'AI_REJECTED').length

  const approvedPct = total === 0 ? 0 : Math.round((aiApproved / total) * 100)
  const rejectedPct = total === 0 ? 0 : Math.round((aiRejected / total) * 100)
  const manualPct = total === 0 ? 0 : Math.max(0, 100 - approvedPct - rejectedPct)

  const approvedDeg = total === 0 ? 0 : (aiApproved / total) * 360
  const rejectedDeg = total === 0 ? 0 : (aiRejected / total) * 360

  const gradient =
    total === 0
      ? EMPTY
      : `conic-gradient(${GREEN} 0deg ${approvedDeg}deg, ${RED} ${approvedDeg}deg ${approvedDeg + rejectedDeg}deg, ${GOLD} ${approvedDeg + rejectedDeg}deg 360deg)`

  const legend = [
    { color: GREEN, label: t.adminPanelTypeAiApproved, pct: approvedPct },
    { color: RED, label: t.adminPanelTypeAiRejected, pct: rejectedPct },
    { color: GOLD, label: t.adminPanelTypeManual, pct: manualPct },
  ]

  return (
    <div className="bg-white rounded-[18px] shadow-[0_2px_4px_rgba(18,22,31,0.04),0_12px_26px_-14px_rgba(18,22,31,0.12)] border border-[rgba(231,226,214,0.7)] p-[22px] flex flex-col">
      <h2 className="font-heading text-[16.5px] font-bold text-admin-ink mb-5">{t.adminPanelDistributionTitle}</h2>

      <div className="flex items-center justify-center mb-6">
        <div
          className="relative w-[168px] h-[168px] rounded-full flex items-center justify-center"
          style={{ background: gradient }}
        >
          <div className="w-[104px] h-[104px] rounded-full bg-white flex flex-col items-center justify-center">
            <span className="font-heading text-[26px] font-bold text-admin-ink">{total}</span>
            <span className="text-[10px] font-semibold tracking-[0.08em] text-admin-text-faint uppercase mt-0.5">
              {t.adminPanelDistributionTotal}
            </span>
          </div>
        </div>
      </div>

      <div className="flex flex-col gap-2.5">
        {legend.map((item) => (
          <div key={item.label} className="flex items-center justify-between text-[12.5px]">
            <div className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
              <span className="text-admin-text font-medium">{item.label}</span>
            </div>
            <span className="font-mono font-semibold text-admin-ink">{item.pct}%</span>
          </div>
        ))}
      </div>
    </div>
  )
}
