package com.akademi.finsight.auth.ratelimiter;

import com.akademi.finsight.auth.ratelimiter.config.PasswordResetRateLimitProperties;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitErrorType;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;
import com.akademi.finsight.auth.ratelimiter.keygenerator.RateLimitKeyGenerator;
import com.akademi.finsight.auth.ratelimiter.service.impl.PasswordResetRateLimitServiceImpl;
import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetRateLimitServiceImplTest {

    private static final String EMAIL = "mehmet@test.com";
    private static final String EMAIL_KEY = "password-reset:requests:email:hashed-email";
    private static final String COOLDOWN_KEY = "password-reset:cooldown:email:hashed-email";
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_PER_EMAIL = 3;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RateLimitKeyGenerator rateLimitKeyGenerator;

    @Mock
    private PasswordResetRateLimitProperties properties;

    @Mock
    private IdentifierHasher identifierHasher;

    private PasswordResetRateLimitServiceImpl rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new PasswordResetRateLimitServiceImpl(
                redisTemplate, rateLimitKeyGenerator, properties, identifierHasher);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(identifierHasher.hash(EMAIL)).thenReturn("hashed-email");
        lenient().when(rateLimitKeyGenerator.createPasswordResetEmailKey("hashed-email")).thenReturn(EMAIL_KEY);
        lenient().when(rateLimitKeyGenerator.createPasswordResetCooldownKey("hashed-email")).thenReturn(COOLDOWN_KEY);
        lenient().when(properties.getMaxRequestsPerEmail()).thenReturn(MAX_PER_EMAIL);
        lenient().when(properties.getDuration()).thenReturn(WINDOW);
        lenient().when(properties.getCooldownDuration()).thenReturn(COOLDOWN);
        lenient().when(valueOperations.setIfAbsent(COOLDOWN_KEY, "1", COOLDOWN)).thenReturn(true);
    }

    // ──────────────────────────────────────────────────────────────
    // checkAndCountOrThrow
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkAndCountOrThrow")
    class CheckAndCountOrThrow {

        @Test
        @DisplayName("should allow the request while the email counter stays within its limit")
        void shouldAllowRequestUnderLimit() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(2L);

            assertDoesNotThrow(() -> rateLimitService.checkAndCountOrThrow(EMAIL));
        }

        /** TTL her istekte yenilenirse pencere hic kapanmaz ve limit sonsuza kadar surer. */
        @Test
        @DisplayName("should set the window TTL only on the first request")
        void shouldSetTtlOnlyOnFirstRequest() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(1L);

            rateLimitService.checkAndCountOrThrow(EMAIL);

            verify(redisTemplate).expire(EMAIL_KEY, WINDOW);
        }

        @Test
        @DisplayName("should throw when the per-email limit is exceeded")
        void shouldThrowWhenEmailLimitExceeded() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn((long) MAX_PER_EMAIL + 1);

            RateLimitException exception = assertThrows(RateLimitException.class,
                    () -> rateLimitService.checkAndCountOrThrow(EMAIL));

            assertEquals(RateLimitErrorType.PASSWORD_RESET_RATE_LIMIT_EXCEEDED, exception.getErrorType());
            verify(valueOperations, never()).setIfAbsent(anyString(), anyString(), any(Duration.class));
        }

        @Test
        @DisplayName("should reject a repeated request while the cooldown is active")
        void shouldRejectRequestDuringCooldown() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(2L);
            when(valueOperations.setIfAbsent(COOLDOWN_KEY, "1", COOLDOWN)).thenReturn(false);

            RateLimitException exception = assertThrows(RateLimitException.class,
                    () -> rateLimitService.checkAndCountOrThrow(EMAIL));

            assertEquals(RateLimitErrorType.PASSWORD_RESET_COOLDOWN_ACTIVE, exception.getErrorType());
        }

        @Test
        @DisplayName("should acquire the cooldown atomically after passing the abuse limit")
        void shouldAcquireCooldownAtomically() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(1L);

            rateLimitService.checkAndCountOrThrow(EMAIL);

            verify(valueOperations).setIfAbsent(COOLDOWN_KEY, "1", COOLDOWN);
        }

        /** E-posta Redis'e hash'lenerek yazilmali, acik PII anahtarda durmamali. */
        @Test
        @DisplayName("should key the counter on the hashed email, never on the raw value")
        void shouldKeyCountersOnHashedValues() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(1L);

            rateLimitService.checkAndCountOrThrow(EMAIL);

            verify(identifierHasher).hash(EMAIL);
            verify(valueOperations, never()).increment(contains(EMAIL));
        }
    }
}
