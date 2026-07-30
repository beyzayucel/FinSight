package com.akademi.finsight.auth.ratelimiter.service.impl;

import com.akademi.finsight.auth.ratelimiter.config.LoginRateLimitProperties;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitErrorType;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;
import com.akademi.finsight.auth.ratelimiter.keygenerator.RateLimitKeyGenerator;
import com.akademi.finsight.auth.ratelimiter.service.LoginBlocklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginBlockListServiceImpl implements LoginBlocklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyGenerator keyGenerator;
    private final LoginRateLimitProperties loginRateLimitProperties;

    @Override
    public void blockUser(String hashedIdentifier) {
        String blockKey = keyGenerator.createBlockKey(hashedIdentifier);
        redisTemplate.opsForValue().set(blockKey, Boolean.TRUE.toString(), loginRateLimitProperties.getBlockDuration());
        log.warn("User has been blocked for {} after exceeding the maximum number of login attempts. User: {}", loginRateLimitProperties.getBlockDuration(), hashedIdentifier);

    }

    @Override
    public boolean isBlocked(String hashedIdentifier) {
        String blockKey = keyGenerator.createBlockKey(hashedIdentifier);
        return Boolean.TRUE.equals(redisTemplate.hasKey(blockKey));
    }

    @Override
    public Long getRemainingBlockTimeInSeconds(String hashedIdentifier) {
        String blockKey = keyGenerator.createBlockKey(hashedIdentifier);
        Long ttl = redisTemplate.getExpire(blockKey, TimeUnit.SECONDS);
        return (ttl != null && ttl > 0) ? ttl : 0L;
    }

    @Override
    public void checkBlockedOrThrow(String hashedIdentifier) throws RateLimitException {
        if (isBlocked(hashedIdentifier)) {
            Long timeRemaining = getRemainingBlockTimeInSeconds(hashedIdentifier);
            log.warn("Blocked user attempted to log in. Remaining block time: {} seconds. User: {}", timeRemaining, hashedIdentifier);
            throw new RateLimitException(RateLimitErrorType.REQUEST_NOT_WRAPPED, timeRemaining);
        }

    }

}
