package com.akademi.finsight.audit.controller;

import com.akademi.finsight.audit.controller.api.AuditLogApi;
import com.akademi.finsight.audit.dto.AuditLogResponse;
import com.akademi.finsight.audit.entity.AuditLogScope;
import com.akademi.finsight.audit.service.AuditLogService;
import com.akademi.finsight.common.controller.BaseController;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuditLogController extends BaseController implements AuditLogApi {

    private final AuditLogService auditLogService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiStandardResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            AuditLogScope scope,
            String search,
            Pageable pageable
    ) {
        return ok(PageResponse.of(
                auditLogService.getAuditLogs(scope, search, pageable)
        ));
    }
}
