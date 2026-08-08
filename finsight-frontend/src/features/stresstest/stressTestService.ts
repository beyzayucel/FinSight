import api from '@/lib/api/client'

const SAVE_DECISION_PATH = '/stress-tests/save'

export interface SaveDecisionPayload {
  /** Fon UUID'si — fon kodu ("TIE") değil. */
  fundId: string
  /** runSimulation'ın döndürdüğü sonuç id'si; backend sayıları yeniden hesaplamaz. */
  stressTestResultId: string
}

/**
 * "Karar Geçmişine Kaydet" — kaydedilmiş stres testi sonucunu o fondaki en güncel karara
 * (manuel senaryo ya da AI kararı) iliştirir. Henüz hiç karar verilmemişse backend 404 döner.
 */
export async function saveDecisionRecord(payload: SaveDecisionPayload): Promise<void> {
  await api.post(SAVE_DECISION_PATH, payload)
}
