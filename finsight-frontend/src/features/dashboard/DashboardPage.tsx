import { useState, useEffect } from 'react'
import DashboardLayout from './components/DashboardLayout'
import AIRecommendationTab from './components/AIRecommendationTab'
import ManualScenarioTab from './components/ManualScenarioTab'
import { getActiveFund, getPendingRecommendation } from './dashboardApi'
import type { Fund, AIRecommendation } from './dashboardApi'

type MenuIndex = 1 | 2 | 3 | 4 | 5
type ActiveTab = 'ai' | 'manual'

export default function DashboardPage() {
  const [activeMenu, setActiveMenu] = useState<MenuIndex>(2) // Defaults to screen 2: AI Önerisi & Karar
  const [activeTab, setActiveTab] = useState<ActiveTab>('manual') // Screen shows manual scenario selected by default

  const [fund, setFund] = useState<Fund | null>(null)
  const [recommendation, setRecommendation] = useState<AIRecommendation | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  const [period, setPeriod] = useState<string>('30')

  async function loadData() {
    try {
      setLoading(true)
      setError(null)
      
      const activeFund = await getActiveFund()
      setFund(activeFund)

      const rec = await getPendingRecommendation(activeFund.id)
      setRecommendation(rec)
    } catch (err: any) {
      setError(err?.message || 'Veriler yüklenirken bir hata oluştu.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  // Refresh recommendation state after decision
  function handleDecisionSubmitted() {
    loadData()
  }

  // Refresh state after applying manual scenario
  function handleScenarioApplied() {
    loadData()
  }

  // Render placeholder content for other sidebar pages
  function renderPlaceholder(title: string, desc: string) {
    return (
      <div className="space-y-6 select-none animate-fade-in">
        {/* Breadcrumb / Küçük Başlık */}
        <div>
          <span className="text-[10px] font-bold tracking-wider text-[#c89834] uppercase block">
            Finsight · Karar Destek Platformu
          </span>
          <h2 className="text-3xl font-extrabold text-slate-800 mt-1">{title}</h2>
          <p className="text-xs text-slate-500 font-medium mt-1.5">{desc}</p>
        </div>

        {/* Placeholder Görsel / Premium Card */}
        <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm p-12 flex flex-col items-center justify-center text-center space-y-4">
          <div className="w-16 h-16 rounded-2xl bg-amber-50 border border-amber-200 flex items-center justify-center text-amber-600 shadow-sm">
            <span className="text-2xl font-bold font-serif">FI</span>
          </div>
          <div className="max-w-md">
            <h3 className="text-lg font-bold text-slate-800">
              Bu Panel Çok Yakında Hizmetinizde!
            </h3>
            <p className="text-sm text-slate-500 mt-2 leading-relaxed">
              Finsight Karar Destek Platformu'nun gelişmiş analitik modülleri üzerinde çalışmaya devam ediyoruz. Bu ekran kısa bir süre içinde canlı verilerle kullanıma açılacaktır.
            </p>
          </div>
        </div>
      </div>
    )
  }

  // Render active right-side page
  function renderMainContent() {
    if (loading) {
      return (
        <div className="flex flex-col items-center justify-center min-h-[50vh]">
          <div className="h-10 w-10 border-4 border-[#c89834] border-t-transparent rounded-full animate-spin mb-4" />
          <span className="text-slate-500 font-medium">Finsight yükleniyor...</span>
        </div>
      )
    }

    if (error || !fund || !recommendation) {
      return (
        <div className="bg-rose-50 border border-rose-100 rounded-2xl p-6 text-center max-w-xl mx-auto mt-12 space-y-4">
          <h3 className="text-lg font-bold text-rose-800">Sistem Bağlantı Hatası</h3>
          <p className="text-sm text-rose-700">{error || 'Veriler yüklenemedi. Lütfen tekrar deneyin.'}</p>
          <button
            onClick={loadData}
            className="px-5 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold uppercase select-none transition-all shadow-sm"
          >
            Yeniden Dene
          </button>
        </div>
      )
    }

    switch (activeMenu) {
      case 1:
        return renderPlaceholder(
          'Fon Dashboard',
          'TIE İş Portföy fonunun genel performans, getiri grafikleri ve varlık dağılım detayları.'
        )
      case 3:
        return renderPlaceholder(
          'Performans Karşılaştırması',
          'Seçilen fonun diğer yatırım araçları ve endekslerle karşılaştırmalı getiri grafikleri.'
        )
      case 4:
        return renderPlaceholder(
          'Stres Testi',
          'Portföyün faiz, kur, enflasyon ve piyasa şoklarına karşı stres dayanıklılık analizi.'
        )
      case 5:
        return renderPlaceholder(
          'Karar Geçmişi',
          'Yapay zeka önerileri ve uygulanan manuel simülasyon senaryolarının geçmiş kaydı.'
        )
      case 2:
      default:
        return (
          <div className="space-y-4 max-w-[1100px] animate-fade-in">
            {/* Breadcrumb / Üst Bilgi */}
            <div className="flex items-start justify-between">
              <div>
                <h2 className="text-xl font-bold text-slate-800 select-none tracking-tight">
                  AI Önerisi & Karar
                </h2>
              </div>

              {/* Karar Durum Etiketi */}
              <div className="flex flex-col items-end select-none text-right">
                <span
                  className="mt-0.5 px-3 py-1 rounded-md text-[9px] font-extrabold tracking-widest bg-slate-100/60 border border-slate-200 text-slate-500 uppercase"
                >
                  {recommendation.status === 'PENDING' && 'KARAR VERİLMEDİ'}
                  {recommendation.status === 'ACCEPTED' && 'KARAR UYGULANDI'}
                  {recommendation.status === 'REJECTED' && 'REDDEDİLDİ'}
                </span>
              </div>
            </div>

            {/* TAB SWITCHER */}
            <div className="flex items-center space-x-1.5 p-1 bg-slate-100 border border-slate-200/50 rounded-xl w-fit select-none">
              <button
                onClick={() => setActiveTab('ai')}
                className={`px-4.5 py-1.5 rounded-lg font-bold text-xs tracking-wider uppercase transition-all duration-200 cursor-pointer ${
                  activeTab === 'ai'
                    ? 'bg-white text-slate-800 shadow-md'
                    : 'text-slate-500 hover:text-slate-700'
                }`}
              >
                AI Önerisi
              </button>
              <button
                onClick={() => setActiveTab('manual')}
                className={`px-4.5 py-1.5 rounded-lg font-bold text-xs tracking-wider uppercase transition-all duration-200 cursor-pointer ${
                  activeTab === 'manual'
                    ? 'bg-white text-slate-800 shadow-md'
                    : 'text-slate-500 hover:text-slate-700'
                }`}
              >
                Manuel Senaryo
              </button>
            </div>

            {/* TAB CONTENT */}
            {activeTab === 'ai' ? (
              <AIRecommendationTab
                recommendation={recommendation}
                onDecisionSubmitted={handleDecisionSubmitted}
              />
            ) : (
              <ManualScenarioTab
                fund={fund}
                onScenarioApplied={handleScenarioApplied}
              />
            )}
          </div>
        )
    }
  }

  return (
    <DashboardLayout
      activeMenuIndex={activeMenu}
      onMenuChange={(idx) => {
        setActiveMenu(idx)
        // Reset default active tab when switching to screen 2
        if (idx === 2) {
          setActiveTab('manual')
        }
      }}
      fundDate={fund?.date}
      fundName={fund?.name}
      analysisPeriod={period}
      onPeriodChange={(newPeriod) => setPeriod(newPeriod)}
    >
      {renderMainContent()}
    </DashboardLayout>
  )
}
