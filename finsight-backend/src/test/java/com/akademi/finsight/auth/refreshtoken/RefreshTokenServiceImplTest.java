package com.akademi.finsight.auth.refreshtoken;

import com.akademi.finsight.audit.entity.AuditActionType;
import com.akademi.finsight.audit.service.AuditLogService;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenRequest;
import com.akademi.finsight.auth.refreshtoken.dto.RefreshTokenResult;
import com.akademi.finsight.auth.refreshtoken.entity.RefreshToken;
import com.akademi.finsight.auth.refreshtoken.exception.RefreshTokenErrorType;
import com.akademi.finsight.auth.refreshtoken.exception.RefreshTokenException;
import com.akademi.finsight.auth.refreshtoken.repository.RefreshTokenRepository;
import com.akademi.finsight.auth.refreshtoken.service.impl.RefreshTokenServiceImpl;
import com.akademi.finsight.security.jwt.config.JwtProperties;
import com.akademi.finsight.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Nested
    @DisplayName("createAndSave")
    class CreateAndSave {

        @Test
        @DisplayName("should save token and return raw token")
        void shouldSaveAndReturnRawToken() {
            User user = createUser();
            when(jwtProperties.getRefreshTokenExpiry()).thenReturn(Duration.ofDays(7));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            RefreshTokenResult result = refreshTokenService.createAndSave(user);

            assertNotNull(result.rawToken());
            assertNotNull(result.refreshToken());
            assertEquals(user, result.refreshToken().getUser());

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokenRepository).save(captor.capture());
            assertNotNull(captor.getValue().getTokenHash());
            assertNotEquals(result.rawToken(), captor.getValue().getTokenHash());
        }
    }

    @Nested
    @DisplayName("rotateToken")
    class RotateToken {

        @Test
        @DisplayName("should revoke old and create new token")
        void shouldRevokeOldAndCreateNew() {
            User user = createUser();
            RefreshToken existingToken = createValidToken(user);

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existingToken));
            when(jwtProperties.getRefreshTokenExpiry()).thenReturn(Duration.ofDays(7));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            RefreshTokenRequest request = new RefreshTokenRequest("old-raw-token");
            RefreshTokenResult result = refreshTokenService.rotateToken(request);

            assertTrue(existingToken.isRevoked());
            assertNotNull(existingToken.getRevokedAt());
            assertNotNull(result.rawToken());
            assertEquals(user, result.refreshToken().getUser());
        }

        @Test
        @DisplayName("should throw REFRESH_TOKEN_NOT_FOUND when token not found")
        void shouldThrowWhenNotFound() {
            RefreshTokenRequest request = new RefreshTokenRequest("invalid");
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            RefreshTokenException exception = assertThrows(RefreshTokenException.class,
                    () -> refreshTokenService.rotateToken(request));
            assertEquals(RefreshTokenErrorType.REFRESH_TOKEN_NOT_FOUND, exception.getErrorType());
        }

        @Test
        @DisplayName("should throw REFRESH_TOKEN_REVOKED when token is revoked")
        void shouldThrowWhenRevoked() {
            User user = createUser();
            RefreshToken revokedToken = createValidToken(user);
            revokedToken.setRevoked(true);
            revokedToken.setRevokedAt(Instant.now());

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revokedToken));

            RefreshTokenRequest request = new RefreshTokenRequest("revoked-token");
            RefreshTokenException exception = assertThrows(RefreshTokenException.class,
                    () -> refreshTokenService.rotateToken(request));
            assertEquals(RefreshTokenErrorType.REFRESH_TOKEN_REVOKED, exception.getErrorType());
        }

        @Test
        @DisplayName("should throw REFRESH_TOKEN_EXPIRED when token is expired")
        void shouldThrowWhenExpired() {
            User user = createUser();
            RefreshToken expiredToken = RefreshToken.builder()
                    .tokenHash("hash")
                    .expiryDate(Instant.now().minusSeconds(3600))
                    .user(user)
                    .build();

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expiredToken));

            RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
            RefreshTokenException exception = assertThrows(RefreshTokenException.class,
                    () -> refreshTokenService.rotateToken(request));
            assertEquals(RefreshTokenErrorType.REFRESH_TOKEN_EXPIRED, exception.getErrorType());
        }
    }

    @Nested
    @DisplayName("revokeToken")
    class RevokeToken {

        @Test
        @DisplayName("should revoke and audit log")
        void shouldRevokeAndAudit() {
            User user = createUser();
            RefreshToken token = createValidToken(user);

            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

            RefreshTokenRequest request = new RefreshTokenRequest("raw-token");
            refreshTokenService.revokeToken(request);

            assertTrue(token.isRevoked());
            assertNotNull(token.getRevokedAt());
            verify(auditLogService).createAuditLogForSelf(AuditActionType.LOGOUT, user);
        }

        @Test
        @DisplayName("should throw REFRESH_TOKEN_NOT_FOUND when token not found")
        void shouldThrowWhenNotFound() {
            when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

            RefreshTokenRequest request = new RefreshTokenRequest("invalid");
            RefreshTokenException exception = assertThrows(RefreshTokenException.class,
                    () -> refreshTokenService.revokeToken(request));
            assertEquals(RefreshTokenErrorType.REFRESH_TOKEN_NOT_FOUND, exception.getErrorType());

            verifyNoInteractions(auditLogService);
        }
    }

    @Nested
    @DisplayName("revokeAllByUser")
    class RevokeAllByUser {

        @Test
        @DisplayName("should call repository bulk revoke")
        void shouldCallBulkRevoke() {
            User user = createUser();

            refreshTokenService.revokeAllByUser(user);

            verify(refreshTokenRepository).revokeAllByUserId(eq(user.getId()), any(Instant.class));
        }
    }

    private User createUser() {
        User user = User.builder()
                .email("ali@test.com")
                .username("alikaygusuz")
                .firstName("Ali")
                .lastName("Test")
                .build();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private RefreshToken createValidToken(User user) {
        return RefreshToken.builder()
                .tokenHash("validhash")
                .expiryDate(Instant.now().plusSeconds(86400))
                .user(user)
                .build();
    }
}
