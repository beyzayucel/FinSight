/**
 * Mirrors the backend DTOs in:
 *   com.akademi.finsight.stresstest.entity.SimulationType
 *   com.akademi.finsight.stresstest.dto.request.PortfolioDataDto
 *   com.akademi.finsight.stresstest.dto.response.PortfolioResultDto
 *   com.akademi.finsight.stresstest.dto.response.StressTestInferenceResponseDto
 */

// Mirrors com.akademi.finsight.stresstest.enums.SimulationType.
// EQUITY_SHOCK is confirmed by the @Parameter example in StressTestApi;
// INTEREST_RATE_SHOCK is inferred for "Faiz Şoku" — confirm the exact
// enum constant name against SimulationType.java and adjust if needed.
export type ScenarioKey = "EQUITY_SHOCK" | "INTEREST_RATE_SHOCK";

export interface PortfolioDataDto {
  initialValue: number;
  /** Asset class -> weight (0-1). Weights must sum to 1.0. */
  assetWeights: Record<string, number>;
}

export interface PortfolioResultDto {
  initialValue: number;
  /** Fractional impact, e.g. -0.0952 for -%9,52 */
  expectedImpactRate: number;
  postShockValue: number;
}

export interface StressTestInferenceResponseDto {
  scenarioKey: string;
  currentPortfolioResult: PortfolioResultDto;
  simulationPortfolioResult: PortfolioResultDto;
  benchmarkPortfolioResult: PortfolioResultDto;
  llmComment: string;
}

/** Options for GET .../period (StressTestApi#getSimulationResultByPeriod) */
export interface StressTestPeriodQuery {
  fundId: string;
  /** e.g. 10, 20, 30, 90 — matches the "Analiz Dönemi" selector. */
  daysAgo: number;
}

export interface ScenarioOption {
  key: ScenarioKey;
  title: string;
  description: string;
}
