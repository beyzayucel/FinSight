import type { Lang } from '@/i18n/translations'

export type NewsItem = {
  id: string
  text: string
  time: string
  url?: string
}

/** Şimdilik mock veri koyuldu*/
const PLACEHOLDER: Record<Lang, NewsItem[]> = {
  tr: [
    { id: '1', text: 'TCMB faiz kararı piyasalar tarafından yakından takip ediliyor.', time: '2s önce' },
    { id: '2', text: 'ABD enflasyon verisi öncesinde dolar endeksi yatay seyrediyor.', time: '5s önce' },
    { id: '3', text: 'Teknoloji hisseleri küresel borsalarda güçlü performans sergiliyor.', time: '1s önce' },
  ],
  en: [
    { id: '1', text: "The central bank's rate decision is being closely watched by markets.", time: '2m ago' },
    { id: '2', text: 'The dollar index holds steady ahead of US inflation data.', time: '5m ago' },
    { id: '3', text: 'Tech stocks post strong gains across global exchanges.', time: '1m ago' },
  ],
}

/** Buraya haber apisi eklencek. */
export async function fetchHighlights(lang: Lang): Promise<NewsItem[]> {
  try {
    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080'
    const response = await fetch(`${baseUrl}/api/v1/news`, {
      headers: {
        'Accept-Language': lang,
      },
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const json = await response.json()
    if (json && json.success && Array.isArray(json.data) && json.data.length > 0) {
      return json.data.map((item: any, index: number) => {
        const timeStr = lang === 'tr'
          ? (item.hoursAgo === 0 ? 'yeni' : `${item.hoursAgo}s önce`)
          : (item.hoursAgo === 0 ? 'new' : `${item.hoursAgo}h ago`)
        return {
          id: item.url || index.toString(),
          text: item.title,
          time: timeStr,
          url: item.url,
        }
      })
    }

    return PLACEHOLDER[lang]
  } catch (error) {
    console.warn('Failed to fetch highlights from API, falling back to mock data:', error)
    return PLACEHOLDER[lang]
  }
}
