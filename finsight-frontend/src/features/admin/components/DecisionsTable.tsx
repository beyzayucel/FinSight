import type { AdminDecisionRecord, DecisionType } from '../adminDecisionApi'
import type { Translations } from '@/i18n/translations'
import { getLang } from '@/lib/authStore'

type DecisionsTableProps = {
  decisions: AdminDecisionRecord[]
  loading: boolean
  t: Translations
}

const BADGE_CLASS: Record<DecisionType, string> = {
  AI_APPROVED: 'bg-admin-green-wash text-admin-green',
  AI_REJECTED: 'bg-admin-red-wash text-admin-red',
  MANUAL: 'bg-admin-gold-wash text-admin-gold',
}

function typeLabel(type: DecisionType, t: Translations): string {
  if (type === 'AI_APPROVED') return t.adminPanelTypeAiApproved
  if (type === 'AI_REJECTED') return t.adminPanelTypeAiRejected
  return t.adminPanelTypeManual
}

function formatDate(dateStr: string): string {
  const lang = getLang()
  const locale = lang === 'en' ? 'en-US' : 'tr-TR'
  return new Date(dateStr).toLocaleDateString(locale, { day: '2-digit', month: 'short', year: 'numeric' })
}

export default function DecisionsTable({ decisions, loading, t }: DecisionsTableProps) {
  const headers = [t.adminPanelColDate, t.adminPanelColUser, t.adminPanelColFund, t.adminPanelColDecision]

  return (
    <div className="bg-white rounded-[18px] shadow-[0_2px_4px_rgba(18,22,31,0.04),0_12px_26px_-14px_rgba(18,22,31,0.12)] border border-[rgba(231,226,214,0.7)] px-[22px] pt-[22px] pb-2">
      {/* Header */}
      <div className="flex items-center gap-2.5 mb-1">
        <h2 className="font-heading text-[16.5px] font-bold text-admin-ink">{t.adminPanelTableTitle}</h2>
        <span className="inline-flex items-center gap-1 text-[10px] font-semibold px-[9px] py-[3px] rounded-full bg-admin-ivory text-admin-text-faint border border-admin-line">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" className="w-2.5 h-2.5">
            <rect x="5" y="11" width="14" height="9" rx="2" />
            <path d="M8 11V7a4 4 0 018 0v4" />
          </svg>
          {t.adminPanelReadOnlyBadge}
        </span>
      </div>
      <p className="text-xs text-admin-text-faint font-medium mb-4 max-w-2xl">{t.adminPanelTableDesc}</p>

      <table className="w-full border-collapse">
        <thead>
          <tr>
            {headers.map((header) => (
              <th
                key={header}
                className="text-left text-[10.5px] font-semibold tracking-[0.06em] uppercase text-admin-text-faint px-2.5 pb-[11px] border-b border-admin-line"
              >
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            Array.from({ length: 4 }).map((_, i) => (
              <tr key={i}>
                <td colSpan={4} className="py-3 px-2.5 border-b border-[#F0ECE1]">
                  <div className="h-5 bg-slate-100 rounded animate-pulse" />
                </td>
              </tr>
            ))
          ) : decisions.length === 0 ? (
            <tr>
              <td colSpan={4} className="py-8 text-center text-sm text-admin-text-faint">
                {t.adminNoRecords}
              </td>
            </tr>
          ) : (
            decisions.map((decision) => (
              <tr key={decision.id} className="hover:bg-[#FBFAF6]">
                <td className="py-3 px-2.5 border-b border-[#F0ECE1] text-admin-text-mute text-xs">
                  {formatDate(decision.decisionDate)}
                </td>
                <td className="py-3 px-2.5 border-b border-[#F0ECE1] font-semibold text-admin-ink text-[13px]">
                  {decision.userName}
                </td>
                <td className="py-3 px-2.5 border-b border-[#F0ECE1] text-admin-text-mute text-[12.5px]">
                  {decision.fundName}
                </td>
                <td className="py-3 px-2.5 border-b border-[#F0ECE1]">
                  <span
                    className={`inline-flex items-center gap-1.5 text-[11px] font-semibold px-[9px] py-1 rounded-full ${BADGE_CLASS[decision.decisionType]}`}
                  >
                    <span className="w-[5px] h-[5px] rounded-full bg-current" />
                    {typeLabel(decision.decisionType, t)}
                  </span>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}
