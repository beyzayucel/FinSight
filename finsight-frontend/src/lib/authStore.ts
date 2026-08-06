const TOKEN_KEY = 'finsight_access_token'
const REFRESH_KEY = 'finsight_refresh_token'
const LANG_KEY = 'finsight_lang'

let accessToken: string | null = localStorage.getItem(TOKEN_KEY)
let refreshToken: string | null = localStorage.getItem(REFRESH_KEY)
let lang: string = localStorage.getItem(LANG_KEY) || 'tr'
const OTP_PENDING_KEY = 'finsight_otp_pending'
let otpPending: boolean = sessionStorage.getItem(OTP_PENDING_KEY) === 'true'

export function setTokens(access: string, refresh: string) {
  accessToken = access
  refreshToken = refresh
  localStorage.setItem(TOKEN_KEY, access)
  localStorage.setItem(REFRESH_KEY, refresh)
}

export function getAccessToken() {
  return accessToken
}

export function getRefreshToken() {
  return refreshToken
}

export function clearTokens() {
  accessToken = null
  refreshToken = null
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_KEY)
}

export function setLang(l: string) {
  lang = l
  localStorage.setItem(LANG_KEY, l)
}

export function getLang() {
  return lang
}

export function setOtpPending(v: boolean) {
  otpPending = v
  if (v) {
    sessionStorage.setItem(OTP_PENDING_KEY, 'true')
  } else {
    sessionStorage.removeItem(OTP_PENDING_KEY)
  }
}

export function isOtpPending() {
  return otpPending
}

const OTP_IDENTIFIER_KEY = 'finsight_otp_identifier'
const OTP_TIMESTAMP_KEY = 'finsight_otp_timestamp'
const OTP_TTL_MS = 3 * 60 * 1000 // 3 dakika — backend OTP_EXPIRE_DURATION ile aynı

export function setOtpIdentifier(id: string) {
  sessionStorage.setItem(OTP_IDENTIFIER_KEY, id)
  sessionStorage.setItem(OTP_TIMESTAMP_KEY, Date.now().toString())
}

export function getOtpIdentifier() {
  return sessionStorage.getItem(OTP_IDENTIFIER_KEY)
}

export function isOtpExpired() {
  const ts = sessionStorage.getItem(OTP_TIMESTAMP_KEY)
  if (!ts) return true
  return Date.now() - Number(ts) > OTP_TTL_MS
}

export function getPostLoginRoute(): string {
  const token = getAccessToken()
  if (!token) return '/dashboard'
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const roles: string[] = payload.roles ?? []
    return roles.includes('ROLE_ADMIN') ? '/admin' : '/dashboard'
  } catch {
    return '/dashboard'
  }
}

export function clearOtpSession() {
  otpPending = false
  sessionStorage.removeItem(OTP_PENDING_KEY)
  sessionStorage.removeItem(OTP_IDENTIFIER_KEY)
  sessionStorage.removeItem(OTP_TIMESTAMP_KEY)
}

export function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now() + 5000
  } catch {
    return true
  }
}

/**
 * Access token expire olduysa refresh token ile yenile.
 * Başarılı → true, başarısız → false (clearTokens çağırır).
 */
export async function tryRefreshIfExpired(): Promise<boolean> {
  const token = getAccessToken()
  if (!token) return false
  if (!isTokenExpired(token)) return true // hâlâ geçerli

  const rt = getRefreshToken()
  if (!rt) {
    clearTokens()
    return false
  }

  try {
    const baseURL = import.meta.env.VITE_API_BASE_URL
    const res = await fetch(`${baseURL}/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: rt }),
    })
    if (!res.ok) throw new Error('refresh failed')
    const json = await res.json()
    const data = json.data
    setTokens(data.accessToken, data.refreshToken)
    return true
  } catch {
    clearTokens()
    return false
  }
}
