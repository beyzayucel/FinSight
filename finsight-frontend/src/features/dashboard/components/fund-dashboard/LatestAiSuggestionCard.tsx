import type { LatestDecisionState } from '../../lib/decisionHistoryApi'

const BADGES: Record<LatestDecisionState, { label: string; className: string }> = {
  PENDING: {
    label: 'Bekliyor',
    className: 'border-slate-200 bg-slate-50 text-slate-500',
  },
  AI_ACCEPTED: {
    label: 'AI Önerisi Kabul Edildi',
    className: 'border-[#c89834]/50 bg-[#c89834]/10 text-[#c89834]',
  },
  AI_REJECTED: {
    label: 'Reddedildi',
    className: 'border-slate-200 bg-slate-100 text-slate-500',
  },
  MANUAL: {
    label: 'Manuel Uygulandı',
    className: 'border-[#c89834]/50 bg-[#c89834]/10 text-[#c89834]',
  },
}

const DESCRIPTIONS: Record<LatestDecisionState, string> = {
  PENDING:
    'Portföyünüz için güncel verilere göre üretilmiş bir ağırlık önerisini inceleyin, gerekçesini okuyun ve kabul/red kararınızı verin.',
  AI_ACCEPTED:
    'En son AI önerisini kabul ettiniz. Uygulanan dağılımın etkisini Performans Karşılaştırması ekranından takip edebilirsiniz.',
  AI_REJECTED:
    'En son AI önerisini reddettiniz. Dilerseniz yeni bir öneri isteyebilir veya kendi manuel senaryonuzu kurabilirsiniz.',
  MANUAL:
    'En son kararınız manuel senaryoydu. Bu dağılım simülasyon portföyü olarak kaydedildi; AI önerisini yine de inceleyebilirsiniz.',
}

const BADGE_BASE =
  'flex-shrink-0 inline-flex items-center px-3.5 py-1.5 rounded-full border text-[10px] font-bold tracking-wider uppercase select-none'

type LatestAiSuggestionCardProps = {
  state: LatestDecisionState | null
  onGoToAiDecision: () => void
}

export default function LatestAiSuggestionCard({ state, onGoToAiDecision }: LatestAiSuggestionCardProps) {
  const badge = state && BADGES[state]

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 space-y-4">
      <h3 className="text-base font-bold font-mono text-slate-800">Son AI Önerisi</h3>
      <div className="flex flex-col sm:flex-row sm:items-center gap-4">
        {badge ? (
          <span className={`${BADGE_BASE} ${badge.className}`}>{badge.label}</span>
        ) : (
          <span className={`${BADGE_BASE} border-slate-200 bg-slate-100 text-transparent animate-pulse`}>
            Bekliyor
          </span>
        )}
        <p className="flex-1 text-sm text-slate-500 leading-relaxed">{DESCRIPTIONS[state ?? 'PENDING']}</p>
        <button
          onClick={onGoToAiDecision}
          className="flex-shrink-0 px-5 py-2.5 bg-[#c89834] hover:bg-[#b3872e] text-white rounded-xl text-sm font-bold select-none transition-all shadow-sm"
        >
          AI Önerisini İncele →
        </button>
      </div>
    </div>
  )
}
