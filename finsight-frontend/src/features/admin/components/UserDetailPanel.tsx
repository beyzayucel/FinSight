import type { ReactNode } from 'react'
import type { UserResponse } from '../adminApi'
import type { Translations } from '@/i18n/translations'
import { getLang } from '@/lib/authStore'

type UserDetailPanelProps = {
  user: UserResponse | null
  currentUserEmail: string
  onEditClick: (user: UserResponse) => void
  onResendVerification: (user: UserResponse) => void
  onChangePassword: () => void
  onSendResetLink: (user: UserResponse) => void
  t: Translations
}

function getInitials(firstName: string, lastName: string): string {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
}

const ROLE_MAP: Record<string, 'roleAdmin' | 'roleUser'> = {
  ADMIN: 'roleAdmin',
  USER: 'roleUser',
}

function getRoleLabel(role: string, t: Translations): string {
  const key = ROLE_MAP[role]
  if (key) return t[key]
  return role.charAt(0) + role.slice(1).toLowerCase()
}

function formatDate(dateStr: string | null, t: Translations): string {
  if (!dateStr) return '—'
  const lang = getLang()
  const locale = lang === 'en' ? 'en-US' : 'tr-TR'
  const date = new Date(dateStr)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const targetDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.floor((today.getTime() - targetDay.getTime()) / 86400000)

  const time = date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' })

  if (diffDays === 0) return `${t.adminToday}, ${time}`
  if (diffDays === 1) return `${t.adminYesterday}, ${time}`
  return date.toLocaleDateString(locale, { day: 'numeric', month: 'long', year: 'numeric' })
}

