export const ROUTES = {
  LOGIN: '/login',
  FORGOT_PASSWORD: '/forgot-password',
  RESET_PASSWORD: '/reset-password',
  OTP: '/otp',
  CHANGE_PASSWORD: '/change-password',
  VERIFY_EMAIL: '/verify-email',

  // Fon paneli — sidebar'daki her menü kalemi kendi URL'sine sahip.
  FUND: '/fund',
  FUND_DASHBOARD: '/fund/dashboard',
  FUND_AI_DECISION: '/fund/ai-decision',
  FUND_PERFORMANCE: '/fund/performance',
  FUND_STRESS_TEST: '/fund/stress-test',
  FUND_DECISION_HISTORY: '/fund/decision-history',

  // Eski tek-sayfa adresi; /fund/dashboard'a yönlendiriliyor.
  DASHBOARD: '/dashboard',

  ADMIN_DASHBOARD: '/admin',
} as const
