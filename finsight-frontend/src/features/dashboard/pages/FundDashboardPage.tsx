import { useEffect, useMemo, useState } from 'react'
import {
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { IoClose, IoSyncOutline } from 'react-icons/io5'
import { getFundDashboard } from '../fundDashboardApi'
import type { FundDashboard, FundDashboardPeriod, StockBreakdownItem } from '../fundDashboardApi'
import { formatCurrency, formatSignedPercent } from '../lib/formatters'

type FundDashboardPageProps = {
  /** Sidebar'daki Analiz Dönemi değeri: '10' | '20' | '30' | '90' */
  analysisPeriod: string
  onGoToAiDecision: () => void
}

// Backend kategori adını doğrudan Türkçe döner; renkler ada göre eşlenir,
// tanınmayan kategoriler sıradaki yedek rengi alır.
const CATEGORY_COLORS: Record<string, string> = {
  'Hisse Senedi': '#1c2530',
  'Ters-Repo': '#c89834',
  'Vadeli İşl. Nakit Teminatı': '#8fa3bf',
  'Yatırım Fonu Katılma Payı': '#d9cfb8',
}
const FALLBACK_COLORS = ['#1c2530', '#c89834', '#8fa3bf', '#d9cfb8', '#6b7683']

const MONTHS_TR = [
  'Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran',
  'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık',
]

function formatIsoDate(iso: string): string {
  const [y, m, d] = iso.split('-')
  return `${d}.${m}.${y}`
}

/** "2026-06" → "Haziran 2026" */
function formatMonthPeriod(period: string): string {
  const [y, m] = period.split('-')
  const monthName = MONTHS_TR[Number(m) - 1]
  return monthName ? `${monthName} ${y}` : period
}

/** "P10D" → 10 (nominal takvim dönemi; `days` işlem günüdür) */
function nominalDays(code: string): number {
  return Number(code.replace(/\D/g, '')) || 0
}

function formatBps(value: number): string {
  const rounded = Math.round(value)
  return `${rounded > 0 ? '+' : ''}${rounded} bps`
}

function daysSince(iso: string): number {
  const [y, m, d] = iso.split('-').map(Number)
  const then = new Date(y, m - 1, d)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  return Math.round((today.getTime() - then.getTime()) / 86_400_000)
}

function formatAxisDate(iso: string): string {
  const [, m, d] = iso.split('-')
  return `${d}.${m}`
}

function formatIndexChange(index: number): string {
  return formatSignedPercent(index - 100)
}

function StockBreakdownModal({
  period,
  items,
  onClose,
}: {
  period: string
  items: StockBreakdownItem[]
  onClose: () => void
}) {
  const maxWeight = Math.max(...items.map((i) => i.weight), 1)

  useEffect(() => {
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', onKeyDown)
    return () => document.removeEventListener('keydown', onKeyDown)
  }, [onClose])

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="stock-breakdown-title"
        className="w-full max-w-lg bg-white rounded-2xl shadow-2xl p-6 space-y-4 animate-fade-in"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between">
          <div>
            <h3 id="stock-breakdown-title" className="text-lg font-bold text-slate-800">
              Hisse Senedi Alt Kırılımı
            </h3>
            <p className="text-[11px] text-slate-500 font-medium mt-1 leading-relaxed">
              Kaynak: KAP Aylık Portföy Açıklaması · {formatMonthPeriod(period)} · Analiz
              Dönemi'nden bağımsız (aylık güncellenir)
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600 transition-colors outline-none"
            aria-label="Kapat"
          >
            <IoClose size={22} />
          </button>
        </div>

        <div className="space-y-2.5">
          {items.map((item) => {
            const isOthers = item.assetCode === 'Others'
            return (
              <div key={item.assetCode} className="flex items-center gap-3">
                <span className="w-16 flex-shrink-0 text-xs font-bold font-mono text-slate-700">
                  {isOthers ? '+ Diğer' : item.assetCode}
                </span>
                <div className="flex-1 h-2 rounded-full bg-slate-100 overflow-hidden">
                  <div
                    className={`h-full rounded-full ${isOthers ? 'bg-slate-400' : 'bg-[#c89834]'}`}
                    style={{ width: `${(item.weight / maxWeight) * 100}%` }}
                  />
                </div>
                <span className="w-16 flex-shrink-0 text-right text-xs font-semibold font-mono text-slate-700">
                  %{item.weight.toFixed(2).replace('.', ',')}
                </span>
              </div>
            )
          })}
        </div>
      </div>
    </div>
  )
}

