import { useState, useEffect, useCallback } from 'react'
import { getAccessToken } from '@/lib/authStore'
import { getApiError } from '@/lib/api/apiError'
import {
  getUsers,
  getUserStats,
  createUser,
  updateUser,
  changeUserStatus,
  deleteUser,
  resendVerification,
  changePassword,
  sendPasswordResetLink,
} from './adminApi'
import type {
  UserResponse,
  UserStatsResponse,
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

function parseCurrentUser(token: string | null): { name: string; initials: string; email: string } {
  if (!token) return { name: 'Yönetici', initials: 'YÖ', email: '' }
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
    return { name: 'Yönetici', initials: 'YÖ', email: '' }
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

  const [confirm, setConfirm] = useState<ConfirmState>(CONFIRM_INITIAL)
  const [toast, setToast] = useState<{ message: string; variant: 'success' | 'error' } | null>(null)

  const token = getAccessToken()
  const { name: currentUserName, initials: currentUserInitials, email: currentUserEmail } = parseCurrentUser(token)

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

  useEffect(() => {
    loadStats()
  }, [loadStats])

  useEffect(() => {
    loadUsers()
  }, [loadUsers])

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
    setConfirm({
      open: true,
      title: isDisabling ? 'Kullanıcıyı Pasifleştir' : 'Kullanıcıyı Aktifleştir',
      message: `${user.firstName} ${user.lastName} ${isDisabling ? 'pasifleştirilsin' : 'aktifleştirilsin'} mi?`,
      variant: isDisabling ? 'danger' : 'success',
      onConfirm: async () => {
        setConfirm(CONFIRM_INITIAL)
        try {
          await changeUserStatus(user.id, !user.enabled)
          refreshAll()
          if (selectedUser?.id === user.id) {
            setSelectedUser({ ...user, enabled: !user.enabled })
          }
          setToast({ message: `Kullanıcı ${isDisabling ? 'pasifleştirildi' : 'aktifleştirildi'}`, variant: 'success' })
        } catch (err) {
          const apiErr = getApiError(err)
          setToast({ message: apiErr.message, variant: 'error' })
        }
      },
    })
  }

  function handleDelete(user: UserResponse) {
    setConfirm({
      open: true,
      title: 'Kullanıcıyı Sil',
      message: `${user.firstName} ${user.lastName} silinsin mi? Bu işlem geri alınamaz.`,
      variant: 'danger',
      onConfirm: async () => {
        setConfirm(CONFIRM_INITIAL)
        try {
          await deleteUser(user.id)
          refreshAll()
          if (selectedUser?.id === user.id) {
            setSelectedUser(null)
          }
          setToast({ message: 'Kullanıcı silindi', variant: 'success' })
        } catch (err) {
          const apiErr = getApiError(err)
          setToast({ message: apiErr.message, variant: 'error' })
        }
      },
    })
  }

  function handleResendVerification(user: UserResponse) {
    setConfirm({
      open: true,
      title: 'Doğrulama E-postası Gönder',
      message: `${user.firstName} ${user.lastName} için doğrulama e-postası tekrar gönderilsin mi?`,
      variant: 'success',
      onConfirm: async () => {
        setConfirm(CONFIRM_INITIAL)
        try {
          await resendVerification(user.id)
          setToast({ message: 'Doğrulama e-postası gönderildi', variant: 'success' })
        } catch (err) {
          const apiErr = getApiError(err)
          setToast({ message: apiErr.message, variant: 'error' })
        }
      },
    })
  }

  async function handleChangePassword(data: ChangePasswordRequest) {
    await changePassword(data)
    setToast({ message: 'Şifre başarıyla değiştirildi', variant: 'success' })
  }

  function handleSendResetLink(user: UserResponse) {
    setConfirm({
      open: true,
      title: 'Şifre Sıfırlama Linki Gönder',
      message: `${user.firstName} ${user.lastName} (${user.email}) adresine şifre sıfırlama linki gönderilsin mi?`,
      variant: 'success',
      onConfirm: async () => {
        setConfirm(CONFIRM_INITIAL)
        try {
          await sendPasswordResetLink(user.email)
          setToast({ message: 'Şifre sıfırlama linki gönderildi', variant: 'success' })
        } catch (err) {
          const apiErr = getApiError(err)
          setToast({ message: apiErr.message, variant: 'error' })
        }
      },
    })
  }

  async function handleProfileClick() {
    if (!currentUserEmail) return
    try {
      const res = await getUsers({ search: currentUserEmail, size: 1 })
      const match = res.data.data.content.find((u) => u.email === currentUserEmail)
      if (match) {
        setSelectedUser(match)
      } else {
        setToast({ message: 'Profil bilgisi bulunamadı', variant: 'error' })
      }
    } catch {
      setToast({ message: 'Profil yüklenirken bir hata oluştu', variant: 'error' })
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
    >
      {/* KPI Kartları */}
      <KpiCards stats={stats} loading={statsLoading} />

      {/* Tablo + Detay paneli */}
      <div className="grid grid-cols-[1fr_320px] gap-5 items-start">
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
        />

        <UserDetailPanel
          user={selectedUser}
          currentUserEmail={currentUserEmail}
          onEditClick={setEditUser}
          onResendVerification={handleResendVerification}
          onChangePassword={() => setChangePasswordOpen(true)}
          onSendResetLink={handleSendResetLink}
        />
      </div>

      {/* Modal'lar */}
      <CreateUserModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        onSubmit={handleCreate}
      />
      <EditUserModal
        user={editUser}
        onClose={() => setEditUser(null)}
        onSubmit={handleUpdate}
      />
      <ChangePasswordModal
        open={changePasswordOpen}
        onClose={() => setChangePasswordOpen(false)}
        onSubmit={handleChangePassword}
      />
      <ConfirmModal
        open={confirm.open}
        title={confirm.title}
        message={confirm.message}
        variant={confirm.variant}
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
