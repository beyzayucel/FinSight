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

api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  config.headers['Accept-Language'] = getLang()
  return config
})

let isRefreshing = false
let pendingRequests: Array<(token: string) => void> = []

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status !== 401 || originalRequest._retry) {
      return Promise.reject(error)
    }

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

    if (isRefreshing) {
      originalRequest._retry = true
      return new Promise((resolve) => {
        pendingRequests.push((newToken: string) => {
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          resolve(api(originalRequest))
        })
      })
    }

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

      pendingRequests.forEach((cb) => cb(newAccessToken))
      pendingRequests = []

      originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
      return api(originalRequest)
    } catch (refreshError) {
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
