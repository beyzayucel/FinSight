import type { ScenarioKey, ScenarioOption } from "./types.tsx";

export const SCENARIOS: ScenarioOption[] = [
  {
    key: "EQUITY_SHOCK",
    title: "Hisse Şoku",
    description: "BIST 100 endeksinde anlık –%10 düşüş",
  },
  {
    key: "INTEREST_RATE_SHOCK",
    title: "Faiz Şoku",
    description: "Gösterge faiz oranında +300 baz puan artış",
  },
];

interface ScenarioSelectorProps {
  selected: ScenarioKey;
  onSelect: (key: ScenarioKey) => void;
  disabled?: boolean;
}

export function ScenarioSelector({ selected, onSelect, disabled }: ScenarioSelectorProps) {
  return (
    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
      {SCENARIOS.map((scenario) => {
        const isActive = scenario.key === selected;
        return (
          <button
            key={scenario.key}
            type="button"
            disabled={disabled}
            aria-pressed={isActive}
            onClick={() => onSelect(scenario.key)}
            className={`rounded-xl border px-6 py-5 text-left transition-colors focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-amber-500 disabled:cursor-not-allowed disabled:opacity-60 ${
              isActive
                ? "border-amber-300 bg-amber-100/70"
                : "border-neutral-200 bg-white hover:border-neutral-300"
            }`}
          >
            <span
              className={`text-sm font-semibold uppercase tracking-wide ${
                isActive ? "text-amber-800" : "text-neutral-900"
              }`}
            >
              {scenario.title}
            </span>
            <p className="mt-1 text-sm text-neutral-500">{scenario.description}</p>
          </button>
        );
      })}
    </div>
  );
}
