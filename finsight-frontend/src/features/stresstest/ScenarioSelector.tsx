import type { ScenarioKey, ScenarioOption } from './types'

export const SCENARIOS: ScenarioOption[] = [
  {
    key: 'EQUITY_SHOCK',
    title: 'Hisse Şoku',
    description: 'BIST 100 endeksinde anlık –%10 düşüş',
  },
  {
    key: 'INTEREST_RATE_SHOCK',
    title: 'Faiz Şoku',
    description: 'Gösterge faiz oranında +300 baz puan artış',
  },
]

interface ScenarioSelectorProps {
  selected: ScenarioKey
  onSelect: (key: ScenarioKey) => void
  disabled?: boolean
}

export function ScenarioSelector({ selected, onSelect, disabled }: ScenarioSelectorProps) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      {SCENARIOS.map((scenario) => {
        const isActive = scenario.key === selected
        return (
          <button
            key={scenario.key}
            type="button"
            disabled={disabled}
            onClick={() => onSelect(scenario.key)}
            className={`rounded-xl border p-5 text-left transition-all ${
              isActive
                ? 'border-[#c89834] bg-[#c89834]/10 shadow-sm'
                : 'border-slate-200 bg-white hover:border-slate-300'
            } disabled:cursor-not-allowed disabled:opacity-60`}
          >
            <span
              className={`text-xs font-bold uppercase tracking-wider block ${
                isActive ? 'text-[#c89834]' : 'text-slate-900'
              }`}
            >
              {scenario.title}
            </span>
            <p className="mt-1 text-xs text-slate-500 font-medium">{scenario.description}</p>
          </button>
        )
      })}
    </div>
  )
}