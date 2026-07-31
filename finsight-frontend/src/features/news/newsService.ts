import type { Lang } from '@/i18n/translations'

export type NewsItem = {
  id: string
  text: string
  time: string
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
  return PLACEHOLDER[lang]
}
