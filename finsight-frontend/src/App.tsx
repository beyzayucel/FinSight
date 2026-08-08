import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ROUTES } from '@/lib/routes'
import ProtectedRoute from '@/components/ProtectedRoute'
import LoginPage from '@/features/auth/LoginPage'
import ForgotPasswordPage from '@/features/auth/ForgotPasswordPage'
import ResetPasswordPage from '@/features/auth/ResetPasswordPage'
import OtpPage from '@/features/auth/OtpPage'
import ChangePasswordPage from '@/features/auth/ChangePasswordPage'
import VerifyEmailPage from '@/features/auth/VerifyEmailPage'
import DashboardShell from '@/features/dashboard/DashboardShell'
import FundDashboardRoute from '@/features/dashboard/pages/FundDashboardRoute'
import AiDecisionPage from '@/features/dashboard/pages/AiDecisionPage'
import PerformanceComparisonRoute from '@/features/dashboard/pages/PerformanceComparisonRoute'
import StressTestPage from '@/features/dashboard/pages/StressTestPage'
import DecisionHistoryPage from '@/features/dashboard/pages/DecisionHistoryPage'
import { DecisionProvider } from '@/features/dashboard/context/DecisionContext'
import AdminRoute from '@/components/AdminRoute'
import AdminDashboardPage from '@/features/admin/AdminDashboardPage'
import AdminPanelPage from '@/features/admin/AdminPanelPage'

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

          {/* Fon paneli — sidebar kabuğu sabit, içerik route'a göre değişiyor */}
          <Route
            path={ROUTES.FUND}
            element={
              <DecisionProvider>
                <DashboardShell />
              </DecisionProvider>
            }
          >
            <Route index element={<Navigate to={ROUTES.FUND_DASHBOARD} replace />} />
            <Route path="dashboard" element={<FundDashboardRoute />} />
            <Route path="ai-decision" element={<AiDecisionPage />} />
            <Route path="performance" element={<PerformanceComparisonRoute />} />
            <Route path="stress-test" element={<StressTestPage />} />
            <Route path="decision-history" element={<DecisionHistoryPage />} />
          </Route>
        </Route>

        {/* Admin — token + ROLE_ADMIN yoksa redirect. Varsayılan sekme: Kullanıcı Yönetimi. */}
        <Route element={<AdminRoute />}>
          <Route path={ROUTES.ADMIN_DASHBOARD} element={<Navigate to={ROUTES.ADMIN_USERS} replace />} />
          <Route path={ROUTES.ADMIN_PANEL} element={<AdminPanelPage />} />
          <Route path={ROUTES.ADMIN_USERS} element={<AdminDashboardPage />} />
        </Route>

        {/* Eski /dashboard adresi */}
        <Route path={ROUTES.DASHBOARD} element={<Navigate to={ROUTES.FUND_DASHBOARD} replace />} />

        <Route path="*" element={<Navigate to={ROUTES.LOGIN} replace />} />
      </Routes>
    </BrowserRouter>
  )
}
