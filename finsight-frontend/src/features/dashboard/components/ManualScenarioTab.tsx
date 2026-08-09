import { useState, useEffect } from 'react'
import { AssetCategoryLabels, applyManualScenario } from '../dashboardApi'
import type { Fund, AssetCategory } from '../dashboardApi'
import { IoAlertCircleOutline, IoCheckmarkCircleOutline } from 'react-icons/io5'

type ManualScenarioTabProps = {
  fund: Fund
  onScenarioApplied: () => void
}

export default function ManualScenarioTab({ fund, onScenarioApplied }: ManualScenarioTabProps) {
  // Input states initialized to existing fund weights (stored as strings with commas for Turkish UI formatting)
  const [weights, setWeights] = useState<Record<AssetCategory, string>>({
    STOCK: (fund.weights?.STOCK ?? 0).toFixed(2).replace('.', ','),
    REPO: (fund.weights?.REPO ?? 0).toFixed(2).replace('.', ','),
    FUTURE: (fund.weights?.FUTURE ?? 0).toFixed(2).replace('.', ','),
    FUND: (fund.weights?.FUND ?? 0).toFixed(2).replace('.', ',')
  })

  const [note, setNote] = useState<string>('')
  const [submitting, setSubmitting] = useState<boolean>(false)
  const [errors, setErrors] = useState<string[]>([])
  const [successMsg, setSuccessMsg] = useState<string | null>(null)

  // Re-initialize state when fund changes
  useEffect(() => {
    setWeights({
      STOCK: (fund.weights?.STOCK ?? 0).toFixed(2).replace('.', ','),
      REPO: (fund.weights?.REPO ?? 0).toFixed(2).replace('.', ','),
      FUTURE: (fund.weights?.FUTURE ?? 0).toFixed(2).replace('.', ','),
      FUND: (fund.weights?.FUND ?? 0).toFixed(2).replace('.', ',')
    })
    setErrors([])
    setSuccessMsg(null)
  }, [fund])

  const categories: AssetCategory[] = ['STOCK', 'REPO', 'FUTURE', 'FUND']

  // Helper to parse input values (handling Turkish commas)
  const getParsedWeight = (valStr: string): number => {
    const normalized = valStr.replace(/,/g, '.')
    const val = parseFloat(normalized)
    return isNaN(val) ? 0 : val
  }

  const getParsedWeightForCat = (cat: AssetCategory): number => {
    return getParsedWeight(weights[cat])
  }

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
    if (stockWeight < 80.00) {
      newErrors.push(`Hisse senedi yoğun fon yasal gerekliliği nedeniyle Hisse Senedi ağırlığı %80.00'den az olamaz. (Mevcut: %${stockWeight.toFixed(2).replace('.', ',')})`)
    }

    // 3. Deviation limits check (±10% max)
    categories.forEach((cat) => {
      const current = fund.weights?.[cat] ?? 0
      const target = getParsedWeightForCat(cat)
      const dev = Math.abs(target - current)
      if (dev > 10.00) {
        const name = AssetCategoryLabels[cat]?.tr || cat
        newErrors.push(`${name} sapması ±10.00% puanı aşamaz. (Sapma: ${dev.toFixed(2).replace('.', ',')}%)`)
      }
    })

    setErrors(newErrors)
  }, [weights, fund])

  function handleInputChange(cat: AssetCategory, val: string) {
    // Allow digits, one comma or dot
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
      [cat]: newVal.toFixed(2).replace('.', ',')
    }))
  }

  function handleReset() {
    setWeights({
      STOCK: (fund.weights?.STOCK ?? 0).toFixed(2).replace('.', ','),
      REPO: (fund.weights?.REPO ?? 0).toFixed(2).replace('.', ','),
      FUTURE: (fund.weights?.FUTURE ?? 0).toFixed(2).replace('.', ','),
      FUND: (fund.weights?.FUND ?? 0).toFixed(2).replace('.', ',')
    })
    setNote('')
    setSuccessMsg(null)
  }

  async function handleSubmit() {
    if (errors.length > 0) return

    try {
      setSubmitting(true)
      setSuccessMsg(null)

      const payload = {
        fundId: fund.id,
        note: note.trim() || undefined,
        weights: {
          STOCK: getParsedWeightForCat('STOCK'),
          REPO: getParsedWeightForCat('REPO'),
          FUTURE: getParsedWeightForCat('FUTURE'),
          FUND: getParsedWeightForCat('FUND')
        }
      }

      await applyManualScenario(payload)
      setSuccessMsg('✓ Manuel dağılım simülasyona uygulandı. Bu dağılım artık ayrı bir Simülasyon Portföyü olarak kaydedildi — mevcut portföyünüz gerçek emirle değişmedi.')
      onScenarioApplied()
    } catch (err: any) {
      setErrors([err?.message || 'Simülasyon kaydedilirken bir hata oluştu.'])
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="space-y-4 animate-fade-in select-none">
      <div className="bg-white rounded-xl border border-slate-200/75 shadow-sm p-4 space-y-4">
        {/* Başlık ve Kılavuz */}
        <div className="space-y-1.5">
          <h3 className="text-xs font-bold text-slate-800">
            Manuel Senaryo Oluştur
          </h3>
          <p className="text-[10.5px] leading-relaxed text-slate-400 font-medium">
            Mevcut portföy referans alınır; AI önerisinden bağımsızdır. Toplam her zaman %100 olmalı, tek bir varlıktaki sapma ±10 puanı aşamaz (taslak uyumluluk kuralı). Ayrıca hisse ağırlığı, hisse senedi yoğun fon statüsü gereği %80'in altına düşürülemez (sabit yasal alt sınır).
          </p>
        </div>

        {/* Form Tablosu */}
        <div className="overflow-hidden border border-slate-100 rounded-xl">
          <table className="w-full border-collapse text-left text-xs">
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
                const label = AssetCategoryLabels[cat]?.tr || cat

                const isFloorBreach = cat === 'STOCK' && target < 80.00
                const isDevError = Math.abs(diff) > 10.00
                const hasError = isFloorBreach || isDevError

                return (
                  <tr key={cat} className={`transition-colors ${isFloorBreach ? 'bg-rose-50/50' : 'hover:bg-slate-50/20'}`}>
                    <td className="px-4 py-2 font-semibold text-slate-700">{label}</td>
                    <td className="px-4 py-2 text-right font-semibold text-slate-500">
                      {current.toFixed(2).replace('.', ',')}%
                    </td>
                    <td className="px-4 py-1.5 text-center">
                      <div className="inline-flex items-center justify-center">
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
                              onClick={() => handleIncrement(cat, 0.10)}
                              className="hover:text-[#c89834] active:scale-95 transition-all outline-none leading-none h-2 flex items-center justify-center cursor-pointer"
                            >
                              <span className="text-[6.5px]">▲</span>
                            </button>
                            <button
                              type="button"
                              onClick={() => handleIncrement(cat, -0.10)}
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
                    {totalWeight.toFixed(2).replace('.', ',')}
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

        {/* Validasyon Hata Mesajları / Uyarı Banner'ları (Bölüm 5.8) */}
        {errors.length > 0 && (
          <div className="space-y-2.5 animate-fade-in">
            {errors.map((err, idx) => {
              const isStockFloor = err.includes('Hisse senedi yoğun fon') || err.includes('%80')
              const isDeviation = err.includes('sapması') || err.includes('±10')

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

        {/* Başarı Mesajı (Bölüm 5.11) */}
        {successMsg && (
          <div className="flex items-center space-x-1.5 text-xs font-semibold py-1 text-[#2d7a4d] animate-fade-in leading-relaxed">
            <span>{successMsg}</span>
          </div>
        )}
      </div>
    </div>
  )
}
