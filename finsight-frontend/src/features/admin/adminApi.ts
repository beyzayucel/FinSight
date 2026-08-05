import api from '@/lib/api/client'
import type { ApiResponse } from '@/features/auth/authApi'

// ── Response types ──

export type UserResponse = {
  id: string
  email: string
  username: string
  firstName: string
  lastName: string
  phoneNumber: string
  emailVerified: boolean
  enabled: boolean
  firstLogin: boolean
  role: string
  lastLoginAt: string | null
  createdAt: string
}

export type UserStatsResponse = {
  totalUsers: number
  activeUsers: number
  inactiveUsers: number
  todayLogins: number
}

export type SortOrder = {
  property: string
  direction: string
}

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
  sort: SortOrder[]
}

// ── Request types ──

export type CreateUserRequest = {
  email: string
  firstName: string
  lastName: string
  phoneNumber: string
}

export type UpdateUserRequest = {
  firstName: string
  lastName: string
  phoneNumber: string
}

export type UsersParams = {
  page?: number
  size?: number
  sort?: string
  search?: string
  enabled?: boolean | null
}

// ── API calls ──

export function getUsers(params: UsersParams = {}) {
  const query: Record<string, string | number> = {}

  if (params.page != null) query.page = params.page
  if (params.size != null) query.size = params.size
  if (params.sort) query.sort = params.sort
  if (params.search) query.search = params.search
  if (params.enabled != null) query.enabled = params.enabled ? 1 : 0

  return api.get<ApiResponse<PageResponse<UserResponse>>>('/users', { params: query })
}

export function getUserById(id: string) {
  return api.get<ApiResponse<UserResponse>>(`/users/${id}`)
}

export function getUserStats() {
  return api.get<ApiResponse<UserStatsResponse>>('/users/stats')
}

export function createUser(data: CreateUserRequest) {
  return api.post<ApiResponse<void>>('/users', data)
}

export function updateUser(id: string, data: UpdateUserRequest) {
  return api.put<ApiResponse<UserResponse>>(`/users/${id}`, data)
}

export function changeUserStatus(id: string, enabled: boolean) {
  return api.patch<ApiResponse<void>>(`/users/${id}/status`, null, {
    params: { enabled },
  })
}

export function deleteUser(id: string) {
  return api.delete<ApiResponse<void>>(`/users/${id}`)
}

export function resendVerification(id: string) {
  return api.post<ApiResponse<void>>(`/users/${id}/resend-verification`)
}

// ── Password ──

export type ChangePasswordRequest = {
  currentPassword: string
  newPassword: string
}

export function changePassword(data: ChangePasswordRequest) {
  return api.post<ApiResponse<void>>('/auth/change-password', data)
}

export function sendPasswordResetLink(email: string) {
  return api.post<ApiResponse<void>>('/auth/forgot-password', { email })
}
