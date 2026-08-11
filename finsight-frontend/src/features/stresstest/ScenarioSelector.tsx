import { getScenarioTitle, type ScenarioKey } from './types'
import { getTranslations } from '@/i18n/translations'

const SCENARIO_KEYS: ScenarioKey[] = ['EQUITY_SHOCK', 'INTEREST_RATE_SHOCK']

interface ScenarioSelectorProps {
  selected: ScenarioKey
  onSelect: (key: ScenarioKey) => void
  disabled?: boolean
}

export function ScenarioSelector({ selected, onSelect, disabled }: ScenarioSelectorProps) {
  const t = getTranslations()
  const scenarios = SCENARIO_KEYS.map((key) => ({
    key,
    title: getScenarioTitle(key),
    description: key === 'EQUITY_SHOCK' ? t.stressEquityDescription : t.stressInterestDescription,
  }))
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      {scenarios.map((scenario) => {
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
