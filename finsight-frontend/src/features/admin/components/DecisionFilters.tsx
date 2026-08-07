import type { DecisionType } from '../adminDecisionApi'
import type { Translations } from '@/i18n/translations'

type UserOption = {
  id: string
  name: string
}

type DecisionFiltersProps = {
  users: UserOption[]
  userFilter: string
  onUserFilterChange: (value: string) => void
  typeFilter: DecisionType | ''
  onTypeFilterChange: (value: DecisionType | '') => void
  daysFilter: string
  onDaysFilterChange: (value: string) => void
  t: Translations
}

const SELECT_CLASS =
  "bg-admin-ivory border border-admin-line rounded-[10px] py-[9px] px-3 pr-7 text-[12.5px] font-medium text-admin-text appearance-none bg-[url('data:image/svg+xml;utf8,<svg%20xmlns=%22http://www.w3.org/2000/svg%22%20width=%2210%22%20height=%226%22%20viewBox=%220%200%2010%206%22><path%20d=%22M1%201l4%204%204-4%22%20stroke=%22%23767a86%22%20stroke-width=%221.4%22%20fill=%22none%22%20stroke-linecap=%22round%22%20stroke-linejoin=%22round%22/></svg>')] bg-no-repeat bg-[position:right_10px_center] cursor-pointer focus:outline-none"

export default function DecisionFilters({
  users,
  userFilter,
  onUserFilterChange,
  typeFilter,
  onTypeFilterChange,
  daysFilter,
  onDaysFilterChange,
  t,
}: DecisionFiltersProps) {
  return (
    <div className="flex items-center gap-2.5 mb-3.5 flex-wrap">
      <select value={userFilter} onChange={(e) => onUserFilterChange(e.target.value)} className={SELECT_CLASS}>
        <option value="">{t.adminPanelFilterAllUsers}</option>
        {users.map((user) => (
          <option key={user.id} value={user.id}>
            {user.name}
          </option>
        ))}
      </select>

      <select
        value={typeFilter}
        onChange={(e) => onTypeFilterChange(e.target.value as DecisionType | '')}
        className={SELECT_CLASS}
      >
        <option value="">{t.adminPanelFilterAllTypes}</option>
        <option value="AI_APPROVED">{t.adminPanelTypeAiApproved}</option>
        <option value="AI_REJECTED">{t.adminPanelTypeAiRejected}</option>
        <option value="MANUAL">{t.adminPanelTypeManual}</option>
      </select>

      <select value={daysFilter} onChange={(e) => onDaysFilterChange(e.target.value)} className={SELECT_CLASS}>
        <option value="7">{t.adminPanelDateLast7}</option>
        <option value="30">{t.adminPanelDateLast30}</option>
        <option value="">{t.adminPanelDateAllTime}</option>
      </select>
    </div>
  )
}
