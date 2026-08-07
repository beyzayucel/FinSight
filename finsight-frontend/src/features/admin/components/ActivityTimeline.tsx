import { useState, useEffect } from 'react'
import type { AuditLogResponse, PageResponse } from '../adminApi'
import type { Translations } from '@/i18n/translations'
import { getLang } from '@/lib/authStore'

type ActivityTimelineProps = {
  data: PageResponse<AuditLogResponse> | null
  loading: boolean
  search: string
  onSearchChange: (value: string) => void
  scope: string
  onScopeChange: (value: string) => void
  t: Translations
}

type ActionStyle = {
  bg: string
  icon: JSX.Element
}

const S = 'stroke-[1.8]'

function getActionStyle(action: string): ActionStyle {
  switch (action) {
    case 'LOGIN_SUCCESS':
      return {
        bg: 'bg-admin-gold-wash',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-admin-gold ${S}`}>
            <path d="M15 3h4a2 2 0 012 2v14a2 2 0 01-2 2h-4" />
            <polyline points="10 17 15 12 10 7" />
            <line x1="15" y1="12" x2="3" y2="12" />
          </svg>
        ),
      }
    case 'LOGOUT':
      return {
        bg: 'bg-[#EDECF0]',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-[#6B6E7B] ${S}`}>
            <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
        ),
      }
    case 'PASSWORD_CHANGED':
      return {
        bg: 'bg-[#EBF0FB]',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-[#4A6FA5] ${S}`}>
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0110 0v4" />
          </svg>
        ),
      }
    case 'PASSWORD_RESET_COMPLETED':
      return {
        bg: 'bg-[#F0EAFB]',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-[#7C5CBF] ${S}`}>
            <path d="M21 2l-2 2m-7.61 7.61a5.5 5.5 0 11-7.78 7.78 5.5 5.5 0 017.78-7.78zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4" />
          </svg>
        ),
      }
    case 'USER_CREATED':
      return {
        bg: 'bg-admin-green-wash',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-admin-green ${S}`}>
            <path d="M16 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2" />
            <circle cx="8.5" cy="7" r="4" />
            <line x1="20" y1="8" x2="20" y2="14" />
            <line x1="23" y1="11" x2="17" y2="11" />
          </svg>
        ),
      }
    case 'USER_UPDATED':
      return {
        bg: 'bg-[#E6F5F0]',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-[#2A8A6E] ${S}`}>
            <path d="M12 20h9" />
            <path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4z" />
          </svg>
        ),
      }
    case 'USER_ACTIVATED':
      return {
        bg: 'bg-[#E8F8EE]',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-[#2D9A5F] ${S}`}>
            <path d="M22 11.08V12a10 10 0 11-5.93-9.14" />
            <polyline points="22 4 12 14.01 9 11.01" />
          </svg>
        ),
      }
    case 'USER_DEACTIVATED':
      return {
        bg: 'bg-[#FEF3E2]',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-[#C47A20] ${S}`}>
            <circle cx="12" cy="12" r="10" />
            <line x1="15" y1="9" x2="9" y2="15" />
            <line x1="9" y1="9" x2="15" y2="15" />
          </svg>
        ),
      }
    case 'USER_DELETED':
      return {
        bg: 'bg-admin-red-wash',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-admin-red ${S}`}>
            <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" />
          </svg>
        ),
      }
    case 'VERIFICATION_RESENT':
      return {
        bg: 'bg-[#E8EDF8]',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-[#4A5B92] ${S}`}>
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z" />
            <polyline points="22,6 12,13 2,6" />
          </svg>
        ),
      }
    default:
      return {
        bg: 'bg-[#F0EDE6]',
        icon: (
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className={`w-3.5 h-3.5 stroke-admin-text-mute ${S}`}>
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
        ),
      }
  }
}

function getActionDescription(log: AuditLogResponse, t: Translations): string {
  const actor = log.actorFullName
  const target = log.targetFullName

  switch (log.action) {
    case 'LOGIN_SUCCESS':
      return t.auditLoginSuccess(actor)
    case 'LOGOUT':
      return t.auditLogout(actor)
    case 'PASSWORD_CHANGED':
      return t.auditPasswordChanged(actor)
    case 'PASSWORD_RESET_COMPLETED':
      return t.auditPasswordResetCompleted(actor)
    case 'USER_CREATED':
      return t.auditUserCreated(actor, target)
    case 'USER_UPDATED':
      return t.auditUserUpdated(actor, target)
    case 'USER_ACTIVATED':
      return t.auditUserActivated(actor, target)
    case 'USER_DEACTIVATED':
      return t.auditUserDeactivated(actor, target)
    case 'USER_DELETED':
      return t.auditUserDeleted(actor, target)
    case 'VERIFICATION_RESENT':
      return t.auditVerificationResent(actor, target)
    default:
      return `${actor} — ${log.action}`
  }
}

