import axios from 'axios'
import { getAccessToken, getRefreshToken, setTokens, clearTokens, getLang } from '@/lib/authStore'
import { ROUTES } from '@/lib/routes'

const baseURL = import.meta.env.VITE_API_BASE_URL
if (!baseURL) {
  throw new Error('VITE_API_BASE_URL is not defined. Check your .env file.')
}

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// ---- Request Interceptor ----
// Her istekte token varsa Authorization header'ına ekle
api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['Accept-Language'] = getLang()
  return config
})

// ---- Response Interceptor ----
// 401 gelirse refresh token ile yeni access token al, isteği tekrar at
let isRefreshing = false
let pendingRequests: Array<(token: string) => void> = []

api.interceptors.response.use(
  // başarılı response — dokunma, geç
  (response) => response,

  // hata response
  async (error) => {
    const originalRequest = error.config

    // 401 değilse veya refresh isteğinin kendisi başarısız olduysa — dokunma
    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

    // login, otp gibi public endpoint'lerde 401 gelirse refresh deneme
    const publicPaths = ['/auth/login', '/auth/otp/verify', '/auth/otp/resend', '/auth/refresh']
    if (publicPaths.some((path) => originalRequest.url?.includes(path))) {
      return Promise.reject(error)
    }

    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      clearTokens()
      window.location.href = ROUTES.LOGIN
      return Promise.reject(error)
    }

    // zaten refresh yapılıyorsa — kuyruğa ekle, bekle
    if (isRefreshing) {
      return new Promise((resolve) => {
        pendingRequests.push((newToken: string) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          resolve(api(originalRequest))
        })
      })
    }

    // refresh başlat
    originalRequest._retry = true
    isRefreshing = true

    try {
      const response = await axios.post(
        `${api.defaults.baseURL}/auth/refresh`,
        { refreshToken },
        { headers: { 'Content-Type': 'application/json' } }
      )

      const newAccessToken = response.data.data.accessToken
      const newRefreshToken = response.data.data.refreshToken
      setTokens(newAccessToken, newRefreshToken)

      // kuyruktaki istekleri yeni token'la çalıştır
      pendingRequests.forEach((cb) => cb(newAccessToken))
      pendingRequests = []

      // orijinal isteği yeni token'la tekrar at
      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
      return api(originalRequest)
    } catch (refreshError) {
      // refresh token da geçersiz — login'e gönder
      clearTokens()
      pendingRequests = []
      window.location.href = ROUTES.LOGIN
      return Promise.reject(refreshError)
    } finally {
      isRefreshing = false
    }
  }
)

export default api
