import { useEffect, useState } from 'react'
import { translations, type Lang } from '@/i18n/translations'
import { fetchHighlights, type NewsItem } from './newsService'
import { FaArrowTrendUp } from 'react-icons/fa6'

type NewsHighlightsProps = {
  lang: Lang
  title: string
}

export default function NewsHighlights({ lang, title }: NewsHighlightsProps) {
  const [items, setItems] = useState<NewsItem[]>([])

  useEffect(() => {
    let active = true
    fetchHighlights(lang).then((data) => {
      if (active) setItems(data)
    })
    return () => {
      active = false
    }
  }, [lang])

  return (
    <div className="w-full max-w-md font-mono">
      <div className="mb-4 flex items-center gap-2 text-accent">
        <FaArrowTrendUp/>
        <span className="text-xs font-semibold tracking-[0.2em] text-white/90">
          {title}
        </span>
      </div>

      {items.length === 0 ? (
        <p className="text-sm font-light text-white/50 italic py-2">
          {translations[lang].noNews as string}
        </p>
      ) : (
        <ul className="space-y-3">
          {items.map((item) => (
            <li key={item.id} className="border-b border-white/15 pb-3">
              <p className="text-sm font-light leading-relaxed text-white/80">
                {item.url ? (
                  <a
                    href={item.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="hover:text-accent transition-colors duration-200"
                  >
                    {item.text}
                  </a>
                ) : (
                  item.text
                )}
                <span className="ml-2 whitespace-nowrap text-xs text-accent/90">
                  — {item.time}
                </span>
              </p>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
