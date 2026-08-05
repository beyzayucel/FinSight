import { useState } from 'react'
import type { UserResponse, PageResponse } from '../adminApi'

type UsersTableProps = {
  data: PageResponse<UserResponse> | null
  loading: boolean
  search: string
  onSearchChange: (value: string) => void
  statusFilter: string
  onStatusFilterChange: (value: string) => void
  selectedUserId: string | null
  onSelectUser: (user: UserResponse) => void
  onPageChange: (page: number) => void
  onCreateClick: () => void
  onEditClick: (user: UserResponse) => void
  onToggleStatus: (user: UserResponse) => void
  onDeleteClick: (user: UserResponse) => void
}

function getInitials(firstName: string, lastName: string): string {
  return `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase()
}

function formatLastLogin(dateStr: string | null): string {
  if (!dateStr) return '—'

  const date = new Date(dateStr)
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const loginDay = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const diffDays = Math.floor((today.getTime() - loginDay.getTime()) / 86400000)

  const time = date.toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })

  if (diffDays === 0) return `Bugün, ${time}`
  if (diffDays === 1) return `Dün, ${time}`
  return date.toLocaleDateString('tr-TR', { day: 'numeric', month: 'short', year: 'numeric' })
}

export default function UsersTable({
  data,
  loading,
  search,
  onSearchChange,
  statusFilter,
  onStatusFilterChange,
  selectedUserId,
  onSelectUser,
  onPageChange,
  onCreateClick,
  onEditClick,
  onToggleStatus,
  onDeleteClick,
}: UsersTableProps) {
  const users = data?.content ?? []
  const totalElements = data?.totalElements ?? 0
  const currentPage = data?.page ?? 0
  const totalPages = data?.totalPages ?? 0
  const pageSize = data?.size ?? 20

  const from = totalElements === 0 ? 0 : currentPage * pageSize + 1
  const to = Math.min((currentPage + 1) * pageSize, totalElements)

  return (
    <div className="bg-white rounded-[18px] shadow-[0_2px_4px_rgba(18,22,31,0.04),0_12px_26px_-14px_rgba(18,22,31,0.12)] border border-[rgba(231,226,214,0.7)] px-[22px] pt-[22px] pb-2">
      {/* Header */}
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="font-heading text-[16.5px] font-bold text-admin-ink">Kullanıcılar</h2>
          <div className="text-xs text-admin-text-faint font-medium mt-0.5">
            {totalElements} kayıtlı kullanıcı
          </div>
        </div>
        <button
          onClick={onCreateClick}
          className="flex items-center gap-[7px] bg-gradient-to-br from-[#C99738] to-admin-gold text-[#241a08] font-bold text-[13px] px-4 py-2.5 rounded-[11px] shadow-[0_8px_18px_-6px_rgba(185,134,43,0.5)] hover:brightness-105 transition"
        >
          <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className="w-[13px] h-[13px] stroke-[#241a08] stroke-[2.5]">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Yeni Kullanıcı
        </button>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-2.5 mb-3.5 flex-wrap">
        <div className="flex-1 min-w-[160px] relative">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.9" className="absolute left-[11px] top-1/2 -translate-y-1/2 w-[13px] h-[13px] text-admin-text-faint">
            <circle cx="11" cy="11" r="7" />
            <path d="M21 21l-4.3-4.3" />
          </svg>
          <input
            type="text"
            placeholder="İsim veya e-posta ara…"
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            className="w-full bg-admin-ivory border border-admin-line rounded-[10px] py-[9px] pl-8 pr-3 text-[12.5px] font-ibm focus:outline-none focus:border-admin-gold-soft"
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => onStatusFilterChange(e.target.value)}
          className="bg-admin-ivory border border-admin-line rounded-[10px] py-[9px] px-3 pr-7 text-[12.5px] font-medium text-admin-text appearance-none bg-[url('data:image/svg+xml;utf8,<svg%20xmlns=%22http://www.w3.org/2000/svg%22%20width=%2210%22%20height=%226%22%20viewBox=%220%200%2010%206%22><path%20d=%22M1%201l4%204%204-4%22%20stroke=%22%23767a86%22%20stroke-width=%221.4%22%20fill=%22none%22%20stroke-linecap=%22round%22%20stroke-linejoin=%22round%22/></svg>')] bg-no-repeat bg-[position:right_10px_center] cursor-pointer focus:outline-none"
        >
          <option value="">Tüm Durumlar</option>
          <option value="true">Aktif</option>
          <option value="false">Pasif</option>
        </select>
      </div>

      {/* Table */}
      <table className="w-full border-collapse">
        <thead>
          <tr>
            {['Ad Soyad', 'E-posta', 'Durum', 'Son Giriş', 'İşlemler'].map((header) => (
              <th
                key={header}
                className="text-left text-[10.5px] font-semibold tracking-[0.06em] uppercase text-admin-text-faint px-2.5 pb-[11px] border-b border-admin-line"
              >
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading ? (
            Array.from({ length: 4 }).map((_, i) => (
              <tr key={i}>
                <td colSpan={5} className="py-3 px-2.5 border-b border-[#F0ECE1]">
                  <div className="h-5 bg-slate-100 rounded animate-pulse" />
                </td>
              </tr>
            ))
          ) : users.length === 0 ? (
            <tr>
              <td colSpan={5} className="py-8 text-center text-sm text-admin-text-faint">
                Kullanıcı bulunamadı
              </td>
            </tr>
          ) : (
            users.map((user) => (
              <tr
                key={user.id}
                onClick={() => onSelectUser(user)}
                className={`cursor-pointer hover:bg-[#FBFAF6] ${selectedUserId === user.id ? 'bg-admin-gold-wash' : ''}`}
              >
                <td className="py-3 px-2.5 border-b border-[#F0ECE1]">
                  <div className="flex items-center gap-2.5">
                    <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#2A3247] to-admin-ink text-admin-gold-soft flex items-center justify-center font-heading font-bold text-[11.5px] shrink-0">
                      {getInitials(user.firstName, user.lastName)}
                    </div>
                    <span className="font-semibold text-admin-ink text-[13px]">
                      {user.firstName} {user.lastName}
                    </span>
                  </div>
                </td>
                <td className="py-3 px-2.5 border-b border-[#F0ECE1] text-admin-text-mute text-[12.5px]">
                  {user.email}
                </td>
                <td className="py-3 px-2.5 border-b border-[#F0ECE1]">
                  {user.enabled ? (
                    <span className="inline-flex items-center gap-1.5 text-[11px] font-semibold px-[9px] py-1 rounded-full bg-admin-green-wash text-admin-green">
                      <span className="w-[5px] h-[5px] rounded-full bg-current" />
                      Aktif
                    </span>
                  ) : (
                    <span className="inline-flex items-center gap-1.5 text-[11px] font-semibold px-[9px] py-1 rounded-full bg-admin-red-wash text-admin-red">
                      <span className="w-[5px] h-[5px] rounded-full bg-current" />
                      Pasif
                    </span>
                  )}
                </td>
                <td className="py-3 px-2.5 border-b border-[#F0ECE1] text-admin-text-mute text-xs">
                  {formatLastLogin(user.lastLoginAt)}
                </td>
                <td className="py-3 px-2.5 border-b border-[#F0ECE1]">
                  <div className="flex items-center gap-1.5" onClick={(e) => e.stopPropagation()}>
                    {/* View */}
                    <button
                      onClick={() => onSelectUser(user)}
                      title="Görüntüle"
                      className="w-7 h-7 rounded-lg flex items-center justify-center border border-admin-line bg-white hover:border-admin-gold-soft hover:bg-admin-gold-wash group transition"
                    >
                      <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className="w-[13px] h-[13px] stroke-admin-text-mute group-hover:stroke-admin-gold transition">
                        <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7-11-7-11-7z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                    </button>
                    {/* Edit */}
                    <button
                      onClick={() => onEditClick(user)}
                      title="Düzenle"
                      className="w-7 h-7 rounded-lg flex items-center justify-center border border-admin-line bg-white hover:border-admin-gold-soft hover:bg-admin-gold-wash group transition"
                    >
                      <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className="w-[13px] h-[13px] stroke-admin-text-mute group-hover:stroke-admin-gold transition">
                        <path d="M12 20h9" />
                        <path d="M16.5 3.5a2.1 2.1 0 013 3L7 19l-4 1 1-4z" />
                      </svg>
                    </button>
                    {/* Toggle status */}
                    <button
                      onClick={() => onToggleStatus(user)}
                      title={user.enabled ? 'Pasifleştir' : 'Aktifleştir'}
                      className={`w-7 h-7 rounded-lg flex items-center justify-center border border-admin-line bg-white transition group ${
                        user.enabled
                          ? 'hover:border-[#e6b3ac] hover:bg-admin-red-wash'
                          : 'hover:border-admin-gold-soft hover:bg-admin-gold-wash'
                      }`}
                    >
                      {user.enabled ? (
                        <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className="w-[13px] h-[13px] stroke-admin-text-mute group-hover:stroke-admin-red transition">
                          <circle cx="12" cy="12" r="9" />
                          <path d="M9 9l6 6" />
                        </svg>
                      ) : (
                        <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className="w-[13px] h-[13px] stroke-admin-text-mute group-hover:stroke-admin-gold transition">
                          <path d="M20 6L9 17l-5-5" />
                        </svg>
                      )}
                    </button>
                    {/* Delete */}
                    <button
                      onClick={() => onDeleteClick(user)}
                      title="Sil"
                      className="w-7 h-7 rounded-lg flex items-center justify-center border border-admin-line bg-white hover:border-[#e6b3ac] hover:bg-admin-red-wash group transition"
                    >
                      <svg viewBox="0 0 24 24" fill="none" strokeLinecap="round" strokeLinejoin="round" className="w-[13px] h-[13px] stroke-admin-text-mute group-hover:stroke-admin-red transition">
                        <path d="M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6" />
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      {/* Footer / Pagination */}
      <div className="flex items-center justify-between py-3.5 px-1 text-xs text-admin-text-faint">
        <span>
          {totalElements > 0
            ? `${totalElements} kullanıcıdan ${from}–${to} arası gösteriliyor`
            : 'Kayıt yok'}
        </span>
        {totalPages > 1 && (
          <div className="flex items-center gap-1.5">
            <button
              disabled={currentPage === 0}
              onClick={() => onPageChange(currentPage - 1)}
              className="px-2.5 py-1 rounded-lg border border-admin-line bg-white text-admin-text-mute hover:bg-admin-gold-wash disabled:opacity-40 disabled:cursor-not-allowed transition text-[11px] font-medium"
            >
              Önceki
            </button>
            <span className="px-2 text-admin-text-mute font-medium">
              {currentPage + 1} / {totalPages}
            </span>
            <button
              disabled={currentPage >= totalPages - 1}
              onClick={() => onPageChange(currentPage + 1)}
              className="px-2.5 py-1 rounded-lg border border-admin-line bg-white text-admin-text-mute hover:bg-admin-gold-wash disabled:opacity-40 disabled:cursor-not-allowed transition text-[11px] font-medium"
            >
              Sonraki
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
