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

export const CATEGORY_TO_ASSET_CLASS: Record<StoredCategory, keyof Weights> = {
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

function readStoredList(): StoredScenario[] {
  const raw = localStorage.getItem('finsight_applied_scenarios')
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function toAppliedScenario(stored: StoredScenario): AppliedScenario {
  const weights = Object.fromEntries(
    Object.entries(stored.weights).map(([category, pct]) => [
      CATEGORY_TO_ASSET_CLASS[category as StoredCategory],
      pct / 100,
    ])
  ) as Weights

  return { weights, note: stored.note, appliedAt: stored.appliedAt }
}

export function getLatestAppliedScenario(): AppliedScenario | null {
  const list = readStoredList()
  const last = list[list.length - 1]
  return last ? toAppliedScenario(last) : null
}
