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
    private static final String CLIENT_IP = "203.0.113.7";
    private static final String EMAIL_KEY = "password-reset:requests:email:hashed-email";
    private static final String IP_KEY = "password-reset:requests:ip:hashed-ip";
    private static final String SUBMIT_IP_KEY = "password-reset:submits:ip:hashed-ip";
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_PER_EMAIL = 3;
    private static final int MAX_PER_IP = 10;
    private static final int MAX_SUBMITS_PER_IP = 20;

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
        lenient().when(identifierHasher.hash(CLIENT_IP)).thenReturn("hashed-ip");
        lenient().when(rateLimitKeyGenerator.createPasswordResetEmailKey("hashed-email")).thenReturn(EMAIL_KEY);
        lenient().when(rateLimitKeyGenerator.createPasswordResetIpKey("hashed-ip")).thenReturn(IP_KEY);
        lenient().when(properties.getMaxRequestsPerEmail()).thenReturn(MAX_PER_EMAIL);
        lenient().when(properties.getMaxRequestsPerIp()).thenReturn(MAX_PER_IP);
        lenient().when(properties.getMaxSubmitsPerIp()).thenReturn(MAX_SUBMITS_PER_IP);
        lenient().when(rateLimitKeyGenerator.createPasswordResetSubmitIpKey("hashed-ip")).thenReturn(SUBMIT_IP_KEY);
        lenient().when(properties.getDuration()).thenReturn(WINDOW);
    }

    // ──────────────────────────────────────────────────────────────
    // checkAndCountOrThrow
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkAndCountOrThrow")
    class CheckAndCountOrThrow {

        @Test
        @DisplayName("should allow the request while both counters stay within their limits")
        void shouldAllowRequestUnderLimit() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(2L);
            when(valueOperations.increment(IP_KEY)).thenReturn(5L);

            assertDoesNotThrow(() -> rateLimitService.checkAndCountOrThrow(EMAIL, CLIENT_IP));
        }

        /** TTL her istekte yenilenirse pencere hic kapanmaz ve limit sonsuza kadar surer. */
        @Test
        @DisplayName("should set the window TTL only on the first request")
        void shouldSetTtlOnlyOnFirstRequest() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(1L);
            when(valueOperations.increment(IP_KEY)).thenReturn(4L);

            rateLimitService.checkAndCountOrThrow(EMAIL, CLIENT_IP);

            verify(redisTemplate).expire(EMAIL_KEY, WINDOW);
            verify(redisTemplate, never()).expire(IP_KEY, WINDOW);
        }

        @Test
        @DisplayName("should throw when the per-email limit is exceeded")
        void shouldThrowWhenEmailLimitExceeded() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn((long) MAX_PER_EMAIL + 1);

            RateLimitException exception = assertThrows(RateLimitException.class,
                    () -> rateLimitService.checkAndCountOrThrow(EMAIL, CLIENT_IP));

            assertEquals(RateLimitErrorType.PASSWORD_RESET_RATE_LIMIT_EXCEEDED, exception.getErrorType());
            verify(valueOperations, never()).increment(IP_KEY);
        }

        /** Tek adresin farkli e-postalar deneyerek limiti dolasmasini engeller. */
        @Test
        @DisplayName("should throw when the per-ip limit is exceeded even if the email is under its limit")
        void shouldThrowWhenIpLimitExceeded() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(1L);
            when(valueOperations.increment(IP_KEY)).thenReturn((long) MAX_PER_IP + 1);

            RateLimitException exception = assertThrows(RateLimitException.class,
                    () -> rateLimitService.checkAndCountOrThrow(EMAIL, CLIENT_IP));

            assertEquals(RateLimitErrorType.PASSWORD_RESET_RATE_LIMIT_EXCEEDED, exception.getErrorType());
        }

        /** E-posta ve IP Redis'e hash'lenerek yazilmali, acik PII anahtarlarda durmamali. */
        @Test
        @DisplayName("should key counters on hashed values, never on raw email or ip")
        void shouldKeyCountersOnHashedValues() {
            when(valueOperations.increment(EMAIL_KEY)).thenReturn(1L);
            when(valueOperations.increment(IP_KEY)).thenReturn(1L);

            rateLimitService.checkAndCountOrThrow(EMAIL, CLIENT_IP);

            verify(identifierHasher).hash(EMAIL);
            verify(identifierHasher).hash(CLIENT_IP);
            verify(valueOperations, never()).increment(contains(EMAIL));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // checkAndCountSubmitOrThrow
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("checkAndCountSubmitOrThrow")
    class CheckAndCountSubmitOrThrow {

        /** Gonderim ucu ayri kovada sayilmali, yoksa talep limiti bosa harcanirdi. */
        @Test
        @DisplayName("should count submits in their own bucket, separate from reset requests")
        void shouldCountSubmitsSeparately() {
            when(valueOperations.increment(SUBMIT_IP_KEY)).thenReturn(3L);

            assertDoesNotThrow(() -> rateLimitService.checkAndCountSubmitOrThrow(CLIENT_IP));

            verify(valueOperations, never()).increment(IP_KEY);
            verify(valueOperations, never()).increment(EMAIL_KEY);
        }

        @Test
        @DisplayName("should throw when the submit limit is exceeded")
        void shouldThrowWhenSubmitLimitExceeded() {
            when(valueOperations.increment(SUBMIT_IP_KEY)).thenReturn((long) MAX_SUBMITS_PER_IP + 1);

            RateLimitException exception = assertThrows(RateLimitException.class,
                    () -> rateLimitService.checkAndCountSubmitOrThrow(CLIENT_IP));

            assertEquals(RateLimitErrorType.PASSWORD_RESET_RATE_LIMIT_EXCEEDED, exception.getErrorType());
        }
    }
}
