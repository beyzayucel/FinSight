import { AxiosError } from 'axios'

type ApiErrorBody = {
  error?: {
    message?: string
    code?: string
  }
}

type ApiError = {
  message: string
  code: string
  status: number
}

const FALLBACK: ApiError = {
  message: 'Bir hata oluştu',
  code: '',
  status: 0,
}

export function getApiError(err: unknown): ApiError {
  if (!(err instanceof AxiosError)) return FALLBACK

  const resp = err.response
  const error = (resp?.data as ApiErrorBody)?.error

  return {
    message: error?.message ?? FALLBACK.message,
    code: error?.code ?? FALLBACK.code,
    status: resp?.status ?? FALLBACK.status,
  }
}
