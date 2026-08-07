package com.akademi.finsight.fund.entity;

// Admin Panel Karar Raporu ekranı için — AI kararının onay/red durumunu ve manuel senaryoyu
// tek bir filtrelenebilir enum'da birleştirir (bkz. Ekran 07 Bölüm 5.4).
public enum DecisionType {
    AI_APPROVED,
    AI_REJECTED,
    MANUAL
}
