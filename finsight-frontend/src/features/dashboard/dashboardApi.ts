import api from '@/lib/api/client'

export type AssetCategory = 'STOCK' | 'REPO' | 'FUTURE' | 'FUND'

export const AssetCategoryLabels: Record<AssetCategory, { tr: string; en: string }> = {
  STOCK: { tr: 'Hisse Senedi', en: 'Stock' },
  REPO: { tr: 'Ters-Repo', en: 'Reverse Repo' },
  FUTURE: { tr: 'Vadeli İşl. Nakit Teminatı', en: 'Futures Cash Collateral' },
  FUND: { tr: 'Yatırım Fonu Katılma Payı', en: 'Mutual Fund Share' },
}

export type RecommendationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED'

export type FundWeight = {
  category: AssetCategory
  weight: number
}

export type Fund = {
  id: string
  code: string
  name: string
  date: string
  weights: Record<AssetCategory, number>
}

export type AIRecommendationWeight = {
  recommendedWeight: number
  currentWeight: number
}

export type AIRecommendation = {
  id: string
  fundId: string
  status: RecommendationStatus
  rationale: string
  expectedRiskChange: string
  weights: Record<AssetCategory, AIRecommendationWeight>
}

import { getFundDashboard } from './lib/fund-dashboard/fundDashboardApi'

const CATEGORY_MAP: Record<string, AssetCategory> = {
  'hisse senedi': 'STOCK',
  'ters-repo': 'REPO',
  'vadeli işlemler teminat': 'FUTURE',
  'vadeli işl. nakit teminatı': 'FUTURE',
  'yatırım fonları katılma payları': 'FUND',
  'yatırım fonu katılma payı': 'FUND',
}

export async function getActiveFund(fundCode: string = 'TIE'): Promise<Fund> {
  const response = await api.get<{ data: Array<{ id: string; fundId: string; category: string; weight: number; date: string }> }>(
    `/fund-distributions/funds/${fundCode}/latest`
  )
  const distributions = response.data?.data || []

  const fundId = distributions[0]?.fundId || ''
  const date = distributions[0]?.date || new Date().toISOString().split('T')[0]

  const weights: Record<AssetCategory, number> = {
    STOCK: 0,
    REPO: 0,
    FUTURE: 0,
    FUND: 0,
  }

  for (const item of distributions) {
    const rawCat = (item.category || '').trim()
    const normalizedCat = rawCat
      .toLocaleLowerCase('tr')
      .replace(/i̇/g, 'i')
      .toLowerCase()

    let mappedKey: AssetCategory | undefined = CATEGORY_MAP[normalizedCat]

    if (!mappedKey) {
      const lower = rawCat.toLowerCase()
      if (lower.includes('hisse') || lower.includes('stock')) mappedKey = 'STOCK'
      else if (lower.includes('repo') || lower.includes('takasbank')) mappedKey = 'REPO'
      else if (lower.includes('vadel') || lower.includes('teminat') || lower.includes('viop') || lower.includes('future')) mappedKey = 'FUTURE'
      else if (lower.includes('fon') || lower.includes('fund') || lower.includes('katıl') || lower.includes('katil')) mappedKey = 'FUND'
    }

    if (mappedKey) {
      weights[mappedKey] = Number(item.weight) || 0
    }
  }

  return {
    id: fundId,
    code: fundCode,
    name: `${fundCode} İş Portföy – BIST 30 Endeksi`,
    date,
    weights,
  }
}

export async function getPendingRecommendation(fundId: string): Promise<AIRecommendation> {
  const response = await api.get<{ data: AIRecommendation }>(`/funds/${fundId}/recommendations/pending`)
  return response.data.data
}

export async function submitRecommendationDecision(
  recommendationId: string,
  status: 'ACCEPTED' | 'REJECTED',
  note?: string
): Promise<void> {
  await api.post(`/funds/recommendations/${recommendationId}/decision`, { status, note })
}

export type ManualScenarioRequest = {
  fundId: string
  note?: string
  weights: Record<AssetCategory, number>
  stockWeights?: Record<string, number>
}

// features/stresstest/... dosyasının içinde
export async function runStressTest(params: { fundId: string; simulationType: string }, body: any) {
    const response = await api.post(`/stress-tests/run`, body, { params })
    
    // BURADA 'return response.data' YAPILIYORDU:
    // Backend veriyi 'data' field'ı içinde sarmalayıp gönderdiği için:
    return response.data.data  // <-- Buraya ikinci '.data'yı ekle!
}

export async function applyManualScenario(data: ManualScenarioRequest): Promise<void> {
  try {
    await api.post('/funds/scenarios/apply', data)
  } catch {
    console.warn('applyManualScenario API failed, falling back to mock save.')
    // Save to list of applied scenarios in localStorage
    const storedScenarios = localStorage.getItem('finsight_applied_scenarios')
    const list = storedScenarios ? JSON.parse(storedScenarios) : []
    list.push({
      ...data,
      appliedAt: new Date().toISOString()
    })
    localStorage.setItem('finsight_applied_scenarios', JSON.stringify(list))
    
    // Also save decision status
    const rec = getStoredRecommendation()
    rec.status = 'ACCEPTED' // Creating manual scenario counts as active action
    setStoredRecommendation(rec)
  }
}



// Reset functions for mock data
export function resetMockData() {
  localStorage.removeItem('finsight_ai_recommendation')
  localStorage.removeItem('finsight_applied_scenarios')
}
