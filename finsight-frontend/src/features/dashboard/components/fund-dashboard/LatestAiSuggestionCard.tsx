import { getTranslations } from '@/i18n/translations'
import type { LatestDecisionState } from '../../lib/decisionHistoryApi'

const BADGE_CLASSES: Record<LatestDecisionState, string> = {
  PENDING: 'border-slate-200 bg-slate-50 text-slate-500',
  AI_ACCEPTED: 'border-[#c89834]/50 bg-[#c89834]/10 text-[#c89834]',
  AI_REJECTED: 'border-slate-200 bg-slate-100 text-slate-500',
  MANUAL: 'border-[#c89834]/50 bg-[#c89834]/10 text-[#c89834]',
}

const BADGE_BASE =
  'flex-shrink-0 inline-flex items-center px-3.5 py-1.5 rounded-full border text-[10px] font-bold tracking-wider uppercase select-none'

type LatestAiSuggestionCardProps = {
  state: LatestDecisionState | null
  onGoToAiDecision: () => void
}

export default function LatestAiSuggestionCard({ state, onGoToAiDecision }: LatestAiSuggestionCardProps) {
  const t = getTranslations()
  const labels: Record<LatestDecisionState, string> = {
    PENDING: t.pending,
    AI_ACCEPTED: t.latestAiAccepted,
    AI_REJECTED: t.latestAiRejected,
    MANUAL: t.latestManualApplied,
  }
  const descriptions: Record<LatestDecisionState, string> = {
    PENDING: t.latestAiDescription,
    AI_ACCEPTED: t.latestAiAcceptedDescription,
    AI_REJECTED: t.latestAiRejectedDescription,
    MANUAL: t.latestManualDescription,
  }

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 space-y-4">
      <h3 className="text-base font-bold font-mono text-slate-800">{t.latestAiSuggestion}</h3>
      <div className="flex flex-col sm:flex-row sm:items-center gap-4">
        {state ? (
          <span className={`${BADGE_BASE} ${BADGE_CLASSES[state]}`}>{labels[state]}</span>
        ) : (
          <span className={`${BADGE_BASE} border-slate-200 bg-slate-100 text-transparent animate-pulse`}>
            {t.pending}
          </span>
        )}
        <p className="flex-1 text-sm text-slate-500 leading-relaxed">{descriptions[state ?? 'PENDING']}</p>
        <button
          onClick={onGoToAiDecision}
          className="flex-shrink-0 px-5 py-2.5 bg-[#c89834] hover:bg-[#b3872e] text-white rounded-xl text-sm font-bold select-none transition-all shadow-sm"
        >
          {t.reviewAiSuggestion}
        </button>
      </div>
    </div>
  )
}
