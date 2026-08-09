type LatestAiSuggestionCardProps = {
  onGoToAiDecision: () => void
}

export default function LatestAiSuggestionCard({ onGoToAiDecision }: LatestAiSuggestionCardProps) {
  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 space-y-4">
      <h3 className="text-base font-bold font-mono text-slate-800">Son AI Önerisi</h3>
      <div className="flex flex-col sm:flex-row sm:items-center gap-4">
        <span className="flex-shrink-0 inline-flex items-center px-3.5 py-1.5 rounded-full border border-slate-200 bg-slate-50 text-[10px] font-bold tracking-wider text-slate-500 uppercase select-none">
          Bekliyor
        </span>
        <p className="flex-1 text-sm text-slate-500 leading-relaxed">
          Portföyünüz için güncel verilere göre üretilmiş bir ağırlık önerisini inceleyin,
          gerekçesini okuyun ve kabul/red kararınızı verin.
        </p>
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