function formatAuditDate(dateStr: string, t: Translations): string {
  const lang = getLang()
  const locale = lang === 'en' ? 'en-US' : 'tr-TR'
  const date = new Date(dateStr)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const logDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.floor((today.getTime() - logDay.getTime()) / 86400000)

  const time = date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' })

  if (diffDays === 0) return `${t.adminToday} · ${time}`
  if (diffDays === 1) return `${t.adminYesterday} · ${time}`
  return `${date.toLocaleDateString(locale, { day: 'numeric', month: 'short' })} · ${time}`
}

const SCOPE_OPTIONS = ['ALL', 'ACTIVE', 'ARCHIVED'] as const

export default function ActivityTimeline({
  data,
  loading,
  search,
  onSearchChange,
  scope,
  onScopeChange,
  t,
}: ActivityTimelineProps) {
  const logs = data?.content ?? []

  const [searchInput, setSearchInput] = useState(search)

  useEffect(() => {
    setSearchInput(search)
  }, [search])

  useEffect(() => {
    const timer = setTimeout(() => onSearchChange(searchInput), 400)
    return () => clearTimeout(timer)
  }, [searchInput])

  const scopeLabels: Record<string, string> = {
    ALL: t.auditScopeAll,
    ACTIVE: t.auditScopeActive,
    ARCHIVED: t.auditScopeArchived,
  }

  return (
    <div className="bg-white rounded-[18px] shadow-[0_2px_4px_rgba(18,22,31,0.04),0_12px_26px_-14px_rgba(18,22,31,0.12)] border border-[rgba(231,226,214,0.7)] px-[22px] pt-[22px] pb-4 mt-5">
      <div className="flex items-center justify-between mb-4">
        <h2 className="font-heading text-[16.5px] font-bold text-admin-ink">{t.auditTitle}</h2>
        <div className="flex items-center gap-1 bg-admin-ivory rounded-[10px] p-0.5 border border-admin-line">
          {SCOPE_OPTIONS.map((s) => (
            <button
              key={s}
              onClick={() => onScopeChange(s)}
              className={`px-3 py-[5px] rounded-[8px] text-[11.5px] font-semibold transition ${
                scope === s
                  ? 'bg-white text-admin-ink shadow-sm'
                  : 'text-admin-text-mute hover:text-admin-ink'
              }`}
            >
              {scopeLabels[s]}
            </button>
          ))}
        </div>
      </div>

      <div className="relative mb-3.5">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" className="absolute left-[11px] top-1/2 -translate-y-1/2 w-[13px] h-[13px] text-admin-text-faint">
          <circle cx="11" cy="11" r="7" />
          <path d="M21 21l-4.3-4.3" />
        </svg>
        <input
          type="text"
          placeholder={t.auditSearchPlaceholder}
          value={searchInput}
          onChange={(e) => setSearchInput(e.target.value)}
          className="w-full bg-admin-ivory border border-admin-line rounded-[10px] py-[9px] pl-8 pr-3 text-[12.5px] font-ibm focus:outline-none focus:border-admin-gold-soft"
        />
      </div>

      <div className="max-h-[420px] overflow-y-auto pr-1">
        {loading ? (
          Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="flex items-start gap-3 py-3 border-b border-[#F0ECE1] last:border-b-0">
              <div className="w-8 h-8 rounded-[10px] bg-slate-100 animate-pulse shrink-0" />
              <div className="flex-1 space-y-1.5 pt-0.5">
                <div className="h-3.5 bg-slate-100 rounded animate-pulse w-3/4" />
                <div className="h-2.5 bg-slate-100 rounded animate-pulse w-1/3" />
              </div>
            </div>
          ))
        ) : logs.length === 0 ? (
          <div className="py-10 text-center text-sm text-admin-text-faint">{t.auditNoRecords}</div>
        ) : (
          logs.map((log) => {
            const style = getActionStyle(log.action)
            return (
              <div key={log.id} className="flex items-start gap-3 py-3 border-b border-[#F0ECE1] last:border-b-0">
                <div className={`w-8 h-8 rounded-[10px] flex items-center justify-center shrink-0 ${style.bg}`}>
                  {style.icon}
                </div>
                <div className="flex-1 min-w-0 pt-0.5">
                  <p className="text-[12.5px] text-admin-ink leading-[1.45]">
                    {getActionDescription(log, t)}
                  </p>
                  <span className="text-[11px] text-admin-text-faint mt-0.5 block">
                    {formatAuditDate(log.createdAt, t)}
                  </span>
                </div>
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}
