package com.akademi.finsight.audit.service;

import com.akademi.finsight.audit.dto.AuditLogResponse;
import com.akademi.finsight.audit.entity.AuditActionType;
import com.akademi.finsight.audit.entity.AuditLogScope;
import com.akademi.finsight.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogService {

    Page<AuditLogResponse> getAuditLogs(AuditLogScope scope, String search, Pageable pageable);



    void createAuditLogForSelf(
            AuditActionType action,
            User user
    );

    void createAuditLogForAdmin(AuditActionType action, User targetUser);
}
