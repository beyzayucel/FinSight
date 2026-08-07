import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { IoChevronDownOutline, IoChevronUpOutline, IoTimeOutline } from 'react-icons/io5'
import { getTranslations } from '@/i18n/translations'
import { getLang } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'
import { AssetCategoryLabels } from '../dashboardApi'
import type { AssetCategory } from '../dashboardApi'
import { useDashboardOutlet } from '../DashboardShell'
import { CATEGORY_TO_ASSET_CLASS } from '../lib/mockDecisionBridge'
import { getDecisionHistory, type DecisionRecord } from '../lib/decisionHistoryApi'
import { formatDate, formatSignedPercent, formatUnsignedPercent } from '../lib/formatters'
import type { Weights } from '../lib/simulation'
import type { ReapplyScenario } from './PerformanceComparisonPage'

const ASSET_CATEGORIES: AssetCategory[] = ['STOCK', 'REPO', 'FUTURE', 'FUND']

function formatPct(value: number): string {
  return `%${value.toFixed(2).replace('.', ',')}`
}

function formatDeltaPts(delta: number): string {
  const sign = delta > 0 ? '+' : ''
  return `${sign}${delta.toFixed(2).replace('.', ',')}`
}

function statusLabel(record: DecisionRecord): { text: string; sourceTag: string } {
  if (record.source === 'AI') {
    return record.status === 'ACCEPTED'
      ? { text: 'AI önerisi kabul edildi', sourceTag: 'AI' }
      : { text: 'Öneri reddedildi', sourceTag: 'AI' }
  }
  return { text: 'Manuel senaryo uygulandı', sourceTag: 'Manuel' }
}

function toSimulationWeights(record: DecisionRecord): Weights {
  const weights = {} as Weights
  for (const w of record.weights) {
    weights[CATEGORY_TO_ASSET_CLASS[w.category]] = w.targetWeight / 100
  }
  return weights
}

