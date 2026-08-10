package com.akademi.finsight.user;

import com.akademi.finsight.audit.entity.AuditActionType;
import com.akademi.finsight.audit.service.AuditLogService;
import com.akademi.finsight.auth.refreshtoken.service.RefreshTokenService;
import com.akademi.finsight.auth.verificationtoken.service.VerificationTokenService;
import com.akademi.finsight.user.dto.request.CreateUserRequest;
import com.akademi.finsight.user.dto.request.UpdateUserRequest;
import com.akademi.finsight.user.dto.response.UserResponse;
import com.akademi.finsight.user.dto.response.UserStatsResponse;
import com.akademi.finsight.user.entity.Role;
import com.akademi.finsight.user.entity.User;
import com.akademi.finsight.user.exception.UserErrorType;
import com.akademi.finsight.user.exception.UserException;
import com.akademi.finsight.user.mapper.UserMapper;
import com.akademi.finsight.user.repository.UserRepository;
import com.akademi.finsight.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private VerificationTokenService tokenService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserServiceImpl userService;



    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("should create user and send verification token")
        void shouldCreateAndSendVerification() {
            CreateUserRequest request = new CreateUserRequest("Ali@Test.com", "Ali", "Kaygusuz", "+905551234567");
            User user = createUser();

            when(userRepository.existsByEmail("ali@test.com")).thenReturn(false);
            when(userRepository.existsByPhoneNumber("+905551234567")).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(user);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(user)).thenReturn(user);

            assertDoesNotThrow(() -> userService.createUser(request));

            verify(tokenService).createAndSendVerificationToken(eq(user), anyString());
            verify(auditLogService).createAuditLogForAdmin(AuditActionType.USER_CREATED, user);
        }

        @Test
        @DisplayName("should throw EMAIL_ALREADY_EXISTS when email already exists")
        void shouldThrowWhenEmailExists() {
            CreateUserRequest request = new CreateUserRequest("ali@test.com", "Ali", "Kaygusuz", "+905551234567");

            when(userRepository.existsByEmail("ali@test.com")).thenReturn(true);

            UserException exception = assertThrows(UserException.class, () -> userService.createUser(request));
            assertEquals(UserErrorType.EMAIL_ALREADY_EXISTS, exception.getErrorType());

            verify(userRepository, never()).save(any());
            verifyNoInteractions(tokenService, auditLogService);
        }

        @Test
        @DisplayName("should throw PHONE_ALREADY_EXISTS when phone already exists")
        void shouldThrowWhenPhoneExists() {
            CreateUserRequest request = new CreateUserRequest("ali@test.com", "Ali", "Kaygusuz", "+905551234567");

            when(userRepository.existsByEmail("ali@test.com")).thenReturn(false);
            when(userRepository.existsByPhoneNumber("+905551234567")).thenReturn(true);

            UserException exception = assertThrows(UserException.class, () -> userService.createUser(request));
            assertEquals(UserErrorType.PHONE_ALREADY_EXISTS, exception.getErrorType());

            verify(userRepository, never()).save(any());
            verifyNoInteractions(tokenService, auditLogService);
        }

        @Test
        @DisplayName("should generate unique username when base exists")
        void shouldGenerateUniqueUsernameWhenBaseExists() {
            CreateUserRequest request = new CreateUserRequest("ali@test.com", "Ali", "Kaygusuz", "+905551234567");
            User user = createUser();

            when(userRepository.existsByEmail("ali@test.com")).thenReturn(false);
            when(userRepository.existsByPhoneNumber("+905551234567")).thenReturn(false);
            when(userRepository.existsByUsername(anyString())).thenReturn(true, true, false);
            when(userMapper.toEntity(request)).thenReturn(user);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(user)).thenReturn(user);

            assertDoesNotThrow(() -> userService.createUser(request));

            verify(userRepository, times(3)).existsByUsername(anyString());
        }
    }

    // ══════════════════════════════════════════════════════════════
    // getCurrentUser
    // ══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getCurrentUser")
    class GetCurrentUser {

        @Test
        @DisplayName("should return user response when found")
        void shouldReturnWhenFound() {
            User user = createUser();
            UserResponse response = createUserResponse(user);

            when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(response);

            UserResponse result = userService.getCurrentUser("ali@test.com");

            assertEquals(response, result);
        }

        @Test
        @DisplayName("should throw USER_NOT_FOUND when user not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            UserException exception = assertThrows(UserException.class,
                    () -> userService.getCurrentUser("unknown@test.com"));
            assertEquals(UserErrorType.USER_NOT_FOUND, exception.getErrorType());

            verifyNoInteractions(userMapper);
        }
    }





    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("should update fields and return response")
        void shouldUpdateAndReturn() {
            UUID userId = UUID.randomUUID();
            User user = createUserWithId(userId);
            UpdateUserRequest request = new UpdateUserRequest("Veli", "Yilmaz", "+905559876543");
            UserResponse expectedResponse = createUserResponse(user);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByPhoneNumberAndIdNot("+905559876543", userId)).thenReturn(false);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(expectedResponse);

            UserResponse result = userService.updateUser(userId, request);

            assertEquals("Veli", user.getFirstName());
            assertEquals("Yilmaz", user.getLastName());
            assertEquals("+905559876543", user.getPhoneNumber());
            assertEquals(expectedResponse, result);
            verify(userRepository).save(user);
            verify(auditLogService).createAuditLogForAdmin(AuditActionType.USER_UPDATED, user);
        }

        @Test
        @DisplayName("should throw PHONE_ALREADY_EXISTS when phone taken by another user")
        void shouldThrowWhenPhoneTaken() {
            UUID userId = UUID.randomUUID();
            UpdateUserRequest request = new UpdateUserRequest("Veli", "Yilmaz", "+905559876543");
            User user = createUserWithId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.existsByPhoneNumberAndIdNot("+905559876543", userId)).thenReturn(true);

            UserException exception = assertThrows(UserException.class,
                    () -> userService.updateUser(userId, request));
            assertEquals(UserErrorType.PHONE_ALREADY_EXISTS, exception.getErrorType());

            verify(userRepository, never()).save(any());
            verifyNoInteractions(auditLogService);
        }
    }


    @Nested
    @DisplayName("changeUserStatus")
    class ChangeUserStatus {

        @Test
        @DisplayName("should activate disabled user")
        void shouldActivateUser() {
            UUID userId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            User user = createUserWithId(userId);
            user.setEnabled(false);
            user.setRole(Role.USER);
            User admin = createUserWithId(adminId);
            admin.setEmail("admin@test.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

            assertDoesNotThrow(() -> userService.changeUserStatus(userId, true, "admin@test.com"));

            assertTrue(user.isEnabled());
            verify(auditLogService).createAuditLogForAdmin(AuditActionType.USER_ACTIVATED, user);
        }

        @Test
        @DisplayName("should throw USER_STATUS_UNCHANGED when status unchanged")
        void shouldThrowWhenStatusUnchanged() {
            UUID userId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            User user = createUserWithId(userId);
            user.setEnabled(true);
            user.setRole(Role.USER);
            User admin = createUserWithId(adminId);
            admin.setEmail("admin@test.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

            UserException exception = assertThrows(UserException.class,
                    () -> userService.changeUserStatus(userId, true, "admin@test.com"));
            assertEquals(UserErrorType.USER_STATUS_UNCHANGED, exception.getErrorType());

            verifyNoInteractions(auditLogService);
        }

        @Test
        @DisplayName("should throw SELF_ACTION_NOT_ALLOWED when admin tries to change own status")
        void shouldThrowWhenSelfAction() {
            UUID userId = UUID.randomUUID();
            User user = createUserWithId(userId);
            user.setRole(Role.USER);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));

            UserException exception = assertThrows(UserException.class,
                    () -> userService.changeUserStatus(userId, false, "ali@test.com"));
            assertEquals(UserErrorType.SELF_ACTION_NOT_ALLOWED, exception.getErrorType());

            verifyNoInteractions(auditLogService);
        }

        @Test
        @DisplayName("should throw ADMIN_PROTECTED when target is admin")
        void shouldThrowWhenTargetIsAdmin() {
            UUID userId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            User targetAdmin = createUserWithId(userId);
            targetAdmin.setRole(Role.ADMIN);
            User currentAdmin = createUserWithId(adminId);
            currentAdmin.setEmail("admin@test.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(targetAdmin));
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(currentAdmin));

            UserException exception = assertThrows(UserException.class,
                    () -> userService.changeUserStatus(userId, false, "admin@test.com"));
            assertEquals(UserErrorType.ADMIN_PROTECTED, exception.getErrorType());

            verifyNoInteractions(auditLogService);
        }
    }




    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("should delete user and revoke tokens")
        void shouldDeleteAndRevokeTokens() {
            UUID userId = UUID.randomUUID();
            UUID adminId = UUID.randomUUID();
            User user = createUserWithId(userId);
            user.setRole(Role.USER);
            User admin = createUserWithId(adminId);
            admin.setEmail("admin@test.com");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

            assertDoesNotThrow(() -> userService.deleteUser(userId, "admin@test.com"));

            verify(refreshTokenService).revokeAllByUser(user);
            verify(userRepository).delete(user);
            verify(auditLogService).createAuditLogForAdmin(AuditActionType.USER_DELETED, user);
        }
    }


    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnWhenFound() {
            User user = createUser();
            when(userRepository.findByEmail("ali@test.com")).thenReturn(Optional.of(user));

            User result = userService.findByEmail("ali@test.com");

            assertEquals(user, result);
        }

        @Test
        @DisplayName("should throw USER_NOT_FOUND when not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            UserException exception = assertThrows(UserException.class,
                    () -> userService.findByEmail("unknown@test.com"));
            assertEquals(UserErrorType.USER_NOT_FOUND, exception.getErrorType());
        }
    }


    @Nested
    @DisplayName("findByIdentifier")
    class FindByIdentifier {

        @Test
        @DisplayName("should normalize email identifier")
        void shouldNormalizeEmailIdentifier() {
            User user = createUser();
            when(userRepository.findByIdentifier("ali@test.com")).thenReturn(Optional.of(user));

            User result = userService.findByIdentifier("Ali@Test.com");

            assertEquals(user, result);
        }

        @Test
        @DisplayName("should pass username as-is")
        void shouldPassUsernameAsIs() {
            User user = createUser();
            when(userRepository.findByIdentifier("alikaygusuz")).thenReturn(Optional.of(user));

            User result = userService.findByIdentifier("alikaygusuz");

            assertEquals(user, result);
        }
    }


    @Nested
    @DisplayName("updatePassword")
    class UpdatePassword {

        @Test
        @DisplayName("should clear firstLogin when flag is true")
        void shouldClearFirstLoginWhenFlagTrue() {
            User user = createUser();
            user.setFirstLogin(true);

            userService.updatePassword(user, "newEncoded", true);

            assertFalse(user.isFirstLogin());
            assertEquals("newEncoded", user.getPassword());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should not clear firstLogin when flag is false")
        void shouldNotClearFirstLoginWhenFlagFalse() {
            User user = createUser();
            user.setFirstLogin(true);

            userService.updatePassword(user, "newEncoded", false);

            assertTrue(user.isFirstLogin());
            verify(userRepository).save(user);
        }
    }



    @Nested
    @DisplayName("resendVerification")
    class ResendVerification {

        @Test
        @DisplayName("should resend when email not verified")
        void shouldResendWhenNotVerified() {
            UUID userId = UUID.randomUUID();
            User user = createUserWithId(userId);
            user.setEmailVerified(false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode(anyString())).thenReturn("newEncoded");
            when(userRepository.save(user)).thenReturn(user);

            assertDoesNotThrow(() -> userService.resendVerification(userId));

            verify(tokenService).resendVerificationToken(eq(user), anyString());
            verify(auditLogService).createAuditLogForAdmin(AuditActionType.VERIFICATION_RESENT, user);
        }

        @Test
        @DisplayName("should throw EMAIL_ALREADY_VERIFIED when email already verified")
        void shouldThrowWhenAlreadyVerified() {
            UUID userId = UUID.randomUUID();
            User user = createUserWithId(userId);
            user.setEmailVerified(true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));

            UserException exception = assertThrows(UserException.class,
                    () -> userService.resendVerification(userId));
            assertEquals(UserErrorType.EMAIL_ALREADY_VERIFIED, exception.getErrorType());

            verifyNoInteractions(tokenService, auditLogService);
        }
    }



    @Nested
    @DisplayName("getUserStats")
    class GetUserStats {

        @Test
        @DisplayName("should return correct stats")
        void shouldReturnStats() {
            when(userRepository.count()).thenReturn(100L);
            when(userRepository.countByEnabled(true)).thenReturn(80L);
            when(userRepository.countByEnabled(false)).thenReturn(20L);
            when(userRepository.countByLastLoginAtAfter(any(Instant.class))).thenReturn(15L);

            UserStatsResponse stats = userService.getUserStats();

            assertEquals(100L, stats.totalUsers());
            assertEquals(80L, stats.activeUsers());
            assertEquals(20L, stats.inactiveUsers());
            assertEquals(15L, stats.todayLogins());
        }
    }

 
    private User createUser() {
        return User.builder()
                .email("ali@test.com")
                .username("alikaygusuz")
                .password("encodedPassword")
                .firstName("Ali")
                .lastName("Kaygusuz")
                .phoneNumber("+905551234567")
                .emailVerified(true)
                .enabled(true)
                .firstLogin(false)
                .role(Role.USER)
                .build();
    }

    private User createUserWithId(UUID id) {
        User user = createUser();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UserResponse createUserResponse(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getUsername(),
                user.getFirstName(), user.getLastName(), user.getPhoneNumber(),
                user.isEmailVerified(), user.isEnabled(), user.isFirstLogin(),
                user.getRole().name(), user.getLastLoginAt(), null
        );
    }
}
