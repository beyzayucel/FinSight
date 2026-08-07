import type { PortfolioResultDto } from "./types.tsx";

interface ResultsRow {
  label: string;
  result: PortfolioResultDto;
}

interface ResultsTableProps {
  rows: ResultsRow[];
}

const currencyFormatter = new Intl.NumberFormat("tr-TR", {
  style: "currency",
  currency: "TRY",
  maximumFractionDigits: 0,
});

// Intl's built-in `style: "percent"` puts the % sign before the number in
// some tr-TR ICU builds (e.g. "%-2,78"), but the reference design always
// puts it after the number ("-2,78%") — so this formats manually.
function formatPercent(rate: number): string {
  const percentValue = (rate * 100).toLocaleString("tr-TR", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
    signDisplay: "exceptZero",
  });
  return `${percentValue}%`;
}

export function ResultsTable({ rows }: ResultsTableProps) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[560px] text-left text-sm">
        <thead>
          <tr className="border-b border-neutral-200 text-xs font-semibold uppercase tracking-wide text-neutral-400">
            <th scope="col" className="py-3 pr-4 font-semibold">
              Portföy
            </th>
            <th scope="col" className="py-3 pr-4 font-semibold">
              Şok Öncesi Değer
            </th>
            <th scope="col" className="py-3 pr-4 font-semibold">
              Beklenen Etki
            </th>
            <th scope="col" className="py-3 font-semibold">
              Şok Sonrası Değer
            </th>
          </tr>
        </thead>
        <tbody>
          {rows.map(({ label, result }) => (
            <tr key={label} className="border-b border-neutral-100 last:border-0">
              <td className="py-4 pr-4 font-semibold text-neutral-900">{label}</td>
              <td className="py-4 pr-4 text-neutral-700">
                {currencyFormatter.format(result.initialValue)}
              </td>
              <td
                className={`py-4 pr-4 font-medium ${
                  result.expectedImpactRate < 0 ? "text-red-500" : "text-emerald-600"
                }`}
              >
                {formatPercent(result.expectedImpactRate)}
              </td>
              <td className="py-4 text-neutral-700">
                {currencyFormatter.format(result.postShockValue)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
