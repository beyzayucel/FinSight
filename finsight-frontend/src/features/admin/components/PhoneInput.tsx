const COUNTRY_CODES = [
  { code: '+90', flag: '\u{1F1F9}\u{1F1F7}', label: 'TR' },
  { code: '+49', flag: '\u{1F1E9}\u{1F1EA}', label: 'DE' },
  { code: '+33', flag: '\u{1F1EB}\u{1F1F7}', label: 'FR' },
  { code: '+39', flag: '\u{1F1EE}\u{1F1F9}', label: 'IT' },
  { code: '+34', flag: '\u{1F1EA}\u{1F1F8}', label: 'ES' },
  { code: '+31', flag: '\u{1F1F3}\u{1F1F1}', label: 'NL' },
  { code: '+32', flag: '\u{1F1E7}\u{1F1EA}', label: 'BE' },
  { code: '+43', flag: '\u{1F1E6}\u{1F1F9}', label: 'AT' },
  { code: '+48', flag: '\u{1F1F5}\u{1F1F1}', label: 'PL' },
  { code: '+46', flag: '\u{1F1F8}\u{1F1EA}', label: 'SE' },
  { code: '+45', flag: '\u{1F1E9}\u{1F1F0}', label: 'DK' },
  { code: '+358', flag: '\u{1F1EB}\u{1F1EE}', label: 'FI' },
  { code: '+353', flag: '\u{1F1EE}\u{1F1EA}', label: 'IE' },
  { code: '+351', flag: '\u{1F1F5}\u{1F1F9}', label: 'PT' },
  { code: '+30', flag: '\u{1F1EC}\u{1F1F7}', label: 'GR' },
  { code: '+420', flag: '\u{1F1E8}\u{1F1FF}', label: 'CZ' },
  { code: '+40', flag: '\u{1F1F7}\u{1F1F4}', label: 'RO' },
  { code: '+36', flag: '\u{1F1ED}\u{1F1FA}', label: 'HU' },
  { code: '+359', flag: '\u{1F1E7}\u{1F1EC}', label: 'BG' },
  { code: '+385', flag: '\u{1F1ED}\u{1F1F7}', label: 'HR' },
  { code: '+421', flag: '\u{1F1F8}\u{1F1F0}', label: 'SK' },
  { code: '+386', flag: '\u{1F1F8}\u{1F1EE}', label: 'SI' },
  { code: '+370', flag: '\u{1F1F1}\u{1F1F9}', label: 'LT' },
  { code: '+371', flag: '\u{1F1F1}\u{1F1FB}', label: 'LV' },
  { code: '+372', flag: '\u{1F1EA}\u{1F1EA}', label: 'EE' },
  { code: '+356', flag: '\u{1F1F2}\u{1F1F9}', label: 'MT' },
  { code: '+357', flag: '\u{1F1E8}\u{1F1FE}', label: 'CY' },
  { code: '+352', flag: '\u{1F1F1}\u{1F1FA}', label: 'LU' },
  { code: '+44', flag: '\u{1F1EC}\u{1F1E7}', label: 'UK' },
  { code: '+1', flag: '\u{1F1FA}\u{1F1F8}', label: 'US' },
] as const

type PhoneInputProps = {
  label: string
  countryCode: string
  phoneLocal: string
  onCountryCodeChange: (code: string) => void
  onPhoneLocalChange: (value: string) => void
}

export function parsePhoneNumber(fullNumber: string): { countryCode: string; phoneLocal: string } {
  if (!fullNumber) return { countryCode: '+90', phoneLocal: '' }

  const match = COUNTRY_CODES.find((c) => fullNumber.startsWith(c.code))
  if (match) {
    return { countryCode: match.code, phoneLocal: fullNumber.slice(match.code.length) }
  }

  return { countryCode: '+90', phoneLocal: fullNumber.replace(/^\+/, '') }
}

export default function PhoneInput({
  label,
  countryCode,
  phoneLocal,
  onCountryCodeChange,
  onPhoneLocalChange,
}: PhoneInputProps) {
  const selected = COUNTRY_CODES.find((c) => c.code === countryCode) ?? COUNTRY_CODES[0]

  function handleLocalChange(value: string) {
    const digitsOnly = value.replace(/\D/g, '')
    onPhoneLocalChange(digitsOnly)
  }

  return (
    <div>
      <label className="text-[10.5px] font-semibold text-admin-text-faint uppercase tracking-[0.06em] mb-1.5 block">
        {label}
      </label>
      <div className="flex gap-2">
        <select
          value={countryCode}
          onChange={(e) => onCountryCodeChange(e.target.value)}
          className="bg-admin-ivory border border-admin-line rounded-[10px] py-[9px] px-2 text-[13px] font-ibm text-admin-text focus:outline-none focus:border-admin-gold-soft transition w-[100px] appearance-none cursor-pointer"
        >
          {COUNTRY_CODES.map((c) => (
            <option key={c.code} value={c.code}>
              {c.flag} {c.code}
            </option>
          ))}
        </select>
        <input
          type="tel"
          value={phoneLocal}
          onChange={(e) => handleLocalChange(e.target.value)}
          placeholder={selected.label === 'TR' ? '5551234567' : '1234567890'}
          maxLength={12}
          className="flex-1 bg-admin-ivory border border-admin-line rounded-[10px] py-[9px] px-3 text-[13px] font-ibm text-admin-text focus:outline-none focus:border-admin-gold-soft transition"
        />
      </div>
    </div>
  )
}
