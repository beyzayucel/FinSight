import { useState } from 'react'
import BrandPanel from './components/BrandPanel'
import LanguageSwitcher from './components/LanguageSwitcher'
import LoginForm from './components/LoginForm'
import { translations, type Lang } from '@/i18n/translations'
import { getLang, setLang as setStoreLang } from '@/lib/authStore'

export default function LoginPage() {
  const stored = getLang()
  const [lang, setLang] = useState<Lang>(stored === 'en' ? 'en' : 'tr')
  const t = translations[lang]

  function handleLangChange(newLang: Lang) {
    setLang(newLang)
    setStoreLang(newLang)
  }

  return (
    <div className="grid min-h-screen grid-cols-1 lg:grid-cols-2">
      {/* ---------- Haber Paneli ---------- */}
      <BrandPanel lang={lang} t={t} />

      {/* ---------- Login Paneli ---------- */}
      <main className="relative flex items-center justify-center bg-surface px-6 py-12 sm:px-10">
        <LanguageSwitcher lang={lang} onChange={handleLangChange} className="absolute right-8 top-8" />

        <div className="w-full max-w-sm">
          <div className="mb-5 flex justify-center">
            <img src="/logo.png" alt="FINSIGHT" className="h-auto w-44" />
          </div>

          <h2 className="text-3xl font-semibold text-ink">{t.loginTitle}</h2>
          <p className="mt-1.5 text-sm text-muted">{t.loginSubtitle}</p>

          <LoginForm t={t} />
        </div>
      </main>
    </div>
  )
}
