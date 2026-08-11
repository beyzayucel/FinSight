import axios from 'axios'
import api from '@/lib/api/client'
import { getAccessToken, tryRefreshIfExpired } from '@/lib/authStore'
import { ASSET_CLASSES, type AssetClass, type Weights } from '@/features/dashboard/lib/simulation'

// GEÇİCİ İŞ-AROUND: InfinaApi'nin @RequestMapping('/infina') değeri, projedeki diğer
// tüm controller'ların aksine /api/v1 prefix'i içermiyor (backend bug, PR #22 —
// feature/services-impl). Normal `api` client'ımız her isteğe /api/v1 önekliyor,
// bu yüzden burada onu bypass edip kök URL'e (VITE_API_BASE_URL - /api/v1) gidiyoruz.
// Arkadaşımız backend'de düzeltince bu dosyayı normal `api` client'ını kullanacak
// şekilde sadeleştir.
const INFINA_BASE_URL = (import.meta.env.VITE_API_BASE_URL as string).replace(/\/api\/v1\/?$/, '')

export type FundReturnPeriod = { period: string; yield: number }
export type FundAssetDistribution = { shortDesc: string; ratio: number }

export type FundInfo = {
  periodReturns: FundReturnPeriod[]
  totalMarketPrice: number
  assetDistribution: FundAssetDistribution[]
}

type ApiResponse<T> = {
  success: boolean
  data: T
  error?: { message?: string; code?: string }
}

export async function getFundInfo(fundCode: string, periods?: string): Promise<FundInfo> {
  // Bu istek workaround nedeniyle paylaşılan `api` client'ının 401→refresh→retry
  // interceptor'ından geçmiyor; token süresi dolmuşsa burada önden tazeliyoruz.
  await tryRefreshIfExpired()
  const token = getAccessToken()
  const response = await axios.get<ApiResponse<FundInfo>>(`${INFINA_BASE_URL}/infina/FundInfo`, {
    params: { fundCode, periods },
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
  })
  if (!response.data.success) {
    throw new Error(response.data.error?.message ?? 'FundInfo isteği başarısız oldu')
  }
  return response.data.data
}

// Infina'nın döndürdüğü serbest metin kategori adlarını bizim sabit AssetClass anahtarlarımıza eşler.
const SHORT_DESC_MAP: Record<string, AssetClass> = {
  'hisse senedi': 'hisseSenedi',
  'ters-repo': 'tersRepo',
  'ters repo': 'tersRepo',
  'vadeli işlemler teminat': 'vadeliIslemNakitTeminati',
  'vadeli işlem nakit teminatı': 'vadeliIslemNakitTeminati',
  'yatırım fonları katılma payları': 'yatirimFonuKatilmaPayi',
  'yatırım fonu katılma payı': 'yatirimFonuKatilmaPayi',
}

function normalize(text: string): string {
  return text.toLocaleLowerCase('tr-TR').trim()
}

/**
 * Infina'nın assetDistribution listesini bizim 4 kategorili Weights şeklimize çevirir.
 * Beklenmeyen bir kategori adı gelirse `null` döner (gerçek veri kalitesi sorunu).
 * Infina genelde %0 olan kategorileri listede hiç döndürmüyor, bu yüzden listede
 * bulunmayan kategoriler hata sayılmaz, 0 kabul edilir.
 */
export function mapAssetDistributionToWeights(distribution: FundAssetDistribution[]): Weights | null {
  const result: Partial<Weights> = {}
  for (const item of distribution) {
    const key = SHORT_DESC_MAP[normalize(item.shortDesc)]
    if (!key) return null
    result[key] = item.ratio / 100
  }
  for (const assetClass of ASSET_CLASSES) {
    if (result[assetClass] === undefined) result[assetClass] = 0
  }
  return result as Weights
}

// Manuel Senaryo backend'i (feat/portfolio-scenario-management) doğru şekilde /api/v1 önekli
// Bizim AssetClass anahtarlarımızı backend'in AssetCategory enum'una eşler.
const ASSET_CATEGORY_MAP: Record<AssetClass, 'STOCK' | 'REPO' | 'FUTURE' | 'FUND'> = {
  hisseSenedi: 'STOCK',
  tersRepo: 'REPO',
  vadeliIslemNakitTeminati: 'FUTURE',
  yatirimFonuKatilmaPayi: 'FUND',
}

/** Manuel senaryoyu POST /funds/scenarios/apply ile kalıcı olarak kaydeder (K1-K3 server-side de doğrulanır). */
export async function submitManualScenario(fundId: string, weights: Weights, note?: string): Promise<void> {
  const payload = {
    fundId,
    note,
    weights: Object.fromEntries(
      ASSET_CLASSES.map((assetClass) => [ASSET_CATEGORY_MAP[assetClass], weights[assetClass] * 100])
    ),
  }
  await api.post('/funds/scenarios/apply', payload)
}
