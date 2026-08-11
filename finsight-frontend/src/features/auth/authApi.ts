import api from '@/lib/api/client'

type LoginRequest = {
  identifier: string
  password: string
}

type ApiResponse<T> = {
  success: boolean
  data: T
  message?: string
  error?: {
    status: number
    code: string
    message: string
  }
}

type AuthenticatedResult = {
  type: 'AUTHENTICATED'
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  firstLogin: boolean
}

type OtpRequiredResult = {
  type: 'OTP_REQUIRED'
  message: string
}

type LoginResponse = AuthenticatedResult | OtpRequiredResult

type OtpVerifyRequest = {
  identifier: string
  code: string
}

type OtpResendRequest = {
  identifier: string
}

type ChangePasswordRequest = {
  currentPassword: string
  newPassword: string
}

type ForgotPasswordRequest = {
  email: string
}

type ResetPasswordRequest = {
  token: string
  newPassword: string
}

type RefreshTokenResponse = {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
}

export function login(data: LoginRequest) {
  return api.post<ApiResponse<LoginResponse>>('/auth/login', data)
}

export function otpVerify(data: OtpVerifyRequest) {
  return api.post<ApiResponse<AuthenticatedResult>>('/auth/otp/verify', data)
}

export function otpResend(data: OtpResendRequest) {
  return api.post<ApiResponse<void>>('/auth/otp/resend', data)
}

export function changePassword(data: ChangePasswordRequest) {
  return api.patch<ApiResponse<void>>('/auth/change-password', data)
}

export function forgotPassword(data: ForgotPasswordRequest) {
  return api.post<ApiResponse<void>>('/auth/forgot-password', data)
}

export function resetPassword(data: ResetPasswordRequest) {
  return api.post<ApiResponse<void>>('/auth/reset-password', data)
}

export function refreshTokens(refreshToken: string) {
  return api.post<ApiResponse<RefreshTokenResponse>>('/auth/refresh', { refreshToken })
}

export function verifyEmail(token: string) {
  return api.get<ApiResponse<void>>('/auth/verify', { params: { token } })
}

export type { ApiResponse, RefreshTokenResponse }
