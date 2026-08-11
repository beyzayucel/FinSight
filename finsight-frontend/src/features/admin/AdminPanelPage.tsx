import { useState, useEffect, useCallback } from 'react'
import { getAccessToken } from '@/lib/authStore'
import { getTranslations } from '@/i18n/translations'
import { getUsers } from './adminApi'
import { getDecisionReport } from './adminDecisionApi'
import type { AdminDecisionRecord, DecisionType } from './adminDecisionApi'
import AdminLayout from './components/AdminLayout'
import DecisionKpiCards from './components/DecisionKpiCards'
import DecisionFilters from './components/DecisionFilters'
import DecisionsTable from './components/DecisionsTable'
import DecisionDistributionChart from './components/DecisionDistributionChart'

type UserOption = { id: string; name: string }

const USER_PAGE_SIZE = 200

/**
 * Kullanıcı filtresi dropdown'ı tüm portföy yöneticilerini içermeli, ama /users sayfalı
 * dönüyor — tek sayfaya sığdığını varsaymak yerine sayfaları sırayla topluyoruz.
 */
async function fetchAllUsers(): Promise<UserOption[]> {
  const firstPage = await getUsers({ page: 0, size: USER_PAGE_SIZE, sort: 'firstName,asc' })
  const { content, totalPages } = firstPage.data.data
  const all = [...content]

  for (let page = 1; page < totalPages; page++) {
    const next = await getUsers({ page, size: USER_PAGE_SIZE, sort: 'firstName,asc' })
    all.push(...next.data.data.content)
  }

  return all.map((user) => ({ id: user.id, name: `${user.firstName} ${user.lastName}` }))
}

function parseCurrentUser(
  token: string | null,
  fallbackName: string,
  fallbackInitials: string,
): { name: string; initials: string } {
  if (!token) return { name: fallbackName, initials: fallbackInitials }
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    const email: string = payload.sub ?? ''
    const name = email.split('@')[0].replace(/\./g, ' ')
    const parts = name.split(' ')
    const initials = parts.length >= 2
      ? `${parts[0][0]}${parts[1][0]}`.toUpperCase()
      : name.substring(0, 2).toUpperCase()
    return { name, initials }
  } catch {
    return { name: fallbackName, initials: fallbackInitials }
  }
}

export default function AdminPanelPage() {
  const t = getTranslations()

  useEffect(() => {
    document.title = 'Finsight · Panel'
  }, [])

  const token = getAccessToken()
  const { name: currentUserName, initials: currentUserInitials } =
    parseCurrentUser(token, t.adminFallbackName, t.adminFallbackInitials)

  const [users, setUsers] = useState<UserOption[]>([])

  const [decisions, setDecisions] = useState<AdminDecisionRecord[]>([])
  const [loading, setLoading] = useState(true)

  const [userFilter, setUserFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState<DecisionType | ''>('')
  const [daysFilter, setDaysFilter] = useState('30')

  useEffect(() => {
    fetchAllUsers()
      .then(setUsers)
      .catch(() => {
        // Dropdown boş kalır; kullanıcı filtresi olmadan rapor yine de görüntülenebilir.
      })
  }, [])

  const loadDecisions = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getDecisionReport({
        userId: userFilter || undefined,
        type: typeFilter || undefined,
        days: daysFilter ? Number(daysFilter) : undefined,
      })
      setDecisions(res.data.data)
    } catch {
      setDecisions([])
    } finally {
      setLoading(false)
    }
  }, [userFilter, typeFilter, daysFilter])

  useEffect(() => {
    loadDecisions()
  }, [loadDecisions])

  return (
    <AdminLayout
      currentUserName={currentUserName}
      currentUserInitials={currentUserInitials}
      title={t.adminPanel}
      breadcrumb={t.adminPanelBreadcrumb}
      t={t}
    >
      <DecisionKpiCards decisions={decisions} loading={loading} t={t} />

      <div className="grid grid-cols-1 items-start gap-5 xl:grid-cols-[minmax(0,1fr)_320px]">
        <div>
          <DecisionFilters
            users={users}
            userFilter={userFilter}
            onUserFilterChange={setUserFilter}
            typeFilter={typeFilter}
            onTypeFilterChange={setTypeFilter}
            daysFilter={daysFilter}
            onDaysFilterChange={setDaysFilter}
            t={t}
          />
          <DecisionsTable decisions={decisions} loading={loading} t={t} />
        </div>

        <div className="flex flex-col gap-5">
          <DecisionDistributionChart decisions={decisions} t={t} />

          <div className="bg-admin-gold-wash border border-admin-gold-soft rounded-[14px] p-4 flex gap-2.5">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" className="w-4 h-4 shrink-0 stroke-admin-gold mt-0.5">
              <circle cx="12" cy="12" r="9" />
              <path d="M12 8h.01M11 12h1v4h1" />
            </svg>
            <p className="text-[11.5px] leading-relaxed text-admin-ink/80 font-medium">{t.adminPanelInfoNote}</p>
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}
