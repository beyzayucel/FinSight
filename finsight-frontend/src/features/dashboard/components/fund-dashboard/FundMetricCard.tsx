import type { ReactNode } from 'react'

type FundMetricCardProps = {
  label: string
  value: string
  badge: ReactNode
  positive: boolean
}

/** KPI şeridindeki tek ölçütlü kart (Günlük Getiri, Birikimli Getiri, Benchmark Farkı). */
export default function FundMetricCard({ label, value, badge, positive }: FundMetricCardProps) {
  return (
    <div className="min-h-[160px] bg-white rounded-2xl border border-slate-200/80 shadow-sm p-5 flex flex-col justify-start space-y-2 select-none">
      <span className="text-[10px] font-bold tracking-wider text-slate-500 uppercase block">
        {label}
      </span>
      <div className="text-[clamp(17px,1.2vw+7px,26px)] leading-tight tracking-tight font-bold font-mono text-slate-800 whitespace-nowrap">
        {value}
      </div>
      <span
        className={`inline-flex w-fit items-center gap-1 text-[10px] font-semibold font-mono rounded-md px-2 py-1 ${
          positive ? 'bg-emerald-50 text-emerald-700' : 'bg-rose-50 text-rose-600'
        }`}
      >
        {badge}
      </span>
    </div>
  )
}
