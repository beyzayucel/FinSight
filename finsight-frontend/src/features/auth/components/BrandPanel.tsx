import NewsHighlights from '@/features/news/NewsHighlights'
import type { Lang, Translations } from '@/i18n/translations'

type BrandPanelProps = {
  lang: Lang
  t: Translations
  className?: string
}

export default function BrandPanel({ lang, t, className = '' }: BrandPanelProps) {
  return (
    <aside className="relative hidden overflow-hidden bg-[#0a1019] lg:block">
      <div
        className="absolute inset-0 bg-cover bg-center"
        style={{ backgroundImage: 'url(/hero-bg.png)' }}
      />
      <div
        className="absolute inset-0"
        style={{
          background:
            'linear-gradient(180deg, rgba(8,14,26,0.62) 0%, rgba(8,14,26,0.5) 40%, rgba(8,14,26,0.9) 100%), radial-gradient(120% 85% at 20% 100%, rgba(6,11,20,0.9) 0%, transparent 65%)',
        }}
      />

      <div className={`relative flex h-full flex-col px-12 py-10 xl:px-16 ${className}`}>
        <div className="flex-[1.5]" />

        <div className="max-w-3xl">
          <p className="mb-2 text-base font-medium tracking-[0.15em] text-accent">
            — {t.tagline}
          </p>

          <h1 className="text-5xl font-light leading-[1.05] tracking-tight text-white xl:text-6xl">
            {t.heroTitle1}{' '}
            <span className="text-accent">{t.heroTitle2}</span>
            <br />
            {t.heroTitle3}
          </h1>
        </div>
        
        <div className="mt-10" />
          <NewsHighlights lang={lang} title={t.highlightsTitle} />
        <div className="flex-[0.12]" />
      </div>
    </aside>
  )
}
