import { useState, useEffect } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { getAccessToken, tryRefreshIfExpired } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'

export default function ProtectedRoute() {
  const [status, setStatus] = useState<'checking' | 'ok' | 'expired'>('checking')

  useEffect(() => {
    const token = getAccessToken()
    if (!token) {
      setStatus('expired')
      return
    }

    tryRefreshIfExpired().then((valid) => {
      setStatus(valid ? 'ok' : 'expired')
    })
  }, [])

  if (status === 'checking') return null
  if (status === 'expired') return <Navigate to={ROUTES.LOGIN} replace />

  return <Outlet />
}
