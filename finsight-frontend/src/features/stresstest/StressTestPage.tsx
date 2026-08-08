import { useState } from 'react'
import { IoLockClosedOutline } from 'react-icons/io5'
import { ScenarioSelector } from './ScenarioSelector'
import { ResultsTable } from './ResultsTable'
import { LLMCommentSection } from './LLMCommentSection'
import type { PortfolioDataDto, ScenarioKey, StressTestInferenceResponseDto } from './types'

interface StressTestPageProps {
  portfolio: PortfolioDataDto | null
  onSaveAndNavigate: (scenarioKey: ScenarioKey) => Promise<void>
}

const SCENARIO_TITLES: Record<ScenarioKey, string> = {
  EQUITY_SHOCK: 'Hisse Şoku',
  INTEREST_RATE_SHOCK: 'Faiz Şoku',
}

const SHOCKS: Record<ScenarioKey, { factors: Record<string, number>; comment: string }> = {
  EQUITY_SHOCK: {
    factors: { HISSE: -0.1, REPO: -0.005, VADELI: -0.005, YAT_FON: -0.01 },
    comment:
      'Portföyün yüksek hisse senedi ağırlığı nedeniyle bu senaryoda önemli bir değer kaybı yaşanabilir. Ters-repo ve nakit benzeri kalemlerin payı, kaybı kısmen sınırlandırır. Simülasyon Portföyü, ağırlığı hisseden kaydırdığı ölçüde Referans Portföye göre daha az etkilenir.\n\nBu bir tahmin değil, belirlenen varsayımlar altında portföyün duyarlılık analizidir.',
  },
  INTEREST_RATE_SHOCK: {
    factors: { HISSE: -0.03, REPO: 0.015, VADELI: 0.012, YAT_FON: -0.01 },
    comment:
      'Faiz oranlarındaki artış hisse senedi pozisyonları üzerinde baskı oluştururken, ters repo ve likit enstrümanların getirisi artmaktadır.\n\nBu bir tahmin değil, belirlenen varsayımlar altında portföyün duyarlılık analizidir.',
  },
}

export default function StressTestPage({
  portfolio,
  onSaveAndNavigate,
}: StressTestPageProps) {
  const [scenario, setScenario] = useState<ScenarioKey>('EQUITY_SHOCK')
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  const isSimulationReady = Boolean(portfolio)

  function calculateResults(): StressTestInferenceResponseDto | null {
    if (!portfolio) return null

    const initialVal = portfolio.initialValue
    const shockFactors = SHOCKS[scenario].factors

    let impactRate = 0
    Object.entries(portfolio.assetWeights).forEach(([assetKey, weight]) => {
      const factor = shockFactors[assetKey] ?? -0.02
      impactRate += weight * factor
    })

    const postShockVal = Math.max(0, initialVal * (1 + impactRate))

    const portfolioResult = {
      initialValue: initialVal,
      expectedImpactRate: impactRate,
      postShockValue: postShockVal,
    }

    return {
      scenarioKey: scenario,
      currentPortfolioResult: portfolioResult,
      simulationPortfolioResult: portfolioResult,
      benchmarkPortfolioResult: {
        initialValue: initialVal,
        expectedImpactRate: impactRate * 0.95,
        postShockValue: Math.max(0, initialVal * (1 + impactRate * 0.95)),
      },
      llmComment: SHOCKS[scenario].comment,
    }
  }

  const result = isSimulationReady ? calculateResults() : null

  async function handleSaveDecision() {
    if (isSaving || !result) return

    setIsSaving(true)
    setSaveError(null)

    try {
      await onSaveAndNavigate(scenario)
    } catch {
      setSaveError('Kayıt başarısız, lütfen tekrar deneyin.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto font-ibm">
      {/* Header */}
      <div>
        <span className="text-[10px] font-extrabold uppercase tracking-widest text-[#c89834]">
          Ekran 05 · Varsayımsal Şok Testi
        </span>
        <h1 className="text-xl font-extrabold text-slate-900 tracking-tight mt-0.5">
          Stres Testi
        </h1>
        <p className="mt-1 text-xs text-slate-500 max-w-2xl">
          Aynı şok senaryosu mevcut portföy, Ekran 04'te oluşturulan simülasyon portföyü ve benchmark üzerine eşzamanlı uygulanır.
        </p>
      </div>

      {/* Karar Yoksa: Kilitli Banner (İçinde Buton Olmayan Versiyon) */}
      {!isSimulationReady ? (
        <div className="bg-[#fbf9f4] border border-[#f0eadd] rounded-2xl p-12 text-center space-y-3 shadow-sm my-8">
          <div className="w-12 h-12 bg-[#f5efe2] rounded-2xl flex items-center justify-center mx-auto text-[#b9862b]">
            <IoLockClosedOutline size={22} />
          </div>
          <div className="space-y-1">
            <h3 className="text-sm font-bold text-[#1c2530]">Stres Testi Kilitli</h3>
            <p className="text-xs text-slate-500 max-w-md mx-auto leading-relaxed">
              🔒 Stres testi, Ekran 04'teki simülasyon sonucundaki portföy ağırlıklarını kullanır — önce simülasyonu çalıştırın.
            </p>
          </div>
        </div>
      ) : (
        /* Karar Varsa: Senaryolar, Tablo ve LLM Yorumu */
        <div className="space-y-6">
          <ScenarioSelector
            selected={scenario}
            onSelect={(s) => setScenario(s)}
            disabled={isSaving}
          />

          {result && (
            <section className="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-sm space-y-4">
              <div>
                <h2 className="text-sm font-bold text-slate-900">
                  Senaryo Sonucu: {SCENARIO_TITLES[scenario]}
                </h2>
                <p className="text-[11px] text-slate-400 mt-0.5">
                  Şok, Ekran 04 simülasyon portföy değerleri üzerinden hesaplanmıştır
                </p>
              </div>

              <ResultsTable
                rows={[
                  { label: 'Mevcut Portföy', result: result.currentPortfolioResult },
                  { label: 'Simülasyon Portföyü', result: result.simulationPortfolioResult },
                  { label: 'Benchmark', result: result.benchmarkPortfolioResult },
                ]}
              />
            </section>
          )}

          {result && (
            <section className="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-sm">
              <LLMCommentSection comment={result.llmComment} />
            </section>
          )}

          {saveError && (
            <div className="text-right text-xs text-rose-600 font-semibold">
              {saveError}
            </div>
          )}

          {/* Karar Geçmişine Kaydet Butonu */}
          <div className="pt-2 flex justify-end">
            <button
              type="button"
              onClick={handleSaveDecision}
              disabled={isSaving}
              className="px-6 py-3 bg-[#12161f] hover:bg-slate-800 text-white rounded-xl text-xs font-bold transition-all shadow-sm disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
            >
              {isSaving ? (
                <>
                  <div className="h-3 w-3 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  <span>Kaydediliyor...</span>
                </>
              ) : (
                <span>Karar Geçmişine Kaydet →</span>
              )}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}