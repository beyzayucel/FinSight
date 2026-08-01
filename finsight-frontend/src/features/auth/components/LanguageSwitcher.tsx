import { Fragment } from 'react'
import type { Lang } from '@/i18n/translations'

type LanguageSwitcherProps = {
  lang: Lang
  onChange: (lang: Lang) => void
  className?: string
}

const LANGS: Lang[] = ['tr', 'en']

export default function LanguageSwitcher({ lang, onChange, className = '' }: LanguageSwitcherProps) {
  return (
    <div className={`flex items-center gap-2 text-sm font-semibold ${className}`}>
      {LANGS.map((code, i) => (
        <Fragment key={code}>
          {i > 0 && <span className="text-muted/50">|</span>}
          <button
            type="button"
            onClick={() => onChange(code)}
            className={
              lang === code ? 'text-ink' : 'text-muted transition-colors hover:text-ink'
            }
          >
            {code.toUpperCase()}
          </button>
        </Fragment>
      ))}
    </div>
  )
}
