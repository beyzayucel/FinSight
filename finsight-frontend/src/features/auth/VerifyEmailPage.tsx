import { useState, useEffect, useRef } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { verifyEmail } from '@/features/auth/authApi'
import { getTranslations } from '@/i18n/translations'
import { ROUTES } from '@/lib/routes'
import { getApiError } from '@/lib/api/apiError'

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const t = getTranslations()

  const [status, setStatus] = useState<'loading' | 'success' | 'error'>(
    token ? 'loading' : 'error',
  )
  const [errorMsg, setErrorMsg] = useState(token ? '' : t.veNoToken)

  const called = useRef(false)

  useEffect(() => {
    if (!token || called.current) return
    called.current = true

    verifyEmail(token)
      .then(() => {
        setStatus('success')
        setTimeout(() => navigate(ROUTES.LOGIN, { replace: true }), 3000)
      })
      .catch((err) => {
        setStatus('error')
        setErrorMsg(getApiError(err).message || t.veError)
      })
  }, [token])

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface px-6">
      <div className="w-full max-w-sm text-center">
        <h2 className="text-3xl font-semibold text-ink">{t.veTitle}</h2>

        {status === 'loading' && (
          <p className="mt-6 text-muted">{t.veVerifying}</p>
        )}

        {status === 'success' && (
          <p className="mt-6 text-green-600">{t.veSuccess}</p>
        )}

        {status === 'error' && (
          <>
            <p className="mt-6 text-red-500">{errorMsg}</p>
            <button
              type="button"
              onClick={() => navigate(ROUTES.LOGIN, { replace: true })}
              className="mt-6 text-sm font-semibold text-primary hover:underline"
            >
              {t.veGoLogin}
            </button>
          </>
        )}
      </div>
    </div>
  )
}
