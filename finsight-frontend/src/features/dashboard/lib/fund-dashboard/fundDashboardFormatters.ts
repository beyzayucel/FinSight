import { formatSignedPercent } from '../formatters'

const MONTHS_TR = [
  'Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran',
  'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık',
]

export function formatIsoDate(iso: string): string {
  const [y, m, d] = iso.split('-')
  return `${d}.${m}.${y}`
}

export function formatMonthPeriod(period: string): string {
  const [y, m] = period.split('-')
  const monthName = MONTHS_TR[Number(m) - 1]
  return monthName ? `${monthName} ${y}` : period
}

export function formatAxisDate(iso: string): string {
  const [, m, d] = iso.split('-')
  return `${d}.${m}`
}

export function nominalDays(code: string): number {
  return Number(code.replace(/\D/g, '')) || 0
}

export function formatBps(value: number): string {
  const rounded = Math.round(value)
  return `${rounded > 0 ? '+' : ''}${rounded} bps`
}

export function formatIndexChange(index: number): string {
  return formatSignedPercent(index - 100)
}
