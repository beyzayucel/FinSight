package com.akademi.finsight.audit.controller.api;

import com.akademi.finsight.audit.dto.AuditLogResponse;
import com.akademi.finsight.audit.entity.AuditLogScope;
import com.akademi.finsight.common.constants.ApiEndpoints;
import com.akademi.finsight.common.response.ApiStandardResponse;
import com.akademi.finsight.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(ApiEndpoints.AuditLogs.BASE)
@Tag(
        name = "Audit Log",
        description = "Admin audit log operations"
)
public interface AuditLogApi {

    @Operation(
            summary = "List audit logs (Admin only)",
            description = "Returns paginated audit logs with optional scope filter and user name search."
    )
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied — requires ADMIN role")
    @GetMapping
    ResponseEntity<ApiStandardResponse<PageResponse<AuditLogResponse>>> getAuditLogs(
            @Parameter(description = "Scope filter: ALL (default), ACTIVE, or ARCHIVED")
            @RequestParam(defaultValue = "ALL")
            AuditLogScope scope,

            @Parameter(description = "Search by actor or target first/last name")
            @RequestParam(required = false)
            String search,

            @ParameterObject
            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    );
}
