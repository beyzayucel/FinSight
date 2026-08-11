type ConfirmModalProps = {
  open: boolean
  title: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  variant?: 'danger' | 'warning' | 'success'
  onConfirm: () => void
  onCancel: () => void
}

export default function ConfirmModal({
  open,
  title,
  message,
  confirmLabel = 'Evet',
  cancelLabel = 'Hayır',
  variant = 'danger',
  onConfirm,
  onCancel,
}: ConfirmModalProps) {
  if (!open) return null

  const confirmColors =
    variant === 'danger'
      ? 'bg-admin-red text-white hover:brightness-110'
      : variant === 'success'
        ? 'bg-admin-green text-white hover:brightness-110'
        : 'bg-gradient-to-br from-[#C99738] to-admin-gold text-[#241a08] hover:brightness-105'

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center overflow-y-auto p-4">
      <div className="absolute inset-0 bg-admin-ink/40 backdrop-blur-sm" onClick={onCancel} />
      <div className="relative max-h-[calc(100dvh-2rem)] w-full max-w-sm overflow-y-auto rounded-[18px] border border-admin-line bg-white p-5 shadow-2xl sm:p-6">
        <h3 className="font-heading text-base font-bold text-admin-ink mb-2">{title}</h3>
        <p className="text-sm text-admin-text-mute leading-relaxed mb-6">{message}</p>

        <div className="flex gap-3">
          {/* Hayır (default — autoFocus) */}
          <button
            onClick={onCancel}
            autoFocus
            className="flex-1 py-2.5 rounded-[11px] border border-admin-line text-sm font-semibold text-admin-ink bg-white hover:bg-admin-ivory focus:ring-2 focus:ring-admin-gold-soft focus:outline-none transition"
          >
            {cancelLabel}
          </button>
          {/* Evet */}
          <button
            onClick={onConfirm}
            className={`flex-1 py-2.5 rounded-[11px] text-sm font-bold shadow-sm transition ${confirmColors}`}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
