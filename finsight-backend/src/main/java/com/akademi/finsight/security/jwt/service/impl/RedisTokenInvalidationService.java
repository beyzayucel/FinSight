package com.akademi.finsight.security.jwt.service.impl;

import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import com.akademi.finsight.security.jwt.config.JwtProperties;
import com.akademi.finsight.security.jwt.service.TokenInvalidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Isaret yalnizca access token omru kadar saklanir: o sure gectiginde zaten hicbir
 * eski token gecerli olmayacagi icin kaydin yasamaya devam etmesinin anlami yok.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenInvalidationService implements TokenInvalidationService {

    private static final String KEY_PREFIX = "auth:tokens-invalid-before:";
    /** Saat kaymasi ve JWT'nin saniyelik iat hassasiyeti icin pay. */
    private static final Duration RETENTION_MARGIN = Duration.ofMinutes(1);

    private final StringRedisTemplate redisTemplate;
    private final IdentifierHasher identifierHasher;
    private final JwtProperties jwtProperties;

    @Override
    public void invalidateTokensIssuedBefore(String username) {
        Instant now = Instant.now();
        Duration retention = jwtProperties.getAccessTokenExpiry().plus(RETENTION_MARGIN);

        redisTemplate.opsForValue().set(key(username), String.valueOf(now.getEpochSecond()), retention);

        log.info("Access tokens invalidated: event=ACCESS_TOKENS_INVALIDATED, before={}", now);
    }

    @Override
    public boolean isInvalidated(String username, Instant issuedAt) {
        String invalidBefore = redisTemplate.opsForValue().get(key(username));

        if (invalidBefore == null) {
            return false;
        }

        try {
            // iat saniye hassasiyetinde; ayni saniyede uretilen token gecerli sayilir
            return issuedAt.getEpochSecond() < Long.parseLong(invalidBefore);
        } catch (NumberFormatException exception) {
            log.warn("Unreadable token invalidation marker, ignoring: value={}", invalidBefore);
            return false;
        }
    }

    private String key(String username) {
        return KEY_PREFIX + identifierHasher.hash(username);
    }
}
