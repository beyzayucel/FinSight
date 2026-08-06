import { useState, useEffect } from 'react'
import DashboardLayout from './components/DashboardLayout'
import ManualScenarioTab from './components/ManualScenarioTab'
import PerformanceComparisonPage from './pages/PerformanceComparisonPage'
import FundDashboardPage from './pages/FundDashboardPage'
import { getActiveFund } from './dashboardApi'
import type { Fund } from './dashboardApi'

type MenuIndex = 1 | 2 | 3 | 4 | 5

export default function DashboardPage() {
  const [activeMenu, setActiveMenu] = useState<MenuIndex>(2) // Defaults to screen 2: AI Önerisi & Karar

  const [fund, setFund] = useState<Fund | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  const [period, setPeriod] = useState<string>('30')

  async function loadData() {
    try {
      setLoading(true)
      setError(null)
      
      const activeFund = await getActiveFund()
      setFund(activeFund)
    } catch (err: any) {
      setError(err?.message || 'Veriler yüklenirken bir hata oluştu.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  // Manuel senaryo uygulanınca veriyi tazele ve sonucu görmesi için
  // kullanıcıyı Performans Karşılaştırması'na (menü 3) geçir.
  function handleScenarioApplied() {
    loadData()
    setActiveMenu(3)
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

    if (error || !fund) {
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
        return (
          <FundDashboardPage
            analysisPeriod={period}
            onGoToAiDecision={() => setActiveMenu(2)}
          />
        )
      case 3:
        return (
          <PerformanceComparisonPage
            onGoToManualScenario={() => setActiveMenu(2)}
            onGoToStressTest={() => setActiveMenu(4)}
          />
        )
      case 4:
        return renderPlaceholder(
          'Stres Testi',
          'Portföyün faiz, kur, enflasyon ve piyasa şoklarına karşı stres dayanıklılık analizi.'
        )
      case 5:
        return renderPlaceholder(
          'Karar Geçmişi',
          'Uygulanan manuel simülasyon senaryolarının geçmiş kaydı.'
        )
      case 2:
      default:
        return (
          <div className="space-y-4 max-w-[1100px] animate-fade-in">
            {/* Breadcrumb / Üst Bilgi */}
            <div className="flex items-start justify-between">
              <div>
                <h2 className="text-xl font-bold text-slate-800 select-none tracking-tight">
                  Manuel Senaryo Simülasyonu
                </h2>
              </div>
            </div>

            {/* TAB CONTENT */}
            <ManualScenarioTab
              fund={fund}
              onScenarioApplied={handleScenarioApplied}
            />
          </div>
        )
    }
  }

  return (
    <DashboardLayout
      activeMenuIndex={activeMenu}
      onMenuChange={(idx) => {
        setActiveMenu(idx)
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