export default function DecisionHistoryPage() {
  const t = getTranslations()
  const lang = getLang() === 'en' ? 'en' : 'tr'
  const navigate = useNavigate()
  const { fund } = useDashboardOutlet()

  const [history, setHistory] = useState<DecisionRecord[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [openId, setOpenId] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setHistory(null)
    setError(null)

    getDecisionHistory(fund.id)
      .then((records) => {
        if (!cancelled) setHistory(records)
      })
      .catch(() => {
        if (!cancelled) setError('Karar geçmişi alınamadı, lütfen tekrar deneyin.')
      })

    return () => {
      cancelled = true
    }
  }, [fund.id])

  function handleReapply(weights: Weights, sourceLabel: string) {
    const reapplyScenario: ReapplyScenario = { weights, sourceLabel }
    navigate(ROUTES.FUND_PERFORMANCE, { state: { reapplyScenario } })
  }

  const isLoading = history === null && !error

  return (
    <div className="space-y-4 max-w-[1100px] animate-fade-in select-none">
      <div>
        <h2 className="text-xl font-bold text-slate-800 tracking-tight">Karar Geçmişi</h2>
        <p className="text-xs text-slate-500 font-medium mt-1">
          En yeniden en eskiye · detay için satıra tıklayın.
        </p>
      </div>

      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-20">
          <div className="h-8 w-8 border-4 border-[#c89834] border-t-transparent rounded-full animate-spin mb-3" />
          <span className="text-slate-500 font-medium text-sm">Karar geçmişi yükleniyor...</span>
        </div>
      ) : error ? (
        <div className="bg-rose-50 border border-rose-100 rounded-2xl p-6 text-center text-sm font-medium text-rose-700">
          {error}
        </div>
      ) : history!.length === 0 ? (
        <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm p-12 flex flex-col items-center justify-center text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-amber-50 border border-amber-200 flex items-center justify-center text-amber-600 shadow-sm">
            <IoTimeOutline size={28} />
          </div>
          <div className="max-w-md">
            <h3 className="text-lg font-bold text-slate-800">Henüz karar kaydı yok</h3>
            <p className="text-sm text-slate-500 mt-2 leading-relaxed">
              AI önerisini kabul/reddettiğinizde ya da manuel bir senaryo uyguladığınızda burada listelenecek.
            </p>
          </div>
          <button
            type="button"
            onClick={() => navigate(ROUTES.FUND_AI_DECISION)}
            className="mt-1 px-5 py-2.5 rounded-xl bg-[#c89834] text-white font-extrabold text-xs tracking-wider uppercase hover:bg-[#b08226] shadow-sm transition-all cursor-pointer"
          >
            Manuel Senaryo Oluştur
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200/75 shadow-sm divide-y divide-slate-100">
          {history!.map((record) => {
            const isOpen = openId === record.id
            const label = statusLabel(record)
            const hasWeights = record.weights.length > 0
            const canReapply = record.status === 'ACCEPTED' && hasWeights
            const m = record.metrics

            return (
              <div key={record.id}>
                <button
                  type="button"
                  onClick={() => setOpenId(isOpen ? null : record.id)}
                  className="w-full flex items-center justify-between gap-4 px-4 py-3 text-left hover:bg-slate-50/50 transition-colors cursor-pointer"
                >
                  <div className="flex items-start gap-2.5">
                    <span
                      className={`mt-1.5 h-1.5 w-1.5 rounded-full flex-shrink-0 ${
                        record.status === 'ACCEPTED' ? 'bg-emerald-500' : 'bg-rose-500'
                      }`}
                    />
                    <div>
                      <p className="text-xs font-bold text-slate-800">
                        {label.text} <span className="ml-1 font-semibold text-slate-400">· {label.sourceTag}</span>
                      </p>
                      {m ? (
                        <p className="text-[11px] text-slate-500 font-medium mt-0.5">
                          {m.analysisWindowDays != null && `Süre: ${m.analysisWindowDays} işlem günü · `}
                          {m.totalReturnPct != null && `Simülasyon getirisi: ${formatSignedPercent(m.totalReturnPct)} · `}
                          {m.benchmarkDiffPct != null && `Benchmark farkı: ${formatSignedPercent(m.benchmarkDiffPct)}`}
                        </p>
                      ) : record.note || record.rationale ? (
                        <p className="text-[11px] text-slate-500 font-medium mt-0.5 line-clamp-1">
                          {record.note || record.rationale}
                        </p>
                      ) : null}
                    </div>
                  </div>
                  <div className="flex items-center gap-3 flex-shrink-0">
                    <span className="text-[10.5px] text-slate-400 font-semibold whitespace-nowrap">
                      {formatDate(new Date(record.createdAt), lang)}
                    </span>
                    {isOpen ? (
                      <IoChevronUpOutline className="text-slate-400" size={14} />
                    ) : (
                      <IoChevronDownOutline className="text-slate-400" size={14} />
                    )}
                  </div>
                </button>

                {isOpen && (
                  <div className="px-4 pb-4 animate-fade-in space-y-3">
                    <div className="rounded-xl bg-slate-50 border border-slate-100 p-3.5">
                      <p className="text-[9.5px] font-bold tracking-wider text-slate-400 uppercase mb-1">
                        {record.source === 'AI' ? 'AI Gerekçesi' : 'Karar Notu'}
                      </p>
                      <p className="text-xs text-slate-600 leading-relaxed">
                        {(record.source === 'AI' ? record.rationale : record.note) || '(kayıtlı not yok)'}
                      </p>
                    </div>

                    {hasWeights ? (
                      <div className="overflow-hidden border border-slate-100 rounded-xl">
                        <table className="w-full border-collapse text-left text-xs">
                          <thead>
                            <tr className="bg-slate-50 border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[9.5px]">
                              <th className="px-4 py-2.5 font-bold">KATEGORİ</th>
                              <th className="px-4 py-2.5 text-right font-bold">O ZAMANKİ MEVCUT</th>
                              <th className="px-4 py-2.5 text-right font-bold">KARAR VERİLEN</th>
                              <th className="px-4 py-2.5 text-right font-bold">DEĞİŞİM</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100">
                            {ASSET_CATEGORIES.map((cat) => {
                              const w = record.weights.find((x) => x.category === cat)
                              if (!w) return null
                              const delta = w.targetWeight - w.currentWeight
                              return (
                                <tr key={cat}>
                                  <td className="px-4 py-2 font-semibold text-slate-700">
                                    {AssetCategoryLabels[cat]?.tr || cat}
                                  </td>
                                  <td className="px-4 py-2 text-right text-slate-500">{formatPct(w.currentWeight)}</td>
                                  <td className="px-4 py-2 text-right font-semibold text-slate-700">
                                    {formatPct(w.targetWeight)}
                                  </td>
                                  <td
                                    className={`px-4 py-2 text-right font-semibold ${
                                      delta > 0 ? 'text-[#3a7d74]' : delta < 0 ? 'text-[#ab6262]' : 'text-slate-400'
                                    }`}
                                  >
                                    {formatDeltaPts(delta)}
                                  </td>
                                </tr>
                              )
                            })}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <div className="rounded-xl bg-rose-50 border border-rose-100 px-4 py-3 text-xs font-medium text-rose-700 text-center">
                        Reddedildiği için kayıtlı ağırlık yok
                      </div>
                    )}

                    {m && (
                      <div className="flex flex-wrap gap-x-4 gap-y-1.5 text-[11px] font-semibold">
                        {m.totalReturnPct != null && (
                          <span className="text-slate-500">
                            Simülasyon getirisi:{' '}
                            <span className={m.totalReturnPct >= 0 ? 'text-[#3a7d74]' : 'text-[#ab6262]'}>
                              {formatSignedPercent(m.totalReturnPct)}
                            </span>
                          </span>
                        )}
                        {m.benchmarkDiffPct != null && (
                          <span className="text-slate-500">
                            Benchmark farkı:{' '}
                            <span className={m.benchmarkDiffPct >= 0 ? 'text-[#3a7d74]' : 'text-[#ab6262]'}>
                              {formatSignedPercent(m.benchmarkDiffPct)}
                            </span>
                          </span>
                        )}
                        {m.maxDrawdownPct != null && (
                          <span className="text-slate-500">
                            Maks. düşüş: <span className="text-[#ab6262]">{formatSignedPercent(m.maxDrawdownPct)}</span>
                          </span>
                        )}
                        {m.dailyVolatilityPct != null && (
                          <span className="text-slate-500">
                            Oynaklık: <span className="text-slate-700">{formatUnsignedPercent(m.dailyVolatilityPct)}</span>
                          </span>
                        )}
                      </div>
                    )}

                    {canReapply && (
                      <button
                        type="button"
                        onClick={() =>
                          handleReapply(toSimulationWeights(record), record.source === 'AI' ? t.pcSourceAi : t.pcSourceManual)
                        }
                        className="px-4 py-2 rounded-xl border border-[#c89834]/40 text-[#c89834] font-bold text-[11px] tracking-wide uppercase hover:bg-[#c89834]/10 transition-all cursor-pointer"
                      >
                        ↻ Tekrar Uygula (bugünün verisiyle)
                      </button>
                    )}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
