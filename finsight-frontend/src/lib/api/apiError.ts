import { AxiosError } from 'axios'

type FieldError = {
  field: string
  message: string
}

type ApiErrorBody = {
  error?: {
    message?: string
    code?: string
    fieldErrors?: FieldError[]
    remainingAttempts?: number
  }
}

type ApiError = {
  message: string
  code: string
  status: number
  fieldErrors?: FieldError[]
  remainingAttempts?: number
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
    fieldErrors: error?.fieldErrors,
    remainingAttempts: error?.remainingAttempts,
  }
}
