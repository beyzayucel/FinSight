import { useState, useEffect } from 'react'
import type { FormEvent } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Button, Checkbox, PasswordField, TextField } from '@/components/ui'
import type { Translations } from '@/i18n/translations'
import { ROUTES } from '@/lib/routes'
import { getApiError } from '@/lib/api/apiError'
import { MdPersonOutline } from 'react-icons/md'
import { IoLockClosed } from 'react-icons/io5'
import { login } from '@/features/auth/authApi'
import { setTokens, setOtpPending, setOtpIdentifier, getPostLoginRoute } from '@/lib/authStore'

type LoginFormProps = {
  t: Translations
}

const REMEMBERED_ID_KEY = 'finsight_remembered_id'

export default function LoginForm({ t }: LoginFormProps) {
  const navigate = useNavigate()
  const location = useLocation()

  const savedId = localStorage.getItem(REMEMBERED_ID_KEY) ?? ''
  const [identifier, setIdentifier] = useState(savedId)
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(savedId !== '')
  const [error, setError] = useState(location.state?.error ?? '')
  const [errorCode, setErrorCode] = useState(location.state?.errorCode ?? '')
  const [loading, setLoading] = useState(false)
  const cameFromPasswordChange = location.state?.passwordChanged === true

  useEffect(() => {
    const el = document.getElementById('password') as HTMLInputElement | null
    const clearPassword = () => {
      setPassword('')
      if (el) el.value = ''
    }
    const delays = [50, 150, 300, 600]
    const timers = delays.map((ms) => setTimeout(clearPassword, ms))

    if (cameFromPasswordChange) {
      setIdentifier('')
      const idEl = document.getElementById('identifier') as HTMLInputElement | null
      if (idEl) idEl.value = ''
    }

    return () => timers.forEach(clearTimeout)
  }, [])

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setErrorCode('')

    const trimmedId = identifier.trim()

    if (!trimmedId && !password) {
      setError(t.identifierAndPasswordRequired)
      return
    }
    if (!trimmedId) {
      setError(t.identifierRequired)
      return
    }
    if (!password) {
      setError(t.passwordRequired)
      return
    }

    setLoading(true)

    try {
      const response = await login({ identifier: trimmedId, password })
      const result = response.data.data

      if (remember) {
        localStorage.setItem(REMEMBERED_ID_KEY, trimmedId)
      } else {
        localStorage.removeItem(REMEMBERED_ID_KEY)
      }

      if (result.type === 'AUTHENTICATED') {
        setTokens(result.accessToken, result.refreshToken)
        navigate(result.firstLogin ? ROUTES.CHANGE_PASSWORD : getPostLoginRoute(), { replace: true })
      } else {
        setOtpPending(true)
        setOtpIdentifier(identifier)
        navigate(ROUTES.OTP, { state: { identifier }, replace: true })
      }
    } catch (err) {
      const apiErr = getApiError(err)
      setErrorCode(apiErr.code)
      setError(apiErr.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mt-8 space-y-5">
      <TextField
        id="identifier"
        type="text"
        label={t.identifierLabel}
        autoComplete="username"
        value={identifier}
        onChange={(e) => setIdentifier(e.target.value)}
        placeholder={t.identifierPlaceholder}
        icon={<MdPersonOutline />}
      />

      <PasswordField
        id="password"
        label={t.passwordLabel}
        autoComplete="current-password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder={t.passwordPlaceholder}
        icon={<IoLockClosed />}
        labelAction={
          <button
            type="button"
            onClick={() => navigate(ROUTES.FORGOT_PASSWORD)}
            className="text-sm font-semibold text-primary transition-colors hover:text-primary-dark"
          >
            {t.forgotPassword}
          </button>
        }
      />

      <Checkbox
        checked={remember}
        onChange={(e) => setRemember(e.target.checked)}
        label={t.rememberMe}
      />

      {error && errorCode === 'EMAIL_NOT_VERIFIED' && (
        <div className="rounded-lg border border-amber-300 bg-amber-50 p-3">
          <p className="text-sm font-medium text-amber-800">{error}</p>
          <p className="mt-1 text-xs text-amber-600">{t.emailNotVerifiedHint}</p>
        </div>
      )}

      {error && (errorCode === 'ACCOUNT_LOCKED' || errorCode === 'OTP_ABUSE_LOCKED') && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-3">
          <p className="text-sm font-medium text-red-700">{error}</p>
          <p className="mt-1 text-xs text-red-500">{t.accountLockedHint}</p>
        </div>
      )}

      {error && errorCode !== 'EMAIL_NOT_VERIFIED' && errorCode !== 'ACCOUNT_LOCKED' && errorCode !== 'OTP_ABUSE_LOCKED' && (
        <p className="text-sm text-red-500">{error}</p>
      )}

      <Button type="submit" disabled={loading}>
        {loading ? '...' : t.loginButton}
      </Button>
    </form>
  )
}
