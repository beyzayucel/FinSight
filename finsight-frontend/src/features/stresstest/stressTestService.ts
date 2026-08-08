import api from '@/lib/api/client'
import type { ScenarioKey, PortfolioDataDto } from './types'

const SAVE_DECISION_PATH = '/stress-tests/save'

export interface SaveDecisionPayload {
  fundId: string
  scenarioKey: ScenarioKey
  portfolioData: PortfolioDataDto // assetWeights ve initialValue barındırır
  llmComment?: string
}

export async function saveDecisionRecord(payload: SaveDecisionPayload): Promise<void> {
  await api.post('/stress-tests/save', payload)
}