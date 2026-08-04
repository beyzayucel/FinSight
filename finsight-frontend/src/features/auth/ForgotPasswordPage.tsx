import { useState } from 'react'
import LanguageSwitcher from './components/LanguageSwitcher'
import ForgotPasswordForm from './components/ForgotPasswordForm'
import { translations, type Lang } from '@/i18n/translations'
import { getLang, setLang as setStoreLang } from '@/lib/authStore'

export default function ForgotPasswordPage() {
  const stored = getLang()
  const [lang, setLang] = useState<Lang>(stored === 'en' ? 'en' : 'tr')
  const t = translations[lang]

  function handleLangChange(newLang: Lang) {
    setLang(newLang)
    setStoreLang(newLang)
  }

  return (
    <main className="relative flex min-h-screen items-center justify-center bg-surface px-6 py-12 sm:px-10">
      <LanguageSwitcher lang={lang} onChange={handleLangChange} className="absolute right-8 top-8" />

      <div className="w-full max-w-sm">
        <div className="mb-5 flex justify-center">
          <img src="/logo.png" alt="FINSIGHT" className="h-auto w-44" />
        </div>

        <h2 className="text-center text-3xl font-semibold text-ink">{t.forgotPasswordTitle}</h2>
        <p className="mt-1.5 text-center text-sm text-muted">{t.forgotPasswordSubtitle}</p>

        <ForgotPasswordForm t={t} />
      </div>
    </main>
  )
}
