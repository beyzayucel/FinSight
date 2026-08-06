// Prototip: gerçek getiri verisi yerine seeded (mulberry32) sentetik veri üretir.
// Bkz. Ekran 04 analiz dokümanı, Bölüm 7 — "Açık Nokta": gerçek üründe bu veri
// Fon Veri Servisi / CSV-geçmiş veri setinden gelmeli.

// Kategori adları Ekran 03 analiz dokümanı Bölüm 5.4 ile birebir eşleşir.
export type AssetClass = 'hisseSenedi' | 'tersRepo' | 'vadeliIslemNakitTeminati' | 'yatirimFonuKatilmaPayi'

export type Weights = Record<AssetClass, number>

export const ASSET_CLASSES: AssetClass[] = [
  'hisseSenedi',
  'tersRepo',
  'vadeliIslemNakitTeminati',
  'yatirimFonuKatilmaPayi',
]

// Fon "Hisse Senedi Yoğun Fon" statüsünde — K3: Hisse Senedi ağırlığı hiçbir zaman %80'in altına inemez.
export const MIN_HISSE_WEIGHT = 0.8
// K2: Manuel senaryoda tek bir kategoride Mevcut'a göre izin verilen maksimum sapma.
export const MAX_MANUAL_DELTA = 0.1

// NOT: Mevcut Portföy ağırlıkları (REF_WEIGHTS) ve başlangıç tutarı (BASE_VALUE) artık
// buradan sabit gelmiyor — gerçek Infina FundInfo API'sinden (bkz. fundApi.ts) çekiliyor
// ve DecisionContext üzerinden bu modüldeki fonksiyonlara parametre olarak geçiliyor.
// V5: API'den alınamazsa sessizce sabit bir değerle devam edilmemeli.

// Fonun stratejik hedef ağırlığı (Benchmark) — MVP kapsamında sabit
export const BENCH_WEIGHTS: Weights = {
  hisseSenedi: 0.92,
  tersRepo: 0.035,
  vadeliIslemNakitTeminati: 0.025,
  yatirimFonuKatilmaPayi: 0.02,
}

// AI önerisi ağırlıkları — doküman Bölüm 5.4 örneğiyle birebir (Hisse: %94,90 → %91,50)
export const AI_WEIGHTS: Weights = {
  hisseSenedi: 0.915,
  tersRepo: 0.041,
  vadeliIslemNakitTeminati: 0.025,
  yatirimFonuKatilmaPayi: 0.019,
}

// Manuel senaryo ağırlıkları — kullanıcının kendi belirlediği varsayım (K2/K3 sınırları içinde)
export const MANUAL_WEIGHTS: Weights = {
  hisseSenedi: 0.85,
  tersRepo: 0.06,
  vadeliIslemNakitTeminati: 0.05,
  yatirimFonuKatilmaPayi: 0.04,
}

export const MAX_WINDOW = 90

const ASSET_PARAMS: Record<AssetClass, { mean: number; vol: number }> = {
  hisseSenedi: { mean: 0.0006, vol: 0.014 },
  tersRepo: { mean: 0.00018, vol: 0.0006 },
  vadeliIslemNakitTeminati: { mean: 0.00015, vol: 0.0007 },
  yatirimFonuKatilmaPayi: { mean: 0.0004, vol: 0.008 },
}

