import React from 'react'
import DashboardLayout from '@/features/dashboard/components/DashboardLayout' // Orijinal layout yolun
import { useDecision } from '../context/decisionStore'

type DashboardLayoutWrapperProps = {
  activeMenuIndex: 1 | 2 | 3 | 4 | 5
  onMenuChange: (index: 1 | 2 | 3 | 4 | 5) => void
  children: React.ReactNode
}

export default function DashboardLayoutWrapper({
  activeMenuIndex,
  onMenuChange,
  children,
}: DashboardLayoutWrapperProps) {
  // Context'ten 90 gün bilgisini ve değiştiren fonksiyonu alıyoruz
  const { activeFund, analysisWindow, setAnalysisWindow } = useDecision()

  return (
    <DashboardLayout
      activeMenuIndex={activeMenuIndex}
      onMenuChange={onMenuChange}
      fundName={activeFund.name}
      assetClassCount={activeFund.assetClassCount}
      analysisPeriod={String(analysisWindow)} // "10", "20", "30" veya "90"
      onPeriodChange={(period) => setAnalysisWindow(Number(period) as any)}
    >
      {children}
    </DashboardLayout>
  )
}