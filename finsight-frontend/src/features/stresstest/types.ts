import { getLang } from '@/lib/authStore'

export type ScenarioKey = 'EQUITY_SHOCK' | 'INTEREST_RATE_SHOCK'

/** Stres Testi ve Karar Geçmişi ekranları aynı senaryo adlarını göstermeli. */
export const SCENARIO_TITLES: Record<ScenarioKey, { tr: string; en: string }> = {
  EQUITY_SHOCK: { tr: 'Hisse Şoku', en: 'Equity Shock' },
  INTEREST_RATE_SHOCK: { tr: 'Faiz Şoku', en: 'Interest Rate Shock' },
}

export function getScenarioTitle(key: ScenarioKey): string {
  return SCENARIO_TITLES[key][getLang() === 'en' ? 'en' : 'tr']
}

export interface PortfolioDataDto {
  initialValue: number
  /** Varlık sınıfı -> Ağırlık (0-1 arası, toplamı 1.0) */
  assetWeights: Record<string, number>
}

export interface PortfolioResultDto {
  initialValue: number
  /** Oransal etki (Örn: -0.0952 -> %-9,52) */
  expectedImpactRate: number
  postShockValue: number
}

export interface StressTestInferenceResponseDto {
  /** Kaydedilen stress_test_results satırının id'si — "Karar Geçmişine Kaydet" bunu geri gönderir. */
  id: string
  scenarioKey: ScenarioKey
  currentPortfolioResult: PortfolioResultDto
  simulationPortfolioResult: PortfolioResultDto
  benchmarkPortfolioResult: PortfolioResultDto
  llmComment: string
}

export interface ScenarioOption {
  key: ScenarioKey
  title: string
  description: string
}

export interface ApiStandardResponse<T> {
  data: T
  message?: string
  status?: string
}
