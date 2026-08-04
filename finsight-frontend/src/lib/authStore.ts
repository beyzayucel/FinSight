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

export function clearOtpSession() {
  otpPending = false
  sessionStorage.removeItem(OTP_PENDING_KEY)
  sessionStorage.removeItem(OTP_IDENTIFIER_KEY)
  sessionStorage.removeItem(OTP_TIMESTAMP_KEY)
}
