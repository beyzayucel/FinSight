import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { IoChevronDownOutline, IoChevronUpOutline, IoPulseOutline, IoTimeOutline } from 'react-icons/io5'
import { getLang } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'
import { getAssetCategoryLabel } from '../dashboardApi'
import type { AssetCategory } from '../dashboardApi'
import { useDashboardOutlet } from '../DashboardShell'
import { getDecisionHistory, type DecisionRecord } from '../lib/decisionHistoryApi'
import { formatCurrency, formatDate } from '../lib/formatters'
import { getScenarioTitle, type PortfolioResultDto } from '@/features/stresstest/types'
import { getTranslations, translations, type Translations } from '@/i18n/translations'

const ASSET_CATEGORIES: AssetCategory[] = ['STOCK', 'REPO', 'FUTURE', 'FUND']

/**
 * Karar anında iliştirilmiş stres testi sonucunu, Stres Testi ekranındaki (Ekran 05) tabloyla
 * aynı sırada ve aynı adlarla listeler. Eski/eksik kayıtlarda portföylerden bazıları boş
 * gelebildiği için satırlar süzülerek kuruluyor.
 */
function stressTestRows(
  stressTest: NonNullable<DecisionRecord['stressTest']>,
  t: Translations
): { label: string; result: PortfolioResultDto }[] {
  return (
    [
      { label: t.stressCurrentPortfolio, result: stressTest.currentPortfolioResult },
      { label: t.stressSimulationPortfolio, result: stressTest.simulationPortfolioResult },
      { label: t.stressBenchmark, result: stressTest.benchmarkPortfolioResult },
    ] as { label: string; result: PortfolioResultDto | null }[]
  ).filter((row): row is { label: string; result: PortfolioResultDto } => row.result != null)
}

function formatPct(value: number, lang: 'tr' | 'en'): string {
  return lang === 'tr' ? `%${value.toFixed(2).replace('.', ',')}` : `${value.toFixed(2)}%`
}

function formatDeltaPts(delta: number, lang: 'tr' | 'en'): string {
  const sign = delta > 0 ? '+' : ''
  const value = lang === 'tr' ? delta.toFixed(2).replace('.', ',') : delta.toFixed(2)
  return `${sign}${value}`
}

function formatSignedPct(value: number, lang: 'tr' | 'en'): string {
  const sign = value > 0 ? '+' : ''
  const formatted = lang === 'tr' ? value.toFixed(2).replace('.', ',') : value.toFixed(2)
  return `${sign}${formatted}%`
}

function formatUnsignedPct(value: number, lang: 'tr' | 'en'): string {
  const formatted = lang === 'tr' ? Math.abs(value).toFixed(2).replace('.', ',') : Math.abs(value).toFixed(2)
  return `${formatted}%`
}

function statusLabel(record: DecisionRecord, t: Translations): { text: string; sourceTag: string } {
  if (record.source === 'AI') {
    return record.status === 'ACCEPTED'
      ? { text: t.historyAiAccepted, sourceTag: 'AI' }
      : { text: t.historyAiRejected, sourceTag: 'AI' }
  }
  return { text: t.historyManualApplied, sourceTag: t.historyManualSource }
}


