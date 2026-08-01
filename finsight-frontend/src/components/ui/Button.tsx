import type { ButtonHTMLAttributes } from 'react'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement>

/** Primary, full-width action button. */
export default function Button({ type = 'button', className = '', ...props }: ButtonProps) {
  return (
    <button
      type={type}
      className={`w-full rounded-xl bg-primary py-3.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-primary-dark focus:outline-none focus:ring-4 focus:ring-primary/25 ${className}`}
      {...props}
    />
  )
}
