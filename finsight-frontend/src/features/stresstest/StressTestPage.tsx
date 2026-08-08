import { useState } from 'react'
import { IoLockClosedOutline } from 'react-icons/io5'
import { ScenarioSelector } from './ScenarioSelector'
import { ResultsTable } from './ResultsTable'
import { LLMCommentSection } from './LLMCommentSection'
import { SCENARIO_TITLES, type PortfolioDataDto, type ScenarioKey, type StressTestInferenceResponseDto } from './types'
import { useDecision } from '@/features/dashboard/context/decisionStore'
import { runSimulation } from './stressTestApi'

interface StressTestPageProps {
  portfolio: PortfolioDataDto | null
  /** /run sırasında kaydedilen stres testi sonucunun id'si — karara bu id ile iliştirilir. */
  onSaveAndNavigate: (stressTestResultId: string) => Promise<void>
}
export default function StressTestPage({
  portfolio,
  onSaveAndNavigate,
}: StressTestPageProps) {
  const { activeFund } = useDecision()

  const [scenario, setScenario] = useState<ScenarioKey>('EQUITY_SHOCK')
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  const [result, setResult] = useState<StressTestInferenceResponseDto | null>(null)
  const [loading, setLoading] = useState<boolean>(false)

  const isSimulationReady = Boolean(portfolio)

  // SENARYO KARTINA TIKLANDIĞINDA ANINDA CANLI HESAPLAMA YAPAR
  const handleSelectScenario = async (selectedScenario: ScenarioKey) => {
    setScenario(selectedScenario)

    if (!isSimulationReady || !portfolio) return

    // Backend fon kodunu da UUID'yi de çözebiliyor (findFund), burada kod yeterli.
    const fundParam = activeFund.code

    setLoading(true)
    setSaveError(null)

    try {
      // Doğrudan güncel verilerle live simülasyonu çalıştırır
      const data = await runSimulation(fundParam, selectedScenario, portfolio)
      setResult(data)
    } catch (err) {
      console.error('Stres testi simülasyon hatası:', err)
      setResult(null)
    } finally {
      setLoading(false)
    }
  }

  async function handleSaveDecision() {
    if (isSaving || !result) return

    setIsSaving(true)
    setSaveError(null)

    try {
      await onSaveAndNavigate(result.id)
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
        <div className="space-y-6">
          {/* Senaryo Seçici */}
          <ScenarioSelector
            selected={scenario}
            onSelect={handleSelectScenario}
            disabled={isSaving || loading}
          />

          {/* Durum Gösterimi */}
          {loading ? (
            <div className="flex justify-center items-center p-12 bg-white rounded-2xl border border-slate-200">
              <div className="h-5 w-5 border-2 border-[#c89834] border-t-transparent rounded-full animate-spin mr-3" />
              <span className="text-xs text-slate-600 font-medium">
                {SCENARIO_TITLES[scenario]} simülasyonu güncel verilerle hesaplanıyor...
              </span>
            </div>
          ) : result ? (
            <>
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

              <section className="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-sm">
                <LLMCommentSection comment={result.llmComment} />
              </section>
            </>
          ) : (
            <div className="p-8 text-center text-xs text-slate-500 bg-white rounded-2xl border border-slate-200">
              Hesaplama yapmak için yukarıdan bir senaryo kartına tıklayın.
            </div>
          )}

          {saveError && (
            <div className="text-right text-xs text-rose-600 font-semibold">
              {saveError}
            </div>
          )}

          <div className="pt-2 flex justify-end">
            <button
              type="button"
              onClick={handleSaveDecision}
              disabled={isSaving || loading || !result}
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