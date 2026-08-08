package com.akademi.finsight.notification.repository;

import com.akademi.finsight.notification.support.NotificationFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisIdempotencyStoreTest {

    private static final String KEY = "notification:idempotency:" + NotificationFixtures.EVENT_ID;
    private static final Duration TTL = Duration.ofHours(24);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisIdempotencyStore store;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new RedisIdempotencyStore(redisTemplate, NotificationFixtures.properties());
    }

    // ──────────────────────────────────────────────────────────────
    // tryAcquire
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("tryAcquire")
    class TryAcquire {

        /** Onekli anahtar ve TTL, SET NX EX ile tek komutta gonderilmeli. */
        @Test
        @DisplayName("should return true with prefixed key and configured TTL when acquired")
        void tryAcquire_shouldReturnTrueWhenKeyAcquired() {
            stubSetIfAbsent(true);

            assertTrue(store.tryAcquire(NotificationFixtures.EVENT_ID));
        }

        @Test
        @DisplayName("should return false when key already exists")
        void tryAcquire_shouldReturnFalseWhenKeyExists() {
            stubSetIfAbsent(false);

            assertFalse(store.tryAcquire(NotificationFixtures.EVENT_ID));
        }

        /** Pipeline/transaction modunda Redis null dondurebilir; bu "aldim" sayilmamali. */
        @Test
        @DisplayName("should return false when Redis returns null")
        void tryAcquire_shouldReturnFalseWhenRedisReturnsNull() {
            stubSetIfAbsent(null);

            assertFalse(store.tryAcquire(NotificationFixtures.EVENT_ID));
        }
    }

    // ──────────────────────────────────────────────────────────────
    // release
    // ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("release")
    class Release {

        @Test
        @DisplayName("should delete the prefixed key")
        void release_shouldDeletePrefixedKey() {
            store.release(NotificationFixtures.EVENT_ID);

            verify(redisTemplate).delete(KEY);
        }
    }

    // ──────────────────────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────────────────────

    private void stubSetIfAbsent(Boolean result) {
        when(valueOperations.setIfAbsent(KEY, "1", TTL)).thenReturn(result);
    }
}
