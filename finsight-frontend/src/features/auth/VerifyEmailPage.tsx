import { useState, useEffect, useRef, useCallback } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { verifyEmail } from '@/features/auth/authApi'
import { getTranslations } from '@/i18n/translations'
import { ROUTES } from '@/lib/routes'
import { getApiError } from '@/lib/api/apiError'
import { Button, Toast } from '@/components/ui'
import { IoCheckmarkCircle, IoCloseCircle } from 'react-icons/io5'

export default function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const t = getTranslations()

  const [status, setStatus] = useState<'loading' | 'success' | 'error'>(
    token ? 'loading' : 'error',
  )
  const [errorMsg, setErrorMsg] = useState(token ? '' : t.veNoToken)
  const [showToast, setShowToast] = useState(false)

  const called = useRef(false)

  useEffect(() => {
    if (!token || called.current) return
    called.current = true

    verifyEmail(token)
      .then(() => {
        setStatus('success')
        setShowToast(true)
      })
      .catch((err) => {
        setStatus('error')
        setErrorMsg(getApiError(err).message || t.veError)
      })
  }, [token])

  const handleToastClose = useCallback(() => {
    navigate(ROUTES.LOGIN, { replace: true })
  }, [navigate])

  return (
    <div className="flex min-h-screen items-center justify-center bg-surface px-6">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white px-10 py-12 text-center shadow-sm">

        {status === 'loading' && (
          <>
            <div className="mx-auto mb-6 h-16 w-16 animate-spin rounded-full border-4 border-slate-200 border-t-primary" />
            <h2 className="text-3xl font-semibold text-ink">{t.veTitle}</h2>
            <p className="mt-4 text-base text-muted">{t.veVerifying}</p>
          </>
        )}

        {status === 'success' && (
          <>
            <IoCheckmarkCircle className="mx-auto mb-6 text-7xl text-green-500" />
            <h2 className="text-3xl font-semibold text-ink">{t.veTitle}</h2>
            <p className="mt-4 text-base text-green-600">{t.veSuccess}</p>
          </>
        )}

        {status === 'error' && (
          <>
            <IoCloseCircle className="mx-auto mb-6 text-7xl text-red-400" />
            <h2 className="text-3xl font-semibold text-ink">{t.veTitle}</h2>
            <p className="mt-4 text-base text-red-500">{errorMsg}</p>
            <div className="mt-8">
              <Button type="button" onClick={() => navigate(ROUTES.LOGIN, { replace: true })}>
                {t.veGoLogin}
              </Button>
            </div>
          </>
        )}
      </div>

      {showToast && (
        <Toast message={t.veRedirect} variant="success" duration={2500} onClose={handleToastClose} />
      )}
    </div>
  )
}
