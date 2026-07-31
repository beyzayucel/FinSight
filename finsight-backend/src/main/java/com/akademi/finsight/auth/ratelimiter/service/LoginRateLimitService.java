package com.akademi.finsight.auth.ratelimiter.service;

import com.akademi.finsight.auth.ratelimiter.exception.RateLimitException;

public interface LoginRateLimitService {
    void incrementFailedAttempts(String identifier);
    void checkAttemptsOrThrow(String identifier) throws RateLimitException;
    void resetAttempts(String identifier);
}
