import { useState } from 'react'
import { submitRecommendationDecision, AssetCategoryLabels } from '../dashboardApi'
import type { AIRecommendation, AssetCategory } from '../dashboardApi'
import { IoCheckmarkCircleOutline, IoCloseCircleOutline, IoTrendingDown } from 'react-icons/io5'

type AIRecommendationTabProps = {
  recommendation: AIRecommendation
  onDecisionSubmitted: () => void
  isLoading?: boolean
}

export default function AIRecommendationTab({
  recommendation,
  onDecisionSubmitted,
  isLoading = false
}: AIRecommendationTabProps) {
  const [submitting, setSubmitting] = useState<boolean>(false)
  const [error, setError] = useState<string | null>(null)
  const [note, setNote] = useState<string>('')

  const categories: AssetCategory[] = ['STOCK', 'REPO', 'FUTURE', 'FUND']

  async function handleDecision(status: 'ACCEPTED' | 'REJECTED') {
    try {
      setSubmitting(true)
      setError(null)
      await submitRecommendationDecision(recommendation.id, status, note.trim() || undefined)
      onDecisionSubmitted()
    } catch (err: any) {
      setError(err?.message || 'Karar iletilirken bir hata oluştu.')
    } finally {
      setSubmitting(false)
    }
  }

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-20 select-none">
        <div className="h-10 w-10 border-4 border-[#c89834] border-t-transparent rounded-full animate-spin mb-4" />
        <span className="text-slate-500 font-medium">AI Önerisi yükleniyor...</span>
      </div>
    )
  }

  const isDecided = recommendation.status !== 'PENDING'

  return (
    <div className="space-y-4 animate-fade-in select-none">
      {/* 1. Kategori Bazında Karşılaştırma Kartı */}
      <div className="bg-white rounded-xl border border-slate-200/75 shadow-sm p-4 space-y-4">
        <div className="space-y-1.5">
          <h3 className="text-xs font-bold text-slate-800">
            Kategori Bazında Karşılaştırma
          </h3>
          <p className="text-[10.5px] text-slate-400 font-medium">
            Mevcut → AI Önerisi (güncel verilerle üretildi)
          </p>
        </div>

        {/* Karşılaştırma Tablosu */}
        <div className="overflow-hidden border border-slate-100 rounded-xl">
          <table className="w-full border-collapse text-left text-xs">
            <thead>
              <tr className="bg-slate-50/50 border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[9.5px]">
                <th className="px-4 py-2.5 font-bold">KATEGORİ</th>
                <th className="px-4 py-2.5 font-bold w-[200px]">MEVCUT → ÖNERİLEN</th>
                <th className="px-4 py-2.5 text-right font-bold">MEVCUT</th>
                <th className="px-4 py-2.5 text-right font-bold">ÖNERİLEN</th>
                <th className="px-4 py-2.5 text-right font-bold">DEĞİŞİM</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {categories.map((cat) => {
                const weightData = recommendation.weights[cat]
                const current = weightData?.currentWeight ?? 0
                const recommended = weightData?.recommendedWeight ?? 0
                const diff = recommended - current
                const label = AssetCategoryLabels[cat]?.tr || cat

                return (
                  <tr key={cat} className="hover:bg-slate-50/20 transition-colors">
                    <td className="px-4 py-2 font-semibold text-slate-700">{label}</td>
                    
                    {/* Mevcut -> Önerilen Karşılaştırma Çubukları */}
                    <td className="px-4 py-1.5">
                      <div className="flex flex-col space-y-1 w-full">
                        {/* Mevcut Bar (Gray) */}
                        <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden">
                          <div
                            className="bg-[#6b7683] h-full rounded-full transition-all duration-500"
                            style={{ width: `${current}%` }}
                          />
                        </div>
                        {/* Önerilen Bar (Gold) */}
                        <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden">
                          <div
                            className="bg-[#c89834] h-full rounded-full transition-all duration-500"
                            style={{ width: `${recommended}%` }}
                          />
                        </div>
                      </div>
                    </td>

                    <td className="px-4 py-2 text-right font-semibold text-slate-500">
                      {current.toFixed(2).replace('.', ',')}%
                    </td>
                    <td className="px-4 py-2 text-right font-semibold text-slate-650">
                      {recommended.toFixed(2).replace('.', ',')}%
                    </td>
                    <td
                      className={`px-4 py-2 text-right font-semibold text-xs ${
                        diff > 0 ? 'text-[#3a7d74]' : diff < 0 ? 'text-[#ab6262]' : 'text-slate-400'
                      }`}
                    >
                      {diff > 0
                        ? `+${diff.toFixed(2).replace('.', ',')}`
                        : diff === 0
                        ? '0,00'
                        : diff.toFixed(2).replace('.', ',')}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>

        {/* Legend */}
        <div className="flex items-center space-x-6 text-[11px] font-semibold text-slate-500 px-1 select-none">
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-[#6b7683]" />
            <span>Mevcut</span>
          </div>
          <div className="flex items-center space-x-1.5">
            <span className="w-2.5 h-2.5 rounded-full bg-[#c89834]" />
            <span>AI Önerisi</span>
          </div>
        </div>
      </div>

      {/* 2. AI Gerekçesi Kartı */}
      <div className="bg-[#fcfaf5] rounded-xl border border-[#e2d5b8] p-4 space-y-3">

        <div className="space-y-1.5">
          <h4 className="text-xs font-bold text-slate-800">
            Al Gerekçesi
          </h4>
          <p className="text-xs text-slate-600 leading-relaxed font-semibold">
            {recommendation.rationale}
          </p>
        </div>

        {/* Beklenen Risk Değişimi */}
        <div className="flex items-center space-x-1.5 text-emerald-700 font-bold text-[11px] pt-1.5 border-t border-[#e2d5b8]/50">
          <IoTrendingDown size={14} />
          <span>Beklenen Risk Değişimi: Volatilite -0.3 puan (azalış)</span>
        </div>
      </div>

      {/* 3. Senaryo Notu (Karar verilmediyse) */}
      {!isDecided && (
        <div className="space-y-1.5 bg-white rounded-xl border border-slate-200/75 shadow-sm p-4">
          <label className="text-[10px] font-bold text-slate-400 tracking-wider uppercase block">
            Senaryo Notu (opsiyonel)
          </label>
          <textarea
            rows={3}
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Örn. AI önerisini kabul ederek portföy riskini azaltmayı hedefliyorum."
            className="w-full bg-white text-slate-800 border border-slate-200 rounded-xl px-4 py-3 text-xs outline-none focus:border-[#c89834] focus:ring-2 focus:ring-[#c89834]/20 transition-all placeholder-slate-400 resize-none font-medium"
          />
        </div>
      )}

      {/* 4. Butonlar / Karar Durumu */}
      <div className="flex flex-col space-y-3 pt-1">
        {error && (
          <span className="text-xs font-bold text-rose-600">
            {error}
          </span>
        )}

        {isDecided ? (
          <div className="space-y-3.5">
            <div className="flex items-center space-x-3">
              <button
                disabled
                className={`px-5 py-2.5 rounded-xl font-extrabold text-xs tracking-wider uppercase select-none transition-all ${
                  recommendation.status === 'ACCEPTED'
                    ? 'bg-[#c89834] text-white opacity-80'
                    : 'bg-slate-100 text-slate-400 border border-slate-200 cursor-not-allowed'
                }`}
              >
                Kabul Et → Simülasyona Uygula
              </button>
              <button
                disabled
                className={`px-5 py-2.5 rounded-xl font-extrabold text-xs tracking-wider uppercase select-none border transition-all ${
                  recommendation.status === 'REJECTED'
                    ? 'border-rose-300 text-rose-700 bg-rose-50/50 opacity-80'
                    : 'border-slate-200 text-slate-400 bg-white cursor-not-allowed'
                }`}
              >
                Reddet
              </button>
            </div>
            <div className="flex items-center space-x-1.5 text-xs font-semibold py-1">
              {recommendation.status === 'ACCEPTED' ? (
                <span className="text-[#2d7a4d] flex items-center gap-1.5 leading-relaxed">
                  ✓ AI önerisi kabul edildi. Bu dağılım artık ayrı bir Simülasyon Portföyü olarak kaydedildi — mevcut portföyünüz gerçek emirle değişmedi.
                </span>
              ) : (
                <span className="text-rose-700 flex items-center gap-1.5 leading-relaxed">
                  ✗ Bu öneri reddedildi.
                </span>
              )}
            </div>
          </div>
        ) : (
          <div className="flex items-center space-x-3">
            <button
              disabled={submitting}
              onClick={() => handleDecision('ACCEPTED')}
              className="px-5 py-2.5 rounded-xl bg-[#c89834] text-white font-extrabold text-xs tracking-wider uppercase hover:bg-[#b08226] shadow-sm hover:shadow disabled:opacity-50 transition-all select-none cursor-pointer"
            >
              {submitting ? 'Uygulanıyor...' : 'Kabul Et → Simülasyona Uygula'}
            </button>
            <button
              disabled={submitting}
              onClick={() => handleDecision('REJECTED')}
              className="px-5 py-2.5 rounded-xl border border-slate-300 text-slate-700 bg-white font-extrabold text-xs tracking-wider uppercase hover:bg-slate-50 hover:border-slate-400 disabled:opacity-50 transition-all select-none cursor-pointer"
            >
              Reddet
            </button>
          </div>
        )}

      </div>
    </div>
  )
}
