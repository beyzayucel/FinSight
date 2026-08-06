import { useState } from 'react'
import { getApiError } from '@/lib/api/apiError'
import type { ChangePasswordRequest } from '../adminApi'
import type { Translations } from '@/i18n/translations'

type ChangePasswordModalProps = {
  open: boolean
  onClose: () => void
  onSubmit: (data: ChangePasswordRequest) => Promise<void>
  t: Translations
}

export default function ChangePasswordModal({ open, onClose, onSubmit, t }: ChangePasswordModalProps) {
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (!open) return null

  function handleChange(field: string, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')

    if (form.newPassword !== form.confirmPassword) {
      setError(t.adminPasswordMismatch)
      return
    }

    if (form.newPassword.length < 8) {
      setError(t.adminPasswordTooShort)
      return
    }

    setLoading(true)
    try {
      await onSubmit({ currentPassword: form.currentPassword, newPassword: form.newPassword })
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
      onClose()
    } catch (err) {
      setError(getApiError(err).message)
    } finally {
      setLoading(false)
    }
  }

  function handleClose() {
    setForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    setError('')
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-admin-ink/40 backdrop-blur-sm" onClick={handleClose} />
      <div className="relative bg-white rounded-[18px] shadow-2xl border border-admin-line w-full max-w-md mx-4 p-6">
        <h3 className="font-heading text-lg font-bold text-admin-ink mb-1">{t.adminChangePasswordTitle}</h3>
        <p className="text-xs text-admin-text-mute mb-5">{t.adminChangePasswordSubtitle}</p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <PasswordField
            label={t.adminCurrentPassword}
            value={form.currentPassword}
            onChange={(v) => handleChange('currentPassword', v)}
            placeholder={t.adminCurrentPasswordPlaceholder}
          />
          <PasswordField
            label={t.adminNewPassword}
            value={form.newPassword}
            onChange={(v) => handleChange('newPassword', v)}
            placeholder={t.adminNewPasswordPlaceholder}
          />
          <PasswordField
            label={t.adminNewPasswordConfirm}
            value={form.confirmPassword}
            onChange={(v) => handleChange('confirmPassword', v)}
            placeholder={t.adminNewPasswordConfirmPlaceholder}
          />

          {error && (
            <div className="text-sm text-admin-red bg-admin-red-wash px-3 py-2 rounded-lg">{error}</div>
          )}

          <div className="flex gap-3 mt-2">
            <button
              type="button"
              onClick={handleClose}
              className="flex-1 py-2.5 rounded-[11px] border border-admin-line text-sm font-semibold text-admin-text-mute hover:bg-admin-ivory transition"
            >
              {t.adminCancel}
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 py-2.5 rounded-[11px] bg-gradient-to-br from-[#C99738] to-admin-gold text-[#241a08] text-sm font-bold shadow-[0_8px_18px_-6px_rgba(185,134,43,0.5)] hover:brightness-105 disabled:opacity-50 transition"
            >
              {loading ? t.adminChangingPassword : t.adminChangePassword}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

function PasswordField({
  label,
  value,
  onChange,
  placeholder,
}: {
  label: string
  value: string
  onChange: (value: string) => void
  placeholder: string
}) {
  const [visible, setVisible] = useState(false)

  return (
    <div>
      <label className="text-[10.5px] font-semibold text-admin-text-faint uppercase tracking-[0.06em] mb-1.5 block">
        {label}
      </label>
      <div className="relative">
        <input
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          required
          className="w-full bg-admin-ivory border border-admin-line rounded-[10px] py-[9px] px-3 pr-10 text-[13px] font-ibm text-admin-text focus:outline-none focus:border-admin-gold-soft transition"
        />
        <button
          type="button"
          onClick={() => setVisible(!visible)}
          className="absolute right-2.5 top-1/2 -translate-y-1/2 text-admin-text-faint hover:text-admin-text-mute transition"
          tabIndex={-1}
        >
          {visible ? (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-4 h-4">
              <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94" />
              <path d="M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19" />
              <path d="M14.12 14.12a3 3 0 11-4.24-4.24" />
              <path d="M1 1l22 22" />
            </svg>
          ) : (
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-4 h-4">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
              <circle cx="12" cy="12" r="3" />
            </svg>
          )}
        </button>
      </div>
    </div>
  )
}
