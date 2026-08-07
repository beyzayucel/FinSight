import apiClient from "@/lib/api/client";
import type { ScenarioKey, StressTestInferenceResponseDto } from "./types";

// StressTestApi is annotated with:
//   @RequestMapping(ApiEndpoints.StressTest.BASE)   -> "/api/v1/stress-tests"
//   @PostMapping(ApiEndpoints.StressTest.RUN)        -> "/run"
//   @GetMapping(ApiEndpoints.StressTest.LATEST)      -> "/latest"
//   @GetMapping("/period")                           -> "/period"
// The frontend's axios client already targets .../api/v1 as its baseURL,
// so paths here must NOT repeat the "/api/v1" prefix.
const BASE_PATH = "/stress-tests";
const RUN_PATH = `${BASE_PATH}/run`;
const LATEST_PATH = `${BASE_PATH}/latest`;
const PERIOD_PATH = `${BASE_PATH}/period`;

// NOTE: ApiStandardResponse<T> wraps the payload — adjust the `.data` access
// below if the actual envelope shape (success/message/data) differs.
// Auth: fundId/email resolution happens server-side from the bearer token
// (@AuthenticationPrincipal String email) — no user id is sent from here.

/**
 * Runs a new stress test simulation for the given fund and scenario.
 * Maps to: POST StressTestApi#runSimulation
 */
export async function runStressTestSimulation(
  fundId: string,
  simulationType: ScenarioKey,
): Promise<StressTestInferenceResponseDto> {
  const { data } = await apiClient.post(
    RUN_PATH,
    null,
    { params: { fundId, simulationType } },
  );
  return data.data ?? data;
}

/**
 * Fetches the most recently saved simulation result for a fund, if any.
 * Maps to: GET StressTestApi#getLatestSimulationResult
 * Returns null when the backend responds 204 No Content.
 */
export async function getLatestStressTestResult(
  fundId: string,
): Promise<StressTestInferenceResponseDto | null> {
  const response = await apiClient.get(LATEST_PATH, {
    params: { fundId },
    validateStatus: (status) => status === 200 || status === 204,
  });
  if (response.status === 204) return null;
  return response.data.data ?? response.data;
}

/**
 * Fetches the stress test result recorded `daysAgo` days back for a fund,
 * for historical/lookback comparisons (e.g. the "Analiz Dönemi" selector).
 * Maps to: GET StressTestApi#getSimulationResultByPeriod
 * Returns null when the backend responds 204 No Content (no record for
 * that period).
 */
export async function getStressTestResultByPeriod(
  fundId: string,
  daysAgo: number,
): Promise<StressTestInferenceResponseDto | null> {
  const response = await apiClient.get(PERIOD_PATH, {
    params: { fundId, daysAgo },
    validateStatus: (status) => status === 200 || status === 204,
  });
  if (response.status === 204) return null;
  return response.data.data ?? response.data;
}
