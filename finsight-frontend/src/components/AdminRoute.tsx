import { useState, useEffect } from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { getAccessToken, tryRefreshIfExpired } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'

function parseRoles(token: string): string[] {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.roles ?? []
  } catch {
    return []
  }
}

export default function AdminRoute() {
  const [status, setStatus] = useState<'checking' | 'ok' | 'expired' | 'forbidden'>('checking')

  useEffect(() => {
    const token = getAccessToken()
    if (!token) {
      setStatus('expired')
      return
    }

    tryRefreshIfExpired().then((valid) => {
      if (!valid) {
        setStatus('expired')
        return
      }
      const freshToken = getAccessToken()!
      const roles = parseRoles(freshToken)
      setStatus(roles.includes('ROLE_ADMIN') ? 'ok' : 'forbidden')
    })
  }, [])

  if (status === 'checking') return null
  if (status === 'expired') return <Navigate to={ROUTES.LOGIN} replace />
  if (status === 'forbidden') return <Navigate to={ROUTES.DASHBOARD} replace />

  return <Outlet />
}
