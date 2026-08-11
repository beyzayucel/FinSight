import { useState, useEffect, useCallback } from 'react'
import { getAccessToken } from '@/lib/authStore'
import { getApiError } from '@/lib/api/apiError'
import { getTranslations } from '@/i18n/translations'
import {
  getUsers,
  getUserStats,
  getAuditLogs,
  createUser,
  updateUser,
  changeUserStatus,
  deleteUser,
  resendVerification,
  getCurrentUser,
  changePassword,
  sendPasswordResetLink,
} from './adminApi'
import type {
  UserResponse,
  UserStatsResponse,
  AuditLogResponse,
  PageResponse,
  CreateUserRequest,
  UpdateUserRequest,
  ChangePasswordRequest,
} from './adminApi'
import AdminLayout from './components/AdminLayout'
import KpiCards from './components/KpiCards'
import UsersTable from './components/UsersTable'
import UserDetailPanel from './components/UserDetailPanel'
import CreateUserModal from './components/CreateUserModal'
import EditUserModal from './components/EditUserModal'
import ConfirmModal from './components/ConfirmModal'
import ChangePasswordModal from './components/ChangePasswordModal'
import ActivityTimeline from './components/ActivityTimeline'

function parseCurrentUser(
  token: string | null,
  fallbackName: string,
  fallbackInitials: string,
): { name: string; initials: string; email: string } {
  if (!token) return { name: fallbackName, initials: fallbackInitials, email: '' }
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const email: string = payload.sub ?? ''
    const name = email.split('@')[0].replace(/\./g, ' ')
    const parts = name.split(' ')
    const initials = parts.length >= 2
      ? `${parts[0][0]}${parts[1][0]}`.toUpperCase()
      : name.substring(0, 2).toUpperCase()
    return { name, initials, email }
  } catch {
    return { name: fallbackName, initials: fallbackInitials, email: '' }
  }
}

type ConfirmState = {
  open: boolean
  title: string
  message: string
  variant: 'danger' | 'warning' | 'success'
  onConfirm: () => void
}

const CONFIRM_INITIAL: ConfirmState = {
  open: false,
  title: '',
  message: '',
  variant: 'danger',
  onConfirm: () => {},
}

const PAGE_SIZE = 20

