import { useState, useEffect, useMemo } from 'react'
import { useNavigate, useLocation, Navigate } from 'react-router-dom'
import { Button, TextField } from '@/components/ui'
import { otpVerify, otpResend } from '@/features/auth/authApi'
import { setTokens, isOtpPending, clearOtpSession, getOtpIdentifier, isOtpExpired, setOtpIdentifier, getPostLoginRoute } from '@/lib/authStore'
import { getTranslations } from '@/i18n/translations'
import { ROUTES } from '@/lib/routes'
import { getApiError } from '@/lib/api/apiError'

const COOLDOWN_SECONDS = 60

export default function OtpPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const identifier = (location.state?.identifier as string) || getOtpIdentifier() || ''
  const t = getTranslations()

  // Guard değerlendirmesini render-time'da yap ama side effect'i useEffect'e bırak
  const shouldRedirect = useMemo(
    () => !identifier || !isOtpPending() || isOtpExpired(),
    [identifier],
  )

  const [code, setCode] = useState('')
  const [error, setError] = useState('')
  const [cooldown, setCooldown] = useState(COOLDOWN_SECONDS)
  const [loading, setLoading] = useState(false)
  const [remainingAttempts, setRemainingAttempts] = useState<number | null>(null)

  useEffect(() => {
    if (shouldRedirect) clearOtpSession()
  }, [shouldRedirect])

  useEffect(() => {
    if (cooldown <= 0) return
    const timer = setInterval(() => setCooldown((c) => c - 1), 1000)
    return () => clearInterval(timer)
  }, [cooldown])

  if (shouldRedirect) {
    return <Navigate to={ROUTES.LOGIN} replace />
  }

  function handleCodeChange(e: React.ChangeEvent<HTMLInputElement>) {
    const value = e.target.value.replace(/\D/g, '').slice(0, 6)
    setCode(value)
    setError('')
  }

  async function handleVerify(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await otpVerify({ identifier, code })
      const result = response.data.data

      clearOtpSession()
      setTokens(result.accessToken, result.refreshToken)
      navigate(result.firstLogin ? ROUTES.CHANGE_PASSWORD : getPostLoginRoute(), { replace: true })
    } catch (err) {
      const apiErr = getApiError(err)

      if (apiErr.status === 429) {
        clearOtpSession()
        navigate(ROUTES.LOGIN, { state: { error: apiErr.message }, replace: true })
      } else {
        setError(apiErr.message || t.otpFallbackError)
        if (apiErr.remainingAttempts != null) {
          setRemainingAttempts(apiErr.remainingAttempts)
        }
      }
    } finally {
      setLoading(false)
    }
  }

  async function handleResend() {
    setError('')

    try {
      await otpResend({ identifier })
      setOtpIdentifier(identifier)
      setCooldown(COOLDOWN_SECONDS)
    } catch (err) {
      const apiErr = getApiError(err)
      setError(apiErr.message || t.otpResendFallbackError)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface px-6">
      <div className="w-full max-w-sm">
        <h2 className="text-3xl font-semibold text-ink">{t.otpTitle}</h2>
        <p className="mt-1.5 text-sm text-muted">{t.otpSubtitle}</p>

        <form onSubmit={handleVerify} className="mt-8 space-y-5">
          <TextField
            id="otp-code"
            type="text"
            inputMode="numeric"
            label={t.otpLabel}
            value={code}
            onChange={handleCodeChange}
            placeholder="123456"
            autoComplete="one-time-code"
            maxLength={6}
          />

          {error && (
            <p className="text-sm text-red-500">{error}</p>
          )}

          {remainingAttempts != null && (
            <p className="text-sm text-amber-600">
              {t.otpRemainingAttempts
                .replace('{remaining}', String(remainingAttempts))
                .replace('{max}', '5')}
            </p>
          )}

          <Button type="submit" disabled={loading || code.length !== 6}>
            {loading ? t.otpVerifying : t.otpVerify}
          </Button>

          <div className="text-center">
            <button
              type="button"
              onClick={handleResend}
              disabled={cooldown > 0}
              className={`w-full rounded-xl border py-3 text-sm font-semibold transition-colors ${
                cooldown > 0
                  ? 'cursor-not-allowed border-slate-200 bg-slate-50 text-slate-400'
                  : 'border-primary text-primary hover:bg-primary/5'
              }`}
            >
              {cooldown > 0 ? `${t.otpResend} (${cooldown}s)` : t.otpResend}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
