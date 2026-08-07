import { useEffect, useState } from "react";
import { ScenarioSelector } from "./ScenarioSelector";
import { ResultsTable } from "./ResultsTable";
import { LLMCommentSection } from "./LLMCommentSection";
import { runStressTestSimulation } from "./stressTestService";
import type { ScenarioKey, StressTestInferenceResponseDto } from "./types";

interface StressTestPageProps {
  /** Active fund id — expected to come from the surrounding fund-selection
   * context/route once that's wired up (see AKTİF FON block in the sidebar). */
  fundId: string;
}

const SCENARIO_TITLES: Record<ScenarioKey, string> = {
  EQUITY_SHOCK: "Hisse Şoku",
  INTEREST_RATE_SHOCK: "Faiz Şoku",
};

export default function StressTestPage({ fundId }: StressTestPageProps) {
  const [scenario, setScenario] = useState<ScenarioKey>("EQUITY_SHOCK");
  const [result, setResult] = useState<StressTestInferenceResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isCancelled = false;

    async function loadScenario() {
      setIsLoading(true);
      setError(null);
      try {
        const response = await runStressTestSimulation(fundId, scenario);
        if (!isCancelled) setResult(response);
      } catch {
        if (!isCancelled) {
          setError("Stres testi sonucu alınamadı. Lütfen tekrar deneyin.");
          setResult(null);
        }
      } finally {
        if (!isCancelled) setIsLoading(false);
      }
    }

    loadScenario();
    return () => {
      isCancelled = true;
    };
  }, [fundId, scenario]);

  async function handleSaveToHistory() {
    // TODO: Karar geçmişi kaydetme endpoint'i tanımlandığında bağlanacak.
  }

  const rows = result
    ? [
        { label: "Mevcut Portföy", result: result.currentPortfolioResult },
        { label: "Simülasyon Portföyü", result: result.simulationPortfolioResult },
        { label: "Benchmark", result: result.benchmarkPortfolioResult },
      ]
    : [];

  return (
    <div className="mx-auto max-w-4xl space-y-8 px-8 py-10">
      <header>
        <p className="text-xs font-semibold uppercase tracking-widest text-amber-600">
          Ekran 04 · Varsayımsal Şok
        </p>
        <h1 className="mt-1 text-3xl font-bold text-neutral-900">Stres Testi</h1>
        <p className="mt-3 max-w-2xl text-sm leading-relaxed text-neutral-500">
          Normal dönemde iyi performans göstermiş bir senaryo, kriz altında yeterince korumalı
          olmayabilir. Aynı şok, üç portföye de eşzamanlı uygulanır — böylece &ldquo;getiri iyiydi
          ama dayanıklı mıydı?&rdquo; sorusu cevaplanır.
        </p>
      </header>

      <ScenarioSelector selected={scenario} onSelect={setScenario} disabled={isLoading} />

      <section className="rounded-xl border border-neutral-200 bg-white p-6">
        <div className="mb-4">
          <h2 className="text-base font-semibold text-neutral-900">
            Senaryo Sonucu: {SCENARIO_TITLES[scenario]}
          </h2>
          <p className="mt-1 text-sm text-neutral-400">
            Şok, güncel portföy ağırlıkları üzerinden hesaplanır
          </p>
        </div>

        {isLoading && <p className="text-sm text-neutral-400">Hesaplanıyor…</p>}
        {error && !isLoading && <p className="text-sm text-red-500">{error}</p>}
        {!isLoading && !error && result && <ResultsTable rows={rows} />}
      </section>

      {result && (
        <section className="rounded-xl border border-neutral-200 bg-white p-6">
          <LLMCommentSection comment={result.llmComment} />
        </section>
      )}

      <button
        type="button"
        onClick={handleSaveToHistory}
        disabled={!result || isLoading}
        className="rounded-lg bg-neutral-900 px-6 py-3 text-sm font-semibold text-white transition-colors hover:bg-neutral-800 disabled:cursor-not-allowed disabled:opacity-50"
      >
        Karar Geçmişine Kaydet →
      </button>
    </div>
  );
}