export default function AdminDashboardPage() {
  const t = getTranslations()

  useEffect(() => {
    document.title = `Finsight · ${t.adminDashboardTitle}`
  }, [t.adminDashboardTitle])

  // ── State ──
  const [stats, setStats] = useState<UserStatsResponse | null>(null)
  const [statsLoading, setStatsLoading] = useState(true)

  const [usersData, setUsersData] = useState<PageResponse<UserResponse> | null>(null)
  const [usersLoading, setUsersLoading] = useState(true)

  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [currentPage, setCurrentPage] = useState(0)

  const [selectedUser, setSelectedUser] = useState<UserResponse | null>(null)

  const [createModalOpen, setCreateModalOpen] = useState(false)
  const [editUser, setEditUser] = useState<UserResponse | null>(null)

  const [changePasswordOpen, setChangePasswordOpen] = useState(false)

  const [auditData, setAuditData] = useState<PageResponse<AuditLogResponse> | null>(null)
  const [auditLoading, setAuditLoading] = useState(true)
  const [auditSearch, setAuditSearch] = useState('')
  const [auditScope, setAuditScope] = useState('ALL')

  const [confirm, setConfirm] = useState<ConfirmState>(CONFIRM_INITIAL)
  const [toast, setToast] = useState<{ message: string; variant: 'success' | 'error' } | null>(null)

  const token = getAccessToken()
  const { name: currentUserName, initials: currentUserInitials, email: currentUserEmail } =
    parseCurrentUser(token, t.adminFallbackName, t.adminFallbackInitials)

  // ── Toast auto-hide ──
  useEffect(() => {
    if (!toast) return
    const timer = setTimeout(() => setToast(null), 3000)
    return () => clearTimeout(timer)
  }, [toast])

  // ── Data fetching ──

  const loadStats = useCallback(async () => {
    setStatsLoading(true)
    try {
      const res = await getUserStats()
      setStats(res.data.data)
    } catch {
      // KPI kartları 0 gösterir
    } finally {
      setStatsLoading(false)
    }
  }, [])

  const loadUsers = useCallback(async () => {
    setUsersLoading(true)
    try {
      const res = await getUsers({
        page: currentPage,
        size: PAGE_SIZE,
        search: search || undefined,
        enabled: statusFilter === '' ? null : statusFilter === 'true',
        sort: 'createdAt,desc',
      })
      setUsersData(res.data.data)
    } catch {
      setUsersData(null)
    } finally {
      setUsersLoading(false)
    }
  }, [currentPage, search, statusFilter])

  const loadAuditLogs = useCallback(async () => {
    setAuditLoading(true)
    try {
      const res = await getAuditLogs({
        scope: auditScope,
        search: auditSearch || undefined,
        page: 0,
        size: 20,
      })
      setAuditData(res.data.data)
    } catch {
      setAuditData(null)
    } finally {
      setAuditLoading(false)
    }
  }, [auditScope, auditSearch])

  useEffect(() => {
    loadStats()
  }, [loadStats])

  useEffect(() => {
    loadUsers()
  }, [loadUsers])

  useEffect(() => {
    loadAuditLogs()
  }, [loadAuditLogs])

  // İlk yüklemede admin profilini göster
  useEffect(() => {
    getCurrentUser()
      .then((res) => setSelectedUser(res.data.data))
      .catch(() => {})
  }, [])

  // Debounce search
  const [searchInput, setSearchInput] = useState('')
  useEffect(() => {
    const timer = setTimeout(() => {
      setSearch(searchInput)
      setCurrentPage(0)
    }, 400)
    return () => clearTimeout(timer)
  }, [searchInput])

  // ── Handlers ──

  function refreshAll() {
    loadStats()
    loadUsers()
    loadAuditLogs()
  }

  async function handleCreate(data: CreateUserRequest) {
    await createUser(data)
    refreshAll()
  }

  async function handleUpdate(id: string, data: UpdateUserRequest) {
    const res = await updateUser(id, data)
    refreshAll()
    if (selectedUser?.id === id) {
      setSelectedUser(res.data.data)
    }
  }

  function handleToggleStatus(user: UserResponse) {
    const isDisabling = user.enabled
    const fullName = `${user.firstName} ${user.lastName}`
    setConfirm({
      open: true,
      title: isDisabling ? t.adminConfirmDeactivateTitle : t.adminConfirmActivateTitle,
      message: isDisabling ? t.adminConfirmDeactivateMsg(fullName) : t.adminConfirmActivateMsg(fullName),
      variant: isDisabling ? 'danger' : 'success',
      onConfirm: async () => {
        setConfirm(CONFIRM_INITIAL)
        try {
          await changeUserStatus(user.id, !user.enabled)
          refreshAll()
          if (selectedUser?.id === user.id) {
            setSelectedUser({ ...user, enabled: !user.enabled })
          }
          setToast({ message: isDisabling ? t.adminToastDeactivated : t.adminToastActivated, variant: 'success' })
        } catch (err) {
          const apiErr = getApiError(err)
          setToast({ message: apiErr.message, variant: 'error' })
        }
      },
    })
  }

  function handleDelete(user: UserResponse) {
    const fullName = `${user.firstName} ${user.lastName}`
    setConfirm({
      open: true,
      title: t.adminConfirmDeleteTitle,
      message: t.adminConfirmDeleteMsg(fullName),
      variant: 'danger',
      onConfirm: async () => {
        setConfirm(CONFIRM_INITIAL)
        try {
          await deleteUser(user.id)
          refreshAll()
          if (selectedUser?.id === user.id) {
            setSelectedUser(null)
          }
          setToast({ message: t.adminToastDeleted, variant: 'success' })
        } catch (err) {
          const apiErr = getApiError(err)
          setToast({ message: apiErr.message, variant: 'error' })
        }
      },
    })
  }

  function handleResendVerification(user: UserResponse) {
    const fullName = `${user.firstName} ${user.lastName}`
    setConfirm({
      open: true,
      title: t.adminConfirmVerificationTitle,
      message: t.adminConfirmVerificationMsg(fullName),
      variant: 'success',
      onConfirm: async () => {
        setConfirm(CONFIRM_INITIAL)
        try {
          await resendVerification(user.id)
          setToast({ message: t.adminToastVerificationSent, variant: 'success' })
        } catch (err) {
          const apiErr = getApiError(err)
          setToast({ message: apiErr.message, variant: 'error' })
        }
      },
    })
  }

  async function handleChangePassword(data: ChangePasswordRequest) {
    await changePassword(data)
    setToast({ message: t.adminToastPasswordChanged, variant: 'success' })
  }

  function handleSendResetLink(user: UserResponse) {
    const fullName = `${user.firstName} ${user.lastName}`
    setConfirm({
      open: true,
      title: t.adminConfirmResetLinkTitle,
      message: t.adminConfirmResetLinkMsg(fullName, user.email),
      variant: 'success',
      onConfirm: async () => {
        setConfirm(CONFIRM_INITIAL)
        try {
          await sendPasswordResetLink(user.email)
          setToast({ message: t.adminToastResetLinkSent, variant: 'success' })
        } catch (err) {
          const apiErr = getApiError(err)
          setToast({ message: apiErr.message, variant: 'error' })
        }
      },
    })
  }

  async function handleProfileClick() {
    try {
      const res = await getCurrentUser()
      setSelectedUser(res.data.data)
    } catch {
      setToast({ message: t.adminProfileLoadError, variant: 'error' })
    }
  }

  function handleStatusFilterChange(value: string) {
    setStatusFilter(value)
    setCurrentPage(0)
  }

  function handlePageChange(page: number) {
    setCurrentPage(page)
  }

  return (
    <AdminLayout
      currentUserName={currentUserName}
      currentUserInitials={currentUserInitials}
      onProfileClick={handleProfileClick}
      t={t}
    >
      {/* KPI Kartları */}
      <KpiCards stats={stats} loading={statsLoading} t={t} />

      {/* Tablo + Detay paneli */}
      <div className="grid grid-cols-1 items-stretch gap-5 xl:grid-cols-[minmax(0,1fr)_320px]">
        <UsersTable
          data={usersData}
          loading={usersLoading}
          search={searchInput}
          onSearchChange={setSearchInput}
          statusFilter={statusFilter}
          onStatusFilterChange={handleStatusFilterChange}
          selectedUserId={selectedUser?.id ?? null}
          onSelectUser={setSelectedUser}
          onPageChange={handlePageChange}
          onCreateClick={() => setCreateModalOpen(true)}
          onEditClick={setEditUser}
          onToggleStatus={handleToggleStatus}
          onDeleteClick={handleDelete}
          currentUserEmail={currentUserEmail}
          t={t}
        />

        <UserDetailPanel
          user={selectedUser}
          currentUserEmail={currentUserEmail}
          onEditClick={setEditUser}
          onResendVerification={handleResendVerification}
          onChangePassword={() => setChangePasswordOpen(true)}
          onSendResetLink={handleSendResetLink}
          t={t}
        />
      </div>

      <ActivityTimeline
        data={auditData}
        loading={auditLoading}
        search={auditSearch}
        onSearchChange={setAuditSearch}
        scope={auditScope}
        onScopeChange={setAuditScope}
        t={t}
      />

      {/* Modal'lar */}
      <CreateUserModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onSubmit={handleCreate}
        t={t}
      />
      <EditUserModal
        user={editUser}
        onClose={() => setEditUser(null)}
        onSubmit={handleUpdate}
        t={t}
      />
      <ChangePasswordModal
        open={changePasswordOpen}
        onClose={() => setChangePasswordOpen(false)}
        onSubmit={handleChangePassword}
        t={t}
      />
      <ConfirmModal
        open={confirm.open}
        title={confirm.title}
        message={confirm.message}
        variant={confirm.variant}
        confirmLabel={t.adminConfirmYes}
        cancelLabel={t.adminConfirmNo}
        onConfirm={confirm.onConfirm}
        onCancel={() => setConfirm(CONFIRM_INITIAL)}
      />

      {/* Toast */}
      {toast && (
        <div className={`fixed bottom-6 right-6 z-50 px-4 py-3 rounded-[12px] shadow-lg text-sm font-medium border transition-all ${
          toast.variant === 'success'
            ? 'bg-admin-green-wash text-admin-green border-admin-green/20'
            : 'bg-admin-red-wash text-admin-red border-admin-red/20'
        }`}>
          {toast.message}
        </div>
      )}
    </AdminLayout>
  )
}
