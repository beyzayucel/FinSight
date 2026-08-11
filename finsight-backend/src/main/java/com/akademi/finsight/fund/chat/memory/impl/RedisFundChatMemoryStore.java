package com.akademi.finsight.fund.chat.memory.impl;

import com.akademi.finsight.common.masking.MaskType;
import com.akademi.finsight.fund.chat.config.FundChatProperties;
import com.akademi.finsight.fund.chat.constant.FundChatRedisKeys;
import com.akademi.finsight.fund.chat.dto.FundChatTurn;
import com.akademi.finsight.fund.chat.memory.FundChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisFundChatMemoryStore implements FundChatMemoryStore {

    private static final String HASH_ALGORITHM = "SHA-256";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final FundChatProperties properties;

    @Override
    public List<FundChatTurn> load(String userIdentifier, String fundCode, String sessionId) {
        String key = key(userIdentifier, fundCode, sessionId);
        String stored = redisTemplate.opsForValue().get(key);

        if (stored == null || stored.isBlank()) {
            return List.of();
        }

        try {
            return Arrays.asList(objectMapper.readValue(stored, FundChatTurn[].class));
        } catch (JacksonException exception) {
            log.warn("Fund chat history could not be parsed and was dropped: user={}, sessionId={}",
                    MaskType.EMAIL.mask(userIdentifier), sessionId, exception);
            redisTemplate.delete(key);
            return List.of();
        }
    }

    @Override
    public void save(String userIdentifier, String fundCode, String sessionId, List<FundChatTurn> turns) {
        if (turns.isEmpty()) {
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(trim(turns));
        } catch (JacksonException exception) {
            log.warn("Fund chat history could not be serialized and was not stored: user={}, sessionId={}",
                    MaskType.EMAIL.mask(userIdentifier), sessionId, exception);
            return;
        }

        redisTemplate.opsForValue().set(
                key(userIdentifier, fundCode, sessionId), payload, properties.getSessionTtl());
    }

    @Override
    public void clear(String userIdentifier, String fundCode, String sessionId) {
        redisTemplate.delete(key(userIdentifier, fundCode, sessionId));
    }

    private List<FundChatTurn> trim(List<FundChatTurn> turns) {
        int limit = properties.getMaxTurns() * 2;
        return turns.size() <= limit ? turns : turns.subList(turns.size() - limit, turns.size());
    }

    private String key(String userIdentifier, String fundCode, String sessionId) {
        return FundChatRedisKeys.HISTORY.formatted(hash(userIdentifier), fundCode, sessionId);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
