package com.akademi.finsight.fund.chat.memory.impl;

import com.akademi.finsight.fund.chat.config.FundChatProperties;
import com.akademi.finsight.fund.chat.dto.FundChatRole;
import com.akademi.finsight.fund.chat.dto.FundChatTurn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisFundChatMemoryStore")
class RedisFundChatMemoryStoreTest {

    private static final String EMAIL = "melis@example.com";
    private static final String FUND_CODE = "TIE";
    private static final String SESSION_ID = "session-0001";
    private static final Duration TTL = Duration.ofMinutes(30);
    private static final int MAX_TURNS = 2;
    private static final Instant AT = Instant.parse("2026-08-11T09:15:00Z");

    private static final String KEY_PATTERN = "fund-chat:history:[0-9a-f]{64}:" + FUND_CODE + ":" + SESSION_ID;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Captor
    private ArgumentCaptor<String> keyCaptor;

    @Captor
    private ArgumentCaptor<String> payloadCaptor;

    private RedisFundChatMemoryStore store;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        FundChatProperties properties = new FundChatProperties();
        properties.setSessionTtl(TTL);
        properties.setMaxTurns(MAX_TURNS);

        store = new RedisFundChatMemoryStore(redisTemplate, objectMapper, properties);
    }

    private static FundChatTurn turn(int index) {
        return index % 2 == 0
                ? FundChatTurn.user("soru-" + index, AT)
                : FundChatTurn.assistant("cevap-" + index, AT);
    }

    private static List<FundChatTurn> turns(int count) {
        return IntStream.range(0, count)
                .mapToObj(RedisFundChatMemoryStoreTest::turn)
                .toList();
    }

    private String storedPayload() {
        verify(valueOperations).set(anyString(), payloadCaptor.capture(), eq(TTL));
        return payloadCaptor.getValue();
    }

    @Nested
    @DisplayName("key")
    class Key {

        @Test
        @DisplayName("should hash the user identifier instead of putting it in the key")
        void shouldHashUserIdentifier() {
            store.save(EMAIL, FUND_CODE, SESSION_ID, turns(2));

            verify(valueOperations).set(keyCaptor.capture(), anyString(), eq(TTL));
            String key = keyCaptor.getValue();

            assertTrue(key.matches(KEY_PATTERN), key);
            assertFalse(key.contains(EMAIL), key);
            assertFalse(key.contains("melis"), key);
        }

        @Test
        @DisplayName("should use the same key for load, save and clear")
        void shouldUseTheSameKeyEverywhere() {
            store.save(EMAIL, FUND_CODE, SESSION_ID, turns(2));
            verify(valueOperations).set(keyCaptor.capture(), anyString(), eq(TTL));
            String saveKey = keyCaptor.getValue();

            store.load(EMAIL, FUND_CODE, SESSION_ID);
            verify(valueOperations).get(saveKey);

            store.clear(EMAIL, FUND_CODE, SESSION_ID);
            verify(redisTemplate).delete(saveKey);
        }
    }

    @Nested
    @DisplayName("load")
    class Load {

        @Test
        @DisplayName("should return an empty history when nothing is stored")
        void shouldReturnEmptyWhenMissing() {
            when(valueOperations.get(anyString())).thenReturn(null);

            assertTrue(store.load(EMAIL, FUND_CODE, SESSION_ID).isEmpty());
        }

        @Test
        @DisplayName("should return an empty history when the stored value is blank")
        void shouldReturnEmptyWhenBlank() {
            when(valueOperations.get(anyString())).thenReturn("   ");

            assertTrue(store.load(EMAIL, FUND_CODE, SESSION_ID).isEmpty());
            verify(redisTemplate, never()).delete(anyString());
        }

        @Test
        @DisplayName("should read back exactly what save wrote")
        void shouldRoundTrip() {
            store.save(EMAIL, FUND_CODE, SESSION_ID, turns(2));
            String payload = storedPayload();
            when(valueOperations.get(anyString())).thenReturn(payload);

            List<FundChatTurn> loaded = store.load(EMAIL, FUND_CODE, SESSION_ID);

            assertEquals(2, loaded.size());
            assertEquals(FundChatRole.USER, loaded.get(0).role());
            assertEquals("soru-0", loaded.get(0).content());
            assertEquals(AT, loaded.get(0).at());
            assertEquals(FundChatRole.ASSISTANT, loaded.get(1).role());
        }

        @Test
        @DisplayName("should drop a corrupted history instead of failing the request")
        void shouldDropCorruptedHistory() {
            when(valueOperations.get(anyString())).thenReturn("{ bozuk json");

            assertTrue(store.load(EMAIL, FUND_CODE, SESSION_ID).isEmpty());
            verify(redisTemplate).delete(anyString());
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should not touch Redis for an empty history")
        void shouldSkipEmptyHistory() {
            store.save(EMAIL, FUND_CODE, SESSION_ID, List.of());

            verifyNoInteractions(valueOperations);
        }

        @Test
        @DisplayName("should store with the configured TTL")
        void shouldStoreWithConfiguredTtl() {
            store.save(EMAIL, FUND_CODE, SESSION_ID, turns(2));

            verify(valueOperations).set(anyString(), anyString(), eq(TTL));
        }

        @Test
        @DisplayName("should keep only the newest maxTurns pairs")
        void shouldTrimToNewestTurns() {
            store.save(EMAIL, FUND_CODE, SESSION_ID, turns(10));

            String payload = storedPayload();
            when(valueOperations.get(anyString())).thenReturn(payload);
            List<FundChatTurn> stored = store.load(EMAIL, FUND_CODE, SESSION_ID);

            assertEquals(MAX_TURNS * 2, stored.size());
            assertEquals("soru-6", stored.getFirst().content());
            assertEquals("cevap-9", stored.getLast().content());
        }

        @Test
        @DisplayName("should store a history that is already short enough untouched")
        void shouldNotTrimShortHistory() {
            store.save(EMAIL, FUND_CODE, SESSION_ID, turns(3));

            String payload = storedPayload();
            when(valueOperations.get(anyString())).thenReturn(payload);

            assertEquals(3, store.load(EMAIL, FUND_CODE, SESSION_ID).size());
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        @DisplayName("should delete the session key without reading it first")
        void shouldDeleteKey() {
            store.clear(EMAIL, FUND_CODE, SESSION_ID);

            verify(redisTemplate).delete(keyCaptor.capture());
            assertTrue(keyCaptor.getValue().matches(KEY_PATTERN));
            verify(valueOperations, never()).get(any());
        }
    }
}