export default function UserDetailPanel({ user, currentUserEmail, onEditClick, onResendVerification, onChangePassword, onSendResetLink, t }: UserDetailPanelProps) {
  const isSelf = user?.email === currentUserEmail
  if (!user) {
    return (
      <div className="bg-white rounded-[18px] shadow-[0_2px_4px_rgba(18,22,31,0.04),0_12px_26px_-14px_rgba(18,22,31,0.12)] border border-[rgba(231,226,214,0.7)] px-5 py-[22px]">
        <div className="flex flex-col items-center justify-center py-12 text-admin-text-faint text-sm">
          {t.adminSelectUser}
        </div>
      </div>
    )
  }

  return (
    <div className="bg-white rounded-[18px] shadow-[0_2px_4px_rgba(18,22,31,0.04),0_12px_26px_-14px_rgba(18,22,31,0.12)] border border-[rgba(231,226,214,0.7)] px-5 py-[22px]">
      {/* Head */}
      <div className="flex flex-col items-center text-center pb-[18px] mb-4 border-b border-admin-line">
        <div className={`w-14 h-14 rounded-[15px] flex items-center justify-center font-heading font-bold text-xl mb-[11px] shadow-[0_8px_18px_-8px_rgba(18,22,31,0.35)] ${
          user.role === 'ADMIN'
            ? 'bg-gradient-to-br from-[#C99738] to-[#8f6620] text-[#241a08]'
            : 'bg-gradient-to-br from-[#2A3247] to-admin-ink text-admin-gold-soft'
        }`}>
          {getInitials(user.firstName, user.lastName)}
        </div>
        <h3 className="font-heading text-base font-bold text-admin-ink">
          {user.firstName} {user.lastName}
          {isSelf && (
            <span className="text-[10px] text-admin-gold font-medium ml-1">({t.adminYou})</span>
          )}
        </h3>
        <div className="text-xs text-admin-text-mute mt-0.5 font-medium">
          @{user.username}
        </div>
        <span className={`inline-block text-[10px] font-semibold px-[7px] py-[1px] rounded-full mt-1.5 ${
          user.role === 'ADMIN'
            ? 'bg-[#C99738]/15 text-[#C99738]'
            : 'bg-admin-ivory text-admin-text-faint'
        }`}>
          {getRoleLabel(user.role, t)}
        </span>
        <div className="mt-[9px]">
          {user.enabled ? (
            <span className="inline-flex items-center gap-1.5 text-[11px] font-semibold px-[9px] py-1 rounded-full bg-admin-green-wash text-admin-green">
              <span className="w-[5px] h-[5px] rounded-full bg-current" />
              {t.adminActive}
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 text-[11px] font-semibold px-[9px] py-1 rounded-full bg-admin-red-wash text-admin-red">
              <span className="w-[5px] h-[5px] rounded-full bg-current" />
              {t.adminInactive}
            </span>
          )}
          {!user.emailVerified && (
            <span className="inline-flex items-center gap-1.5 text-[11px] font-semibold px-[9px] py-1 rounded-full bg-amber-50 text-amber-600 ml-1.5">
              {t.adminEmailNotVerified}
            </span>
          )}
        </div>
      </div>

      {/* Detail rows */}
      <div className="flex flex-col gap-3">
        <DetailRow
          icon={
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-[13px] h-[13px]">
              <path d="M4 4h16v16H4z" />
              <path d="M4 6l8 7 8-7" />
            </svg>
          }
          label={t.adminLabelEmail}
          value={user.email}
        />
        <DetailRow
          icon={
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-[13px] h-[13px]">
              <path d="M22 16.92v3a2 2 0 01-2.18 2 19.8 19.8 0 01-8.63-3.07 19.5 19.5 0 01-6-6A19.8 19.8 0 012.12 4.18 2 2 0 014.11 2h3a2 2 0 012 1.72c.13.81.37 1.6.7 2.35a2 2 0 01-.45 2.11L8.09 9.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.75.33 1.54.57 2.35.7A2 2 0 0122 16.92z" />
            </svg>
          }
          label={t.adminLabelPhone}
          value={user.phoneNumber}
        />
        <DetailRow
          icon={
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-[13px] h-[13px]">
              <rect x="3" y="4" width="18" height="18" rx="2" />
              <path d="M16 2v4M8 2v4M3 10h18" />
            </svg>
          }
          label={t.adminLabelRegisteredAt}
          value={formatDate(user.createdAt, t)}
        />
        <DetailRow
          icon={
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-[13px] h-[13px]">
              <circle cx="12" cy="12" r="9" />
              <path d="M12 7v5l3.5 2" />
            </svg>
          }
          label={t.adminLabelLastLogin}
          value={formatDate(user.lastLoginAt, t)}
        />
      </div>

      {/* Actions */}
      <div className="flex flex-col gap-2 mt-[18px] pt-4 border-t border-admin-line">
        <div className="flex gap-2">
          <button
            onClick={() => onEditClick(user)}
            className="flex-1 text-center py-[9px] px-2 rounded-[10px] border border-admin-gold-soft bg-admin-gold-wash text-[11.5px] font-semibold text-[#8a6420] hover:bg-[#f0e4c8] hover:border-admin-gold transition"
          >
            {t.adminEditProfile}
          </button>
          {!user.emailVerified && (
            <button
              onClick={() => onResendVerification(user)}
              className="flex-1 text-center py-[9px] px-2 rounded-[10px] border border-admin-green/30 bg-admin-green-wash text-[11.5px] font-semibold text-admin-green hover:bg-[#d4edda] hover:border-admin-green/50 transition"
            >
              {t.adminSendVerification}
            </button>
          )}
        </div>
        {isSelf ? (
          <button
            onClick={onChangePassword}
            className="w-full text-center py-[9px] px-2 rounded-[10px] border border-admin-line text-[11.5px] font-semibold text-admin-ink bg-white hover:border-admin-gold-soft hover:bg-admin-gold-wash hover:text-[#8a6420] transition"
          >
            {t.adminChangePassword}
          </button>
        ) : (
          <button
            onClick={() => onSendResetLink(user)}
            className="w-full text-center py-[9px] px-2 rounded-[10px] border border-admin-line text-[11.5px] font-semibold text-admin-ink bg-white hover:border-admin-gold-soft hover:bg-admin-gold-wash hover:text-[#8a6420] transition"
          >
            {t.adminSendResetLink}
          </button>
        )}
      </div>
    </div>
  )
}

function DetailRow({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <div className="flex items-start gap-[11px]">
      <div className="w-7 h-7 rounded-lg bg-admin-ivory flex items-center justify-center shrink-0 text-admin-gold">
        {icon}
      </div>
      <div>
        <div className="text-[10px] text-admin-text-faint font-semibold uppercase tracking-[0.05em]">{label}</div>
        <div className="text-[12.5px] text-admin-ink font-medium mt-0.5 break-all">{value}</div>
      </div>
    </div>
  )
}
