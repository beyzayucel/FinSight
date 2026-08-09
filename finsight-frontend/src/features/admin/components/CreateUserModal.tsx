import { useState } from 'react'
import { getApiError } from '@/lib/api/apiError'
import type { CreateUserRequest } from '../adminApi'
import type { Translations } from '@/i18n/translations'

type CreateUserModalProps = {
  open: boolean
  onClose: () => void
  onSubmit: (data: CreateUserRequest) => Promise<void>
  t: Translations
}

export default function CreateUserModal({ open, onClose, onSubmit, t }: CreateUserModalProps) {
  const [form, setForm] = useState<CreateUserRequest>({
    email: '',
    firstName: '',
    lastName: '',
    phoneNumber: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  if (!open) return null

  function handleChange(field: keyof CreateUserRequest, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await onSubmit(form)
      setForm({ email: '', firstName: '', lastName: '', phoneNumber: '' })
      onClose()
    } catch (err) {
      setError(getApiError(err).message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto p-4">
      <div className="absolute inset-0 bg-admin-ink/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative max-h-[calc(100dvh-2rem)] w-full max-w-md overflow-y-auto rounded-[18px] border border-admin-line bg-white p-5 shadow-2xl sm:p-6">
        <h3 className="font-heading text-lg font-bold text-admin-ink mb-5">{t.adminCreateUserTitle}</h3>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <ModalField label={t.adminLabelEmail} value={form.email} onChange={(v) => handleChange('email', v)} placeholder="ornek@infina.com" type="email" />
          <ModalField label={t.adminLabelFirstName} value={form.firstName} onChange={(v) => handleChange('firstName', v)} placeholder="Ali Rıza" />
          <ModalField label={t.adminLabelLastName} value={form.lastName} onChange={(v) => handleChange('lastName', v)} placeholder="Kaygusuz" />
          <ModalField label={t.adminLabelPhone} value={form.phoneNumber} onChange={(v) => handleChange('phoneNumber', v)} placeholder="+905551234567" type="tel" />

          {error && (
            <div className="text-sm text-admin-red bg-admin-red-wash px-3 py-2 rounded-lg">{error}</div>
          )}

          <div className="flex gap-3 mt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 rounded-[11px] border border-admin-line text-sm font-semibold text-admin-text-mute hover:bg-admin-ivory transition"
            >
              {t.adminCancel}
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 py-2.5 rounded-[11px] bg-gradient-to-br from-[#C99738] to-admin-gold text-[#241a08] text-sm font-bold shadow-[0_8px_18px_-6px_rgba(185,134,43,0.5)] hover:brightness-105 disabled:opacity-50 transition"
            >
              {loading ? t.adminCreating : t.adminCreate}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

function ModalField({
  label,
  value,
  onChange,
  placeholder,
  type = 'text',
}: {
  label: string
  value: string
  onChange: (value: string) => void
  placeholder: string
  type?: string
}) {
  return (
    <div>
      <label className="text-[10.5px] font-semibold text-admin-text-faint uppercase tracking-[0.06em] mb-1.5 block">
        {label}
      </label>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        required
        className="w-full bg-admin-ivory border border-admin-line rounded-[10px] py-[9px] px-3 text-[13px] font-ibm text-admin-text focus:outline-none focus:border-admin-gold-soft transition"
      />
    </div>
  )
}