export default function DecisionHistoryPage() {
  const lang = getLang() === 'en' ? 'en' : 'tr'
  const t = getTranslations()
  const navigate = useNavigate()
  const { fund } = useDashboardOutlet()

  const [history, setHistory] = useState<DecisionRecord[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [openId, setOpenId] = useState<string | null>(null)
  const [openStocksId, setOpenStocksId] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setHistory(null)
    setError(null)

    getDecisionHistory(fund.id)
      .then((records) => {
        if (!cancelled) setHistory(records)
      })
      .catch(() => {
        if (!cancelled) setError(t.historyLoadError)
      })

    return () => {
      cancelled = true
    }
  }, [fund.id, t.historyLoadError])

  function handleReapply() {
    navigate(ROUTES.FUND_PERFORMANCE)
  }

  const isLoading = history === null && !error

  return (
    <div className="space-y-4 max-w-[1100px] animate-fade-in select-none">
      <div>
        <h2 className="text-xl font-bold text-slate-800 tracking-tight">{t.historyTitle}</h2>
        <p className="text-xs text-slate-500 font-medium mt-1">
          {t.historySubtitle}
        </p>
      </div>

      {isLoading ? (
        <div className="flex flex-col items-center justify-center py-20">
          <div className="h-8 w-8 border-4 border-[#c89834] border-t-transparent rounded-full animate-spin mb-3" />
          <span className="text-slate-500 font-medium text-sm">{t.historyLoading}</span>
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
            <h3 className="text-lg font-bold text-slate-800">{t.historyEmptyTitle}</h3>
            <p className="text-sm text-slate-500 mt-2 leading-relaxed">
              {t.historyEmptyDescription}
            </p>
          </div>
          <button
            type="button"
            onClick={() => navigate(ROUTES.FUND_AI_DECISION)}
            className="mt-1 px-5 py-2.5 rounded-xl bg-[#c89834] text-white font-extrabold text-xs tracking-wider uppercase hover:bg-[#b08226] shadow-sm transition-all cursor-pointer"
          >
            {t.historyCreateManual}
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-slate-200/75 shadow-sm divide-y divide-slate-100">
          {history!.map((record) => {
            const isOpen = openId === record.id
            const label = statusLabel(record, t)
            const hasWeights = record.weights.length > 0
            // Kayıt, kırılımın tamamını (fonun tüm hisseleri) tutuyor; geçmiş ekranında anlamlı olan
            // yalnızca karar anında değiştirilenler. Değişmeyenleri listelemek 50+ satırlık gürültü olur.
            const changedStocks = record.stockWeights.filter((s) => s.targetWeight !== s.currentWeight)
            const hasStockWeights = changedStocks.length > 0
            const isStocksOpen = openStocksId === record.id
            const canReapply = record.status === 'ACCEPTED' && hasWeights
            const m = record.metrics
            const rawSummary = record.note || record.rationale
            const localizedSummary =
              lang === 'en' && rawSummary === translations.tr.aiDefaultRationale ? t.aiDefaultRationale : rawSummary

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
                        {record.stressTest && (
                          <span
                            className="ml-2 inline-flex items-center gap-1 align-middle rounded-md bg-[#c89834]/10 px-1.5 py-0.5 text-[9px] font-bold uppercase tracking-wider text-[#c89834]"
                            title={t.historyStressAttached}
                          >
                            <IoPulseOutline size={9} />
                            {t.historyStressTest}
                          </span>
                        )}
                      </p>
                      {m ? (
                        <p className="text-[11px] text-slate-500 font-medium mt-0.5">
                          {m.analysisWindowDays != null && `${t.historyDuration(m.analysisWindowDays)} · `}
                          {m.totalReturnPct != null && `${t.historySimulationReturn}: ${formatSignedPct(m.totalReturnPct, lang)} · `}
                          {m.benchmarkDiffPct != null && `${t.historyBenchmarkDifference}: ${formatSignedPct(m.benchmarkDiffPct, lang)}`}
                        </p>
                      ) : record.note || record.rationale ? (
                        <p className="text-[11px] text-slate-500 font-medium mt-0.5 line-clamp-1">
                          {localizedSummary}
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
                        {record.source === 'AI' ? t.historyAiRationale : t.historyDecisionNote}
                      </p>
                      <p className="text-xs text-slate-600 leading-relaxed">
                        {(record.source === 'AI'
                          ? lang === 'en' && record.rationale === translations.tr.aiDefaultRationale
                            ? t.aiDefaultRationale
                            : record.rationale
                          : record.note) || t.historyNoNote}
                      </p>
                    </div>

                    {hasWeights ? (
                      <div className="overflow-x-auto border border-slate-100 rounded-xl">
                        <table className="min-w-[580px] w-full border-collapse text-left text-xs">
                          <thead>
                            <tr className="bg-slate-50 border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[9.5px]">
                              <th className="px-4 py-2.5 font-bold">{t.historyCategory}</th>
                              <th className="px-4 py-2.5 text-right font-bold">{t.historyPreviousWeight}</th>
                              <th className="px-4 py-2.5 text-right font-bold">{t.historyDecidedWeight}</th>
                              <th className="px-4 py-2.5 text-right font-bold">{t.historyChange}</th>
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
                                    {getAssetCategoryLabel(cat)}
                                  </td>
                                  <td className="px-4 py-2 text-right text-slate-500">{formatPct(w.currentWeight, lang)}</td>
                                  <td className="px-4 py-2 text-right font-semibold text-slate-700">
                                    {formatPct(w.targetWeight, lang)}
                                  </td>
                                  <td
                                    className={`px-4 py-2 text-right font-semibold ${
                                      delta > 0 ? 'text-[#3a7d74]' : delta < 0 ? 'text-[#ab6262]' : 'text-slate-400'
                                    }`}
                                  >
                                    {formatDeltaPts(delta, lang)}
                                  </td>
                                </tr>
                              )
                            })}
                          </tbody>
                        </table>
                      </div>
                    ) : (
                      <div className="rounded-xl bg-rose-50 border border-rose-100 px-4 py-3 text-xs font-medium text-rose-700 text-center">
                        {t.historyNoRejectedWeights}
                      </div>
                    )}

                    {hasStockWeights && (
                      <div>
                        <button
                          type="button"
                          onClick={() => setOpenStocksId(isStocksOpen ? null : record.id)}
                          className="flex items-center gap-1.5 text-[11px] font-bold text-[#c89834] hover:underline cursor-pointer"
                        >
                          {isStocksOpen ? <IoChevronUpOutline size={11} /> : <IoChevronDownOutline size={11} />}
                          {t.historyViewStocks}
                          <span className="font-semibold text-slate-400">
                            ({t.historyChangedStocks(changedStocks.length)})
                          </span>
                        </button>

                        {isStocksOpen && (
                          <div className="mt-2 overflow-x-auto border border-slate-100 rounded-xl animate-fade-in">
                            <table className="min-w-[580px] w-full border-collapse text-left text-xs">
                              <thead>
                                <tr className="bg-slate-50 border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[9.5px]">
                                  <th className="px-4 py-2.5 font-bold">{t.historyStock}</th>
                                  <th className="px-4 py-2.5 text-right font-bold">{t.historyPreviousWeight}</th>
                                  <th className="px-4 py-2.5 text-right font-bold">{t.historyDecidedWeight}</th>
                                  <th className="px-4 py-2.5 text-right font-bold">{t.historyChange}</th>
                                </tr>
                              </thead>
                              <tbody className="divide-y divide-slate-100">
                                {changedStocks.map((s) => {
                                  const delta = s.targetWeight - s.currentWeight
                                  return (
                                    <tr key={s.assetCode}>
                                      <td className="px-4 py-2 font-semibold text-slate-700">{s.assetCode}</td>
                                      <td className="px-4 py-2 text-right text-slate-500">{formatPct(s.currentWeight, lang)}</td>
                                      <td className="px-4 py-2 text-right font-semibold text-slate-700">
                                        {formatPct(s.targetWeight, lang)}
                                      </td>
                                      <td
                                        className={`px-4 py-2 text-right font-semibold ${
                                          delta > 0 ? 'text-[#3a7d74]' : delta < 0 ? 'text-[#ab6262]' : 'text-slate-400'
                                        }`}
                                      >
                                        {formatDeltaPts(delta, lang)}
                                      </td>
                                    </tr>
                                  )
                                })}
                              </tbody>
                            </table>
                            <p className="border-t border-slate-100 px-4 py-2 text-[10px] text-slate-400">
                              {t.historyStocksNote}
                            </p>
                          </div>
                        )}
                      </div>
                    )}

                    {m && (
                      <div className="flex flex-wrap gap-x-4 gap-y-1.5 text-[11px] font-semibold">
                        {m.totalReturnPct != null && (
                          <span className="text-slate-500">
                            {t.historySimulationReturn}:{' '}
                            <span className={m.totalReturnPct >= 0 ? 'text-[#3a7d74]' : 'text-[#ab6262]'}>
                              {formatSignedPct(m.totalReturnPct, lang)}
                            </span>
                          </span>
                        )}
                        {m.benchmarkDiffPct != null && (
                          <span className="text-slate-500">
                            {t.historyBenchmarkDifference}:{' '}
                            <span className={m.benchmarkDiffPct >= 0 ? 'text-[#3a7d74]' : 'text-[#ab6262]'}>
                              {formatSignedPct(m.benchmarkDiffPct, lang)}
                            </span>
                          </span>
                        )}
                        {m.maxDrawdownPct != null && (
                          <span className="text-slate-500">
                            {t.historyMaxDrawdown}: <span className="text-[#ab6262]">{formatSignedPct(m.maxDrawdownPct, lang)}</span>
                          </span>
                        )}
                        {m.dailyVolatilityPct != null && (
                          <span className="text-slate-500">
                            {t.historyVolatility}: <span className="text-slate-700">{formatUnsignedPct(m.dailyVolatilityPct, lang)}</span>
                          </span>
                        )}
                      </div>
                    )}

                    {record.stressTest && (
                      <div className="rounded-xl border border-slate-100 overflow-hidden">
                        <div className="bg-slate-50 border-b border-slate-100 px-4 py-2.5 flex items-center gap-2">
                          <IoPulseOutline className="text-[#c89834]" size={13} />
                          <span className="text-[9.5px] font-bold tracking-wider text-slate-400 uppercase">
                            {t.historyStressTest} · {getScenarioTitle(record.stressTest.scenarioKey)}
                          </span>
                        </div>

                        <div className="overflow-x-auto">
                          <table className="w-full min-w-[440px] border-collapse text-left text-xs">
                            <thead>
                              <tr className="border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[9.5px]">
                                <th className="px-4 py-2.5 font-bold">{t.historyPortfolio}</th>
                                <th className="px-4 py-2.5 text-right font-bold">{t.historyPreShock}</th>
                                <th className="px-4 py-2.5 text-right font-bold">{t.historyExpectedImpact}</th>
                                <th className="px-4 py-2.5 text-right font-bold">{t.historyPostShock}</th>
                              </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-100">
                              {stressTestRows(record.stressTest, t).map(({ label, result }) => (
                                <tr key={label}>
                                  <td className="px-4 py-2 font-semibold text-slate-700 whitespace-nowrap">{label}</td>
                                  <td className="px-4 py-2 text-right text-slate-500 whitespace-nowrap">
                                    {formatCurrency(result.initialValue)}
                                  </td>
                                  <td
                                    className={`px-4 py-2 text-right font-semibold whitespace-nowrap ${
                                      result.expectedImpactRate >= 0 ? 'text-[#3a7d74]' : 'text-[#ab6262]'
                                    }`}
                                  >
                                    {formatSignedPct(result.expectedImpactRate * 100, lang)}
                                  </td>
                                  <td className="px-4 py-2 text-right font-semibold text-slate-700 whitespace-nowrap">
                                    {formatCurrency(result.postShockValue)}
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>

                        {record.stressTest.llmComment && (
                          <p className="border-t border-slate-100 px-4 py-3 text-[11px] italic leading-relaxed text-slate-500">
                            {record.stressTest.llmComment}
                          </p>
                        )}
                      </div>
                    )}

                    {canReapply && (
                      <button
                        type="button"
                        onClick={() => handleReapply()}
                        className="px-4 py-2 rounded-xl border border-[#c89834]/40 text-[#c89834] font-bold text-[11px] tracking-wide uppercase hover:bg-[#c89834]/10 transition-all cursor-pointer"
                      >
                        {t.historyReapply}
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
