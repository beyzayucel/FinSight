import axiosInstance from '@/lib/api/client'
import type { 
  ApiStandardResponse, 
  PortfolioDataDto, 
  StressTestInferenceResponseDto,
  ScenarioKey 
} from './types'

const BASE_PATH = '/stress-tests'

/**
 * Fonun varlık kategorilerini, stres testi motorunun şok katsayılarını tanımladığı dört risk
 * kovasına çevirir. Motor ağırlıkları EQUITY/BOND/FX/CASH adlarıyla okuyor
 * (RuleBasedStressTestEngineImpl.parseAssetWeights); çeviri yapılmazsa hiçbir anahtar eşleşmiyor
 * ve mevcut portföyün etkisi her senaryoda %0,00 çıkıyor.
 *
 * Ters-Repo ve Vadeli İşl. Nakit Teminatı CASH'e giriyor — senaryo tanımı faiz artışının bu iki
 * kaleme olumlu yansıdığını söylüyor ve faiz şokunda pozitif katsayısı olan tek kova CASH.
 *
 * NOT: yatirimFonuKatilmaPayi -> BOND, kalan tek makul kova olduğu için seçildi; PR #49'un
 * yazarından teyit bekliyor.
 */
const RISK_BUCKET: Record<string, string> = {
  hisseSenedi: 'EQUITY',
  tersRepo: 'CASH',
  vadeliIslemNakitTeminati: 'CASH',
  yatirimFonuKatilmaPayi: 'BOND',
}

function toRiskBuckets(assetWeights: Record<string, number>): Record<string, number> {
  return Object.entries(assetWeights).reduce<Record<string, number>>((acc, [category, weight]) => {
    // Tanınmayan anahtar zaten kova adıdır (EQUITY/BOND/FX/CASH) — olduğu gibi geçir.
    const bucket = RISK_BUCKET[category] ?? category
    // İki kategori aynı kovaya düştüğü için üzerine yazmak değil, toplamak gerekiyor.
    acc[bucket] = (acc[bucket] ?? 0) + weight
    return acc
  }, {})
}

/**
 * Canlı Stres Testi Simülasyonunu Çalıştırır (POST /stress-tests/run)
 */
export async function runSimulation(
  fundId: string,
  simulationType: ScenarioKey,
  portfolioData: PortfolioDataDto,
  analysisWindow?: number
): Promise<StressTestInferenceResponseDto | null> {
  const response = await axiosInstance.post<ApiStandardResponse<StressTestInferenceResponseDto>>(
    `${BASE_PATH}/run`,
    { ...portfolioData, assetWeights: toRiskBuckets(portfolioData.assetWeights) },
    {
      params: {
        fundId,
        simulationType,
        ...(analysisWindow ? { analysisWindow } : {})
      }
    }
  )
  return response.data?.data ?? null
}

/**
 * Seçilen analiz dönemine (örn. 20 gün önce) ait geçmiş stres testi sonucunu getirir
 * (GET /stress-tests/period?fundId=...&daysAgo=20)
 */
export async function getSimulationResultByPeriod(
  fundId: string,
  daysAgo: number
): Promise<StressTestInferenceResponseDto | null> {
  try {
    const response = await axiosInstance.get(`${BASE_PATH}/period`, {
      params: { fundId, daysAgo: daysAgo.toString() },
    })

    console.log('RAW response.data:', response.data)          // ← ekle
    console.log('RAW response.status:', response.status)       // ← ekle

    const resData = response.data as any
    const payload = resData?.data ?? resData

    console.log('Çözümlenen payload:', payload)                 // ← ekle
    console.log('payload keys:', payload ? Object.keys(payload) : null) // ← ekle

    return payload as StressTestInferenceResponseDto
  } catch (error) {
    console.warn('Geçmiş stres testi çekilemedi:', error)
    return null
  }
}

export async function saveStressTestDecision(
  fundId: string,
  stressTestResultId: string
): Promise<void> {
  await axiosInstance.post(`${BASE_PATH}/save`, {
    fundId,
    stressTestResultId,
  })
}