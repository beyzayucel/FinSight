import type { ButtonHTMLAttributes } from 'react'

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement>

/** Primary, full-width action button. */
export default function Button({ type = 'button', className = '', ...props }: ButtonProps) {
  return (
    <button
      type={type}
      className={`w-full rounded-xl py-3.5 text-sm font-semibold text-white shadow-sm transition-colors focus:outline-none focus:ring-4 focus:ring-primary/25 bg-primary hover:bg-primary-dark disabled:bg-slate-300 disabled:cursor-not-allowed disabled:shadow-none ${className}`}
      {...props}
    />
  )
}
