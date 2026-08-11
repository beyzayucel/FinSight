import { useState } from 'react'
import type { InputHTMLAttributes, ReactNode } from 'react'
import TextField from './TextField'
import { IoMdEye, IoMdEyeOff } from 'react-icons/io'
import { getTranslations } from '@/i18n/translations'

type PasswordFieldProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & {
  label: string
  icon?: ReactNode
  labelAction?: ReactNode
}

export default function PasswordField(props: PasswordFieldProps) {
  const [visible, setVisible] = useState(false)
  const t = getTranslations()

  return (
    <TextField
      {...props}
      type={visible ? 'text' : 'password'}
      trailing={
        <button
          type="button"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? t.hidePassword : t.showPassword}
          className="absolute right-3 top-1/2 -translate-y-1/2 rounded-lg p-1.5 text-muted transition-colors hover:text-ink"
        >
          {visible ? <IoMdEye /> : <IoMdEyeOff />}
        </button>
      }
    />
  )
}
