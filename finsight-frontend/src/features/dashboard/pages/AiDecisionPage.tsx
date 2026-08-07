import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import ManualScenarioTab from '../components/ManualScenarioTab'
import AIRecommendationTab from '../components/AIRecommendationTab'
import { useDashboardOutlet } from '../DashboardShell'
import { ROUTES } from '@/lib/routes'
import { getPendingRecommendation } from '../dashboardApi'
import type { AIRecommendation } from '../dashboardApi'

export default function AiDecisionPage() {
  const navigate = useNavigate()
  const { fund, reloadFund } = useDashboardOutlet()

  const [activeTab, setActiveTab] = useState<'ai' | 'manual'>('ai')
  const [recommendation, setRecommendation] = useState<AIRecommendation | null>(null)
  const [recLoading, setRecLoading] = useState<boolean>(true)
  const [recError, setRecError] = useState<string | null>(null)

  async function loadRecommendation() {
    if (!fund?.id) return
    try {
      setRecLoading(true)
      setRecError(null)
      const data = await getPendingRecommendation(fund.id)
      setRecommendation(data)
    } catch (err: any) {
      setRecError(err?.message || 'AI önerisi yüklenirken bir hata oluştu.')
    } finally {
      setRecLoading(false)
    }
  }

  useEffect(() => {
    loadRecommendation()
  }, [fund?.id])

  // Manuel senaryo uygulanınca veriyi tazele ve sonucu görmesi için
  // kullanıcıyı Performans Karşılaştırması'na geçir.
  function handleScenarioApplied() {
    reloadFund()
    navigate(ROUTES.FUND_PERFORMANCE)
  }

  function handleDecisionSubmitted() {
    reloadFund()
    loadRecommendation()
  }

  return (
    <div className="space-y-6 animate-fade-in select-none">
      {/* Breadcrumb / Üst Bilgi */}
      <div className="flex items-center justify-between">
        <div>
          <span className="text-[10px] font-bold tracking-wider text-[#c89834] uppercase block">
            Finsight · Karar Destek Platformu
          </span>
          <h2 className="text-3xl font-extrabold text-slate-800 mt-1">AI Önerisi & Karar</h2>
          <p className="text-xs text-slate-500 font-medium mt-1.5">
            AI tarafından üretilen dağılım önerilerini inceleyin veya kendi senaryonuzu simüle edin.
          </p>
        </div>
        <div className="flex-shrink-0">
          {recommendation?.status === 'ACCEPTED' ? (
            <span className="inline-flex items-center px-3.5 py-1.5 rounded-full border border-emerald-250 bg-emerald-50 text-[9.5px] font-extrabold tracking-wider text-emerald-700 uppercase select-none shadow-sm">
              KABUL EDİLDİ
            </span>
          ) : recommendation?.status === 'REJECTED' ? (
            <span className="inline-flex items-center px-3.5 py-1.5 rounded-full border border-rose-250 bg-rose-50 text-[9.5px] font-extrabold tracking-wider text-rose-700 uppercase select-none shadow-sm">
              REDDEDİLDİ
            </span>
          ) : (
            <span className="inline-flex items-center px-3.5 py-1.5 rounded-full border border-slate-250 bg-slate-100 text-[9.5px] font-extrabold tracking-wider text-slate-600 uppercase select-none shadow-sm">
              KARAR VERİLMEDİ
            </span>
          )}
        </div>
      </div>

      {/* Tab Switcher */}
      <div className="flex space-x-1 bg-slate-100/80 p-1.5 rounded-xl max-w-[320px] border border-slate-200/50">
        <button
          onClick={() => setActiveTab('ai')}
          className={`flex-1 py-2 text-xs font-extrabold uppercase tracking-wider rounded-lg transition-all duration-200 select-none cursor-pointer ${
            activeTab === 'ai'
              ? 'bg-white text-slate-800 shadow-sm border border-slate-200/40'
              : 'text-slate-400 hover:text-slate-700'
          }`}
        >
          AI Önerisi
        </button>
        <button
          onClick={() => setActiveTab('manual')}
          className={`flex-1 py-2 text-xs font-extrabold uppercase tracking-wider rounded-lg transition-all duration-200 select-none cursor-pointer ${
            activeTab === 'manual'
              ? 'bg-white text-slate-800 shadow-sm border border-slate-200/40'
              : 'text-slate-400 hover:text-slate-700'
          }`}
        >
          Manuel Senaryo
        </button>
      </div>

      {/* TAB CONTENT */}
      {activeTab === 'ai' ? (
        recError ? (
          <div className="bg-rose-50 border border-rose-100 rounded-2xl p-6 text-center max-w-xl mx-auto mt-6 space-y-4">
            <h3 className="text-lg font-bold text-rose-800">Öneri Yüklenemedi</h3>
            <p className="text-sm text-rose-700">{recError}</p>
            <button
              onClick={loadRecommendation}
              className="px-5 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold uppercase select-none transition-all shadow-sm cursor-pointer"
            >
              Yeniden Dene
            </button>
          </div>
        ) : (
          <AIRecommendationTab
            recommendation={recommendation!}
            isLoading={recLoading || !recommendation}
            onDecisionSubmitted={handleDecisionSubmitted}
          />
        )
      ) : (
        <ManualScenarioTab fund={fund} onScenarioApplied={handleScenarioApplied} />
      )}
    </div>
  )
}
