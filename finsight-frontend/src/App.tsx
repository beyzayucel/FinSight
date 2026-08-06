import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ROUTES } from '@/lib/routes'
import ProtectedRoute from '@/components/ProtectedRoute'
import LoginPage from '@/features/auth/LoginPage'
import ForgotPasswordPage from '@/features/auth/ForgotPasswordPage'
import ResetPasswordPage from '@/features/auth/ResetPasswordPage'
import OtpPage from '@/features/auth/OtpPage'
import ChangePasswordPage from '@/features/auth/ChangePasswordPage'
import VerifyEmailPage from '@/features/auth/VerifyEmailPage'
import DashboardPage from '@/features/dashboard/DashboardPage'
import AdminRoute from '@/components/AdminRoute'
import AdminDashboardPage from '@/features/admin/AdminDashboardPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path={ROUTES.LOGIN} element={<LoginPage />} />
        <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPasswordPage />} />
        <Route path={ROUTES.RESET_PASSWORD} element={<ResetPasswordPage />} />
        <Route path={ROUTES.OTP} element={<OtpPage />} />
        <Route path={ROUTES.VERIFY_EMAIL} element={<VerifyEmailPage />} />

        {/* Protected — token yoksa login'e atar */}
        <Route element={<ProtectedRoute />}>
          <Route path={ROUTES.CHANGE_PASSWORD} element={<ChangePasswordPage />} />
          <Route path={ROUTES.DASHBOARD} element={<DashboardPage />} />
        </Route>

        {/* Admin — token + ROLE_ADMIN yoksa redirect */}
        <Route element={<AdminRoute />}>
          <Route path={ROUTES.ADMIN_DASHBOARD} element={<AdminDashboardPage />} />
        </Route>

        <Route path="*" element={<Navigate to={ROUTES.LOGIN} replace />} />
      </Routes>
    </BrowserRouter>
  )
}
