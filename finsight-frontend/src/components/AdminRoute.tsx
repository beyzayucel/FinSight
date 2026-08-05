import { Navigate, Outlet } from 'react-router-dom'
import { getAccessToken, clearTokens } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'

function parseRoles(token: string): string[] {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.roles ?? []
  } catch {
    return []
  }
}

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now() + 5000
  } catch {
    return true
  }
}

export default function AdminRoute() {
  const token = getAccessToken()

  if (!token || isTokenExpired(token)) {
    clearTokens()
    return <Navigate to={ROUTES.LOGIN} replace />
  }

  const roles = parseRoles(token)
  if (!roles.includes('ROLE_ADMIN')) {
    return <Navigate to={ROUTES.DASHBOARD} replace />
  }

  return <Outlet />
}
