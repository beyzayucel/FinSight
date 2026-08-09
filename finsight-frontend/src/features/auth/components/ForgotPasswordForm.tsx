import { useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, TextField } from '@/components/ui'
import type { Translations } from '@/i18n/translations'
import { ROUTES } from '@/lib/routes'
import { forgotPassword } from '@/features/auth/authApi'
import { getApiError } from '@/lib/api/apiError'
import { MdOutlineEmail } from 'react-icons/md'
import { IoArrowBack } from 'react-icons/io5'

type ForgotPasswordFormProps = {
  t: Translations
}

export default function ForgotPasswordForm({ t }: ForgotPasswordFormProps) {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')
  const submittingRef = useRef(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (submittingRef.current) return

    submittingRef.current = true
    setError('')
    setLoading(true)

    try {
      await forgotPassword({ email })
      setSuccess(true)
    } catch (err) {
      submittingRef.current = false
      const apiErr = getApiError(err)
      if (apiErr.status === 0) {
        setError(t.forgotPasswordFallbackError)
      } else {
        setError(apiErr.fieldErrors?.[0]?.message ?? apiErr.message)
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mt-8 space-y-5">
      <TextField
        id="email"
        type="email"
        label={t.forgotPasswordEmailLabel}
        autoComplete="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder={t.forgotPasswordEmailPlaceholder}
        icon={<MdOutlineEmail />}
        disabled={success}
      />

      {success && <p className="text-sm text-green-600">{t.forgotPasswordSuccess}</p>}
      {error && <p className="text-sm text-red-500">{error}</p>}

      <Button type="submit" disabled={loading || success}>
        {loading ? t.continueSending : t.continueButton}
      </Button>

      <button
        type="button"
        onClick={() => navigate(ROUTES.LOGIN)}
        className="flex items-center gap-1.5 text-sm font-semibold text-primary transition-colors hover:text-primary-dark"
      >
        <IoArrowBack /> {t.backToLogin}
      </button>
    </form>
  )
}