function mulberry32(seed: number) {
  let a = seed
  return function random() {
    a |= 0
    a = (a + 0x6d2b79f5) | 0
    let t = Math.imul(a ^ (a >>> 15), 1 | a)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

// Box-Muller ile standart normal dağılımdan örnekleme (seeded uniform RNG üzerinden)
function nextGaussian(random: () => number): number {
  const u1 = Math.max(random(), Number.EPSILON)
  const u2 = random()
  return Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2)
}

/**
 * Fon koduna göre sabit bir seed üretir; aynı fon için her zaman aynı sentetik
 * getiri serisi üretilir (Analiz Penceresi değişse bile MAX_WINDOW'luk seri aynı kalır,
 * yalnızca sondan N gün dilimlenir — K5).
 */
function seedFromFundCode(fundCode: string): number {
  let seed = 0
  for (let i = 0; i < fundCode.length; i++) {
    seed = (seed * 31 + fundCode.charCodeAt(i)) | 0
  }
  return seed || 1
}

/** Her varlık sınıfı için MAX_WINDOW uzunluğunda günlük getiri serisi üretir. */
function generateAssetReturns(fundCode: string): Record<AssetClass, number[]> {
  const random = mulberry32(seedFromFundCode(fundCode))
  const result = {} as Record<AssetClass, number[]>
  for (const asset of ASSET_CLASSES) {
    const { mean, vol } = ASSET_PARAMS[asset]
    result[asset] = Array.from({ length: MAX_WINDOW }, () => mean + vol * nextGaussian(random))
  }
  return result
}

/** Ağırlıklara göre günlük portföy getiri serisini hesaplar (K1: aynı varlık getirileri, farklı ağırlıklar). */
function weightedDailyReturns(assetReturns: Record<AssetClass, number[]>, weights: Weights): number[] {
  const length = assetReturns.hisseSenedi.length
  return Array.from({ length }, (_, day) =>
    ASSET_CLASSES.reduce((sum, asset) => sum + weights[asset] * assetReturns[asset][day], 0)
  )
}

/** Günlük getiri serisinden kümülatif değer eğrisini (t=0 dahil) üretir. */
function toCurve(dailyReturns: number[], baseValue: number): number[] {
  const curve = [baseValue]
  for (const r of dailyReturns) {
    curve.push(curve[curve.length - 1] * (1 + r))
  }
  return curve
}

export type PortfolioMetrics = {
  currentValue: number
  totalReturnPct: number
  maxDrawdownPct: number
  dailyVolatilityPct: number
}

export function metricsFromCurve(curve: number[]): PortfolioMetrics {
  const first = curve[0]
  const last = curve[curve.length - 1]

  let peak = curve[0]
  let maxDrawdown = 0
  for (const value of curve) {
    if (value > peak) peak = value
    const drawdown = (value - peak) / peak
    if (drawdown < maxDrawdown) maxDrawdown = drawdown
  }

  const dailyReturns: number[] = []
  for (let i = 1; i < curve.length; i++) {
    dailyReturns.push(curve[i] / curve[i - 1] - 1)
  }
  const avg = dailyReturns.reduce((s, r) => s + r, 0) / dailyReturns.length
  const variance = dailyReturns.reduce((s, r) => s + (r - avg) ** 2, 0) / dailyReturns.length

  return {
    currentValue: last,
    totalReturnPct: (last / first - 1) * 100,
    maxDrawdownPct: Math.min(maxDrawdown, 0) * 100,
    dailyVolatilityPct: Math.sqrt(variance) * 100,
  }
}

export function sumWeights(weights: Weights): number {
  return ASSET_CLASSES.reduce((sum, asset) => sum + weights[asset], 0)
}

export type ManualScenarioValidation = {
  sumValid: boolean
  deltaValid: boolean
  hisseFloorValid: boolean
  isValid: boolean
  breachedDeltaAssets: AssetClass[]
}

/** K1 (toplam=%100), K2 (±10 puan sapma), K3 (Hisse ≥ %80) doğrulamaları — V1/V3/V4. */
export function validateManualScenario(weights: Weights, reference: Weights): ManualScenarioValidation {
  const sumValid = Math.abs(sumWeights(weights) - 1) <= 0.0001
  const breachedDeltaAssets = ASSET_CLASSES.filter(
    (asset) => Math.abs(weights[asset] - reference[asset]) > MAX_MANUAL_DELTA + 1e-9
  )
  const deltaValid = breachedDeltaAssets.length === 0
  const hisseFloorValid = weights.hisseSenedi >= MIN_HISSE_WEIGHT - 1e-9

  return {
    sumValid,
    deltaValid,
    hisseFloorValid,
    isValid: sumValid && deltaValid && hisseFloorValid,
    breachedDeltaAssets,
  }
}

export type SimulationWindow = 10 | 20 | 30 | 90

export type SimulationResult = {
  points: { day: number; mevcut: number; simulasyon: number; benchmark: number }[]
  metrics: {
    mevcut: PortfolioMetrics
    simulasyon: PortfolioMetrics
    benchmark: PortfolioMetrics
  }
}

/**
 * Üç portföyün (Mevcut/Simülasyon/Benchmark) getiri eğrisini ve metriklerini hesaplar.
 * @param refWeights Gerçek Fon Veri Servisi'nden (Infina FundInfo) gelen mevcut portföy ağırlıkları.
 * @param simulationWeights Ekran 03'teki karara göre belirlenen ağırlıklar (Reddedildi ise refWeights ile aynı olmalı — K4).
 * @param baseValue Gerçek güncel AUM (Infina FundInfo — totalMarketPrice).
 */
export function runSimulation(
  fundCode: string,
  window: SimulationWindow,
  refWeights: Weights,
  simulationWeights: Weights,
  baseValue: number
): SimulationResult {
  const assetReturns = generateAssetReturns(fundCode)
  const slice = (returns: number[]) => returns.slice(MAX_WINDOW - window)

  const mevcutReturns = slice(weightedDailyReturns(assetReturns, refWeights))
  const simulasyonReturns = slice(weightedDailyReturns(assetReturns, simulationWeights))
  const benchmarkReturns = slice(weightedDailyReturns(assetReturns, BENCH_WEIGHTS))

  const mevcutCurve = toCurve(mevcutReturns, baseValue)
  const simulasyonCurve = toCurve(simulasyonReturns, baseValue)
  const benchmarkCurve = toCurve(benchmarkReturns, baseValue)

  const points = mevcutCurve.map((_, i) => ({
    day: i,
    mevcut: mevcutCurve[i],
    simulasyon: simulasyonCurve[i],
    benchmark: benchmarkCurve[i],
  }))

  return {
    points,
    metrics: {
      mevcut: metricsFromCurve(mevcutCurve),
      simulasyon: metricsFromCurve(simulasyonCurve),
      benchmark: metricsFromCurve(benchmarkCurve),
    },
  }
}
