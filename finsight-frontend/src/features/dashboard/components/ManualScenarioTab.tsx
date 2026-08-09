import { useState, useEffect, useMemo } from 'react'
import { getAssetCategoryLabel, applyManualScenario } from '../dashboardApi'
import type { Fund, AssetCategory } from '../dashboardApi'
import { getFundDashboard } from '../lib/fund-dashboard/fundDashboardApi'

type ManualScenarioTabProps = {
  fund: Fund
  onScenarioApplied: () => void
}

type StockItem = {
  assetCode: string
  defaultWeight: number
}

export default function ManualScenarioTab({ fund, onScenarioApplied }: ManualScenarioTabProps) {
  // Input states for main categories (stored as strings with commas for Turkish UI formatting)
  const [weights, setWeights] = useState<Record<AssetCategory, string>>({
    STOCK: (fund.weights?.STOCK ?? 0).toFixed(2).replace('.', ','),
    REPO: (fund.weights?.REPO ?? 0).toFixed(2).replace('.', ','),
    FUTURE: (fund.weights?.FUTURE ?? 0).toFixed(2).replace('.', ','),
    FUND: (fund.weights?.FUND ?? 0).toFixed(2).replace('.', ','),
  })

  // Dynamic stock breakdown list from DB / API
  const [stocks, setStocks] = useState<StockItem[]>([])
  const [loadingStocks, setLoadingStocks] = useState<boolean>(true)
  const [stockBreakdownOpen, setStockBreakdownOpen] = useState<boolean>(false)
  const [showOnlyChanged, setShowOnlyChanged] = useState<boolean>(false)
  const [stockBaseline, setStockBaseline] = useState<Record<string, number>>({})
  const [stockInputs, setStockInputs] = useState<Record<string, string>>({})

  const [note, setNote] = useState<string>('')
  const [submitting, setSubmitting] = useState<boolean>(false)
  const [errors, setErrors] = useState<string[]>([])
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Fetch dynamic stock breakdown from dashboard API
  useEffect(() => {
    let isMounted = true
    async function loadStockBreakdown() {
      try {
        setLoadingStocks(true)
        const dashboard = await getFundDashboard(fund.code || 'TIE')
        if (dashboard?.stockBreakdown?.items && isMounted) {
          const dynamicStocks: StockItem[] = dashboard.stockBreakdown.items.map((item) => ({
            assetCode: item.assetCode,
            defaultWeight: Number(item.weight) || 0,
          }))

          const newBaseline: Record<string, number> = {}
          const newInputs: Record<string, string> = {}

          dynamicStocks.forEach((s) => {
            newBaseline[s.assetCode] = s.defaultWeight
            newInputs[s.assetCode] = s.defaultWeight.toFixed(2).replace('.', ',')
          })

          setStocks(dynamicStocks)
          setStockBaseline(newBaseline)
          setStockInputs(newInputs)
        }
      } catch (err) {
        console.warn('Could not fetch dynamic stock breakdown from DB API', err)
      } finally {
        if (isMounted) setLoadingStocks(false)
      }
    }
    loadStockBreakdown()
    return () => {
      isMounted = false
    }
  }, [fund?.code])

  // Helper to parse input values (handling Turkish commas)
  const getParsedWeight = (valStr: string): number => {
    const normalized = (valStr || '').replace(/,/g, '.')
    const val = parseFloat(normalized)
    return isNaN(val) ? 0 : val
  }

  const getParsedWeightForCat = (cat: AssetCategory): number => {
    return getParsedWeight(weights[cat])
  }

  const getParsedWeightForStock = (assetCode: string): number => {
    return getParsedWeight(stockInputs[assetCode])
  }

  // Dynamic sum of stock breakdown inputs
  const totalStockBreakdownSum = useMemo(() => {
    return stocks.reduce((sum, s) => sum + getParsedWeightForStock(s.assetCode), 0)
  }, [stocks, stockInputs])

  // Re-initialize state when fund id changes
  useEffect(() => {
    setWeights({
      STOCK: (fund.weights?.STOCK ?? 0).toFixed(2).replace('.', ','),
      REPO: (fund.weights?.REPO ?? 0).toFixed(2).replace('.', ','),
      FUTURE: (fund.weights?.FUTURE ?? 0).toFixed(2).replace('.', ','),
      FUND: (fund.weights?.FUND ?? 0).toFixed(2).replace('.', ','),
    })
    setErrors([])
    setSuccessMsg(null)
  }, [fund?.id])

  const categories: AssetCategory[] = ['STOCK', 'REPO', 'FUTURE', 'FUND']

  // Calculate sum of weights
  const totalWeight = categories.reduce((sum, cat) => sum + getParsedWeightForCat(cat), 0)

  // Validate values dynamically
  useEffect(() => {
    const newErrors: string[] = []

    // 1. Sum check (within tolerance)
    const diffFrom100 = Math.abs(totalWeight - 100)
    if (diffFrom100 > 0.01) {
      newErrors.push(`Toplam ağırlık %100.00 olmalıdır. (Mevcut: %${totalWeight.toFixed(2).replace('.', ',')})`)
    }

    // 2. STOCK minimum check
    const stockWeight = getParsedWeightForCat('STOCK')
    if (stockWeight < 80.0) {
      newErrors.push(
        `Hisse senedi yoğun fon yasal gerekliliği nedeniyle Hisse Senedi ağırlığı %80.00'den az olamaz. (Mevcut: %${stockWeight.toFixed(2).replace('.', ',')})`
      )
    }

    // 3. Category deviation limits check (±10% max)
    categories.forEach((cat) => {
      const current = fund.weights?.[cat] ?? 0
      const target = getParsedWeightForCat(cat)
      const dev = Math.abs(target - current)
      if (dev > 10.0) {
        const name = getAssetCategoryLabel(cat)
        newErrors.push(`${name} sapması ±10.00% puanı aşamaz. (Sapma: ${dev.toFixed(2).replace('.', ',')}%)`)
      }
    })

    // 4. Stock deviation limits check (±5% max) and stock breakdown total 100% check
    if (stockBreakdownOpen) {
      const diffFrom100 = Math.abs(totalStockBreakdownSum - 100)

      if (diffFrom100 > 0.01) {
        newErrors.push(
          `Hisse senedi alt kırılım toplamı %100.00 olmalıdır. (Mevcut: %${totalStockBreakdownSum.toFixed(2).replace('.', ',')})`
        )
      }

      stocks.forEach((stock) => {
        const isReadOnly = stock.assetCode === 'Others' || stock.assetCode === '+ Diğer'
        if (!isReadOnly) {
          const current = stockBaseline[stock.assetCode] ?? stock.defaultWeight
          const target = getParsedWeightForStock(stock.assetCode)
          const dev = Math.abs(target - current)
          if (dev > 5.0) {
            newErrors.push(
              `Hissede (${stock.assetCode}) izin verilen maksimum sapma ±5.00 puanı aşamaz. (Sapma: ${dev.toFixed(2).replace('.', ',')}%)`
            )
          }
        }
      })
    }

    setErrors(newErrors)
  }, [weights, fund, stockBreakdownOpen, stockInputs, stockBaseline, totalWeight, stocks, totalStockBreakdownSum])

  function handleInputChange(cat: AssetCategory, val: string) {
    const clean = val.replace(/\./g, ',')
    if (clean === '' || /^[0-9]*,?[0-9]*$/.test(clean)) {
      setWeights((prev) => ({ ...prev, [cat]: clean }))
    }
  }

  function handleIncrement(cat: AssetCategory, step: number) {
    const currentVal = getParsedWeightForCat(cat)
    const newVal = Math.max(0, Math.min(100, currentVal + step))
    setWeights((prev) => ({
      ...prev,
      [cat]: newVal.toFixed(2).replace('.', ','),
    }))
  }

  function handleStockInputChange(assetCode: string, val: string) {
    if (assetCode === 'Others' || assetCode === '+ Diğer') return
    const clean = val.replace(/\./g, ',')
    if (clean === '' || /^[0-9]*,?[0-9]*$/.test(clean)) {
      setStockInputs((prev) => ({ ...prev, [assetCode]: clean }))
    }
  }

  function handleStockIncrement(assetCode: string, step: number) {
    if (assetCode === 'Others' || assetCode === '+ Diğer') return
    const currentVal = getParsedWeightForStock(assetCode)
    const newVal = Math.max(0, Math.min(100, currentVal + step))
    setStockInputs((prev) => ({
      ...prev,
      [assetCode]: newVal.toFixed(2).replace('.', ','),
    }))
  }

  function handleReset() {
    setWeights({
      STOCK: (fund.weights?.STOCK ?? 0).toFixed(2).replace('.', ','),
      REPO: (fund.weights?.REPO ?? 0).toFixed(2).replace('.', ','),
      FUTURE: (fund.weights?.FUTURE ?? 0).toFixed(2).replace('.', ','),
      FUND: (fund.weights?.FUND ?? 0).toFixed(2).replace('.', ','),
    })

    const resetInputs: Record<string, string> = {}
    stocks.forEach((s) => {
      const current = stockBaseline[s.assetCode] ?? s.defaultWeight
      resetInputs[s.assetCode] = current.toFixed(2).replace('.', ',')
    })
    setStockInputs(resetInputs)

    setNote('')
    setSuccessMsg(null)
  }

  async function handleSubmit() {
    if (errors.length > 0) return

    try {
      setSubmitting(true)
      setSuccessMsg(null)

      const stockWeightsPayload: Record<string, number> = {}
      if (stockBreakdownOpen) {
        stocks.forEach((s) => {
          stockWeightsPayload[s.assetCode] = getParsedWeightForStock(s.assetCode)
        })
      }

      const payload = {
        fundId: fund.id,
        note: note.trim() || undefined,
        weights: {
          STOCK: getParsedWeightForCat('STOCK'),
          REPO: getParsedWeightForCat('REPO'),
          FUTURE: getParsedWeightForCat('FUTURE'),
          FUND: getParsedWeightForCat('FUND'),
        },
        stockWeights: stockBreakdownOpen ? stockWeightsPayload : undefined,
      }

      await applyManualScenario(payload)
      setSuccessMsg(
        '✓ Manuel dağılım simülasyona uygulandı. Bu dağılım artık ayrı bir Simülasyon Portföyü olarak kaydedildi — mevcut portföyünüz gerçek emirle değişmedi.'
      )
      onScenarioApplied()
    } catch (err: any) {
      setErrors([err?.message || 'Simülasyon kaydedilirken bir hata oluştu.'])
    } finally {
      setSubmitting(false)
    }
  }

  // Filter stocks if showOnlyChanged is true
  const displayedStocks = useMemo(() => {
    if (!showOnlyChanged) return stocks
    return stocks.filter((stock) => {
      const isReadOnly = stock.assetCode === 'Others' || stock.assetCode === '+ Diğer'
      if (isReadOnly) return false
      const current = stockBaseline[stock.assetCode] ?? stock.defaultWeight
      const target = getParsedWeightForStock(stock.assetCode)
      return Math.abs(target - current) > 0.001
    })
  }, [showOnlyChanged, stockBaseline, stockInputs, stocks])

  return (
    <div className="space-y-4 animate-fade-in select-none">
      <div className="bg-white rounded-xl border border-slate-200/75 shadow-sm p-4 space-y-4">
        {/* Başlık ve Kılavuz */}
        <div className="space-y-1.5">
          <h3 className="text-xs font-bold text-slate-800">Manuel Senaryo Oluştur</h3>
          <p className="text-[10.5px] leading-relaxed text-slate-400 font-medium">
            Mevcut portföy referans alınır; AI önerisinden bağımsızdır. Toplam her zaman %100 olmalı, tek bir varlıktaki sapma ±10 puanı aşamaz (taslak uyumluluk kuralı). Ayrıca hisse ağırlığı, hisse senedi yoğun fon statüsü gereği %80'in altına düşürülemez (sabit yasal alt sınır).
          </p>
        </div>

        {/* Form Tablosu */}
        <div className="overflow-x-auto border border-slate-100 rounded-xl">
          <table className="min-w-[580px] w-full border-collapse text-left text-xs">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[9.5px] select-none">
                <th className="px-4 py-2.5 font-bold">KATEGORİ</th>
                <th className="px-4 py-2.5 text-right font-bold">MEVCUT</th>
                <th className="px-4 py-2.5 text-center font-bold">YENİ AĞIRLIK</th>
                <th className="px-4 py-2.5 text-right font-bold">DEĞİŞİM</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {categories.map((cat) => {
                const current = fund.weights?.[cat] ?? 0
                const target = getParsedWeightForCat(cat)
                const diff = target - current
                const label = getAssetCategoryLabel(cat)

                const isStock = cat === 'STOCK'
                const isFloorBreach = isStock && target < 80.0
                const isDevError = Math.abs(diff) > 10.0

                return (
                  <tr
                    key={cat}
                    className={`transition-colors ${
                      isFloorBreach ? 'bg-rose-50/50' : 'hover:bg-slate-50/20'
                    }`}
                  >
                    <td className="px-4 py-2">
                      <div className="font-semibold text-slate-700">{label}</div>
                      {isStock && (
                        <button
                          type="button"
                          onClick={() => setStockBreakdownOpen(!stockBreakdownOpen)}
                          className="inline-flex items-center text-[11px] font-semibold text-[#c89834] hover:text-[#b08226] transition-colors mt-0.5 outline-none cursor-pointer select-none"
                        >
                          <span>
                            {stockBreakdownOpen ? '▲ Hisse bazında gizle' : '▼ Hisse bazında düzenle'}
                          </span>
                        </button>
                      )}
                    </td>
                    <td className="px-4 py-2 text-right font-semibold text-slate-500">
                      {current.toFixed(2).replace('.', ',')}%
                    </td>
                    <td className="px-4 py-1.5 text-center">
                      <div className="inline-flex flex-col items-center justify-center">
                        <div
                          className={`relative flex items-center bg-white border ${
                            isFloorBreach
                              ? 'border-[#d9383a] ring-1 ring-[#d9383a]/30'
                              : isDevError
                              ? 'border-amber-400 focus-within:ring-amber-200'
                              : 'border-slate-200 focus-within:border-[#c89834] focus-within:ring-[#c89834]/20'
                          } rounded-lg px-2.5 py-1 focus-within:ring-2 focus-within:ring-opacity-50 transition-all w-32`}
                        >
                          <input
                            type="text"
                            value={weights[cat]}
                            onChange={(e) => handleInputChange(cat, e.target.value)}
                            className="w-full text-right outline-none font-semibold text-xs bg-transparent text-slate-650 pr-1"
                          />
                          <div className="flex flex-col border-l border-slate-200 pl-1.5 ml-1 text-slate-400 select-none">
                            <button
                              type="button"
                              onClick={() => handleIncrement(cat, 0.1)}
                              className="hover:text-[#c89834] active:scale-95 transition-all outline-none leading-none h-2 flex items-center justify-center cursor-pointer"
                            >
                              <span className="text-[6.5px]">▲</span>
                            </button>
                            <button
                              type="button"
                              onClick={() => handleIncrement(cat, -0.1)}
                              className="hover:text-[#c89834] active:scale-95 transition-all outline-none leading-none h-2 flex items-center justify-center cursor-pointer mt-0.5"
                            >
                              <span className="text-[6.5px]">▼</span>
                            </button>
                          </div>
                        </div>
                      </div>
                    </td>
                    <td
                      className={`px-4 py-2 text-right font-semibold text-xs ${
                        diff > 0
                          ? 'text-[#3a7d74]'
                          : diff < 0
                          ? 'text-[#ab6262]'
                          : 'text-[#8a94a6]'
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

              {/* Toplam Row */}
              <tr className="bg-slate-50 border-t border-slate-200 font-bold select-none text-slate-800">
                <td className="px-4 py-2.5 font-bold">Toplam</td>
                <td className="px-4 py-2.5 text-right font-bold text-slate-500">100,00%</td>
                <td className="px-4 py-2.5 text-center font-bold">
                  <span
                    className={`${
                      Math.abs(totalWeight - 100) > 0.01 ? 'text-rose-600' : 'text-[#c89834]/90'
                    }`}
                  >
                    {totalWeight.toFixed(2).replace('.', ',')}%
                  </span>
                </td>
                <td
                  className={`px-4 py-2.5 text-right font-semibold ${
                    totalWeight - 100 > 0.01
                      ? 'text-[#3a7d74]'
                      : totalWeight - 100 < -0.01
                      ? 'text-[#ab6262]'
                      : 'text-slate-400'
                  }`}
                >
                  {totalWeight - 100 > 0
                    ? `+${(totalWeight - 100).toFixed(2).replace('.', ',')}`
                    : (totalWeight - 100).toFixed(2).replace('.', ',')}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        {/* HISSE ALT KIRILIM TABLOSU (ACCORDION EXPANDED) */}
        {stockBreakdownOpen && (
          <div className="bg-[#fffdf9] border border-[#f5d9a8]/70 rounded-xl p-4 space-y-3 animate-fade-in shadow-xs">
            {/* Filtreleme ve Başlık Barı */}
            <div className="flex items-center justify-between pt-1">
              <span className="text-[11px] font-bold uppercase tracking-wider text-slate-500">
                Hisse Senedi Alt Kırılımı
              </span>
              <button
                type="button"
                onClick={() => setShowOnlyChanged(!showOnlyChanged)}
                className="text-xs font-semibold text-slate-600 hover:text-[#c89834] transition-colors outline-none cursor-pointer flex items-center gap-1 select-none"
              >
                <span>{showOnlyChanged ? 'Tüm Hisseleri Göster ▾' : 'Sadece Değişenleri Göster ▴'}</span>
              </button>
            </div>

            {/* Alt Kırılım Tablosu */}
            {loadingStocks ? (
              <div className="flex items-center justify-center py-8 text-xs text-slate-500 font-medium bg-white rounded-xl border border-slate-200/80">
                <div className="h-4 w-4 border-2 border-[#c89834] border-t-transparent rounded-full animate-spin mr-2.5" />
                Hisse kırılım verileri yükleniyor...
              </div>
            ) : (
              <div className="overflow-x-auto border border-slate-200/80 rounded-xl bg-white shadow-2xs">
                <table className="min-w-[580px] w-full border-collapse text-left text-xs">
                  <thead>
                    <tr className="bg-slate-50/80 border-b border-slate-100 text-slate-400 font-bold uppercase tracking-wider text-[9px] select-none">
                    <th className="px-4 py-2 font-bold">HİSSE</th>
                    <th className="px-4 py-2 text-right font-bold">MEVCUT</th>
                    <th className="px-4 py-2 text-center font-bold">YENİ AĞIRLIK</th>
                    <th className="px-4 py-2 text-right font-bold">DEĞİŞİM</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {displayedStocks.map((stock) => {
                    const current = stockBaseline[stock.assetCode] ?? stock.defaultWeight
                    const isReadOnly = stock.assetCode === 'Others' || stock.assetCode === '+ Diğer'
                    const target = isReadOnly ? current : getParsedWeightForStock(stock.assetCode)
                    const diff = target - current
                    const isDevError = !isReadOnly && Math.abs(diff) > 5.0
                    const displayName = isReadOnly ? '+ Diğer' : stock.assetCode

                    return (
                      <tr
                        key={stock.assetCode}
                        className={`transition-colors ${
                          isDevError ? 'bg-amber-50/50' : 'hover:bg-slate-50/30'
                        }`}
                      >
                        <td className="px-4 py-2 font-bold font-mono text-slate-700">
                          {displayName}
                        </td>
                        <td className="px-4 py-2 text-right font-semibold font-mono text-slate-500">
                          {current.toFixed(2).replace('.', ',')}%
                        </td>
                        <td className="px-4 py-1.5 text-center">
                          <div className="inline-flex items-center justify-center">
                            <div
                              title={isReadOnly ? 'Diğer hisseler kategorisi sabittir ve değiştirilemez' : undefined}
                              className={`relative flex items-center ${
                                isReadOnly
                                  ? 'bg-slate-50/90 border-slate-200/90 cursor-not-allowed'
                                  : isDevError
                                  ? 'bg-white border-amber-400 ring-1 ring-amber-300/40'
                                  : 'bg-white border-slate-200 focus-within:border-[#c89834] focus-within:ring-[#c89834]/20'
                              } border rounded-lg px-2.5 py-0.5 focus-within:ring-2 focus-within:ring-opacity-50 transition-all w-28`}
                            >
                              <input
                                type="text"
                                value={isReadOnly ? current.toFixed(2).replace('.', ',') : stockInputs[stock.assetCode]}
                                readOnly={isReadOnly}
                                onChange={(e) =>
                                  handleStockInputChange(stock.assetCode, e.target.value)
                                }
                                className={`w-full text-right outline-none font-semibold text-xs bg-transparent ${
                                  isReadOnly ? 'text-slate-500 cursor-not-allowed' : 'text-slate-700'
                                } pr-1`}
                              />
                              {!isReadOnly ? (
                                <div className="flex flex-col border-l border-slate-200 pl-1 ml-1 text-slate-400 select-none">
                                  <button
                                    type="button"
                                    onClick={() => handleStockIncrement(stock.assetCode, 0.1)}
                                    className="hover:text-[#c89834] active:scale-95 transition-all outline-none leading-none h-2 flex items-center justify-center cursor-pointer"
                                  >
                                    <span className="text-[6px]">▲</span>
                                  </button>
                                  <button
                                    type="button"
                                    onClick={() => handleStockIncrement(stock.assetCode, -0.1)}
                                    className="hover:text-[#c89834] active:scale-95 transition-all outline-none leading-none h-2 flex items-center justify-center cursor-pointer mt-0.5"
                                  >
                                    <span className="text-[6px]">▼</span>
                                  </button>
                                </div>
                              ) : (
                                <span className="text-[10px] text-slate-400 font-bold select-none">%</span>
                              )}
                            </div>
                          </div>
                        </td>
                        <td
                          className={`px-4 py-2 text-right font-semibold font-mono text-xs ${
                            diff > 0
                              ? 'text-[#3a7d74]'
                              : diff < 0
                              ? 'text-[#ab6262]'
                              : 'text-[#8a94a6]'
                          }`}
                        >
                          {diff > 0
                            ? `+${diff.toFixed(2).replace('.', ',')}`
                            : diff === 0
                            ? '+0,00'
                            : diff.toFixed(2).replace('.', ',')}
                        </td>
                      </tr>
                    )
                  })}
                  {displayedStocks.length === 0 && (
                    <tr>
                      <td colSpan={4} className="px-4 py-4 text-center text-slate-400 text-xs italic">
                        Henüz değiştirilmiş bir hisse bulunmamaktadır.
                      </td>
                    </tr>
                  )}

                  {/* Hisse Alt Kırılım Toplamı Satırı */}
                  <tr className="bg-slate-50 border-t border-slate-200 font-bold select-none text-slate-800">
                    <td className="px-4 py-2 font-bold font-mono">Toplam</td>
                    <td className="px-4 py-2 text-right font-bold font-mono text-slate-500">
                      100,00%
                    </td>
                    <td className="px-4 py-2 text-center font-bold font-mono">
                      <span
                        className={`${
                          Math.abs(totalStockBreakdownSum - 100) > 0.01
                            ? 'text-rose-600 font-extrabold'
                            : 'text-[#c89834]/90'
                        }`}
                      >
                        {totalStockBreakdownSum.toFixed(2).replace('.', ',')}%
                      </span>
                    </td>
                    <td
                      className={`px-4 py-2 text-right font-bold font-mono text-xs ${
                        totalStockBreakdownSum - 100 > 0.01
                          ? 'text-[#3a7d74]'
                          : totalStockBreakdownSum - 100 < -0.01
                          ? 'text-[#ab6262]'
                          : 'text-[#8a94a6]'
                      }`}
                    >
                      {totalStockBreakdownSum - 100 > 0.01
                        ? `+${(totalStockBreakdownSum - 100).toFixed(2).replace('.', ',')}`
                        : Math.abs(totalStockBreakdownSum - 100) <= 0.01
                        ? '+0,00'
                        : (totalStockBreakdownSum - 100).toFixed(2).replace('.', ',')}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            )}

            {/* Alt Kırılım Toplamı %100 Olmalıdır Uyarısı */}
            {Math.abs(totalStockBreakdownSum - 100) > 0.01 && (
              <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 flex items-center space-x-2 text-xs font-semibold animate-fade-in shadow-2xs">
                <span className="text-sm leading-none flex-shrink-0">⚠️</span>
                <span>
                  Hisse senedi alt kırılım toplamı %100.00 olmalıdır. (Mevcut: %{totalStockBreakdownSum.toFixed(2).replace('.', ',')})
                </span>
              </div>
            )}
          </div>
        )}

        {/* Validasyon Hata Mesajları / Uyarı Banner'ları (Bölüm 5.8) */}
        {errors.filter((err) => !err.includes('alt kırılım toplamı')).length > 0 && (
          <div className="space-y-2.5 animate-fade-in">
            {errors
              .filter((err) => !err.includes('alt kırılım toplamı'))
              .map((err, idx) => {
                const isStockFloor = err.includes('Hisse senedi yoğun fon') || err.includes('%80')
                const isDeviation = err.includes('sapması') || err.includes('±10') || err.includes('±5')

                if (isStockFloor) {
                  // 🚫 Sabit Alt Sınır Uyarısı: Koyu/dolu kırmızı, daha vurgulu
                  return (
                    <div
                      key={idx}
                      className="p-3.5 rounded-xl bg-[#d9383a] text-white shadow-sm flex items-start space-x-2.5 text-xs font-medium"
                    >
                      <span className="text-base leading-none flex-shrink-0">🚫</span>
                      <span className="leading-relaxed">{err}</span>
                    </div>
                  )
                }

                if (isDeviation) {
                  // ⚠️ Sapma Uyarısı: Açık turuncu/kırmızı, hafif uyarı stili
                  return (
                    <div
                      key={idx}
                      className="p-3.5 rounded-xl bg-[#fff8ee] border border-[#f5d9a8] text-[#9c5a14] flex items-start space-x-2.5 text-xs font-semibold"
                    >
                      <span className="text-sm leading-none flex-shrink-0 mt-0.5">⚠️</span>
                      <span className="leading-relaxed">{err}</span>
                    </div>
                  )
                }

                // Toplam Ağırlık Uyarısı: Açık kırmızı/pembe hafif uyarı stili
                return (
                  <div
                    key={idx}
                    className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 flex items-start space-x-2.5 text-xs font-semibold"
                  >
                    <span className="text-sm leading-none flex-shrink-0 mt-0.5">⚠️</span>
                    <span className="leading-relaxed">{err}</span>
                  </div>
                )
              })}
          </div>
        )}

        {/* Senaryo Notu */}
        <div className="space-y-1.5">
          <label className="text-[10px] font-bold text-slate-400 tracking-wider uppercase block">
            Senaryo Notu (opsiyonel)
          </label>
          <textarea
            rows={3}
            value={note}
            onChange={(e) => setNote(e.target.value)}
            placeholder="Örn. Yaklaşan temettü döneminde likiditeyi güçlendirmek istiyorum."
            className="w-full bg-white text-slate-800 border border-slate-200 rounded-xl px-4 py-3 text-xs outline-none focus:border-[#c89834] focus:ring-2 focus:ring-[#c89834]/20 transition-all placeholder-slate-400 resize-none font-medium"
          />
        </div>

        {/* Butonlar - Left Aligned */}
        <div className="flex items-center justify-start space-x-3 pt-1">
          <button
            type="button"
            disabled={submitting || errors.length > 0}
            onClick={handleSubmit}
            className="px-5 py-2.5 rounded-xl bg-[#c89834] text-white font-extrabold text-xs tracking-wider uppercase hover:bg-[#b08226] shadow-sm hover:shadow disabled:opacity-50 disabled:cursor-not-allowed transition-all select-none cursor-pointer"
          >
            {submitting ? 'Kaydediliyor...' : 'Dağılımı Simülasyona Uygula'}
          </button>
          <button
            type="button"
            onClick={handleReset}
            className="px-5 py-2.5 rounded-xl border border-slate-200 text-slate-600 bg-white font-extrabold text-xs tracking-wider uppercase hover:bg-slate-50 select-none cursor-pointer"
          >
            Mevcut Portföye Sıfırla
          </button>
        </div>

        {/* Başarı Mesajı */}
        {successMsg && (
          <div className="p-3.5 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-800 flex items-start space-x-2.5 text-xs font-semibold animate-fade-in shadow-2xs">
            <span className="text-base leading-none text-emerald-600 flex-shrink-0 mt-0.5">✓</span>
            <span className="leading-relaxed">{successMsg}</span>
          </div>
        )}
      </div>
    </div>
  )
}
