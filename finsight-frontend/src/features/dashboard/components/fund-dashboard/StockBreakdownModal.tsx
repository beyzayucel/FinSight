import { useEffect } from 'react'
import { IoClose } from 'react-icons/io5'
import type { StockBreakdownItem } from '../../lib/fund-dashboard/fundDashboardApi'
import { formatMonthPeriod } from '../../lib/fund-dashboard/fundDashboardFormatters'
import { getTranslations } from '@/i18n/translations'

type StockBreakdownModalProps = {
  period: string
  items: StockBreakdownItem[]
  onClose: () => void
}

export default function StockBreakdownModal({ period, items, onClose }: StockBreakdownModalProps) {
  const t = getTranslations()
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
        className="max-h-[calc(100dvh-2rem)] w-full max-w-lg overflow-y-auto bg-white rounded-2xl shadow-2xl p-5 sm:p-6 space-y-4 animate-fade-in"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between">
          <div>
            <h3 id="stock-breakdown-title" className="text-lg font-bold text-slate-800">
              {t.stockBreakdownTitle}
            </h3>
            <p className="text-[11px] text-slate-500 font-medium mt-1 leading-relaxed">
              {t.stockBreakdownSubtitle(formatMonthPeriod(period))}
            </p>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-slate-600 transition-colors outline-none"
            aria-label={t.close}
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
                  {isOthers ? t.other : item.assetCode}
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
