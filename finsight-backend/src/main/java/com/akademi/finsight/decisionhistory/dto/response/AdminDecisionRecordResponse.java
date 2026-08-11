package com.akademi.finsight.decisionhistory.dto.response;

import com.akademi.finsight.decisionhistory.entity.DecisionType;

import java.time.Instant;
import java.util.UUID;

// Admin Panel Karar Raporu tablosu için — kullanıcı/fon adını içerir, PM'nin kendi
// Karar Geçmişi ekranındaki DecisionRecordResponse'dan farklı olarak tek kullanıcıya/fona
// bağlı değildir (bkz. Ekran 07 Bölüm 5.5).
public record AdminDecisionRecordResponse(
        UUID id,
        Instant decisionDate,
        String userName,
        String fundName,
        DecisionType decisionType
) {}
