import type { Lang } from '@/i18n/translations'
import api from '@/lib/api/client'

export type NewsItem = {
  id: string
  text: string
  time: string
  url?: string
}

type NewsApiItem = {
  title: string
  url: string
  hoursAgo: number
}

export async function fetchHighlights(lang: Lang): Promise<NewsItem[]> {
  try {
    const response = await api.get<{ success: boolean; data: NewsApiItem[] }>('/news')

    if (!response.data.success || !Array.isArray(response.data.data)) {
      return []
    }

    return response.data.data.map((item, index) => ({
      id: item.url || index.toString(),
      text: item.title,
      time:
        lang === 'tr'
          ? item.hoursAgo === 0 ? 'yeni' : `${item.hoursAgo}s önce`
          : item.hoursAgo === 0 ? 'new' : `${item.hoursAgo}h ago`,
      url: item.url,
    }))
  } catch {
    return []
  }
}
