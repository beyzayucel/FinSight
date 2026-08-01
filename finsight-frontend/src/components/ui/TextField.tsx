import type { InputHTMLAttributes, ReactNode } from 'react'

type TextFieldProps = InputHTMLAttributes<HTMLInputElement> & {

  label: string
  icon?: ReactNode
  labelAction?: ReactNode
  trailing?: ReactNode
}

export default function TextField({
  label,
  icon,
  labelAction,
  trailing,
  id,
  className = '',
  ...inputProps
}: TextFieldProps) {
  return (
    <div>
      <div className="mb-2 flex items-center justify-between">
        <label htmlFor={id} className="text-xs font-semibold tracking-[0.12em] text-muted">
          {label}
        </label>
        {labelAction}
      </div>
      <div className="relative">
        {icon && (
          <span className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2">
            {icon}
          </span>
        )}
        <input
          id={id}
          className={[
            'w-full rounded-xl border border-slate-300 bg-slate-100 py-3.5 text-sm text-ink outline-none transition focus:border-primary/40 focus:bg-white focus:ring-4 focus:ring-primary/10',
            icon ? 'pl-12' : 'pl-4',
            trailing ? 'pr-12' : 'pr-4',
            className,
          ].join(' ')}
          {...inputProps}
        />
        {trailing}
      </div>
    </div>
  )
}
