package com.akademi.finsight.audit.service.impl;

import com.akademi.finsight.audit.dto.AuditLogResponse;
import com.akademi.finsight.audit.entity.AuditActionType;
import com.akademi.finsight.audit.entity.AuditLog;
import com.akademi.finsight.audit.entity.AuditLogScope;
import com.akademi.finsight.audit.mapper.AuditLogMapper;
import com.akademi.finsight.audit.repository.AuditLogRepository;
import com.akademi.finsight.audit.repository.AuditLogSpecification;
import com.akademi.finsight.audit.service.AuditLogService;
import com.akademi.finsight.common.web.RequestIdFilter;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.UserErrorType;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.repository.UserRepository;
import com.akademi.finsight.user.util.EmailNormalizer;
import com.akademi.finsight.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(AuditLogScope scope, String search, Pageable pageable) {
        Specification<AuditLog> spec = Specification
                .where(AuditLogSpecification.withScope(scope))
                .and(AuditLogSpecification.withSearch(search));

        return auditLogRepository.findAll(spec, pageable)
                .map(auditLogMapper::toResponse);
    }


    @Override
    @Transactional
    public void createAuditLogForSelf(AuditActionType action, User user) {
        saveAuditLog(action, user, user);
    }

    @Override
    @Transactional
    public void createAuditLogForAdmin(AuditActionType action, User targetUser) {
        String adminEmail = SecurityUtils.getCurrentUserEmail();
        User admin = userRepository.findByEmail(EmailNormalizer.normalize(adminEmail))
                .orElseThrow(() -> new UserException(UserErrorType.USER_NOT_FOUND));
        saveAuditLog(action, admin, targetUser);
    }

    private void saveAuditLog(AuditActionType action, User actorUser, User targetUser) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .actorUserId(actorUser.getId())
                .actorFullName(actorUser.getFullName())
                .targetUserId(targetUser.getId())
                .targetFullName(targetUser.getFullName())
                .requestId(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY))
                .build();

        auditLogRepository.save(auditLog);
    }
}
