import type { Lang } from '@/i18n/translations'

export type NewsItem = {
  id: string
  text: string
  time: string
  url?: string
}



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
    if (json && json.success && Array.isArray(json.data)) {
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

    throw new Error('Invalid response structure or success is false')
  } catch (error) {
    console.error('Failed to fetch highlights from API:', error)
    return []
  }
}
