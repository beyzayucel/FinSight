import { useEffect, useState, useRef } from 'react'
import { IoLockClosedOutline } from 'react-icons/io5'
import { ScenarioSelector } from './ScenarioSelector'
import { ResultsTable } from './ResultsTable'
import { LLMCommentSection } from './LLMCommentSection'
import { getScenarioTitle, type PortfolioDataDto, type ScenarioKey, type StressTestInferenceResponseDto } from './types'
import { useDecision } from '@/features/dashboard/context/decisionStore'
import { runSimulation, getSimulationResultByPeriod } from './stressTestApi'
import { formatIsoDate } from '@/features/dashboard/lib/fund-dashboard/fundDashboardFormatters'

interface StressTestPageProps {
  portfolio: PortfolioDataDto | null
  analysisWindow?: number
  periodDate?: string
  loadingPeriod?: boolean
  onSaveAndNavigate: (stressTestResultId: string) => Promise<void>
}

type ViewMode = 'IDLE' | 'LIVE' | 'HISTORICAL'

export default function StressTestPage({
  portfolio,
  analysisWindow = 30,
  periodDate,
  loadingPeriod,
  onSaveAndNavigate,
}: StressTestPageProps) {
  const { activeFund } = useDecision()

  const [scenario, setScenario] = useState<ScenarioKey>('EQUITY_SHOCK')
  const [viewMode, setViewMode] = useState<ViewMode>('IDLE')
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  const [result, setResult] = useState<StressTestInferenceResponseDto | null>(null)
  const [loading, setLoading] = useState<boolean>(false)

  const isSimulationReady = Boolean(portfolio)

  // Sayfa ilk yüklendiğinde useEffect'in otomatik çalışmasını engellemek için ref kullanıyoruz
  const isFirstRender = useRef(true)

  // 1. SOL ÜST ANALİZ DÖNEMİ DEĞİŞTİĞİNDE (Kullanıcı dropdown'dan gün seçtiğinde GEÇMİŞ TESTİ ÇEK)
  useEffect(() => {
    // İlk render'da çalıştırma — Kullanıcı tıklamadan otomatik veri gelmesin!
    if (isFirstRender.current) {
      isFirstRender.current = false
      return
    }

    if (!activeFund?.code) return

    let isMounted = true
    setLoading(true)
    setViewMode('HISTORICAL') // Modu Geçmiş Veri moduna al

    getSimulationResultByPeriod(activeFund.code, analysisWindow)
      .then((historicalData) => {
        console.log("===> BACKEND'DEN GELEN APIGELEN DATA:", historicalData);
        if (isMounted) {
          setResult(historicalData) // DB'deki o güne ait test sonucunu basar
        }
      })
      .catch((err) => {
        console.warn('Geçmiş stres testi sonucu bulunamadı:', err)
        if (isMounted) setResult(null)
      })
      .finally(() => {
        if (isMounted) setLoading(false)
      })

    return () => {
      isMounted = false
    }
  }, [activeFund?.code, analysisWindow])

  // 2. SENARYO KARTLARINA TIKLANDIĞINDA (Kullanıcı karta bastığında CANLI HESAPLAMA YAP)
  const handleSelectScenario = async (selectedScenario: ScenarioKey) => {
    setScenario(selectedScenario)

    if (!isSimulationReady || !portfolio || !activeFund?.code) return

    setLoading(true)
    setSaveError(null)
    setViewMode('LIVE') // Modu Canlı Test moduna al

    try {
      // Güncel portföy verisiyle canlı simülasyonu çalıştırır
      const data = await runSimulation(activeFund.code, selectedScenario, portfolio, analysisWindow)
      setResult(data)
    } catch (err) {
      console.error('Stres testi canlı simülasyon hatası:', err)
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
          Stres Testi {viewMode === 'HISTORICAL' ? `(Son ${analysisWindow} Günlük Dönem Analizi)` : ''}
        </h1>
        <p className="mt-1 text-xs text-slate-500 max-w-2xl">
          Seçili analiz dönemi (Son {analysisWindow} gün{periodDate ? ` · ${formatIsoDate(periodDate)}'den beri` : ''}) baz alınarak, aynı şok senaryosu mevcut portföy, Ekran 04'te oluşturulan simülasyon portföyü ve benchmark üzerine eşzamanlı uygulanır.
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
            disabled={isSaving || loading || loadingPeriod}
          />

          {/* Durum Gösterimi */}
          {loading || loadingPeriod ? (
            <div className="flex justify-center items-center p-12 bg-white rounded-2xl border border-slate-200/80 shadow-sm">
              <div className="h-5 w-5 border-2 border-[#c89834] border-t-transparent rounded-full animate-spin mr-3" />
              <span className="text-xs text-slate-600 font-semibold tracking-wide">
                {viewMode === 'HISTORICAL'
                  ? `Seçili döneme (Son ${analysisWindow} Gün) ait analiz verileri yükleniyor...`
                  : `${getScenarioTitle(scenario)} senaryosu canlı hesaplanıyor...`}
              </span>
            </div>
          ) : result ? (
            <>
              <section className="rounded-2xl border border-slate-200/80 bg-white p-6 shadow-sm space-y-4">
                <div>
                  <div className="flex items-center justify-between">
                    <h2 className="text-sm font-bold text-slate-900">
                      Senaryo Sonucu: {getScenarioTitle(scenario)}
                    </h2>
                    {/* Hangi modda olunduğunu gösteren Rozet (Badge) */}
                    <span
                      className={`text-[10px] px-3 py-1 rounded-full font-extrabold uppercase tracking-wider ${
                        viewMode === 'HISTORICAL'
                          ? 'bg-amber-50 text-[#c89834] border border-[#c89834]/30'
                          : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                      }`}
                    >
                      {viewMode === 'HISTORICAL' ? `Dönemsel Kayıt · Son ${analysisWindow} Gün` : 'Canlı Hesaplama'}
                    </span>
                  </div>
                  <p className="text-[11px] text-slate-500 mt-1">
                    {viewMode === 'HISTORICAL'
                      ? `Şok testi, seçilen ${analysisWindow} günlük dönem başlangıcındaki portföy değerleri üzerinden hesaplanmıştır.`
                      : 'Şok testi, Ekran 04 simülasyon portföyünün güncel değerleri üzerinden canlı hesaplanmıştır.'}
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
            /* Ürün Seviyesinde Bilgilendirme Kutusu */
            <div className="p-10 text-center bg-white rounded-2xl border border-slate-200/80 shadow-sm space-y-2">
              <div className="text-xs font-bold text-slate-800">
                {viewMode === 'HISTORICAL'
                  ? `Son ${analysisWindow} Günlük Döneme Ait Kayıt Bulunamadı`
                  : 'Stres Testi Simülasyonu'}
              </div>
              <p className="text-xs text-slate-500 max-w-lg mx-auto leading-relaxed">
                {viewMode === 'HISTORICAL'
                  ? `Seçilen analiz dönemi (Son ${analysisWindow} Gün) için veritabanında önceden kaydedilmiş bir şok testi bulunmamaktadır. Canlı hesaplama yapmak için yukarıdaki senaryo kartlarından birini seçebilirsiniz.`
                  : 'Varsayımsal şok senaryolarını çalıştırmak için yukarıdaki senaryo kartlarından birini seçebilir veya sol menüden analiz dönemini değiştirebilirsiniz.'}
              </p>
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
              className="px-6 py-3 bg-[#12161f] hover:bg-slate-800 text-white rounded-xl text-xs font-bold transition-all shadow-sm disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2 cursor-pointer"
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
