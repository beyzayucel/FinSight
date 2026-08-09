package com.akademi.finsight.security.jwt;

import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import com.akademi.finsight.security.jwt.config.JwtProperties;
import com.akademi.finsight.security.jwt.service.impl.RedisTokenInvalidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisTokenInvalidationServiceTest {

    private static final String USERNAME = "mehmet@test.com";
    private static final String KEY = "auth:tokens-invalid-before:hashed-user";
    private static final Duration ACCESS_TOKEN_EXPIRY = Duration.ofMinutes(15);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private IdentifierHasher identifierHasher;

    @Mock
    private JwtProperties jwtProperties;

    private RedisTokenInvalidationService tokenInvalidationService;

    @BeforeEach
    void setUp() {
        tokenInvalidationService = new RedisTokenInvalidationService(redisTemplate, identifierHasher, jwtProperties);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(identifierHasher.hash(USERNAME)).thenReturn("hashed-user");
        lenient().when(jwtProperties.getAccessTokenExpiry()).thenReturn(ACCESS_TOKEN_EXPIRY);
    }

    // ──────────────────────────────────────────────────────────────
    // invalidateTokensIssuedBefore
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("invalidateTokensIssuedBefore")
    class InvalidateTokensIssuedBefore {

        /** Isaret access token omrunden once silinirse eski token'lar yeniden gecerli olurdu. */
        @Test
        @DisplayName("should keep the marker at least as long as an access token lives")
        void shouldOutliveTheAccessToken() {
            tokenInvalidationService.invalidateTokensIssuedBefore(USERNAME);

            ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
            verify(valueOperations).set(eq(KEY), any(), ttlCaptor.capture());
            assertTrue(ttlCaptor.getValue().compareTo(ACCESS_TOKEN_EXPIRY) > 0);
        }

        /** Kullanici adi acik yazilirsa Redis anahtarlarinda PII birikir. */
        @Test
        @DisplayName("should key the marker on the hashed username")
        void shouldKeyOnHashedUsername() {
            tokenInvalidationService.invalidateTokensIssuedBefore(USERNAME);

            verify(valueOperations).set(eq(KEY), any(), any(Duration.class));
            verify(identifierHasher).hash(USERNAME);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // isInvalidated
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isInvalidated")
    class IsInvalidated {

        @Test
        @DisplayName("should return false when no marker exists")
        void shouldReturnFalseWhenNoMarker() {
            when(valueOperations.get(KEY)).thenReturn(null);

            assertFalse(tokenInvalidationService.isInvalidated(USERNAME, Instant.now()));
        }

        @Test
        @DisplayName("should return true for a token issued before the password change")
        void shouldReturnTrueForOlderToken() {
            Instant passwordChangedAt = Instant.now();
            when(valueOperations.get(KEY)).thenReturn(String.valueOf(passwordChangedAt.getEpochSecond()));

            assertTrue(tokenInvalidationService.isInvalidated(USERNAME, passwordChangedAt.minusSeconds(60)));
        }

        @Test
        @DisplayName("should return false for a token issued after the password change")
        void shouldReturnFalseForNewerToken() {
            Instant passwordChangedAt = Instant.now();
            when(valueOperations.get(KEY)).thenReturn(String.valueOf(passwordChangedAt.getEpochSecond()));

            assertFalse(tokenInvalidationService.isInvalidated(USERNAME, passwordChangedAt.plusSeconds(60)));
        }

        /** Bozuk bir kayit tum kullanicilarin oturumunu kilitlememeli. */
        @Test
        @DisplayName("should ignore an unreadable marker instead of failing the request")
        void shouldIgnoreUnreadableMarker() {
            when(valueOperations.get(KEY)).thenReturn("bozuk-deger");

            assertFalse(tokenInvalidationService.isInvalidated(USERNAME, Instant.now()));
        }
    }
}
