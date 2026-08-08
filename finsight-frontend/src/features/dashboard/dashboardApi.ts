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

// Mock database in localStorage
const MOCK_FUND_ID = '084867bb-92cb-4c1f-af08-9d1725f932cc'
const MOCK_REC_ID = 'f1a2b3c4-d5e6-4f7g-8h9i-0j1k2l3m4n5o'

const DEFAULT_FUND: Fund = {
  id: MOCK_FUND_ID,
  code: 'TIE',
  name: 'TIE İş Portföy – BIST 30 Endeksi',
  date: '2026-07-28',
  weights: {
    STOCK: 94.90,
    REPO: 4.22,
    FUTURE: 0.62,
    FUND: 0.26
  }
}

const DEFAULT_RECOMMENDATION: AIRecommendation = {
  id: MOCK_REC_ID,
  fundId: MOCK_FUND_ID,
  status: 'PENDING',
  rationale: 'Güncel verilere göre (28.07.2026 itibarıyla), hisse senedi tarafında artan oynaklık ve kısa vadeli likidite ihtiyacındaki yükseliş nedeniyle ters-repo ağırlığının artırılması ve hisse senedi ağırlığının kademeli azaltılması öneriliyor.',
  expectedRiskChange: 'Volatilite -0.3 puan (azalış)',
  weights: {
    STOCK: { recommendedWeight: 91.50, currentWeight: 94.90 },
    REPO: { recommendedWeight: 7.80, currentWeight: 4.22 },
    FUTURE: { recommendedWeight: 0.50, currentWeight: 0.62 },
    FUND: { recommendedWeight: 0.20, currentWeight: 0.26 }
  }
}

function getStoredRecommendation(): AIRecommendation {
  const stored = localStorage.getItem('finsight_ai_recommendation')
  if (stored) {
    return JSON.parse(stored)
  }
  localStorage.setItem('finsight_ai_recommendation', JSON.stringify(DEFAULT_RECOMMENDATION))
  return DEFAULT_RECOMMENDATION
}

function setStoredRecommendation(rec: AIRecommendation) {
  localStorage.setItem('finsight_ai_recommendation', JSON.stringify(rec))
}

const CATEGORY_MAP: Record<string, AssetCategory> = {
  'hisse senedi': 'STOCK',
  'stock': 'STOCK',
  'ters-repo': 'REPO',
  'ters repo': 'REPO',
  'repo': 'REPO',
  'vadeli işl. nakit teminatı': 'FUTURE',
  'vadeli işlem nakit teminatı': 'FUTURE',
  'vadeli işlemler teminat': 'FUTURE',
  'future': 'FUTURE',
  'yatırım fonu katılma payı': 'FUND',
  'yatırım fonları katılma payları': 'FUND',
  'fund': 'FUND',
}

export async function getActiveFund(fundCode: string = 'TIE'): Promise<Fund> {
  try {
    const response = await api.get<{ data: Array<{ id: string; fundId: string; category: string; weight: number; date: string }> }>(
      `/fund-distributions/funds/${fundCode}/latest`
    )
    const distributions = response.data?.data

    if (!distributions || distributions.length === 0) {
      return DEFAULT_FUND
    }

    const fundId = distributions[0].fundId
    const date = distributions[0].date

    const weights: Record<AssetCategory, number> = {
      STOCK: 0,
      REPO: 0,
      FUTURE: 0,
      FUND: 0,
    }

    for (const item of distributions) {
      const normalizedCat = item.category?.trim().toLowerCase()
      const mappedKey = CATEGORY_MAP[normalizedCat]
      if (mappedKey) {
        weights[mappedKey] = Number(item.weight) || 0
      }
    }

    return {
      id: fundId || MOCK_FUND_ID,
      code: fundCode,
      name: `${fundCode} İş Portföy – BIST 30 Endeksi`,
      date: date || DEFAULT_FUND.date,
      weights,
    }
  } catch (error) {
    console.warn('getActiveFund API failed, falling back to default data.', error)
    return DEFAULT_FUND
  }
}

export async function getPendingRecommendation(fundId: string = MOCK_FUND_ID): Promise<AIRecommendation> {
  try {
    const response = await api.get(`/funds/${fundId}/recommendations/pending`)
    return response.data.data
  } catch {
    console.warn('getPendingRecommendation API failed, falling back to local storage.')
    return getStoredRecommendation()
  }
}

export async function submitRecommendationDecision(recommendationId: string, status: 'ACCEPTED' | 'REJECTED', note?: string): Promise<void> {
  try {
    await api.post(`/funds/recommendations/${recommendationId}/decision`, { status, note })
  } catch {
    console.warn('submitRecommendationDecision API failed, falling back to local storage.')
    const rec = getStoredRecommendation()
    if (rec.id === recommendationId) {
      rec.status = status
      setStoredRecommendation(rec)
    }
  }
}

export type ManualScenarioRequest = {
  fundId: string
  note?: string
  weights: Record<AssetCategory, number>
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
