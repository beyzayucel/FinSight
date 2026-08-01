import { useState } from 'react'
import type { FormEvent } from 'react'
import { Button, Checkbox, PasswordField, TextField } from '@/components/ui'
import type { Translations } from '@/i18n/translations'
import { MdOutlineEmail } from "react-icons/md";
import { IoLockClosed } from 'react-icons/io5';

type LoginFormProps = {
  t: Translations
}

/** Şimdilik frontend için gösterimini sağlıyor. 
 * Backende bağalanacak. */
export default function LoginForm({ t }: LoginFormProps) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    console.log({ email, password, remember })
  }

  return (
    <form onSubmit={handleSubmit} className="mt-8 space-y-5">
      <TextField
        id="email"
        type="email"
        label={t.emailLabel}
        autoComplete="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder={t.emailPlaceholder}
        icon={<MdOutlineEmail />}
      />

      <PasswordField
        id="password"
        label={t.passwordLabel}
        autoComplete="current-password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder={t.passwordPlaceholder}
        icon={<IoLockClosed />}
        labelAction={
          <button
            type="button"
            className="text-sm font-semibold text-primary transition-colors hover:text-primary-dark"
          >
            {t.forgotPassword}
          </button>
        }
      />

      <Checkbox
        checked={remember}
        onChange={(e) => setRemember(e.target.checked)}
        label={t.rememberMe}
      />

      <Button type="submit">{t.loginButton}</Button>
    </form>
  )
}
