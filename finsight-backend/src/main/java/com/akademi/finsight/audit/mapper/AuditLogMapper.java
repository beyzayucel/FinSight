package com.akademi.finsight.audit.mapper;

import com.akademi.finsight.audit.dto.AuditLogResponse;
import com.akademi.finsight.audit.entity.AuditLog;
import com.akademi.finsight.common.mapper.BaseMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = BaseMapperConfig.class)
public interface AuditLogMapper {

    AuditLogResponse toResponse(AuditLog auditLog);
}
