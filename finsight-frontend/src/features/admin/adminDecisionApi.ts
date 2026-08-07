import api from '@/lib/api/client'
import type { ApiResponse } from '@/features/auth/authApi'

export type DecisionType = 'AI_APPROVED' | 'AI_REJECTED' | 'MANUAL'

export type AdminDecisionRecord = {
  id: string
  decisionDate: string
  userName: string
  fundName: string
  decisionType: DecisionType
}

export type DecisionReportParams = {
  userId?: string
  type?: DecisionType
  days?: number
}

export function getDecisionReport(params: DecisionReportParams = {}) {
  const query: Record<string, string | number> = {}

  if (params.userId) query.userId = params.userId
  if (params.type) query.type = params.type
  if (params.days != null) query.days = params.days

  return api.get<ApiResponse<AdminDecisionRecord[]>>('/admin/decisions', { params: query })
}
