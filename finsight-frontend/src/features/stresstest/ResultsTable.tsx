import type { PortfolioResultDto } from './types'

interface ResultsRow {
  label: string
  result?: PortfolioResultDto | null
}

interface ResultsTableProps {
  rows: ResultsRow[]
}

const currencyFormatter = new Intl.NumberFormat('tr-TR', {
  style: 'currency',
  currency: 'TRY',
  maximumFractionDigits: 0,
})

function formatPercent(rate: number): string {
  const percentValue = (rate * 100).toLocaleString('tr-TR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
    signDisplay: 'exceptZero',
  })
  return `${percentValue}%`
}

export function ResultsTable({ rows }: ResultsTableProps) {
  console.log('ResultsTable Gelen Rows:', rows)
  
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[560px] text-left text-xs">
        <thead>
          <tr className="border-b border-slate-200 text-slate-400 font-bold uppercase tracking-wider">
            <th scope="col" className="pb-3 pr-4 font-semibold">Portföy</th>
            <th scope="col" className="pb-3 pr-4 font-semibold">Şok Öncesi Değer</th>
            <th scope="col" className="pb-3 pr-4 font-semibold">Beklenen Etki</th>
            <th scope="col" className="pb-3 font-semibold">Şok Sonrası Değer</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100 font-medium">
          {rows.map(({ label, result }) => {
            if (!result) return null

            return (
              <tr key={label} className="hover:bg-slate-50/50 transition-colors">
                <td className="py-3.5 pr-4 font-bold text-slate-900">{label}</td>
                <td className="py-3.5 pr-4 text-slate-700">
                  {currencyFormatter.format(result.initialValue ?? 0)}
                </td>
                <td
                  className={`py-3.5 pr-4 font-bold ${
                    (result.expectedImpactRate ?? 0) < 0 ? 'text-rose-600' : 'text-emerald-600'
                  }`}
                >
                  {formatPercent(result.expectedImpactRate ?? 0)}
                </td>
                <td className="py-3.5 text-slate-900 font-semibold">
                  {currencyFormatter.format(Math.max(0, result.postShockValue ?? 0))}
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}