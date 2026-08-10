import { useEffect, useState } from 'react'
import { IoCheckmarkCircle, IoCloseCircle } from 'react-icons/io5'

type ToastVariant = 'success' | 'error'

type ToastProps = {
  message: string
  variant: ToastVariant
  duration?: number
  onClose: () => void
}

const VARIANT_STYLES: Record<ToastVariant, string> = {
  success:
    'bg-green-50 text-green-700 border-green-200',
  error:
    'bg-red-50 text-red-700 border-red-200',
}

const VARIANT_ICON: Record<ToastVariant, React.ReactNode> = {
  success: <IoCheckmarkCircle className="text-green-600 text-lg flex-shrink-0" />,
  error: <IoCloseCircle className="text-red-600 text-lg flex-shrink-0" />,
}

export default function Toast({ message, variant, duration = 3000, onClose }: ToastProps) {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    const showTimer = requestAnimationFrame(() => setVisible(true))

    const hideTimer = setTimeout(() => {
      setVisible(false)
      setTimeout(onClose, 300)
    }, duration)

    return () => {
      cancelAnimationFrame(showTimer)
      clearTimeout(hideTimer)
    }
  }, [duration, onClose])

  return (
    <div
      className={[
        'fixed top-6 left-1/2 z-50 flex items-center gap-2 rounded-xl border px-4 py-3 shadow-lg text-sm font-medium transition-all duration-300',
        VARIANT_STYLES[variant],
        visible ? 'translate-x-[-50%] translate-y-0 opacity-100' : 'translate-x-[-50%] -translate-y-4 opacity-0',
      ].join(' ')}
    >
      {VARIANT_ICON[variant]}
      {message}
    </div>
  )
}
