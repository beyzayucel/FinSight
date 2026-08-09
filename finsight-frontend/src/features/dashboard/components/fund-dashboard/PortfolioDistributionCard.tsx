import { Fragment, useMemo } from 'react'
import { Cell, Pie, PieChart, ResponsiveContainer } from 'recharts'
import type { FundDistributionItem } from '../../lib/fund-dashboard/fundDashboardApi'
import { categoryColor } from './fundColors'

type PortfolioDistributionCardProps = {
  distribution: FundDistributionItem[]
  onOpenStockBreakdown: () => void
}

export default function PortfolioDistributionCard({
  distribution,
  onOpenStockBreakdown,
}: PortfolioDistributionCardProps) {
  const slices = useMemo(
    () =>
      distribution
        .map((item, i) => ({ ...item, color: categoryColor(item.category, i) }))
        .sort((a, b) => b.weight - a.weight),
    [distribution]
  )

  return (
    <div className="lg:col-span-5 bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 flex flex-col">
      <div>
        <h3 className="text-base font-bold text-slate-800">Portföy Dağılımı</h3>
        <p className="text-[11px] text-slate-500 font-medium mt-0.5">
          {slices.length} yatırım kategorisi · güncel ağırlıklar
        </p>
      </div>

      <div className="flex-1 flex flex-col sm:flex-row items-center gap-6 py-6">
        <div className="w-[170px] h-[170px] flex-shrink-0">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={slices}
                dataKey="weight"
                nameKey="category"
                innerRadius={52}
                outerRadius={80}
                startAngle={90}
                endAngle={-270}
                stroke="#ffffff"
                strokeWidth={2}
                isAnimationActive={false}
              >
                {slices.map((item) => (
                  <Cell key={item.category} fill={item.color} />
                ))}
              </Pie>
            </PieChart>
          </ResponsiveContainer>
        </div>

        <div className="grid w-full grid-cols-[0.75rem_1fr_auto] items-center gap-x-2.5 gap-y-3 text-xs">
          {slices.map((item) => (
            <Fragment key={item.category}>
              <span
                className="w-3 h-3 rounded-full"
                style={{ backgroundColor: item.color }}
              />
              <span className="font-semibold text-slate-700 leading-snug">{item.category}</span>
              <span className="text-right font-bold font-mono tabular-nums text-slate-800">
                {item.weight.toFixed(2).replace('.', ',')}%
              </span>
            </Fragment>
          ))}
        </div>
      </div>

      <button
        onClick={onOpenStockBreakdown}
        className="self-start text-xs font-semibold text-[#c89834] underline underline-offset-4 hover:text-[#a87e2a] transition-colors outline-none select-none"
      >
        Hisse Senedi alt kırılımını gör →
      </button>
    </div>
  )
}
