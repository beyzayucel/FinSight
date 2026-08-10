import api from '@/lib/api/client'
import { getLang } from '@/lib/authStore'

export type FundChatSource = 'RULE' | 'KNOWLEDGE' | 'FALLBACK'

export type FundChatAnswer = {
  sessionId: string
  reply: string
  source: FundChatSource
  answeredAt: string
}

export const FUND_CHAT_MAX_MESSAGE = 1000

export async function askFundChat(
  fundCode: string,
  message: string,
  sessionId?: string | null
): Promise<FundChatAnswer> {
  const response = await api.post(`/funds/${fundCode}/chat`, {
    ...(sessionId ? { sessionId } : {}),
    message,
    language: getLang() === 'en' ? 'en' : 'tr',
  })
  return response.data.data
}

export async function resetFundChat(fundCode: string, sessionId: string): Promise<void> {
  await api.delete(`/funds/${fundCode}/chat/${sessionId}`)
}
