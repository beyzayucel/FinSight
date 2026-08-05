import { useState, useEffect } from 'react'
import type { UserResponse, UpdateUserRequest } from '../adminApi'

type EditUserModalProps = {
  user: UserResponse | null
  onClose: () => void
  onSubmit: (id: string, data: UpdateUserRequest) => Promise<void>
}

export default function EditUserModal({ user, onClose, onSubmit }: EditUserModalProps) {
  const [form, setForm] = useState<UpdateUserRequest>({
    firstName: '',
    lastName: '',
    phoneNumber: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (user) {
      setForm({
        firstName: user.firstName,
        lastName: user.lastName,
        phoneNumber: user.phoneNumber,
      })
      setError('')
    }
  }, [user])

  if (!user) return null

  function handleChange(field: keyof UpdateUserRequest, value: string) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await onSubmit(user!.id, form)
      onClose()
    } catch (err: any) {
      setError(err?.response?.data?.error?.message || 'Bir hata oluştu')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-admin-ink/40 backdrop-blur-sm" onClick={onClose} />
      <div className="relative bg-white rounded-[18px] shadow-2xl border border-admin-line w-full max-w-md mx-4 p-6">
        <h3 className="font-heading text-lg font-bold text-admin-ink mb-1">Kullanıcı Düzenle</h3>
        <p className="text-xs text-admin-text-mute mb-5">{user.email}</p>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <ModalField label="Ad" value={form.firstName} onChange={(v) => handleChange('firstName', v)} placeholder="Ali Rıza" />
          <ModalField label="Soyad" value={form.lastName} onChange={(v) => handleChange('lastName', v)} placeholder="Kaygusuz" />
          <ModalField label="Telefon" value={form.phoneNumber} onChange={(v) => handleChange('phoneNumber', v)} placeholder="+905551234567" type="tel" />

          {error && (
            <div className="text-sm text-admin-red bg-admin-red-wash px-3 py-2 rounded-lg">{error}</div>
          )}

          <div className="flex gap-3 mt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 py-2.5 rounded-[11px] border border-admin-line text-sm font-semibold text-admin-text-mute hover:bg-admin-ivory transition"
            >
              İptal
            </button>
            <button
              type="submit"
              disabled={loading}
              className="flex-1 py-2.5 rounded-[11px] bg-gradient-to-br from-[#C99738] to-admin-gold text-[#241a08] text-sm font-bold shadow-[0_8px_18px_-6px_rgba(185,134,43,0.5)] hover:brightness-105 disabled:opacity-50 transition"
            >
              {loading ? 'Kaydediliyor…' : 'Kaydet'}
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
