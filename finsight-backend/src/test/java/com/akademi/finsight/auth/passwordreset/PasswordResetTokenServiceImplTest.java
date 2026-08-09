package com.akademi.finsight.auth.passwordreset;

import com.akademi.finsight.auth.passwordreset.config.PasswordResetProperties;
import com.akademi.finsight.auth.passwordreset.dto.PasswordResetEmailRequest;
import com.akademi.finsight.auth.passwordreset.entity.PasswordResetToken;
import com.akademi.finsight.auth.passwordreset.exception.PasswordResetErrorType;
import com.akademi.finsight.auth.passwordreset.exception.PasswordResetException;
import com.akademi.finsight.auth.passwordreset.repository.PasswordResetTokenRepository;
import com.akademi.finsight.auth.passwordreset.service.impl.PasswordResetTokenServiceImpl;
import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import com.akademi.finsight.notification.service.EmailService;
import com.akademi.finsight.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceImplTest {

    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final String RESET_URL_PREFIX = "https://finsight.local/reset-password?token=";
    private static final Duration EXPIRE_DURATION = Duration.ofMinutes(30);
    private static final String TOKEN_HASH = "hashed-token";

    @Mock
    private IdentifierHasher tokenHasher;

    @Mock
    private PasswordResetTokenRepository repository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordResetProperties properties;

    @InjectMocks
    private PasswordResetTokenServiceImpl passwordResetTokenService;

    // ──────────────────────────────────────────────────────────────
    // createAndSendResetToken
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createAndSendResetToken")
    class CreateAndSendResetToken {

        /** Eski token once silinmezse kullanicinin ayni anda birden fazla gecerli sifirlama linki olur. */
        @Test
        @DisplayName("should delete previous tokens before saving the new one")
        void shouldDeletePreviousTokensFirst() {
            stubTokenCreation();

            passwordResetTokenService.createAndSendResetToken(createUser());

            InOrder inOrder = inOrder(repository);
            inOrder.verify(repository).deleteByUserId(USER_ID);
            inOrder.verify(repository).flush();
            inOrder.verify(repository).save(any(PasswordResetToken.class));
        }

        /** Veritabani sizarsa ham token ile sifre sifirlanabilmemeli. */
        @Test
        @DisplayName("should store the hashed token, never the raw one")
        void shouldStoreHashedToken() {
            stubTokenCreation();

            passwordResetTokenService.createAndSendResetToken(createUser());

            assertEquals(TOKEN_HASH, captureSavedToken().getToken());
            assertNotEquals(captureRawToken(), captureSavedToken().getToken());
        }

        @Test
        @DisplayName("should set expiry from the configured duration")
        void shouldSetExpiryFromConfiguredDuration() {
            stubTokenCreation();
            Instant before = Instant.now();

            passwordResetTokenService.createAndSendResetToken(createUser());

            Instant expiresAt = captureSavedToken().getExpiresAt();
            assertFalse(expiresAt.isBefore(before.plus(EXPIRE_DURATION)));
            assertFalse(expiresAt.isAfter(Instant.now().plus(EXPIRE_DURATION)));
        }

        @Test
        @DisplayName("should send reset mail with url built from configured prefix and raw token")
        void shouldSendMailWithResetUrl() {
            stubTokenCreation();
            User user = createUser();

            passwordResetTokenService.createAndSendResetToken(user);

            PasswordResetEmailRequest request = captureEmailRequest();
            assertEquals(user.getFirstName(), request.firstName());
            assertEquals(user.getEmail(), request.email());
            assertTrue(request.resetUrl().startsWith(RESET_URL_PREFIX));
            assertDoesNotThrow(() -> UUID.fromString(captureRawToken()));
        }

        /** Ayni token iki kez uretilirse eski link yeniden kullanilabilir hale gelir. */
        @Test
        @DisplayName("should generate a different token on every call")
        void shouldGenerateDifferentTokenPerCall() {
            stubTokenCreation();
            User user = createUser();

            passwordResetTokenService.createAndSendResetToken(user);
            passwordResetTokenService.createAndSendResetToken(user);

            ArgumentCaptor<PasswordResetEmailRequest> captor = ArgumentCaptor.forClass(PasswordResetEmailRequest.class);
            verify(emailService, times(2)).sendPasswordResetEmail(captor.capture(), any());
            assertNotEquals(captor.getAllValues().get(0).resetUrl(), captor.getAllValues().get(1).resetUrl());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // consumeToken
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("consumeToken")
    class ConsumeToken {

        @Test
        @DisplayName("should look up the hashed token and return its user")
        void shouldReturnUserForValidToken() {
            User user = createUser();
            stubTokenLookup(Optional.of(createResetToken(user)));

            User result = passwordResetTokenService.consumeToken("raw-token");

            assertEquals(user, result);
            verify(repository).findByTokenAndExpiresAtAfter(eq(TOKEN_HASH), any(Instant.class));
        }

        /** Token tuketilmezse ayni linkle sifre defalarca sifirlanabilir. */
        @Test
        @DisplayName("should delete the token so it cannot be reused")
        void shouldDeleteTokenAfterConsuming() {
            PasswordResetToken resetToken = createResetToken(createUser());
            stubTokenLookup(Optional.of(resetToken));

            passwordResetTokenService.consumeToken("raw-token");

            verify(repository).delete(resetToken);
        }

        /** Repository sorgusu suresi gecmis kayitlari zaten eliyor, ikisi de ayni hataya duser. */
        @Test
        @DisplayName("should throw PASSWORD_RESET_TOKEN_INVALID when token is unknown or expired")
        void shouldThrowWhenTokenInvalid() {
            stubTokenLookup(Optional.empty());

            PasswordResetException exception = assertThrows(PasswordResetException.class,
                    () -> passwordResetTokenService.consumeToken("raw-token"));

            assertEquals(PasswordResetErrorType.PASSWORD_RESET_TOKEN_INVALID, exception.getErrorType());
            verify(repository, never()).delete(any(PasswordResetToken.class));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // deleteExpiredTokens
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteExpiredTokens")
    class DeleteExpiredTokens {

        @Test
        @DisplayName("should return the number of rows deleted by the repository")
        void shouldReturnDeletedCount() {
            when(repository.deleteExpiredTokens(any(Instant.class))).thenReturn(4);

            assertEquals(4, passwordResetTokenService.deleteExpiredTokens());
        }
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private void stubTokenCreation() {
        when(properties.getUrl()).thenReturn(RESET_URL_PREFIX);
        when(properties.getExpireDuration()).thenReturn(EXPIRE_DURATION);
        when(tokenHasher.hash(anyString())).thenReturn(TOKEN_HASH);
    }

    private void stubTokenLookup(Optional<PasswordResetToken> result) {
        when(tokenHasher.hash("raw-token")).thenReturn(TOKEN_HASH);
        when(repository.findByTokenAndExpiresAtAfter(eq(TOKEN_HASH), any(Instant.class))).thenReturn(result);
    }

    private PasswordResetToken captureSavedToken() {
        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(repository, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private PasswordResetEmailRequest captureEmailRequest() {
        ArgumentCaptor<PasswordResetEmailRequest> captor = ArgumentCaptor.forClass(PasswordResetEmailRequest.class);
        verify(emailService, atLeastOnce()).sendPasswordResetEmail(captor.capture(), any());
        return captor.getValue();
    }

    private String captureRawToken() {
        return captureEmailRequest().resetUrl().substring(RESET_URL_PREFIX.length());
    }

    private User createUser() {
        return User.builder()
                .id(USER_ID)
                .email("mehmet@test.com")
                .username("mehmet")
                .password("encodedPassword")
                .firstName("Mehmet")
                .lastName("Test")
                .phoneNumber("5551234567")
                .emailVerified(true)
                .build();
    }

    private PasswordResetToken createResetToken(User user) {
        return PasswordResetToken.builder()
                .user(user)
                .token(TOKEN_HASH)
                .expiresAt(Instant.now().plus(EXPIRE_DURATION))
                .build();
    }
}
