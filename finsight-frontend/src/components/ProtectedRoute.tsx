import { Navigate, Outlet } from 'react-router-dom'
import { getAccessToken, clearTokens } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now() + 5000
  } catch {
    return true
  }
}

export default function ProtectedRoute() {
  const token = getAccessToken()

  if (!token || isTokenExpired(token)) {
    clearTokens()
    return <Navigate to={ROUTES.LOGIN} replace />
  }

  return <Outlet />
}
