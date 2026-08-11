import { normalizeCategoryName } from '../../lib/categoryNames'

const CATEGORY_COLORS: Record<string, string> = {
  'hisse senedi': '#1c2530',

  'ters-repo': '#c89834',
  'ters repo': '#c89834',

  'vadeli işlemler teminat': '#8fa3bf',
  'vadeli işl. nakit teminatı': '#8fa3bf',
  'vadeli işlem nakit teminatı': '#8fa3bf',

  'yatırım fonları katılma payları': '#d9cfb8',
  'yatırım fonu katılma payı': '#d9cfb8',
}

const FALLBACK_COLORS = ['#1c2530', '#c89834', '#8fa3bf', '#d9cfb8', '#6b7683']

export function categoryColor(category: string, index: number): string {
  return (
    CATEGORY_COLORS[normalizeCategoryName(category)] ??
    FALLBACK_COLORS[index % FALLBACK_COLORS.length]
  )
}
