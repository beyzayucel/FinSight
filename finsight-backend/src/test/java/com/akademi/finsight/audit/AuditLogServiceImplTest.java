package com.akademi.finsight.audit;

import com.akademi.finsight.audit.entity.AuditActionType;
import com.akademi.finsight.audit.entity.AuditLog;
import com.akademi.finsight.audit.mapper.AuditLogMapper;
import com.akademi.finsight.audit.repository.AuditLogRepository;
import com.akademi.finsight.audit.service.impl.AuditLogServiceImpl;
import com.akademi.finsight.user.entity.Role;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.UserErrorType;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ──────────────────────────────────────────────────────────────
    // createAuditLogForSelf
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createAuditLogForSelf - should save audit log with user as both actor and target")
    void createAuditLogForSelf_shouldSaveWithUserAsBothActorAndTarget() {
        User user = createUser();

        auditLogService.createAuditLogForSelf(AuditActionType.LOGIN_SUCCESS, user);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(AuditActionType.LOGIN_SUCCESS, saved.getAction());
        assertEquals(user.getId(), saved.getActorUserId());
        assertEquals(user.getId(), saved.getTargetUserId());
        assertEquals(user.getFullName(), saved.getActorFullName());
        assertEquals(user.getFullName(), saved.getTargetFullName());
    }

    // ──────────────────────────────────────────────────────────────
    // createAuditLogForAdmin
    // ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createAuditLogForAdmin - should save with admin as actor and target user as target")
    void createAuditLogForAdmin_shouldSaveWithAdminAsActor() {
        User admin = createUser();
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);
        UUID adminId = UUID.randomUUID();
        ReflectionTestUtils.setField(admin, "id", adminId);

        User targetUser = createUser();
        UUID targetId = UUID.randomUUID();
        ReflectionTestUtils.setField(targetUser, "id", targetId);

        setSecurityContext("admin@test.com");
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        auditLogService.createAuditLogForAdmin(AuditActionType.USER_CREATED, targetUser);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(AuditActionType.USER_CREATED, saved.getAction());
        assertEquals(adminId, saved.getActorUserId());
        assertEquals(targetId, saved.getTargetUserId());
    }

    @Test
    @DisplayName("createAuditLogForAdmin - should throw USER_NOT_FOUND when admin user not found")
    void createAuditLogForAdmin_shouldThrowWhenAdminNotFound() {
        User targetUser = createUser();

        setSecurityContext("unknown@test.com");
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        UserException exception = assertThrows(UserException.class,
                () -> auditLogService.createAuditLogForAdmin(AuditActionType.USER_CREATED, targetUser));
        assertEquals(UserErrorType.USER_NOT_FOUND, exception.getErrorType());

        verifyNoInteractions(auditLogRepository);
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private User createUser() {
        User user = User.builder()
                .email("ali@test.com")
                .username("alikaygusuz")
                .firstName("Ali")
                .lastName("Kaygusuz")
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private void setSecurityContext(String email) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null));
    }
}
