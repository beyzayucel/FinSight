import type { Weights } from '@/features/dashboard/lib/simulation'

// Beyza'nın dashboardApi.ts'inin (features/dashboard/dashboardApi.ts) mock/localStorage
// katmanına salt-okunur bir köprü. Onun dosyalarına dokunmuyoruz, sadece
// applyManualScenario'nun yazdığı localStorage kaydını okuyup bizim Weights
// formatımıza (Turkish AssetClass anahtarları, 0-1 fraksiyon) çeviriyoruz.

type StoredCategory = 'STOCK' | 'REPO' | 'FUTURE' | 'FUND'

type StoredScenario = {
  fundId: string
  note?: string
  weights: Record<StoredCategory, number>
  appliedAt: string
}

const CATEGORY_TO_ASSET_CLASS: Record<StoredCategory, keyof Weights> = {
  STOCK: 'hisseSenedi',
  REPO: 'tersRepo',
  FUTURE: 'vadeliIslemNakitTeminati',
  FUND: 'yatirimFonuKatilmaPayi',
}

export type AppliedScenario = {
  weights: Weights
  note?: string
  appliedAt: string
}

export function getLatestAppliedScenario(): AppliedScenario | null {
  const raw = localStorage.getItem('finsight_applied_scenarios')
  if (!raw) return null

  let list: StoredScenario[]
  try {
    list = JSON.parse(raw)
  } catch {
    return null
  }

  const last = list[list.length - 1]
  if (!last) return null

  const weights = Object.fromEntries(
    Object.entries(last.weights).map(([category, pct]) => [
      CATEGORY_TO_ASSET_CLASS[category as StoredCategory],
      pct / 100,
    ])
  ) as Weights

  return { weights, note: last.note, appliedAt: last.appliedAt }
}
