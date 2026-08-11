import type { InputHTMLAttributes } from 'react'

type CheckboxProps = InputHTMLAttributes<HTMLInputElement> & {
  label: string
}

export default function Checkbox({ label, className = '', ...inputProps }: CheckboxProps) {
  return (
    <label className="flex cursor-pointer select-none items-center gap-2.5 text-sm text-ink">
      <input
        type="checkbox"
        className={`h-4 w-4 rounded border-slate-300 accent-primary ${className}`}
        {...inputProps}
      />
      {label}
    </label>
  )
}
