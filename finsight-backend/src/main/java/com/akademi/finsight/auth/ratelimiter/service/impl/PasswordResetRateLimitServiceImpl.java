package com.akademi.finsight.auth.ratelimiter.service.impl;

import com.akademi.finsight.auth.ratelimiter.config.PasswordResetRateLimitProperties;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitErrorType;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;
import com.akademi.finsight.auth.ratelimiter.keygenerator.RateLimitKeyGenerator;
import com.akademi.finsight.auth.ratelimiter.service.PasswordResetRateLimitService;
import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetRateLimitServiceImpl implements PasswordResetRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;
    private final PasswordResetRateLimitProperties properties;
    private final IdentifierHasher identifierHasher;

    @Override
    public void checkAndCountOrThrow(String email) {
        String hashedEmail = identifierHasher.hash(email);
        countOrThrow(rateLimitKeyGenerator.createPasswordResetEmailKey(hashedEmail),
                properties.getMaxRequestsPerEmail(), "email", hashedEmail);
    }

    private void countOrThrow(String key, int maxRequests, String scope, String hashedValue) {
        long requests = Optional.ofNullable(redisTemplate.opsForValue().increment(key)).orElse(0L);

        // Pencere ilk istekte baslar; her istekte yenilenirse limit hic sifirlanmazdi.
        if (requests == 1L) {
            redisTemplate.expire(key, properties.getDuration());
        }

        if (requests > maxRequests) {
            log.warn("Password reset rate limit exceeded: scope={} requests={} max={} key={}",
                    scope, requests, maxRequests, hashedValue);
            throw new RateLimitException(RateLimitErrorType.PASSWORD_RESET_RATE_LIMIT_EXCEEDED);
        }
    }
}
