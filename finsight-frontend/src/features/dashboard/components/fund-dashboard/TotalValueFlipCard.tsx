import { useState } from 'react'
import { IoSyncOutline } from 'react-icons/io5'
import type { FundDashboardPeriod } from '../../lib/fund-dashboard/fundDashboardApi'
import { formatIsoDate } from '../../lib/fund-dashboard/fundDashboardFormatters'
import { formatCurrency, formatSignedPercent } from '../../lib/formatters'

type TotalValueFlipCardProps = {
  totalValue: number
  dataDate: string
  period: FundDashboardPeriod
  periodDays: number
}

export default function TotalValueFlipCard({
  totalValue,
  dataDate,
  period,
  periodDays,
}: TotalValueFlipCardProps) {
  const [flipped, setFlipped] = useState(false)
  const changePositive = period.change >= 0

  return (
    <button
      type="button"
      onClick={() => setFlipped((f) => !f)}
      aria-pressed={flipped}
      aria-label={
        flipped ? 'Güncel portföy değerine dön' : `${periodDays} gün önceki portföy değerini gör`
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
              {formatCurrency(totalValue)}
            </div>
            <span className="inline-block text-[10px] font-semibold font-mono rounded-md px-2 py-1 text-slate-600 bg-slate-100">
              Veri Tarihi: {formatIsoDate(dataDate)}
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
              {formatIsoDate(period.previousDate)} ({periodDays} gün önce)
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
              {formatCurrency(Math.abs(period.change))} ({formatSignedPercent(period.changePercent)})
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
  )
}
