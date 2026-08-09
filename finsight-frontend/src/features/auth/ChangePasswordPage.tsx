import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, PasswordField, Toast } from '@/components/ui'
import { changePassword } from '@/features/auth/authApi'
import { clearTokens } from '@/lib/authStore'
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

export default function ChangePasswordPage() {
  const navigate = useNavigate()
  const t = getTranslations()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)
  const [loading, setLoading] = useState(false)

  const checks = getPasswordChecks(newPassword)
  const allChecksPassed = Object.values(checks).every(Boolean)

  const handleToastClose = useCallback(() => {
    navigate(ROUTES.LOGIN, { replace: true, state: { passwordChanged: true } })
  }, [navigate])

  async function handleSubmit(e: React.FormEvent) {
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
      await changePassword({ currentPassword, newPassword })
      clearTokens()
      setSuccess(true)
    } catch (err) {
      const apiErr = getApiError(err)
      setError(apiErr.message || t.cpFallbackError)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface px-6">
      <div className="w-full max-w-sm">
        <h2 className="text-3xl font-semibold text-ink">{t.cpTitle}</h2>
        <p className="mt-1.5 text-sm text-muted">{t.cpSubtitle}</p>

        <form onSubmit={handleSubmit} className="mt-8 space-y-5">
          <PasswordField
            id="current-password"
            label={t.cpCurrentLabel}
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            placeholder={t.cpCurrentPlaceholder}
            autoComplete="off"
            icon={<IoLockClosed />}
          />

          <PasswordField
            id="new-password"
            label={t.cpNewLabel}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder={t.cpNewPlaceholder}
            autoComplete="new-password"
            icon={<IoLockClosed />}
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
          />

          {confirmPassword.length > 0 && newPassword !== confirmPassword && (
            <p className="text-sm text-red-500">{t.cpMismatch}</p>
          )}

          {newPassword.length > 0 && currentPassword.length > 0 && newPassword === currentPassword && (
            <p className="text-sm text-red-500">{t.cpSamePassword}</p>
          )}

          {error && (
            <p className="text-sm text-red-500">{error}</p>
          )}

          <Button type="submit" disabled={loading || !currentPassword || !newPassword || !confirmPassword || !allChecksPassed || newPassword !== confirmPassword || newPassword === currentPassword}>
            {loading ? t.cpChanging : t.cpButton}
          </Button>
        </form>

        {success && (
          <Toast message={t.cpSuccess} variant="success" duration={2000} onClose={handleToastClose} />
        )}
      </div>
    </div>
  )
}
