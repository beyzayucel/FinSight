export function formatCurrency(value: number): string {
  return `₺${Math.round(value).toLocaleString('tr-TR')}`
}

export function formatSignedPercent(value: number): string {
  const sign = value > 0 ? '+' : ''
  return `${sign}${value.toFixed(2).replace('.', ',')}%`
}

export function formatUnsignedPercent(value: number): string {
  return `${Math.abs(value).toFixed(2).replace('.', ',')}%`
}

export function formatDate(date: Date, lang: 'tr' | 'en'): string {
  return date.toLocaleDateString(lang === 'tr' ? 'tr-TR' : 'en-GB', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  })
}

export function formatDateTime(date: Date): string {
  return date.toLocaleString('tr-TR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}
