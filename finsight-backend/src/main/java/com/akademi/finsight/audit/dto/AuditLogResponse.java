package com.akademi.finsight.audit.dto;

import com.akademi.finsight.audit.entity.AuditActionType;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        AuditActionType action,
        String actorFullName,
        String targetFullName,
        Instant createdAt
) {
}
