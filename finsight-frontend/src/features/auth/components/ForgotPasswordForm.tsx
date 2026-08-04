import { useState } from 'react'
import type { FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, TextField } from '@/components/ui'
import type { Translations } from '@/i18n/translations'
import { ROUTES } from '@/lib/routes'
import { MdOutlineEmail } from 'react-icons/md'
import { IoArrowBack } from 'react-icons/io5'

type ForgotPasswordFormProps = {
  t: Translations
}

/** Şimdilik frontend için gösterimini sağlıyor.
 * Backende bağlanacak. */
export default function ForgotPasswordForm({ t }: ForgotPasswordFormProps) {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    console.log({ email })
  }

  return (
    <form onSubmit={handleSubmit} className="mt-8 space-y-5">
      <TextField
        id="email"
        type="email"
        label={t.forgotPasswordEmailLabel}
        autoComplete="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder={t.forgotPasswordEmailPlaceholder}
        icon={<MdOutlineEmail />}
      />

      <Button type="submit">{t.continueButton}</Button>

      <button
        type="button"
        onClick={() => navigate(ROUTES.LOGIN)}
        className="flex items-center gap-1.5 text-sm font-semibold text-primary transition-colors hover:text-primary-dark"
      >
        <IoArrowBack /> {t.backToLogin}
      </button>
    </form>
  )
}