export default function FundDashboardPage({ analysisPeriod, onGoToAiDecision }: FundDashboardPageProps) {
  const [data, setData] = useState<FundDashboard | null>(null)
  const [loading, setLoading] = useState<boolean>(true)
  const [error, setError] = useState<string | null>(null)

  const [flipped, setFlipped] = useState(false)
  const [breakdownOpen, setBreakdownOpen] = useState(false)

  async function loadData() {
    try {
      setLoading(true)
      setError(null)
      const dashboard = await getFundDashboard('TIE')
      setData(dashboard)
    } catch (err: any) {
      setError(
        err?.response?.status === 404
          ? 'Fon verisi henüz senkronize edilmemiş. Lütfen daha sonra tekrar deneyin.'
          : err?.message || 'Fon dashboard verisi yüklenirken bir hata oluştu.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  // Sidebar '30' → payload'daki P30D. Dönem değişimi ağa çıkmaz, hepsi payload'da.
  const period: FundDashboardPeriod | null = useMemo(() => {
    if (!data || data.periods.length === 0) return null
    return data.periods.find((p) => p.code === `P${analysisPeriod}D`) ?? data.periods[0]
  }, [data, analysisPeriod])

  const benchmarkSeries = period?.series ?? []

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh]">
        <div className="h-10 w-10 border-4 border-[#c89834] border-t-transparent rounded-full animate-spin mb-4" />
        <span className="text-slate-500 font-medium">Fon verileri yükleniyor...</span>
      </div>
    )
  }

  if (error || !data || !period) {
    return (
      <div className="bg-rose-50 border border-rose-100 rounded-2xl p-6 text-center max-w-xl mx-auto mt-12 space-y-4">
        <h3 className="text-lg font-bold text-rose-800">Fon Verisi Alınamadı</h3>
        <p className="text-sm text-rose-700">
          {error || 'Fon dashboard verisi bulunamadı. Lütfen tekrar deneyin.'}
        </p>
        <button
          onClick={loadData}
          className="px-5 py-2.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl text-xs font-bold uppercase select-none transition-all shadow-sm"
        >
          Yeniden Dene
        </button>
      </div>
    )
  }

  const periodDays = nominalDays(period.code)
  const changePositive = period.change >= 0
  const dailyPositive = data.dailyReturn >= 0
  const benchAbove = period.benchmarkDiffBps >= 0
  const cumulativePositive = period.cumulativeReturn >= 0
  const dataAge = daysSince(data.fund.dataDate)
  // Fon NAV'ı bir gün gecikmeli; hafta sonuyla birlikte 3 güne kadar normal.
  const dataStale = dataAge > 3

  return (
    <div className="space-y-5 animate-fade-in">
      <h2 className="text-3xl font-bold font-mono text-slate-800 select-none tracking-tight">
        Fon Dashboard
      </h2>

      {/* ---------- KPI KARTLARI ---------- */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {/* Toplam Portföy Değeri — çevrilebilir kart */}
        <button
          type="button"
          onClick={() => setFlipped((f) => !f)}
          aria-pressed={flipped}
          aria-label={
            flipped
              ? 'Güncel portföy değerine dön'
              : `${periodDays} gün önceki portföy değerini gör`
          }
          className="min-h-[160px] w-full text-left cursor-pointer select-none [perspective:1200px] rounded-2xl outline-none focus-visible:ring-2 focus-visible:ring-[#c89834] focus-visible:ring-offset-2"
        >
          <div
            className={`relative h-full w-full transition-transform duration-500 [transform-style:preserve-3d] ${
              flipped ? '[transform:rotateY(180deg)]' : ''
            }`}
          >
            {/* Ön yüz: güncel değer */}
            <div className="absolute inset-0 [backface-visibility:hidden] bg-white rounded-2xl border border-slate-200/80 shadow-sm p-5 flex flex-col justify-between">
              <div className="space-y-2">
                <span className="text-[10px] font-bold tracking-wider text-slate-500 uppercase block">
                  Toplam Portföy Değeri
                </span>
                <div className="text-[clamp(17px,1.2vw+7px,26px)] leading-tight tracking-tight font-bold font-mono text-slate-800 whitespace-nowrap">
                  {formatCurrency(data.totalValue)}
                </div>
                <span
                  className={`inline-block text-[10px] font-semibold font-mono rounded-md px-2 py-1 ${
                    dataStale ? 'bg-amber-100 text-amber-800' : 'text-slate-600 bg-slate-100'
                  }`}
                >
                  Veri Tarihi: {formatIsoDate(data.fund.dataDate)}
                  {dataAge > 0 && ` · ${dataAge} gün önce`}
                </span>
              </div>
              <span className="flex items-center gap-1.5 text-[10px] text-slate-400 font-medium">
                <IoSyncOutline size={12} />
                {periodDays} gün önceki değeri görmek için çevir
              </span>
            </div>

            {/* Arka yüz: seçili dönemin başlangıç değeri */}
            <div className="absolute inset-0 [backface-visibility:hidden] [transform:rotateY(180deg)] bg-amber-50 rounded-2xl border border-amber-200 shadow-sm p-5 flex flex-col justify-between">
              <div className="space-y-2">
                <span className="text-[10px] font-bold tracking-wider text-amber-800/80 uppercase block">
                  {formatIsoDate(period.previousDate)} ({periodDays} gün önce · {period.days} işlem
                  günü)
                </span>
                <div className="text-[clamp(17px,1.2vw+7px,26px)] leading-tight tracking-tight font-bold font-mono text-slate-800 whitespace-nowrap">
                  {formatCurrency(period.previousTotalValue)}
                </div>
                <span
                  className={`inline-block text-[10px] font-semibold font-mono rounded-md px-2 py-1 ${
                    changePositive ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'
                  }`}
                >
                  {changePositive ? '+' : '-'}
                  {formatCurrency(Math.abs(period.change))} (
                  {formatSignedPercent(period.changePercent)})
                </span>
                <span className="block text-[9px] font-medium text-amber-700/70 leading-snug">
                  Büyüklük değişimi — yatırımcı giriş/çıkışları dahil, fon getirisi değildir
                </span>
              </div>
              <span className="flex items-center gap-1.5 text-[10px] text-amber-700/70 font-medium">
                <IoSyncOutline size={12} />
                bugüne dönmek için çevir
              </span>
            </div>
          </div>
        </button>

        {/* Günlük Getiri */}
        <div className="min-h-[160px] bg-white rounded-2xl border border-slate-200/80 shadow-sm p-5 flex flex-col justify-start space-y-2 select-none">
          <span className="text-[10px] font-bold tracking-wider text-slate-500 uppercase block">
            Günlük Getiri
          </span>
          <div className="text-[clamp(17px,1.2vw+7px,26px)] leading-tight tracking-tight font-bold font-mono text-slate-800 whitespace-nowrap">
            {formatSignedPercent(data.dailyReturn)}
          </div>
          <span
            className={`inline-flex w-fit items-center gap-1 text-[10px] font-semibold font-mono rounded-md px-2 py-1 ${
              dailyPositive ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-600'
            }`}
          >
            {dailyPositive ? '▲ pozitif' : '▼ negatif'}
          </span>
        </div>

        {/* Birikimli Getiri */}
        <div className="min-h-[160px] bg-white rounded-2xl border border-slate-200/80 shadow-sm p-5 flex flex-col justify-start space-y-2 select-none">
          <span className="text-[10px] font-bold tracking-wider text-slate-500 uppercase block">
            Birikimli Getiri
          </span>
          <div className="text-[clamp(17px,1.2vw+7px,26px)] leading-tight tracking-tight font-bold font-mono text-slate-800 whitespace-nowrap">
            {formatSignedPercent(period.cumulativeReturn)}
          </div>
          <span
            className={`inline-flex w-fit items-center gap-1 text-[10px] font-semibold font-mono rounded-md px-2 py-1 ${
              cumulativePositive ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-600'
            }`}
          >
            {cumulativePositive ? '▲' : '▼'} son {periodDays} gün · {period.days} işlem günü
          </span>
        </div>

        {/* Benchmark Farkı */}
        <div className="min-h-[160px] bg-white rounded-2xl border border-slate-200/80 shadow-sm p-5 flex flex-col justify-start space-y-2 select-none">
          <span className="text-[10px] font-bold tracking-wider text-slate-500 uppercase block">
            Benchmark Farkı
          </span>
          <div className="text-[clamp(17px,1.2vw+7px,26px)] leading-tight tracking-tight font-bold font-mono text-slate-800 whitespace-nowrap">
            {formatBps(period.benchmarkDiffBps)}
          </div>
          <span
            className={`inline-flex w-fit items-center gap-1 text-[10px] font-semibold font-mono rounded-md px-2 py-1 ${
              benchAbove ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-600'
            }`}
          >
            {benchAbove ? '▲ benchmark üstü' : '▼ benchmark altı'} ·{' '}
            {formatSignedPercent(period.benchmarkDiffBps / 100)}
          </span>
        </div>
      </div>

      {/* ---------- ORTA SIRA: DAĞILIM + BENCHMARK ---------- */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4">
        {/* Portföy Dağılımı */}
        <div className="lg:col-span-5 bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 flex flex-col">
          <div>
            <h3 className="text-base font-bold text-slate-800">Portföy Dağılımı</h3>
            <p className="text-[11px] text-slate-500 font-medium mt-0.5">
              {data.distribution.length} yatırım kategorisi · güncel ağırlıklar
            </p>
          </div>

          <div className="flex-1 flex flex-col sm:flex-row items-center gap-6 py-6">
            <div className="w-[170px] h-[170px] flex-shrink-0">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={data.distribution}
                    dataKey="weight"
                    nameKey="category"
                    innerRadius={52}
                    outerRadius={80}
                    startAngle={90}
                    endAngle={-270}
                    stroke="none"
                    isAnimationActive={false}
                  >
                    {data.distribution.map((item, i) => (
                      <Cell
                        key={item.category}
                        fill={CATEGORY_COLORS[item.category] ?? FALLBACK_COLORS[i % FALLBACK_COLORS.length]}
                      />
                    ))}
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            </div>

            <div className="space-y-3 w-full">
              {data.distribution.map((item, i) => (
                <div key={item.category} className="flex items-center gap-2.5 text-xs">
                  <span
                    className="w-3 h-3 rounded-full flex-shrink-0"
                    style={{
                      backgroundColor:
                        CATEGORY_COLORS[item.category] ?? FALLBACK_COLORS[i % FALLBACK_COLORS.length],
                    }}
                  />
                  <span className="font-semibold text-slate-700">{item.category}</span>
                  <span className="text-slate-400">·</span>
                  <span className="font-bold font-mono text-slate-800">
                    {item.weight.toFixed(2).replace('.', ',')}%
                  </span>
                </div>
              ))}
            </div>
          </div>

          <button
            onClick={() => setBreakdownOpen(true)}
            className="self-start text-xs font-semibold text-[#c89834] underline underline-offset-4 hover:text-[#a87e2a] transition-colors outline-none select-none"
          >
            Hisse Senedi alt kırılımını gör →
          </button>
        </div>

        {/* Benchmark Karşılaştırması */}
        <div className="lg:col-span-7 bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 flex flex-col">
          <div>
            <h3 className="text-base font-bold text-slate-800">Benchmark Karşılaştırması</h3>
            <p className="text-[11px] text-slate-500 font-medium mt-0.5">
              Fon getirisi vs. karma benchmark · son {periodDays} gün ({period.days} işlem günü) ·{' '}
              {formatIsoDate(period.previousDate)} = 100
            </p>
          </div>

          {benchmarkSeries.length === 0 ? (
            <div className="flex-1 min-h-[200px] flex items-center justify-center">
              <p className="max-w-[260px] text-center text-xs text-slate-400 font-medium leading-relaxed">
                Bu dönem için benchmark serisi henüz oluşmamış. Fon senkronizasyonu çalıştığında
                grafik dolacak.
              </p>
            </div>
          ) : (
            <>
              <div className="flex items-center gap-5 mt-4">
                <span className="flex items-center gap-2 text-[11px] font-medium text-slate-500">
                  <span className="inline-block w-5 border-t-2 border-[#c89834]" /> Fon
                </span>
                <span className="flex items-center gap-2 text-[11px] font-medium text-slate-500">
                  <span className="inline-block w-5 border-t-2 border-[#1c2530]" /> Benchmark
                </span>
              </div>

              <div className="flex-1 min-h-[200px] mt-3">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={benchmarkSeries} margin={{ top: 8, right: 8, left: 8, bottom: 8 }}>
                    <CartesianGrid vertical={false} stroke="#e5e7eb" />
                    <XAxis
                      dataKey="date"
                      tickFormatter={formatAxisDate}
                      tick={{ fontSize: 10, fill: '#94a3b8' }}
                      tickLine={false}
                      axisLine={false}
                      minTickGap={28}
                    />
                    <YAxis hide domain={['dataMin', 'dataMax']} />
                    <Tooltip
                      labelFormatter={(label) =>
                        `${formatIsoDate(String(label))} · ${formatIsoDate(period.previousDate)}'dan beri`
                      }
                      formatter={(value, name) => [
                        formatIndexChange(Number(value)),
                        name === 'fund' ? 'Fon' : 'Benchmark',
                      ]}
                      contentStyle={{
                        borderRadius: '0.75rem',
                        border: '1px solid #e2e8f0',
                        fontSize: '11px',
                      }}
                    />
                    <Line
                      type="monotone"
                      dataKey="fund"
                      stroke="#c89834"
                      strokeWidth={2.5}
                      dot={false}
                      isAnimationActive={false}
                    />
                    <Line
                      type="monotone"
                      dataKey="benchmark"
                      stroke="#1c2530"
                      strokeWidth={2}
                      dot={false}
                      isAnimationActive={false}
                      connectNulls
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </>
          )}
        </div>
      </div>

      {/* ---------- SON AI ÖNERİSİ ---------- */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 space-y-4">
        <h3 className="text-base font-bold font-mono text-slate-800">Son AI Önerisi</h3>
        <div className="flex flex-col sm:flex-row sm:items-center gap-4">
          <span className="flex-shrink-0 inline-flex items-center px-3.5 py-1.5 rounded-full border border-slate-200 bg-slate-50 text-[10px] font-bold tracking-wider text-slate-500 uppercase select-none">
            Bekliyor
          </span>
          <p className="flex-1 text-sm text-slate-500 leading-relaxed">
            Portföyünüz için güncel verilere göre üretilmiş bir ağırlık önerisini inceleyin,
            gerekçesini okuyun ve kabul/red kararınızı verin.
          </p>
          <button
            onClick={onGoToAiDecision}
            className="flex-shrink-0 px-5 py-2.5 bg-[#c89834] hover:bg-[#b3872e] text-white rounded-xl text-sm font-bold select-none transition-all shadow-sm"
          >
            AI Önerisini İncele →
          </button>
        </div>
      </div>

      {breakdownOpen && (
        <StockBreakdownModal
          period={data.stockBreakdown.period}
          items={data.stockBreakdown.items}
          onClose={() => setBreakdownOpen(false)}
        />
      )}
    </div>
  )
}
