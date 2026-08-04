import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Button, PasswordField } from '@/components/ui'
import { resetPassword } from '@/features/auth/authApi'
import { getTranslations } from '@/i18n/translations'
import { ROUTES } from '@/lib/routes'
import { getApiError } from '@/lib/api/apiError'
import { IoLockClosed } from 'react-icons/io5'

function getPasswordChecks(pw: string) {
  return {
    length: pw.length >= 8 && pw.length <= 32,
    lowercase: /\p{Ll}/u.test(pw),
    uppercase: /\p{Lu}/u.test(pw),
    digit: /[0-9]/.test(pw),
    special: [...pw].some((ch) => !/\p{L}/u.test(ch) && !/[0-9]/.test(ch)),
  }
}

export default function ResetPasswordPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const t = getTranslations()

  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

  const checks = getPasswordChecks(newPassword)
  const allChecksPassed = Object.values(checks).every(Boolean)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    setError('')

    if (!allChecksPassed) {
      setError(t.cpAllChecks)
      return
    }
    if (newPassword !== confirmPassword) {
      setError(t.cpMismatch)
      return
    }

    setLoading(true)

    try {
      await resetPassword({ token: token as string, newPassword })
      setSuccess(true)
      setTimeout(() => navigate(ROUTES.LOGIN, { replace: true }), 2000)
    } catch (err) {
      const apiErr = getApiError(err)
      setError(apiErr.status === 0 ? t.rpFallbackError : apiErr.message || t.rpFallbackError)
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-surface px-6">
        <div className="w-full max-w-sm text-center">
          <h2 className="text-3xl font-semibold text-ink">{t.rpTitle}</h2>
          <p className="mt-6 text-red-500">{t.rpNoToken}</p>
          <button
            type="button"
            onClick={() => navigate(ROUTES.FORGOT_PASSWORD)}
            className="mt-6 text-sm font-semibold text-primary hover:underline"
          >
            {t.rpRequestNewLink}
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface px-6">
      <div className="w-full max-w-sm">
        <h2 className="text-3xl font-semibold text-ink">{t.rpTitle}</h2>
        <p className="mt-1.5 text-sm text-muted">{t.rpSubtitle}</p>

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          <PasswordField
            id="new-password"
            label={t.cpNewLabel}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder={t.cpNewPlaceholder}
            autoComplete="new-password"
            icon={<IoLockClosed />}
            disabled={success}
          />

          {newPassword.length > 0 && (
            <ul className="space-y-1 text-xs">
              <li className={checks.length ? 'text-green-600' : 'text-red-500'}>
                {checks.length ? '✓' : '✗'} {t.cpCheckLength}
              </li>
              <li className={checks.lowercase ? 'text-green-600' : 'text-red-500'}>
                {checks.lowercase ? '✓' : '✗'} {t.cpCheckLower}
              </li>
              <li className={checks.uppercase ? 'text-green-600' : 'text-red-500'}>
                {checks.uppercase ? '✓' : '✗'} {t.cpCheckUpper}
              </li>
              <li className={checks.digit ? 'text-green-600' : 'text-red-500'}>
                {checks.digit ? '✓' : '✗'} {t.cpCheckDigit}
              </li>
              <li className={checks.special ? 'text-green-600' : 'text-red-500'}>
                {checks.special ? '✓' : '✗'} {t.cpCheckSpecial}
              </li>
            </ul>
          )}

          <PasswordField
            id="confirm-password"
            label={t.cpConfirmLabel}
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder={t.cpConfirmPlaceholder}
            autoComplete="new-password"
            icon={<IoLockClosed />}
            disabled={success}
          />

          {confirmPassword.length > 0 && newPassword !== confirmPassword && (
            <p className="text-sm text-red-500">{t.cpMismatch}</p>
          )}

          {success && <p className="text-sm text-green-600">{t.rpSuccess}</p>}
          {error && !success && <p className="text-sm text-red-500">{error}</p>}

          <Button
            type="submit"
            disabled={
              loading ||
              success ||
              !newPassword ||
              !confirmPassword ||
              !allChecksPassed ||
              newPassword !== confirmPassword
            }
          >
            {loading ? t.rpSubmitting : t.rpButton}
          </Button>
        </form>
      </div>
    </div>
  )
}
