package com.akademi.finsight.auth.ratelimiter.service.impl;

import com.akademi.finsight.auth.ratelimiter.config.LoginRateLimitProperties;
import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;
import com.akademi.finsight.auth.ratelimiter.keygenerator.RateLimitKeyGenerator;
import com.akademi.finsight.auth.ratelimiter.service.LoginBlocklistService;
import com.akademi.finsight.auth.ratelimiter.service.LoginRateLimitService;
import com.akademi.finsight.auth.ratelimiter.util.IdentifierHasher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginRateLimitServiceImpl implements LoginRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;
    private final LoginRateLimitProperties loginRateLimitProperties;
    private final LoginBlocklistService blocklistService;
    private final IdentifierHasher identifierHasher;

    @Override
    public void incrementFailedAttempts(String identifier) {
        String hashedIdentifier = identifierHasher.hash(identifier);
        String attemptKey = rateLimitKeyGenerator.createAttemptKey(hashedIdentifier);
        Long attempts = Optional.ofNullable(redisTemplate.opsForValue().increment(attemptKey)).orElse(0L) ;

        log.debug("Failed login attempt {} for user: {}", attempts, hashedIdentifier);

        initializeExpirationIfNeeded(attemptKey,attempts);

        handleMaxAttempts(hashedIdentifier, attempts);
    }

    @Override
    public void checkAttemptsOrThrow(String identifier) {
        blocklistService.checkBlockedOrThrow(identifierHasher.hash(identifier));
    }

    @Override
    public void resetAttempts(String identifier) {
        resetAttemptsByHash(identifierHasher.hash(identifier));
    }

    private void resetAttemptsByHash(String hashedIdentifier) {
        String key = rateLimitKeyGenerator.createAttemptKey(hashedIdentifier);
        Optional.ofNullable(redisTemplate.delete(key))
                .filter(Boolean::booleanValue)
                .ifPresent(ignored -> log.debug("Reset login attempts for user: {}", hashedIdentifier));
    }

    private void initializeExpirationIfNeeded(String attemptKey, Long attempts){
        if (attempts == 1L){
            redisTemplate.expire(attemptKey, loginRateLimitProperties.getDuration());
        }
    }

    private void handleMaxAttempts(String hashedIdentifier, Long attempts){
        if(attempts >= loginRateLimitProperties.getMaxAttempts()){
            log.warn("User exceeded maximum login attempts ({}). Blocking user: {}", attempts, hashedIdentifier);
            blocklistService.blockUser(hashedIdentifier);
            resetAttemptsByHash(hashedIdentifier);
        }
    }
}
