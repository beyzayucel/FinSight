import api from '@/lib/api/client'
import type { ScenarioKey } from './types'

const SAVE_DECISION_PATH = '/decisions/save'

export interface SaveDecisionPayload {
  fundId: string
  scenarioKey: ScenarioKey
  initialValue: number
  assetWeights: Record<string, number>
  llmComment?: string
}

/**
 * Karar + simülasyon + stres testi verilerini DB'ye kaydeder. (Doküman Bölüm 12)
 */
export async function saveDecisionRecord(payload: SaveDecisionPayload): Promise<void> {
  await api.post(SAVE_DECISION_PATH, payload)
}